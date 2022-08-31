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

package com.vyhodb.server;

import com.vyhodb.admin.server.AdminConfig;
import com.vyhodb.admin.slave.SlaveConfig;
import com.vyhodb.rsi.server.RsiConfig;
import com.vyhodb.storage.StorageConfig;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 *
 * @author Igor Vykhodtcev
 */
public class Config implements StorageConfig, RsiConfig, AdminConfig, SlaveConfig {
    
    private static final String EMPTY = "";
    
    @Prop(name="exitVmOnCriticalException")
    private String _exitVmOnCriticalException = "false";
    
    @Prop(name="storage.data")
    private String _storageData = EMPTY;
    
    @Prop(name="storage.log")
    private String _storageLog = EMPTY;
    
    @Prop(name="space.dictionary")
    private String _storageDictionary = EMPTY;
    
    @Prop(name="storage.durable")
    private String _storageDurable = "false";
    
    @Prop(name="storage.cacheSize")
    private int _storageCacheSize = 50000;
    
    @Prop(name="storage.bankCount")
    private int _storageBankCount = 10;
    
    @Prop(name="storage.readDescriptorCount")
    private int _storageReadingQueueLength = 20;
    
    @Prop(name="storage.modifyBufferSize")
    private int _storageModifyBufferSize = 25000;   // 25mb for 1024 page
    
    @Prop(name="storage.logBufferSize")
    private int _storageLogBufferSize = 25000;      // 25mb for 1024 page

    @Prop(name="space.record.maxRecordSize")
    private int _storageMaxRecordSize = 16384;
    
    @Prop(name="space.record.initBufferSize")
    private int _storageInitRecordBufferSize = 8;
    
    @Prop(name="storage.lock.timeout")
    private int _storageLockTimeout = 120;
    
    @Prop(name="space.record.modifyCacheSize")
    private int _storageRecordModifyCacheSize = 300;
    
    @Prop(name="space.mapping.inMemorySize")
    private int _spaceMappingInMemorySize = 3000000;
    
    @Prop(name="space.mapping.directory")
    private String _spaceMappingDirectory = "";
    
    @Prop(name="rsi.host")
    private String _rsiHost = "localhost";
    
    @Prop(name="rsi.enabled")
    private String _rsiEnabled = "false";
    
    @Prop(name="rsi.backlog")
    private int _rsiBacklog = 100;
    
    @Prop(name="rsi.port")
    private int _rsiPort = 47777;
    
    @Prop(name="rsi.cluster.enabled")
    private String _rsiBalancerEnabled = "false";
    
    @Prop(name="rsi.cluster.probe.attempts")
    private int _rsiBalancerAttempts = 40;
    
    @Prop(name="rsi.cluster.probe.timeout")
    private long _rsiBalancerTimeout = 100;
    
    @Prop(name="admin.backlog")
    private int _adminBacklog = 40;
    
    @Prop(name="admin.port")
    private int _adminBindport = 46666;
    
    @Prop(name="admin.host")
    private String _adminHost = "localhost";
    
    @Prop(name="admin.enabled")
    private String _adminEnabled = "false";
    
    @Prop(name="admin.connectionBufferSize")
    private int _adminBufferSize = 64;
    
    @Prop(name="slave.enabled")
    private String _slaveIsEnbled = "false";
    
    @Prop(name="slave.master.host")
    private String _slaveMasterHost = "localhost";
    
    @Prop(name="slave.master.port")
    private int _slaveMasterPort = 46666;
    
    @Prop(name="slave.mode")
    private String _slaveMode = "realtime";
    
    @Prop(name="slave.ttl")
    private long _slaveTTL = 86400000; // 24 hours
    
    @Prop(name="slave.checkTimeout")
    private long _slaveCheckTimeout = 500; // 0,5 second
    
    @Prop(name="slave.cron")
    private String _slaveCron = "60 * * * *"; //
    
    public Config(Properties properties) throws IOException {
        apply(properties);
    }
    
