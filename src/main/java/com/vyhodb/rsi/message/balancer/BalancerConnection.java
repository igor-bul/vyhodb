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

package com.vyhodb.rsi.message.balancer;

import com.vyhodb.rsi.RsiClientException;
import com.vyhodb.rsi.message.Message;
import com.vyhodb.rsi.message.MessageConnection;
import com.vyhodb.rsi.message.Pool;
import com.vyhodb.rsi.message.TcpPool;
import com.vyhodb.rsi.socket.SimpleTcpFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

public class BalancerConnection implements MessageConnection {
    private final URL _configURL;
    private final Random _random;
    private final long _startTime;
    
    private ArrayList<Pool> _readPools;
    private Pool _modifyPool;
    private BalancerConfig _config;
    private volatile long _lastRefresh;
    
    public BalancerConnection(URL configURL) throws IOException {
        _startTime = System.currentTimeMillis();
        _configURL = configURL;
        _random = new Random();
        refreshConfig();
    }

    @Override
    public Message process(Message messageIn) throws Throwable {
        Message messageOut;
        
        if (messageIn.readOnly) {
            messageOut = processRead(messageIn);
        } else {
            if (isReadOnlyBalancer()) {
                throw new RsiClientException("No modify pool is configured for balancer connection.");
            }
            messageOut = processModify(messageIn);
        }
        
        return messageOut;
    }
    
    @Override
    public synchronized void close() throws IOException {
        if (_modifyPool != null) {
            _modifyPool.close();
        }
        
        if (_readPools != null) {
            for (Pool readPool : _readPools) {
                readPool.close();
            }
        }
    }

    @Override
    public long getStartTime() {
        return _startTime;
    }
    
    private Message processRead(Message message) throws Throwable {
        Pool pool;
        Message response = null;
        MessageConnection mc = null;
        
        int attemp = 0;
        
        // Gets pooled connection
        do {
            try {
                attemp++;
                pool = getReadPool();
                mc = pool.take();
                break;
            } catch(Throwable th) {
                error(th);
                refreshConfig();
                
                if (attemp == _config.getErrorAttempts()) {
                    throw th;
                }
                sleep(_config.getErrorTimeout());
            }
        } 
        while (true);
        
        // Sends request
        attemp = 0;
        do {
            try {
                attemp++;
                
                if (pool == null) {
                    pool = getReadPool();
                    mc = pool.take();
                }
                
                response = mc.process(message);
                break;
            } catch(Throwable th) {
                pool.free(mc);
                pool = null;
                
                error(th);
                refreshConfig();
                
                if (attemp == _config.getErrorAttempts()) {
                    throw th;
                }
                
                sleep(_config.getErrorTimeout());
            }
        }
        while (true);
        
        // Returns connection to pool
        pool.offer(mc);
        return response;
    }

    private Message processModify(Message message) throws Throwable {
        Pool pool;
        Message response = null;
        MessageConnection mc = null;
        
        int attemp = 0;
        
        // Gets pooled connection
        do {
            try {
                attemp++;
                pool = getModifyPool();
                mc = pool.take();
                break;
            } catch(Throwable th) {
                error(th);
                refreshConfig();
                
                if (attemp == _config.getErrorAttempts()) {
                    throw th;
                }
                sleep(_config.getErrorTimeout());
            }
        } 
        while (true);
        
        // Sends request
        try {
            response = mc.process(message);
        } catch(Throwable th) {
            pool.free(mc);
            error(th);
            refreshConfig();
            throw th;
        }
        
        // Returns connection to pool
        pool.offer(mc);
        return response;
    }
    
    private synchronized Pool getReadPool() throws IOException {
        // Check for elapsed time
        if (isRefreshConfigNeeded()) {
            refreshConfig();
        }
        
        int poolIndex = _random.nextInt(_readPools.size());
        return _readPools.get(poolIndex);
    }
    
    private synchronized Pool getModifyPool() throws IOException {
        // Check for elapsed time
        if (isRefreshConfigNeeded()) {
            refreshConfig();
        }
                
        return _modifyPool;
    }
    
    private synchronized void refreshConfig() throws IOException {
        BalancerConfig newConfig = new BalancerConfig(loadProperties());
        _lastRefresh = System.currentTimeMillis();
        if (! newConfig.equals(_config)) {
            
            // Close current pools
            if (_modifyPool != null) {
                _modifyPool.warmClose();
                for (Pool readPool : _readPools) {
                    readPool.warmClose();
                }
            }
            
            _config = newConfig;
            
            // Creates new pools
            NodeConfig modifyNodeConfig = _config.getModifyNode();
            if (modifyNodeConfig != null) {
                _modifyPool = newTcpPool(modifyNodeConfig, _config.isDebug());
            }
            
            _readPools = new ArrayList<>(_config.getReadNodeCount());
            for (NodeConfig readNode : _config.getReadNodes()) {
                _readPools.add(newTcpPool(readNode, _config.isDebug()));
            }
            
            // Debug TODO implements log about pools
            if (_config.isDebug()) {
                System.out.println("config has been refreshed.");
            }
        }
    }
    
    private Pool newTcpPool(NodeConfig nodeConfig, boolean debug) {
        return new TcpPool(new SimpleTcpFactory(nodeConfig.getHostName(), nodeConfig.getPort()), nodeConfig.getPoolSize(), nodeConfig.getPoolTTL(), debug);
    }
    
    private boolean isRefreshConfigNeeded() {
        return (System.currentTimeMillis() - _lastRefresh) >= _config.getCheckConfigTimeout();
    }

    private Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        URLConnection connection = _configURL.openConnection();
        connection.connect();
        try (InputStream in = connection.getInputStream()) {
            properties.load(in);
        }
        
        return properties;
    }
        
    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {}
    }
    
    private void error(Throwable th) {
        if (_config.isDebug()) {
            th.printStackTrace();
        }
    }
    
    private boolean isReadOnlyBalancer() {
        return _modifyPool == null;
    }
}
