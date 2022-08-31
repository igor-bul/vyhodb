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
import com.vyhodb.server.Loggers;
import com.vyhodb.server.ServerClosedException;
import com.vyhodb.storage.CriticalExceptionHandler;
import com.vyhodb.storage.log.TransactionCorruptedException;
import com.vyhodb.storage.pagestorage.PageStorage;
import it.sauronsoftware.cron4j.SchedulingPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 * @author Igor Vykhodtcev
 */
class CronSlaveAgent extends AbstractAgent {

    private final Logger _logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_REPLICATION_AGENT);
    private final static long CRON_WAIT_TIMEOUT = 60000;    // ~ 1 minute
    
    private final SchedulingPattern _pattern;
    private final CriticalExceptionHandler _criticalExceptionHandler;
    private AdminClient _client;
    private volatile boolean _isClosed = false;
    
    CronSlaveAgent(PageStorage pageStorage, CriticalExceptionHandler criticalExceptionHandler, String threadName, String masterHost, int masterPort, String cron) {
        super(pageStorage, threadName, masterHost, masterPort);
        
        _criticalExceptionHandler = criticalExceptionHandler;
        _pattern = new SchedulingPattern(cron);
    }
    
    @Override
    public void run() {
        _logger.info("Started");
        
        try
        {    
            while(true)
            {
                try
                {
                    if (_isClosed) break;
                    waitCron();
                    if (_isClosed) break;
                    
                    _client = newReplicationClient();
                    checkSlave(_client);
                    session(_client);
                }
                catch(WrongPageIdException ex)
                {
                    _logger.error(MESSAGE_EX_OUT_OF_SYNC, ex);
                    close();
                    _criticalExceptionHandler.shutdown(ex);
                    break;
                }
                catch(TransactionCorruptedException tce)
                {
                    _logger.error(MESSAGE_EX_CORRUPTED, tce);
                    close();
                    _criticalExceptionHandler.shutdown(tce);
                    break;
                }
                catch(IOException | InterruptedException ex)
                {
                    _logger.debug("", ex);
                }
                finally
                {
                    close();
                }
            }
        }
        catch(SlaveStoppedException | ServerClosedException sse)
        {
            _logger.debug("", sse);
        }

        _logger.info("Stopped");
    }
    
    @Override
    public synchronized void close()
    {
        if (! _isClosed) {
            _isClosed = true;
             
            if (_client != null) {
                _client.close();
            }

            _client = null;
        }
    }
    
    private void waitCron() throws InterruptedException
    {
        long time = 0;
        
        do
        {
            Thread.sleep(CRON_WAIT_TIMEOUT);
            time = System.currentTimeMillis();
        }
        while( !(_pattern.match(time) || _pattern.match(time - CRON_WAIT_TIMEOUT)) );
        // Double match is needed if thread scheduler starts current thread after 
        // CRON_WAIT_TIMEOUT. In this case we could loose execution.
    }
}
