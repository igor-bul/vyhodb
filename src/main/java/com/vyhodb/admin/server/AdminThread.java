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

package com.vyhodb.admin.server;

import com.vyhodb.admin.LogInfo;
import com.vyhodb.admin.WrongPageIdException;
import com.vyhodb.server.Loggers;
import com.vyhodb.server.Server;
import com.vyhodb.storage.pagefile.*;
import com.vyhodb.storage.pagestorage.PageStorage;
import com.vyhodb.storage.pagestorage.SourceBackupData;
import com.vyhodb.storage.pagestorage.SourceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
final class AdminThread implements Runnable {
    private final Logger _logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_ADMIN);
    private final SocketChannel _sc;
    private final PageStorage _pageStorage;
    private final BufferPool _pool;
    private final String _threadName;
    private final Thread _thread;
    private final AdminListener _parentListener;
    private final Server _parentStorage;

    private volatile boolean _isClosed = false;
    private ByteBuffer _buffer;
    
    AdminThread(AdminListener parentListener, Server parentStorage, PageStorage pageStorage, SocketChannel sc, BufferPool pool, String threadName)
    {
        _parentListener = parentListener;
        _parentStorage = parentStorage;
        _pageStorage = pageStorage;
        _sc = sc;
        _pool = pool;
        _threadName = threadName;
        _thread = new Thread(this, _threadName);
        _thread.setDaemon(true);
    }
    
    public void start()
    {
        _thread.start();
    }
    
    public synchronized void close() {
        if (!_isClosed) {
            _isClosed = true;
            
            try {
                _sc.close();
            } catch (IOException ex1) {
                _logger.debug("Exception occurred during socket closing", ex1);
            }
        }
    }
    
    @Override
    public void run() {
        _logger.debug("Started");
        
        Request request;
        try
        {
            _buffer = _pool.getBuffer();
            
            while (true)
            {
                request = Request.receive(_sc, _buffer);
                
                switch(request.command)
                {
                    case Request.COMMAND_CLOSE:
                        return;

                    case Request.COMMAND_READ_DATA:
                        readData();
                        break;
                        
                    case Request.COMMAND_CHECK_LOG:
                        checkLastLog(request);
                        break;
                        
                    case Request.COMMAND_SYNC_LOG:
                        syncLog(request);
                        break;
                        
                    case Request.COMMAND_READ_ONE_LOG_PAGE:
                        readOneLogPage(request);
                        break;
                        
                    case Request.COMMAND_CLEAR_SLAVE:
                        clearSlave();
                        break;
                        
                    case Request.COMMAND_GET_LOG_INFO:
                        getLogInfo();
                        break;
                        
                    case Request.COMMAND_SHRINK:
                        shrink(request);
                        break;
                        
                    case Request.COMMAND_STORAGE_CLOSE:
                        storageClose(request);
                        break;
                        
                    case Request.COMMAND_PING:
                        ping();
                        break; 
                }
            }
        }
        catch(Throwable ex)
        {
            if (_isClosed) {
                _logger.debug("Exception occurred during admin request processing", ex);
            }
            else {
                _logger.warn("Exception occurred during admin request processing", ex);
            }
        }
        finally
        {
            _parentListener.notifyThreadClosed(this);
            
            // Returns buffer to pool
            if (_buffer != null) 
                _pool.returnBuffer(_buffer);
            
            // Closes socket
            close();
        }
        
        _logger.debug("Stopped");
    }
    
    @SuppressWarnings("resource")
    private void readData() throws IOException
    {
        // Sends response
        LogInfo logInfo = _pageStorage.getLogInfo();
        Response.newReadData(logInfo).send(_sc, _buffer);
        
        // Reads and sends pages
        final long dataLength = logInfo.getDataLength();
        if (dataLength > 0)
        {
            Source source = new SourceBackupData(_pageStorage);
            Destination destination = new DestinationNIO(_sc);
            destination.transferFrom(source, _buffer, dataLength);
        }
    }
    
    @SuppressWarnings("resource")
    private void syncLog(Request request) throws WrongPageIdException, IOException
    {
        LogInfo logInfo = _pageStorage.getLogInfo();
        
        // Checks request
        if (request.next < logInfo.getStart() || request.next > logInfo.getNext())
        {    
            Response.newWrongLogPage().send(_sc, _buffer);
            return;
        }

        // Sends response
        Response.newSyncLog(logInfo).send(_sc, _buffer);
        
        // Sends log pages
        final long count = logInfo.getNext() - request.next;
        if (count > 0)
        {
            Source source = new SourceLog(_pageStorage, request.next);
            Destination destination = new DestinationNIO(_sc);
            destination.transferFrom(source, _buffer, count);
        }
    }
    
    private void readOneLogPage(Request request) throws WrongPageIdException, IOException
    {
        LogInfo logInfo = _pageStorage.getLogInfo();
        
        // Checks request
        if (request.next < logInfo.getStart() || request.next > logInfo.getNext())
        {
            Response.newWrongLogPage().send(_sc, _buffer);
            return;
        }
        
        // Sends response
        send(Response.newOk());
  
        // Reads page
        _pageStorage.adminLogRead(request.next, _buffer, 0, 1);
        
        // Sends page
        IOUtils.writeNIO(_sc, _buffer, 0, 1);
    }
    
    private void checkLastLog(Request request) throws IOException
    {
        LogInfo logInfo = _pageStorage.getLogInfo();
        
        // Checks masterLogId
        if (! logInfo.getLogId().equals(request.masterLogId))
        {    
            send(Response.newWrongMaster());
            return;
        }
            
        // Checks request.next
        if ((request.next - 1) < logInfo.getStart() || request.next > logInfo.getNext())
        {    
            send(Response.newWrongLogPage());
            return;
        }
            
        // Reads last log page
        try
        {    
            _pageStorage.adminLogRead(request.next - 1, _buffer, 0, 1);
        }
        catch(WrongPageIdException ex)
        {
            send(Response.newWrongLogPage());
            return;
        }
        
        // Reads last crc and trx id
        int lastCrc = PageHeader.getPageCrc(_buffer, 0);
        UUID lastTrxId = PageHeader.getTrxId(_buffer, 0);
        
        // Checks last page crc
        if ( lastCrc != request.lastCrc)
        {    
            send(Response.newWrongLastPage());
            return;
        }
            
        // Checks last page trx id
        if (! lastTrxId.equals(request.lastTrxId))
        {    
            send(Response.newWrongLastPage());
            return;
        }
        
        // Sends successful response
        send(Response.newOk());
    }
    
    private void send(Response response) throws IOException
    {
        response.send(_sc, _buffer);
    }
    
    private void shrink(Request request) throws IOException
    {
        try
        {
            _pageStorage.shrink(request.shrinkStart);
            send(Response.newOk());
        }
        catch(WrongPageIdException wpe)
        {
            send(Response.newWrongLogPage());
        }
        catch(IOException ex)
        {
            send(Response.newException());
        }
    }
    
    private void clearSlave() throws IOException
    {
        _pageStorage.clearSlave();
        send(Response.newOk());
    }
    
    private void storageClose(Request request) throws IOException
    {
        StorageCloser shutdown = new StorageCloser(request.storageCloseTimeout);
        shutdown.start();
        send(Response.newOk());
    }
    
    private void getLogInfo() throws IOException
    {
        LogInfo logInfo = _pageStorage.getLogInfo();
        send(Response.newGetInfo(logInfo));
    }
    
    private void ping() throws IOException
    {
        send(Response.newOk());
    }
    
    private class StorageCloser implements Runnable
    {
        private final Thread thread = new Thread(this, "Shutdown");
        private final long _timeout;
        
        StorageCloser(long timeout)
        {
            _timeout = timeout;
        }
        
        void start()
        {
            thread.start();
        }

        @Override
        public void run() {
            _logger.info("Storage will be closed in {} seconds", _timeout / 1000);
            
            try {
                Thread.sleep(_timeout);
            } catch (InterruptedException ex) {
            }
            
            
            try {
                _parentStorage.close();
            } catch (IOException ex) {
                 _logger.debug("", ex);
            }
        }
    }
}
