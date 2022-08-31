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

package com.vyhodb.admin.slave;

import com.vyhodb.admin.WrongPageIdException;
import com.vyhodb.admin.server.AdminClient;
import com.vyhodb.server.ServerClosedException;
import com.vyhodb.storage.CriticalExceptionHandler;
import com.vyhodb.storage.log.TransactionCorruptedException;
import com.vyhodb.storage.pagestorage.PageStorage;

import java.io.IOException;

/**
 *
 * @author Igor Vykhodtcev
 */
 final class RealtimeSlaveAgent extends AbstractAgent {
       
    private AdminClient _client;
    private volatile boolean _isClosed = false;
    private final long _checkTimeout;
    private final long _connectionTTL;
    private final CriticalExceptionHandler _criticalExceptionHandler;

    RealtimeSlaveAgent(PageStorage pageStorage, CriticalExceptionHandler criticalExceptionHandler, String threadName, 
            String masterHost, int masterPort, long checkTimeout, long connectionTTL) {
        super(pageStorage, threadName, masterHost, masterPort);
        
        _criticalExceptionHandler = criticalExceptionHandler;
        _checkTimeout = checkTimeout;
        _connectionTTL = connectionTTL;
    }
    
    @Override
    public void run() {
        logger.info("Started");
        
        try
        {    
            while(true)
            {
                try
                {
                    if (_isClosed) break;
                    Thread.sleep(_checkTimeout);
                    if (_isClosed) break;
                    
                    getClient();
                    session(_client);
                }
                catch(WrongPageIdException ex)
                {
                    logger.error(MESSAGE_EX_OUT_OF_SYNC, ex);
                    close();
                    _criticalExceptionHandler.shutdown(ex);
                    break;
                }
                catch(TransactionCorruptedException ex)
                {
                    logger.error(MESSAGE_EX_CORRUPTED, ex);
                    close();
                    _criticalExceptionHandler.shutdown(ex);
                    break;
                }
                catch(IOException | InterruptedException ex)
                {
                    logger.debug("", ex);
                    
                    if (_client != null) {
                        _client.close();
                        _client = null;
                    }
                }
            }
        }
        catch(SlaveStoppedException | ServerClosedException sse)
        {
        }

        close();
        logger.info("Stopped");
    }
    
    public synchronized void close()
    {
        if (!_isClosed) {
            _isClosed = true;
            if (_client != null) {
                _client.close();
            }
            _client = null;
        }
    }
    
    private synchronized void getClient() throws IOException, WrongPageIdException, SlaveStoppedException
    {
        if (_client == null)
        {
            _client = newReplicationClient();
            checkSlave(_client);
        }
        
        if ((_client.getStartTime() + _connectionTTL) < System.currentTimeMillis())
        {
            _client.close();
            _client = newReplicationClient();
            checkSlave(_client);
        }
    }
    
    
}
