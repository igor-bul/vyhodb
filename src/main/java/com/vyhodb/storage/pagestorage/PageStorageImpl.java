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

import com.vyhodb.admin.LogInfo;
import com.vyhodb.admin.WrongPageIdException;
import com.vyhodb.server.Loggers;
import com.vyhodb.server.ServerClosedException;
import com.vyhodb.storage.CriticalExceptionHandler;
import com.vyhodb.storage.StorageConfig;
import com.vyhodb.storage.data.Data;
import com.vyhodb.storage.data.DataImpl;
import com.vyhodb.storage.lock.LockManager;
import com.vyhodb.storage.lock.LockManagerImpl;
import com.vyhodb.storage.log.Log;
import com.vyhodb.storage.log.LogImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public class PageStorageImpl implements PageStorage {

    public static final int MAX_LOG_BUFFER = 2097149;
    public static final int MIN_LOG_BUFFER = 5;
    
    public static final int MAX_MODIFY_BUFFER = 2097149;
    public static final int MIN_MODIFY_BUFFER = 0;
    
    private final Logger _logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_SERVER);
    private final CriticalExceptionHandler _criticalExceptionHandler;
    private final LockManager _lockManager;
    private Log _log;
    private Data _data;
        
    private volatile boolean _isClosed = false;
    
    public PageStorageImpl(CriticalExceptionHandler criticalExceptionHandler, StorageConfig config) throws IOException
    {
        _criticalExceptionHandler = criticalExceptionHandler;
        
        String logFilename = config.getLogFilename();
        String dataFilename = config.getDataFilename();
        
        if (logFilename == null || logFilename.trim().isEmpty())
            throw new IllegalArgumentException("logFilename is empty");
        
        if (dataFilename == null || dataFilename.trim().isEmpty())
            throw new IllegalArgumentException("dataFilename is empty");
        
        if (logFilename.equals(dataFilename))
            throw new IOException("log and data filenames are equals!");

        if (config.getLogBufferSize() < MIN_LOG_BUFFER || config.getLogBufferSize() > MAX_LOG_BUFFER)
            throw new IllegalArgumentException("Illegal Log Buffer size: " + config.getLogBufferSize() + ". Log Buffer size must be: " + MIN_LOG_BUFFER + " <= [size] <=" + MAX_LOG_BUFFER);
        
        if (config.getModifyBufferSize() < MIN_MODIFY_BUFFER || config.getModifyBufferSize() > MAX_MODIFY_BUFFER)
            throw new IllegalArgumentException("Illegal Modify Buffer size: " + config.getModifyBufferSize() + ". Modify Buffer size must be: " + MIN_MODIFY_BUFFER + " <= [size] <=" + MAX_MODIFY_BUFFER);
        
        _lockManager = new LockManagerImpl(config.getLockTimeout());
        
        try {
            _data = new DataImpl(config);
            _log = new LogImpl(config, _data);
        }
        catch(Exception ex) {
            if (_data != null) {
                _data.close();
            }
            
            if (_log != null) {
                _log.shutdown();
            }
            
            throw ex;
        }
    }

    @Override
    public synchronized void close() {
        if (!_isClosed)
        {
            try
            {
                _isClosed = true;
                _log.close();
                _data.close();
            }
            catch(IOException ex)
            {
                _logger.error("Error at page storage closing.", ex);
            }
        }
    }
    
    private synchronized void shutdown(Exception ex) {
        if (! _isClosed) {
            try {
                _isClosed = true;
                _log.shutdown();
                _data.close();
            }
            catch(IOException ioex) {
                _logger.debug("Error at page storage shutdown.", ioex);
            }
            
            _criticalExceptionHandler.shutdown(ex);
            throw new ServerClosedException(ex);
        }
    }

    @Override
    public void dataRead(long pageId, ByteBuffer buffer, int pageIndex) {
        if (_isClosed) 
            throw new ServerClosedException();
        
        try {
            _data.readData(pageId, buffer, pageIndex);
        } catch (IOException ex) {
            shutdown(ex);
        }
    }

    @Override
    public void adminDataRead(long pageId, ByteBuffer buffer, int pageIndex) {
        if (_isClosed) 
            throw new ServerClosedException();
        
        try {
            _data.readDataAdmin(pageId, buffer, pageIndex);
        } catch (IOException ex) {
            shutdown(ex);
        }
    }

    @Override
    public LogInfo getLogInfo() {
        if (_isClosed) 
            throw new ServerClosedException();
        
        return _log.getLogInfo();
    }

    @Override
    public void adminLogRead(long fromLogPageId, ByteBuffer buffer, int offset, int count) throws WrongPageIdException {
        if (_isClosed) 
            throw new ServerClosedException();
        
        try {
            _log.readAdmin(fromLogPageId, buffer, offset, count);
        }
        catch (WrongPageIdException wpe)
        {
            throw wpe;
        }
        catch (IOException ex) {
            shutdown(ex);
        }
    }

    @Override
    public void commitModify(long trxSize) {
        if (_isClosed) 
            throw new ServerClosedException();
        
        _lockManager.lockCommit();
        
        try {
            _log.commit(trxSize);
        } 
        catch (IOException ex) {
            shutdown(ex);
        }
        finally
        {
            _lockManager.unlockCommit();
            _lockManager.unlockModify();
        }
    }
    
    @Override
    public void shrink(long shrinkPageId) throws WrongPageIdException {
        _lockManager.lockModify();
        try
        {
            _log.shrink(shrinkPageId);
        }
        catch (WrongPageIdException wpe)
        {
            throw wpe;
        }
        catch(IOException ex)
        {
            shutdown(ex);
        }
        finally
        {
            _lockManager.unlockModify();
        }
    }

    @Override
    public void clearSlave() {
        _lockManager.lockModify();
        try
        {
            _log.clearSlave();
        }
        catch(IOException ex)
        {
            shutdown(ex);
        }
        finally
        {
            _lockManager.unlockModify();
        }
    }

    @Override
    public LogInfo startModify() {
        _lockManager.lockModify();
        _log.start();
        return _log.getLogInfo();
    }

    @Override
    public void logRead(long logPageId, ByteBuffer buffer, int pageIndex) {
        try
        {
            _log.read(logPageId, buffer, pageIndex);
        }
        catch(IOException ex)
        {
            shutdown(ex);
        }
    }
    
    @Override
    public void logAppend(long startLogPageId, ByteBuffer buffer, int count) {
        try
        {
            _log.append(startLogPageId, buffer, count);
        }
        catch(IOException ex)
        {
            shutdown(ex);
        }    
    }

    @Override
    public void rollbackModify() {
        _log.rollback();
        _lockManager.unlockModify();
    }

    @Override
    public void startRead() {
        _lockManager.lockRead();
    }

    @Override
    public void rollbackRead() {
        _lockManager.unlockRead();
    }

    @Override
    public ByteBuffer getLogBuffer() {
        return _log.getLogBuffer();
    }
}
