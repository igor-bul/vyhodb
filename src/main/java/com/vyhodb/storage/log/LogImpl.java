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

package com.vyhodb.storage.log;

import com.vyhodb.admin.LogInfo;
import com.vyhodb.admin.WrongPageIdException;
import com.vyhodb.server.Loggers;
import com.vyhodb.server.ServerClosedException;
import com.vyhodb.server.admin.AdminImpl;
import com.vyhodb.storage.StorageConfig;
import com.vyhodb.storage.data.Data;
import com.vyhodb.storage.pagefile.DummyDestination;
import com.vyhodb.storage.pagefile.PageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public class LogImpl implements Log {

    private final Logger _logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_SERVER);
   
    private final Data _data;
    private final ByteBuffer _buffer;
    private final boolean _durable;
   
    private LogFile _file;
    private boolean _isClosed = false;
    
    public LogImpl(StorageConfig config, Data data) throws IOException {
        _durable = config.isDurable();
        _data = data;
        
        try {
            _file = new LogFile(config.getLogFilename(), true);
            _buffer = ByteBuffer.allocateDirect(config.getLogBufferSize() << PageHeader.PAGE_SIZE_MULTIPLICATOR);
            
            // Checks
            {
                // Checks logId
                _file.checkDataHeader(_data.getLogId());
    
                // Checks data size
                _file.checkDataSize(data);
                
                // Checks for recovery
                if (!_file.isSuccessfulStop())
                {
                    recover();
                }   
            }
            
            // Set successfulStop = false
            _file.setSuccessfulStop(false);
            _file.syncHeader();
        } 
        catch(Exception ex) {
            shutdown();
            throw ex;
        }
    }
    
    public ByteBuffer getLogBuffer() {
        return _buffer;
    }

    @Override
    public synchronized LogInfo getLogInfo() {
        if (_isClosed) 
            throw new ServerClosedException();
        return _file.getLogInfo();
    }

    @Override
    public synchronized void close() throws IOException {
        if (!_isClosed)
        {
            _file.checkpoint(_data, true);
            _file.close();
            _isClosed = true;
        }
    }
    
    public synchronized void shutdown() throws IOException {
        if (!_isClosed)
        {
            if (_file != null) {
                _file.close();
            }
            _isClosed = true;
        }
    }
     
    @Override
    public synchronized void readAdmin(long fromLogPageId, ByteBuffer buffer, int offset, int count) throws WrongPageIdException, IOException {
        if (_isClosed) 
            throw new ServerClosedException();
        
        if (fromLogPageId < _file.getStart()) {
            throw new WrongPageIdException("Required log pageId (" + fromLogPageId + ") is less then first available page (" + _file.getStart() + ")");
        }

        if ((fromLogPageId + count) > _file.getNext()) {
            throw new WrongPageIdException("Required log pageId (" + (fromLogPageId + count - 1) + ") is more more then last available page (" + (_file.getNext() - 1) + ")");
        }

        _file.position(fromLogPageId);
        _file.read(buffer, offset, count);
    }
    
    private void recover() throws IOException {
        _logger.info("Log recover started");
        ByteBuffer pageBuffer = ByteBuffer.allocateDirect(PageHeader.PAGE_SIZE);
                
        long trxCount = 0;
        final long start = _file.getCheckpoint();
        final long size = _file.size();
        _file.position(start);
        
        TrxReader reader = new TrxReader(_file, new DummyDestination(), pageBuffer);
        
        try
        {    
            long trxSize;
            while ((trxSize = reader.readTrx(size - _file.getNext())) > 0)
            {
                apply(trxSize);
                trxCount++;
            }
        } 
        catch (TransactionCorruptedException tce)
        {
            _logger.warn("Some transactions can't be recovered. ", tce.getMessage());
            _logger.debug("Some transactions can't be recovered. ", tce);
        }
        
        
        _file.truncate(_file.getNext());         // Trunc file to restored transactions
        _file.checkpoint(_data, false);
        
        _logger.info("Log recovery completed. Recovered transactions:{}, recovered pages:{}.", trxCount,  (_file.getNext() - start));
    }
    
    
    @Override
    public synchronized void shrink(long startLogPageId) throws IOException, WrongPageIdException {
        if (_isClosed) 
            throw new ServerClosedException();
        
        if (startLogPageId == Long.MIN_VALUE) {
            startLogPageId = _file.getNext();
        }
        
        // Checks start page
        if (startLogPageId > _file.getNext())
            throw new WrongPageIdException("Can't start shrink. Shrink start page > next. Next:" + _file.getNext() + ", shrink start page:" + startLogPageId);
        if ((startLogPageId - 1) < _file.getStart())
            throw new WrongPageIdException("Can't start shrink. Shrink start page must be more then start. Start:" + _file.getStart() + ", shrink start page:" + startLogPageId);
        
        long start = startLogPageId - 1;
        
        // Do checkpoint and close logFile
        _file.checkpoint(_data, true);
        _file.close();
        
        // Shrinks
        _file = AdminImpl.commonShrink(start, _file.filename(), _buffer);
    }

    @Override
    public synchronized void clearSlave() throws IOException {
        if (_isClosed) 
            throw new ServerClosedException();

        _file.clearSlave();
    }

    @Override
    public synchronized LogInfo start() {
        if (_isClosed) 
            throw new ServerClosedException();
               
        return _file.getLogInfo();
    }

    @Override
    public synchronized void append(long startLogPageId, ByteBuffer pageBuffer, int count) throws IOException {
        if (_isClosed) 
            throw new ServerClosedException();
        
        _file.position(startLogPageId);
        _file.write(pageBuffer, 0, count);
    }

    @Override
    public synchronized void read(long logPageId, ByteBuffer pageBuffer, int pageIndex) throws IOException {
        if (_isClosed) 
            throw new ServerClosedException();
        
        _file.position(logPageId);
        _file.read(pageBuffer, pageIndex, 1);
    }

    @Override
    public synchronized void rollback() {
        if (_isClosed) 
            throw new ServerClosedException();
        
        // Right now we don't do truncate because it might reduce performance of rollback operations.
    }

    @Override
    public synchronized void commit(long trxSize) throws IOException {
        if (_isClosed) 
            throw new ServerClosedException();

        if (trxSize == 0) {
            return;
        }
        
        // Fsync log file if needed
        if (_durable)
        {
            _file.fsync();
        }
        
        apply(trxSize);
    }
    
    private void apply(long trxSize) throws IOException
    {
        // Checks for checkpoint
        if (trxSize > _data.getModifyBufferRemaining())
        {
            _file.checkpoint(_data, false);
        }
      
        _file.position(_file.getNext());
        DataLengthCalculator source = new DataLengthCalculator(_file);
                
        // Large Trx
        if (trxSize > _data.getModifyBufferRemaining())
        {
            _data.applyDirect(source, trxSize);
        }
        else
        {
            _data.apply(source, trxSize);
        }
        
        // Update header
        _file.incNext(trxSize);
        _file.setDataLength(source.getDataLength());
    }
}