    private void apply(Properties properties) throws IOException
    {
        try
        {
            Prop prop;
            String value;
            Field[] fields = this.getClass().getDeclaredFields();
            
            for (Field field : fields) {
                prop = field.getAnnotation(Prop.class);
                if (prop != null)
                {
                    value = properties.getProperty(prop.name());
                    if (value != null)
                    {
                        if (field.getType().equals(Integer.TYPE))
                        {
                            field.setInt(this, Integer.parseInt(value));
                        }
                        if (field.getType().equals(Long.TYPE))
                        {
                            field.setLong(this, Long.parseLong(value));
                        }
                        else if (field.getType().equals(String.class))
                        {
                            field.set(this, value);
                        }
                    }
                }
            }
        }
        catch(IllegalArgumentException | IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    public int getCacheSize() {
        return _storageCacheSize;
    }

    @Override
    public int getModifyBufferSize() {
        return _storageModifyBufferSize;
    }

    @Override
    public String getDataFilename() {
        return _storageData;
    }

    @Override
    public String getLogFilename() {
        return _storageLog;
    }

    @Override
    public int getMaxRecordSize() {
        return _storageMaxRecordSize;
    }

    @Override
    public int getInitRecordBufferSize() {
        return _storageInitRecordBufferSize;
    }

    @Override
    public int getLockTimeout() {
        return _storageLockTimeout;
    }

    @Override
    public boolean isRsiEnabled() {
        return  toBool(_rsiEnabled);
    }

    @Override
    public String getRsiBindHost() {
        return _rsiHost;
    }

    @Override
    public int getRsiBindPort() {
        return _rsiPort;
    }

    @Override
    public int getRsiBacklog() {
        return _rsiBacklog;
    }

    @Override
    public int getLogBufferSize() {
        return _storageLogBufferSize;
    }
    
    private boolean toBool(String value)
    {
        return "TRUE".equalsIgnoreCase(value) ||
               "YES".equalsIgnoreCase(value) ||
               "ENABLED".equalsIgnoreCase(value) ||
               "1".equalsIgnoreCase(value);
    }

    @Override
    public boolean isDurable() {
        return toBool(_storageDurable);
    }

    @Override
    public boolean isAdminEnabled() {
        return toBool(_adminEnabled);
    }

    @Override
    public String getAdminBindHost() {
        return _adminHost;
    }

    @Override
    public int getAdminBindPort() {
        return _adminBindport;
    }

    @Override
    public int getAdminBacklog() {
        return _adminBacklog;
    }

    @Override
    public boolean isSlaveEnabled() {
        return toBool(_slaveIsEnbled);
    }

    @Override
    public String getSlaveMasterHost() {
        return _slaveMasterHost;
    }

    @Override
    public int getSlaveMasterPort() {
        return _slaveMasterPort;
    }

    @Override
    public String getSlaveMode() {
        return _slaveMode;
    }

    @Override
    public long getSlaveTTL() {
        return _slaveTTL;
    }

    @Override
    public long getSlaveCheckTimeout() {
        return _slaveCheckTimeout;
    }

    @Override
    public String getSlaveCronString() {
        return _slaveCron;
    }

    @Override
    public int getAdminBufferSize() {
        return _adminBufferSize;
    }

    @Override
    public int getRecordModifyCacheSize() {
        return _storageRecordModifyCacheSize;
    }

    @Override
    public int getBankCount() {
        return _storageBankCount;
    }

    @Override
    public int getReadingQueueLength() {
        return _storageReadingQueueLength;
    }

    @Override
    public String getDictionaryFilename() {
        return _storageDictionary;
    }

    @Override
    public boolean isBalancerEnabled() {
        return toBool(_rsiBalancerEnabled);
    }

    @Override
    public int getBalancerAttempts() {
        return _rsiBalancerAttempts;
    }

    @Override
    public long getBalancerTimeout() {
        return _rsiBalancerTimeout;
    }
    
    public boolean getExitVmOnCriticalException() {
        return toBool(_exitVmOnCriticalException);
    }

    @Override
    public int getMappingInMemorySize() {
        return _spaceMappingInMemorySize;
    }

    @Override
    public String getMappingDirectory() {
        return _spaceMappingDirectory;
    }
}
