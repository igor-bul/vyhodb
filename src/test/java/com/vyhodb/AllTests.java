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

package com.vyhodb;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.vyhodb.admin.Admin;
import com.vyhodb.admin.AllAdminTests;
import com.vyhodb.admin.DummyBackupListener;
import com.vyhodb.admin.DummyProgressListener;
import com.vyhodb.f.FunctionTestSuite;
import com.vyhodb.omn.OnmTestSuite;
import com.vyhodb.rsi.BalancerTests;
import com.vyhodb.rsi.RsiTests;
import com.vyhodb.space.RecordTests;
import com.vyhodb.space.index.IndexTestSuite;
import com.vyhodb.space.links.LinksTests;
import com.vyhodb.storage.Data2LogMappingTests;
import com.vyhodb.storage.RecoveryTests;
import com.vyhodb.storage.StorageTests;

@RunWith(Suite.class)
@SuiteClasses({ 
    LinksTests.class,
    RecordTests.class, 
    IndexTestSuite.class,
    RsiTests.class,
    AllAdminTests.class,
    BalancerTests.class,
    OnmTestSuite.class,
    FunctionTestSuite.class,
    RecoveryTests.class,
    StorageTests.class,
    Data2LogMappingTests.class})
public class AllTests {

    public final static String PROPERTY_TESTS_DIR = "com.vyhodb.tests.dir";
    private final static String DEFAULT_TESTS_DIR = ".";
    
    private final static Path unitTestsPath;
    
    static {
        String strUnitTestsPath = System.getProperty(PROPERTY_TESTS_DIR, DEFAULT_TESTS_DIR);
        if (strUnitTestsPath.trim().isEmpty()) {
            throw new RuntimeException("System property [" + PROPERTY_TESTS_DIR + "] must be specified");
        }
        
        unitTestsPath = Paths.get(strUnitTestsPath);
        
        // initReplicationProperties();
    }
    
    public static final String MASTER_LOG_FILENAME = getAbsoluteFilename("master_vyho_log");
    public static final String MASTER_DATA_FILENAME = getAbsoluteFilename("master_vyho_data");
    public static final String SLAVE_1_LOG_FILENAME = getAbsoluteFilename("slave_vyho_1_log");
    public static final String SLAVE_1_DATA_FILENAME = getAbsoluteFilename("slave_vyho_1_data");
    public static final String SLAVE_2_LOG_FILENAME = getAbsoluteFilename("slave_vyho_2_log");
    public static final String SLAVE_2_DATA_FILENAME = getAbsoluteFilename("slave_vyho_2_data");
    public static final String BACKUP_FILENAME = getAbsoluteFilename("test_backup.bak");
   
    public static Path resolve(String filename) {
        return unitTestsPath.resolve(filename);
    }
        
    public static String getAbsoluteFilename(String filename) {
        return unitTestsPath.resolve(filename).toString();
    }
    
    public static String getURI(String filename) {
        return unitTestsPath.resolve(filename).toUri().toString();
    }

    public static Properties newMasterProperties() {
        Properties props = new Properties();
        
        props.setProperty("storage.log", MASTER_LOG_FILENAME);
        props.setProperty("storage.data", MASTER_DATA_FILENAME);
        
        props.setProperty("rsi.enabled", "true");
        props.setProperty("rsi.port", "47777");
        props.setProperty("rsi.host", "localhost");
        
        props.setProperty("admin.enabled", "true");
        props.setProperty("admin.port", "60000");
        props.setProperty("admin.host", "localhost");
        
        return props;
    }
    
    public static InetSocketAddress newMasterAdminAddress() {
        return new InetSocketAddress("localhost", 60000);
    }
    
    public static Properties newSlaveProperties1() {
        Properties props = new Properties();
        
        props.setProperty("storage.log", SLAVE_1_LOG_FILENAME);
        props.setProperty("storage.data", SLAVE_1_DATA_FILENAME);
        
        props.setProperty("rsi.enabled", "true");
        props.setProperty("rsi.port", "47778");
        props.setProperty("rsi.host", "localhost");
        
        props.setProperty("slave.enabled", "true");
        props.setProperty("slave.master.host", "localhost");
        props.setProperty("slave.master.port", "60000");
        
        props.setProperty("admin.enabled", "true");
        props.setProperty("admin.port", "60001");
        props.setProperty("admin.host", "localhost");
        
        return props;
    }
    
    public static InetSocketAddress newSlave1AdminAddress() {
        return new InetSocketAddress("localhost", 60001);
    }
    
    public static Properties newSlaveProperties2() {
        Properties props = new Properties();
        
        props.setProperty("storage.log", SLAVE_2_LOG_FILENAME);
        props.setProperty("storage.data", SLAVE_2_DATA_FILENAME);
        
        props.setProperty("rsi.enabled", "true");
        props.setProperty("rsi.port", "47779");
        props.setProperty("rsi.host", "localhost");
        
        props.setProperty("slave.enabled", "true");
        props.setProperty("slave.master.host", "localhost");
        props.setProperty("slave.master.port", "60000");
        
        props.setProperty("admin.enabled", "true");
        props.setProperty("admin.port", "60002");
        props.setProperty("admin.host", "localhost");
        
        return props;
    }

    public static InetSocketAddress newSlave2AdminAddress() {
        return new InetSocketAddress("localhost", 60002);
    }
    
    public static InetSocketAddress newLocalAddress() {
        return new InetSocketAddress(0);
    }
    
    public static void backupAndRestoreCluster(Admin admin) throws IOException {
        admin.removeStorageFiles(SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME);
        admin.removeStorageFiles(SLAVE_2_LOG_FILENAME, SLAVE_2_DATA_FILENAME);
        
        Files.deleteIfExists(Paths.get(BACKUP_FILENAME));
                
        admin.backup(
                MASTER_LOG_FILENAME, 
                MASTER_DATA_FILENAME,
                BACKUP_FILENAME,
                32,
                DummyBackupListener.SINGLETON
        );
        
        admin.restore(
                BACKUP_FILENAME, 
                SLAVE_1_LOG_FILENAME, 
                SLAVE_1_DATA_FILENAME, 
                16,
                true, 
                DummyProgressListener.SINGLETON
        );
        
        admin.restore(
                BACKUP_FILENAME, 
                SLAVE_2_LOG_FILENAME, 
                SLAVE_2_DATA_FILENAME, 
                16,
                true, 
                DummyProgressListener.SINGLETON
        );
    }
    
    public static Collection<InetSocketAddress> getSlavesAddresses() {
        ArrayList<InetSocketAddress> result = new ArrayList<>();
        
        Properties slave1 = newSlaveProperties1();
        Properties slave2 = newSlaveProperties2();
        
        result.add(new InetSocketAddress(slave1.getProperty("admin.host"), Integer.parseInt(slave1.getProperty("admin.port"))));
        result.add(new InetSocketAddress(slave2.getProperty("admin.host"), Integer.parseInt(slave2.getProperty("admin.port"))));
        
        return result;
    }
    
    public static void sleep(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
