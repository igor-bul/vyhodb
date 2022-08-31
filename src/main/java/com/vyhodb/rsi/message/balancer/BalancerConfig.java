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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

class BalancerConfig {
    public static final String PROP_DEBUG = "debug";
    public static final String PROP_REFRESH_CONFIG_TIMEOUT = "refreshConfigTimeout";
    public static final String PROP_READ_NODE_COUNT = "read.poolCount";
    public static final String PREFIX_PROP_READ = "read.";

    public static final String PROP_ERROR_ATTEMPTS = "error.attempts";
    public static final String PROP_ERROR_TIMEOUT = "error.timeout";

    public static final String PROP_MODIFY_POOL_SIZE = "modify.poolSize";
    public static final String PROP_MODIFY_POOL_TTL = "modify.poolTTL";
    public static final String PROP_MODIFY_HOSTNAME = "modify.host";
    public static final String PROP_MODIFY_PORT = "modify.port";
    
    public static final String SUFFIX_PROP_HOSTNAME = ".host";
    public static final String SUFFIX_PROP_PORT = ".port";
    public static final String SUFFIX_PROP_POOL_SIZE = ".poolSize";
    public static final String SUFFIX_PROP_POOL_TTL = ".poolTTL";
    
    public static final long DEFAULT_REFRESH_CONFIG_TIMEOUT = 600000;
    public static final int DEFAULT_POOL_SIZE = 5;
    public static final long DEFAULT_POOL_TTL = 3600000;
    public static final int DEFAULT_ERROR_ATTEMPTS = 6;
    public static final long DEFAULT_ERROR_TIMEOUT = 1000;
    public static final boolean DEFAULT_DEBUG = false;
    public static final int DEFAULT_RSI_PORT = 47777;
    
    private final long _checkConfigTimeout;
    private final int _errorAttempts;
    private final long _errorTimeOut;
    private final boolean _debug;
    private final int _readNodeCount;
    private final ArrayList<NodeConfig> _readNodes;
    private final Properties _properties;
    private final NodeConfig _modifyNode;
    
    BalancerConfig(Properties properties) {
        _properties = properties;
        _checkConfigTimeout = getProperty(properties, PROP_REFRESH_CONFIG_TIMEOUT, DEFAULT_REFRESH_CONFIG_TIMEOUT);
        
        _debug = getProperty(properties, PROP_DEBUG, DEFAULT_DEBUG);
        _errorAttempts = getProperty(properties, PROP_ERROR_ATTEMPTS, DEFAULT_ERROR_ATTEMPTS);
        _errorTimeOut = getProperty(properties, PROP_ERROR_TIMEOUT, DEFAULT_ERROR_TIMEOUT);
        
        _modifyNode = getModifyNodeConfig(properties);

        // PROP_READ_NODE_COUNT
        if (!properties.containsKey(PROP_READ_NODE_COUNT)) {
            throw new RsiClientException("Property [" + PROP_READ_NODE_COUNT + "] is absent.");
        }
        _readNodeCount = Integer.decode(properties.getProperty(PROP_READ_NODE_COUNT));
        
        // Creates ReadNodeConfigs
        _readNodes = new ArrayList<>(_readNodeCount);
        for (int i = 0; i < _readNodeCount; i++) {
            _readNodes.add(getReadNodeConfig(properties, i));
        }
    }
    
    private NodeConfig getModifyNodeConfig(Properties properties) {
        if (! properties.containsKey(PROP_MODIFY_HOSTNAME)) {
            return null;
        }
        
        String modifyHostName = properties.getProperty(PROP_MODIFY_HOSTNAME);
        int modifyPort = getProperty(properties, PROP_MODIFY_PORT, DEFAULT_RSI_PORT);
        int modifyPoolSize = getProperty(properties, PROP_MODIFY_POOL_SIZE, DEFAULT_POOL_SIZE);
        long modifyPoolTTL = getProperty(properties, PROP_MODIFY_POOL_TTL, DEFAULT_POOL_TTL);
        
        return new NodeConfig(modifyHostName, modifyPort, modifyPoolSize, modifyPoolTTL);
    }
    
    private NodeConfig getReadNodeConfig(Properties properties, int i) {
        String hostNameKey = PREFIX_PROP_READ + i + SUFFIX_PROP_HOSTNAME;
        String portKey = PREFIX_PROP_READ + i + SUFFIX_PROP_PORT;
        String poolSizeKey = PREFIX_PROP_READ + i + SUFFIX_PROP_POOL_SIZE;
        String poolTTLKey = PREFIX_PROP_READ + i + SUFFIX_PROP_POOL_TTL;
        
        if (! properties.containsKey(hostNameKey)) {
            throw new RsiClientException("Property [" + hostNameKey + "] is absent.");
        }
        
        String hostName = properties.getProperty(hostNameKey);
        int port = getProperty(properties, portKey, DEFAULT_RSI_PORT);
        int poolSize = getProperty(properties, poolSizeKey, DEFAULT_POOL_SIZE);
        long poolTTL = getProperty(properties, poolTTLKey, DEFAULT_POOL_TTL);
        
        return new NodeConfig(hostName, port, poolSize, poolTTL);
    }
    
    long getCheckConfigTimeout() {
        return _checkConfigTimeout;
    }
    
    int getErrorAttempts() {
        return _errorAttempts;
    }
    
    long getErrorTimeout() {
        return _errorTimeOut;
    }
    
    boolean isDebug() {
        return _debug;
    }
    
    NodeConfig getModifyNode() {
        return _modifyNode;
    }
    
    List<NodeConfig> getReadNodes() {
        return _readNodes;
    }
    
    int getReadNodeCount() {
        return _readNodeCount;
    }

    private int getProperty(Properties properties, String propertyName, int defaultValue) {
        String value = properties.getProperty(propertyName);
        if (value == null || "".equals(value.trim())) {
            return defaultValue;
        }
        return Integer.decode(value);
    }
    
    private long getProperty(Properties properties, String propertyName, long defaultValue) {
        String value = properties.getProperty(propertyName);
        if (value == null || "".equals(value.trim())) {
            return defaultValue;
        }
        return Long.decode(value);
    }
    
    private boolean getProperty(Properties properties, String propertyName, boolean defaultValue) {
        String value = properties.getProperty(propertyName);
        if (value == null || "".equals(value.trim())) {
            return defaultValue;
        }
        return Boolean.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (! (obj instanceof BalancerConfig)) {
            return false;
        }
        
        BalancerConfig other = (BalancerConfig) obj;
        return _properties.equals(other._properties);
    }
    
    @Override
    public String toString() {
        return null;
    }
    
}
