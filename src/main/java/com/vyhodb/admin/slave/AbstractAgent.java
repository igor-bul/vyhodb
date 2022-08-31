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

import com.vyhodb.admin.LogInfo;
import com.vyhodb.admin.WrongPageIdException;
import com.vyhodb.admin.server.AdminClient;
import com.vyhodb.server.Loggers;
import com.vyhodb.storage.log.TransactionCorruptedException;
import com.vyhodb.storage.log.TrxReader;
import com.vyhodb.storage.pagefile.PageHeader;
import com.vyhodb.storage.pagestorage.PageStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public abstract class AbstractAgent implements Runnable {
    
    public static final String MESSAGE_EX_CLOSED = "Exception occurred during socket closing";
    public static final String MESSAGE_EX_OUT_OF_SYNC = "Can't use specified master server any more. Out of synchronization.";
    public static final String MESSAGE_EX_CORRUPTED = "Can't use specified master server any more. Corrupted transaction has found.";
    
    protected final Logger logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_REPLICATION_AGENT);
    
    private final PageStorage _pageStorage;
    private final Thread _thread;
    
    private final InetSocketAddress _masterSocketAddress;
    private final InetSocketAddress _localSocketAddress;
    
    protected final String threadName;
    
    private final ByteBuffer _pageBuffer = ByteBuffer.allocateDirect(PageHeader.PAGE_SIZE);
    
    protected AbstractAgent(PageStorage pageStorage, String threadName, String masterHost, int masterPort)
    {
        
        _pageStorage = pageStorage;
        
        _masterSocketAddress = new InetSocketAddress(masterHost, masterPort);
        _localSocketAddress = new InetSocketAddress(0);
        
        this.threadName = threadName;
        _thread = new Thread(this, threadName);
    }
    
    public final void start()
    {
        _thread.start();
    }
    
    public abstract void close();
    
    protected final AdminClient newReplicationClient() throws IOException
    {
        return new AdminClient(_masterSocketAddress, _localSocketAddress);
    }
    
    protected final void checkSlave(AdminClient client) throws IOException, WrongPageIdException, SlaveStoppedException
    {
        
        LogInfo logInfo = _pageStorage.getLogInfo();
        
        // Check here if slave is turned off
        if (!logInfo.isSlave())
            throw new SlaveStoppedException();
        
        // Reads logInfo and last log page
        _pageStorage.adminLogRead(logInfo.getNext() - 1, _pageBuffer, 0, 1);
                
        // Checks last log page and masterLogId
        client.checkLastLog(
                logInfo.getMasterLogId(), 
                logInfo.getNext(), 
                PageHeader.getTrxId(_pageBuffer, 0), 
                PageHeader.getPageCrc(_pageBuffer, 0)
        );
    }
    
    protected final void session(AdminClient client) throws IOException, WrongPageIdException, SlaveStoppedException, TransactionCorruptedException
    {
        LogInfo logInfo = _pageStorage.getLogInfo();
        
        // Check here if slave is turned off
        if (!logInfo.isSlave())
            throw new SlaveStoppedException();
        
        // Starts to sync log
        long slaveNext = logInfo.getNext();
        long masterNext = client.syncLog(slaveNext);
        
        // Synchronization is needed
        if (masterNext != slaveNext)
        {
            long trxCount = 0;
            while(slaveNext < masterNext)
            {
               slaveNext += readAndApply(client, masterNext - slaveNext);
               trxCount++;
            }
            
            logger.debug("Session completed. {} transactions applied", trxCount);
        }
        else
        {
            logger.debug("Session completed. No new updates.");
        }
    }
    
    /**
     *
     * @return applied transaction size
     * @throws IOException 
     */
    private long readAndApply(AdminClient adminClient, long rest) throws IOException, SlaveStoppedException, TransactionCorruptedException
    {
        long trxSize = 0;
        
        try
        {    
            // Starts transaction
            LogInfo logInfo = _pageStorage.startModify();
            if (! logInfo.isSlave())
                 throw new SlaveStoppedException();
            
            TrxDestination destination = new TrxDestination(_pageStorage, _pageStorage.getLogBuffer(), logInfo.getNext());
            TrxReader reader = new TrxReader(adminClient, destination, _pageBuffer);
            
            // Reads transaction and writes log pages
            trxSize = reader.readTrx(rest);
            
            // Commits transaction
            if (trxSize > 0) {
                destination.flush();
                _pageStorage.commitModify(trxSize);
            } 
            else {
                _pageStorage.rollbackModify();
            }
        }
        catch(Exception ex)
        {
            _pageStorage.rollbackModify();
            throw ex;
        }
        
        return trxSize;
    }
}
