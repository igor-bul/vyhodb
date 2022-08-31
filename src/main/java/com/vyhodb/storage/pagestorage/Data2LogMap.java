/*
 * MIT License
 *
 * Copyright (c) 2015-present Igor Vykhodtsev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.vyhodb.storage.pagestorage;

import com.vyhodb.server.Loggers;
import com.vyhodb.server.TransactionRolledbackException;
import com.vyhodb.storage.StorageConfig;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Long.MIN_VALUE is used as a marker for absent value. Log pages are started from (Long.MIN_VALUE + 1)
 * 
 * 
 * When writing/reading to/from file, log page id are transformed
 * writing: 0 => Long.MIN_VALUE
 * reading: 0 => Long.MIN_VALUE, Long.MIN_VALUE => 0
 * 
 * 
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
class Data2LogMap {

    private final Logger _logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_SERVER);

    private final static float MAP_LOAD_FACTOR = .75f;
    private final static int INITIAL_MAP_SIZE = 2048;
    
    private final static String ERROR_OPENING_FILE = "Exception at opening trx page mapping file.";
    private final static String ERROR_WRITING_FILE = "Exception at writing in trx page mapping file.";
    private final static String ERROR_READING_FILE = "Exception at reading from trx page mapping file.";
    
    private boolean _inMemory = true;
    private Long2LongOpenHashMap _data2log;
    
    private FileChannel _fc;
    private ByteBuffer _buffer;
    
    private final int maxMapSize;
    private String directory;
    
    public Data2LogMap(StorageConfig config) {
        _data2log = new Long2LongOpenHashMap(INITIAL_MAP_SIZE, MAP_LOAD_FACTOR);
        _data2log.defaultReturnValue(Long.MIN_VALUE);
        
        directory = getDirectory(config);
        maxMapSize = config.getMappingInMemorySize();
    }
    
    public long get(long pageDataId) {
        if (_inMemory) {
            return _data2log.get(pageDataId);
        }
        else {
            try {
                // Changes position
                _fc.position(pageDataId << 3);
                
                // Reads logPageId
                _buffer.clear();
                if (_fc.read(_buffer) <= 0) {
                    return Long.MIN_VALUE;  // End of file reached, 
                                            // which means that we don't have required mapping for dataPageId in file.
                }
                _buffer.clear();
                
                // Transforms read logPageId
                long readLogPageId = _buffer.getLong();
                if (readLogPageId == 0) {
                    return Long.MIN_VALUE;
                } else if (readLogPageId == Long.MIN_VALUE) {
                    return 0;
                } else {
                    return readLogPageId;
                }
            }
            catch(IOException ioe) {
                _logger.error(ERROR_READING_FILE, ioe);
                throw new TransactionRolledbackException(ERROR_READING_FILE, ioe);
            }
        }
    }
    
    public void put(long pageDataId, long logPageId) {
        if (_inMemory && _data2log.size() == maxMapSize) {
            moveToFile();
        }
        
        if (_inMemory) {
            _data2log.put(pageDataId, logPageId);
        } else {
            try {
                // Transforms logPageId
                if (logPageId == 0) {
                    logPageId = Long.MIN_VALUE;
                }
                
                // Writes logPageId
                _buffer.clear();
                _buffer.putLong(logPageId);
                _buffer.clear();
                _fc.position(pageDataId << 3);
                _fc.write(_buffer);
            }
            catch(IOException ioe) {
                _logger.error(ERROR_WRITING_FILE, ioe);
                throw new TransactionRolledbackException(ERROR_WRITING_FILE, ioe);
            }
        }
    }
    
    private void moveToFile() {
        _inMemory = false;
        
        try {
            _fc = FileChannel.open(getPath(), 
                    StandardOpenOption.CREATE_NEW, 
                    StandardOpenOption.SPARSE, 
                    StandardOpenOption.READ, 
                    StandardOpenOption.WRITE,
                    StandardOpenOption.DELETE_ON_CLOSE);
            _fc.lock();
            
            ObjectIterator<Long2LongMap.Entry> iterator = _data2log.long2LongEntrySet().fastIterator();
            
            _buffer = ByteBuffer.allocate(8);
            Long2LongMap.Entry entry = null;
            long dataPageId;
            long logPageId;
            long offset;
            
            while(iterator.hasNext()) {
                entry = iterator.next();
                dataPageId = entry.getLongKey();
                logPageId = entry.getLongValue();
                
                if (logPageId == 0) {
                    logPageId = Long.MIN_VALUE;
                }
                
                offset = dataPageId * 8;
                
                _buffer.clear();
                _buffer.putLong(logPageId);
                _buffer.clear();
                
                _fc.position(offset);
                _fc.write(_buffer);
            }
        } 
        catch (IOException ioe) {
            _logger.error(ERROR_OPENING_FILE, ioe);
            throw new TransactionRolledbackException(ERROR_OPENING_FILE, ioe);
        }
        finally {
            _data2log = null;
        }
    }
    
    private Path getPath() {
        UUID uuid = UUID.randomUUID();
        return Paths.get(directory, uuid.toString() + ".tmp");
    }
    
    private String getDirectory(StorageConfig config) {
        String logMappingDirectory = config.getMappingDirectory();
        if (logMappingDirectory == null || logMappingDirectory.isEmpty()) {
            Path dataFilePath = Paths.get(config.getDataFilename());
            directory = dataFilePath.getParent().toString();
            return (directory == null) ? "" : directory.toString();
        } else {
            return logMappingDirectory;
        }
    }
    
    public void close() {
        if (! _inMemory) {
            try {
                _fc.close();
            } catch(IOException ioe) {
                _logger.error("Exception at closing trx page mapping file.", ioe);
            }
        } else {
            _data2log = null;   // just to help GC
        }
    }
}
