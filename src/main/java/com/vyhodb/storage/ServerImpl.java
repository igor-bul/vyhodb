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

package com.vyhodb.storage;

import com.vyhodb.admin.server.AdminListener;
import com.vyhodb.admin.slave.AbstractAgent;
import com.vyhodb.admin.slave.SlaveAgentFactory;
import com.vyhodb.rsi.server.RsiCallFactory;
import com.vyhodb.rsi.server.RsiServer;
import com.vyhodb.server.*;
import com.vyhodb.storage.pagestorage.*;
import com.vyhodb.storage.rm.BlockManager;
import com.vyhodb.storage.rm.Descriptor;
import com.vyhodb.storage.rm.RecordManager;
import com.vyhodb.storage.space.Dictionary;
import com.vyhodb.storage.space.modify.TrxSpaceModify;
import com.vyhodb.storage.space.read.TrxSpaceRead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class ServerImpl extends Server implements CriticalExceptionHandler {

    private static final String PAGE_SLAVE_MODE = "Can't start modify transaction on slave storage.";
    private static final String CPR = "\nvyhodb database management system. Version 0.9.0.\nCopyright (C) 2015 Igor Vykhodtsev. See LICENSE file for a terms of use.\n";
    
    private final Logger _logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_SERVER);
    private RsiServer _rsiServer;
    private AdminListener _adminServer;
    private AbstractAgent _slaveAgent;
    private PageStorage _pageStorage;
    private Dictionary _dictionary;
    private volatile boolean _isClosed = false;
    private Thread _shutdownHook;
    private Config _config;
    
    
    private final boolean _exitVmOnCriticalException;
//    private final int _recordBufferSize;
//    private final int _maxRecordSize;
//    private final int _recordModifyCacheSize;
    
    public ServerImpl(Properties properties)  throws IOException {
        System.out.println(CPR);
        
        _config = new Config(properties);
        
//        _recordBufferSize = config.getInitRecordBufferSize();
//        _maxRecordSize = config.getMaxRecordSize();
//        _recordModifyCacheSize = config.getRecordModifyCacheSize();
        _exitVmOnCriticalException = _config.getExitVmOnCriticalException();
        _dictionary = new DictionaryImpl(_config);
        
        try {
            _pageStorage = new PageStorageImpl(this, _config);
            
            if (_config.isAdminEnabled())
            {
                _adminServer = new AdminListener(this, _pageStorage, "Admin", "Admin connection", _config);
                _adminServer.start();
            }
            
            if (_config.isSlaveEnabled() && _pageStorage.getLogInfo().isSlave())
            {
                _slaveAgent = SlaveAgentFactory.newSlave(_pageStorage, this, _config);
                _slaveAgent.start();
            }
            
            if (_config.isRsiEnabled())
            {
                _rsiServer = new RsiServer(_config, new RsiCallFactory(this), this, "RSI Server", "RSI connection");
            }
            
            // Shutdown hook
            _shutdownHook = new Thread(new Shutdown(), "vyhodb shutdown hook");
            Runtime.getRuntime().addShutdownHook(_shutdownHook);
            
            _logger.info("\n  Log file: {}\n  Data file: {}\n  Dictionary file: {}\n\n  Cache size: {} pages\n  Modify buffer size: {} pages\n  Log buffer size: {} pages\n",
                    Paths.get(_config.getLogFilename()).toAbsolutePath(),
                    Paths.get(_config.getDataFilename()).toAbsolutePath(),
                    _config.getDictionaryFilename().trim().equals("") ? "" : Paths.get(_config.getDictionaryFilename()).toAbsolutePath(),
                    
                    _config.getCacheSize(),
                    _config.getModifyBufferSize(),
                    _config.getLogBufferSize()
                    );
            _logger.info("vyhodb server started.");
        }
        catch(Exception ex) {
            _logger.error("", ex);
            close0(_shutdownHook != null);
            throw ex;
        }
    }
    
    @Override
    public  void close() {
        close0(true);
    }
    
    private synchronized void close0(boolean hookUnregister) {
        if (!_isClosed)    {
            _isClosed = true;
            
            if (hookUnregister) {
                Runtime.getRuntime().removeShutdownHook(_shutdownHook);
            }
            
            if (_rsiServer != null) {
                _rsiServer.close();
                _rsiServer = null;
            }
            
            if (_adminServer != null) {
                _adminServer.close();
                _adminServer = null;
            }
            
            if (_slaveAgent != null) {
                _slaveAgent.close();
                _slaveAgent = null;
            }
            
            if (_pageStorage != null) {
                _pageStorage.close();
                _pageStorage = null;
            }
            
            _logger.info("vyhodb server closed.");
        }
    }
    
    @Override
    public synchronized void shutdown(Throwable ex) {
        _logger.error("Critical exception occured. Server will be closed.", ex);
        
        close0(true);
        
        if (_exitVmOnCriticalException) {
            _logger.error("JVM will be stopped");
            System.exit(1);
        }
    }
    
    public TrxSpace startReadTrx(UUID trxId) {
        if (_isClosed) {
            throw new ServerClosedException();
        }
        
        PageTrx pTrx = new ReadTrxImpl(_pageStorage);
        pTrx.start();
        return new TrxSpaceRead(buildRM(pTrx));
        //return new TrxSpaceReadCache(buildRM(pTrx));
    }

    public TrxSpace startModifyTrx(UUID trxId) {
        if (_isClosed) {
            throw new ServerClosedException();
        }
        
        if (_pageStorage.getLogInfo().isSlave())
            throw new TransactionRolledbackException(PAGE_SLAVE_MODE);
        
        PageTrx pTrx = new ModifyTrxImpl(_pageStorage, trxId, _config);
        pTrx.start();
        return new TrxSpaceModify(buildRM(pTrx), _config);
    }
    
    @Override
    public TrxSpace startReadTrx() {
        return startReadTrx(UUID.randomUUID());
    }

    @Override
    public TrxSpace startModifyTrx() {
        return startModifyTrx(UUID.randomUUID());
    }
    
    @Override
    public boolean isClosed() {
        return _isClosed;
    }
    
    public long getNext() {
        if (_isClosed) {
            throw new ServerClosedException();
        }
        
        return _pageStorage.getLogInfo().getNext();
    }
    
    private RecordManager buildRM(PageTrx pageTrx)
    {
        final BlockManager bm = new BlockManager(pageTrx);
        final Descriptor dm = new Descriptor(bm);
        return new RecordManager(bm, dm, _dictionary, _config);
    }
    
    private class Shutdown implements Runnable {
        @Override
        public void run() {
            close0(false);
        }
    }
}
