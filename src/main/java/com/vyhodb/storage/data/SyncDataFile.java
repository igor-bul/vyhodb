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

package com.vyhodb.storage.data;

import com.vyhodb.server.ServerClosedException;
import com.vyhodb.storage.StorageConfig;
import com.vyhodb.storage.pagefile.Destination;
import com.vyhodb.storage.pagefile.File;
import com.vyhodb.storage.pagefile.HeaderFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class SyncDataFile {
    
    private final DataFile _writeFile;
    
    private final Destination _applier;
    private boolean _isClosed = false;
    private ArrayBlockingQueue<File> _readQueue;
    
    public SyncDataFile(StorageConfig config) throws IOException
    {
        String dataFilename = config.getDataFilename();
        
        try {
            _writeFile = new DataFile(dataFilename, true, false);
            _applier = _writeFile.getApplier();

            HeaderFile readFile;
            int queueLenght = config.getReadingQueueLength();
            _readQueue = new ArrayBlockingQueue<>(queueLenght, true);
            for (int i = 0; i < queueLenght; i++) {
                readFile = new HeaderFile(dataFilename, true, true);
                _readQueue.put(readFile);
            }
        }
        catch(Exception ie) {
            close();
            throw new IOException("Can't open data file.", ie);
        }
    }
    
    public void readPage(long pageId, ByteBuffer buffer, int pageIndex) throws IOException {
        if (_isClosed) 
            throw new ServerClosedException();
        
        try {
            File readFile = _readQueue.take();
            try {
                readFile.position(pageId);
                readFile.read(buffer, pageIndex, 1);
            }
            finally {
                _readQueue.put(readFile);
            }
        }
        catch(InterruptedException ie) {
            throw new IOException("Can't read from reading queue.", ie);
        }
    }
            
    public synchronized void apply(ByteBuffer buffer, int offset, int count) throws IOException
    {
        if (_isClosed) 
            throw new ServerClosedException();
        
        _applier.write(buffer, offset, count);
    }
    
    public synchronized void close() throws IOException
    {
        if (!_isClosed)
        {
            _isClosed = true;
            
            if (_writeFile != null) {
                _writeFile.close();
            }
            
            if (_readQueue != null) {
                try {
                    File readFile;
                    while (!_readQueue.isEmpty()) {
                        readFile = _readQueue.take();
                        readFile.close();
                    }
                }
                catch(Exception ie) {
                    throw new IOException("Can't close reading queue.", ie);
                }
            }
        }
    }
    
    public synchronized long size() throws IOException
    {
        if (_isClosed) 
            throw new ServerClosedException();
        
        return _writeFile.size();
    }
    
    public synchronized void fsync() throws IOException
    {
        _writeFile.fsync();
    }
    
    public UUID getLogId() {
        return _writeFile.getLogId();
    }
}
