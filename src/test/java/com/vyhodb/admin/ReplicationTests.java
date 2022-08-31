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

package com.vyhodb.admin;

import static com.vyhodb.AllTests.*;
import static com.vyhodb.admin.AdminTestDataHelper.createTestData;
import static com.vyhodb.admin.AdminTestDataHelper.getCount;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Test;

import com.vyhodb.admin.Admin;
import com.vyhodb.server.Server;

public class ReplicationTests {

    private static final int RUNTIME_REPLICATION_WAIT_TIME = 1500;
    private static final int CRON_REPLICATION_WAIT_TIME = 75000;
    
    private static final InetSocketAddress masterAddress = newMasterAdminAddress();
    private static final InetSocketAddress slaveAddress = newSlave1AdminAddress();
    
    @Test
    public void test_Replication_Runtime() throws IOException, InterruptedException {
        Admin admin = Admin.getInstance();
        
        Files.deleteIfExists(Paths.get(BACKUP_FILENAME));
        admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        admin.removeStorageFiles(SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME);
        admin.newStorage(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);

        Server slave = null;
        Server master = Server.start(getMasterProps());
        try {
            // Adds data on MASTER
            createTestData(master, 50);
            
            // Create SLAVE backup
            admin.remoteBackup(masterAddress, BACKUP_FILENAME, 64, DummyBackupListener.SINGLETON);
            
            // Restores SLAVE storage
            admin.restore(BACKUP_FILENAME, SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME, 16, true,  new ConsoleRestoreListener(40));
            
            // Adds additional data on MASTER
            createTestData(master, 50);
            
            // Starts SLAVE and check its data
            slave = Server.start(getRuntimeSlaveProps());
            Thread.sleep(RUNTIME_REPLICATION_WAIT_TIME); // Wait until replication completed.
            assertEquals("Slave has incorrect data after replication", 100, getCount(slave));
            
            // Tests replication iteration
            {
                createTestData(master, 20);
                Thread.sleep(RUNTIME_REPLICATION_WAIT_TIME);
                assertEquals("Slave has incorrect data after replication", 120, getCount(slave));
            }
            
            // Clear slave flag
            {
                admin.remoteClearSlave(slaveAddress);
                Thread.sleep(RUNTIME_REPLICATION_WAIT_TIME);    // To wait SlaveStoppedException
                createTestData(slave, 20);
                assertEquals("Slave has incorrect data", 140, getCount(slave));
            }
        }
        finally {
            if (slave != null) {
                slave.close();
            }
            if (master != null) {
                master.close();
            }
        }
    }
    
    @Test
    public void test_Replication_Cron() throws IOException, InterruptedException {
        Admin admin = Admin.getInstance();
        
        Files.deleteIfExists(Paths.get(BACKUP_FILENAME));
        admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        admin.removeStorageFiles(SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME);
        admin.newStorage(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);

        Server slave = null;
        Server master = Server.start(getMasterProps());
        try {
            // Adds data on MASTER
            createTestData(master, 50);
            
            // Create SLAVE backup
            admin.remoteBackup(masterAddress, BACKUP_FILENAME, 64, DummyBackupListener.SINGLETON);
            
            // Restores SLAVE storage
            admin.restore(BACKUP_FILENAME, SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME, 16, true,  new ConsoleRestoreListener(40));
            
            // Adds additional data on MASTER
            createTestData(master, 50);
            
            // Starts SLAVE and check its data
            slave = Server.start(getCronSlaveProps());
            Thread.sleep(CRON_REPLICATION_WAIT_TIME); // Wait until replication completed.
            assertEquals("Slave has incorrect data after replication", 100, getCount(slave));
            
            // Clear slave flag
            {
                admin.remoteClearSlave(slaveAddress);
                createTestData(slave, 20);
                assertEquals("Slave has incorrect data", 120, getCount(slave));
            }
        }
        finally {
            if (slave != null) {
                slave.close();
            }
            if (master != null) {
                master.close();
            }
        }
    }
    
    @Test
    public void test_Replication_OutOfSync_Runtime() throws IOException, InterruptedException {
        Admin admin = Admin.getInstance();
        
        Files.deleteIfExists(Paths.get(BACKUP_FILENAME));
        admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        admin.removeStorageFiles(SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME);
        admin.newStorage(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);

        Server slave = null;
        Server master = Server.start(getMasterProps());
        try {
            // Adds data on MASTER
            createTestData(master, 50);
            
            // Create SLAVE backup
            admin.remoteBackup(masterAddress, BACKUP_FILENAME, 64, DummyBackupListener.SINGLETON);
            
            // Restores SLAVE storage
            admin.restore(BACKUP_FILENAME, SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME, 16, true,  new ConsoleRestoreListener(40));
            
            // Adds additional data on MASTER
            createTestData(master, 50);
            
            // Shrink MASTER
            admin.remoteShrink(masterAddress, ConsoleShrinkListener.SINGLETON);
            
            // Starts SLAVE. Out of sync on first replication iteration
            slave = Server.start(getRuntimeSlaveProps());
            Thread.sleep(RUNTIME_REPLICATION_WAIT_TIME); // Wait until replication completed.
            assertTrue(slave.isClosed());
        }
        finally {
            if (slave != null) {
                slave.close();
            }
            if (master != null) {
                master.close();
            }
        }
    }
    
    @Test
    public void test_Replication_OutOfSync_Cron() throws IOException, InterruptedException {
        Admin admin = Admin.getInstance();
        
        Files.deleteIfExists(Paths.get(BACKUP_FILENAME));
        admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        admin.removeStorageFiles(SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME);
        admin.newStorage(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);

        Server slave = null;
        Server master = Server.start(getMasterProps());
        try {
            // Adds data on MASTER
            createTestData(master, 50);
            
            // Create SLAVE backup
            admin.remoteBackup(masterAddress, BACKUP_FILENAME, 64, DummyBackupListener.SINGLETON);
            
            // Restores SLAVE storage
            admin.restore(BACKUP_FILENAME, SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME, 16, true,  new ConsoleRestoreListener(40));
            
            // Adds additional data on MASTER
            createTestData(master, 50);
            
            // Shrink MASTER
            admin.remoteShrink(masterAddress, ConsoleShrinkListener.SINGLETON);
            
            // Starts SLAVE. Out of sync on first replication iteration
            slave = Server.start(getCronSlaveProps());
            Thread.sleep(CRON_REPLICATION_WAIT_TIME); // Wait until replication completed.
            assertTrue(slave.isClosed());
        }
        finally {
            if (slave != null) {
                slave.close();
            }
            if (master != null) {
                master.close();
            }
        }
    }
    
    @Test
    public void test_Replication_Cascade() throws IOException, InterruptedException {
        Admin admin = Admin.getInstance();
        
        Files.deleteIfExists(Paths.get(BACKUP_FILENAME));
        admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        admin.removeStorageFiles(SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME);
        admin.removeStorageFiles(SLAVE_2_LOG_FILENAME, SLAVE_2_DATA_FILENAME);
        admin.newStorage(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);

        Server slave1 = null;
        Server cascadedSlave = null;
        Server master = Server.start(getMasterProps());
        try {
            // Adds data on MASTER
            createTestData(master, 50);
            
            // Create SLAVE 1 
            admin.remoteBackup(masterAddress, BACKUP_FILENAME, 64, DummyBackupListener.SINGLETON);
            admin.restore(BACKUP_FILENAME, SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME, 16, true,  new ConsoleRestoreListener(100));
                        
            // Create SLAVE 2
            Files.deleteIfExists(Paths.get(BACKUP_FILENAME));
            admin.backup(SLAVE_1_LOG_FILENAME, SLAVE_1_DATA_FILENAME, BACKUP_FILENAME, 16, DummyBackupListener.SINGLETON);
            admin.restore(BACKUP_FILENAME, SLAVE_2_LOG_FILENAME, SLAVE_2_DATA_FILENAME, 16, true,  new ConsoleRestoreListener(100));
            
            // Adds additional data on MASTER
            createTestData(master, 50);
            
            // Starts both slaves
            cascadedSlave = Server.start(getCascadeSlaveProps());
            slave1 = Server.start(getRuntimeSlaveProps());

            // Checks data on both slaves
            Thread.sleep(RUNTIME_REPLICATION_WAIT_TIME); // Wait until replication completed.
            assertEquals("Cascaded slave has incorrect data after replication", 100, getCount(cascadedSlave));
            assertEquals("Slave has incorrect data after replication", 100, getCount(slave1));
            
            // Tests replication iteration
            {
                createTestData(master, 20);
                Thread.sleep(RUNTIME_REPLICATION_WAIT_TIME);
                assertEquals("Cascaded Slave has incorrect data after replication", 120, getCount(cascadedSlave));
                assertEquals("Slave has incorrect data after replication", 120, getCount(slave1));
            }
            
            // Clear slave flag on Slave 1
            {
                admin.remoteClearSlave(slaveAddress);
                Thread.sleep(RUNTIME_REPLICATION_WAIT_TIME);    // To wait SlaveStoppedException
                createTestData(slave1, 20);
                assertEquals("Slave has incorrect data", 140, getCount(slave1));
                
                // Wait replication to Cascaded slave
                Thread.sleep(RUNTIME_REPLICATION_WAIT_TIME);
                assertEquals("Cascaded slave has incorrect data", 140, getCount(cascadedSlave));
            }
        }
        finally {
            if (slave1 != null) {
                slave1.close();
            }
            if (cascadedSlave != null) {
                cascadedSlave.close();
            }
            if (master != null) {
                master.close();
            }
        }
    }
    
    private static Properties getMasterProps() {
        Properties props = newMasterProperties();
        props.setProperty("storage.logBufferSize", "50");
        return props;
    }
    
    private static Properties getRuntimeSlaveProps() {
        Properties props = getSlaveProps();
        
        props.setProperty("slave.mode", "realtime");
        props.setProperty("slave.checkTimeout", "25");
        props.setProperty("slave.ttl", "250");          // We set so small ttl to check new slave connection instantiation
        
        return props;
    }
    
    private static Properties getCronSlaveProps() {
        Properties props = getSlaveProps();
        
        props.setProperty("slave.mode", "cron");
        props.setProperty("slave.cron", " * * * * *");
                
        return props;
    }
    
    private static Properties getSlaveProps() {
        Properties props = newSlaveProperties1();
        props.setProperty("storage.logBufferSize", "50");
        return props;
    }
    
    private static Properties getCascadeSlaveProps() {
        Properties props = newSlaveProperties2();
        
        props.setProperty("slave.mode", "realtime");
        props.setProperty("slave.checkTimeout", "25");
        props.setProperty("slave.ttl", "250");          // We set so small ttl to check new slave connection instantiation
        
        Properties master = newSlaveProperties1();
        props.setProperty("slave.master.host", master.getProperty("admin.host"));
        props.setProperty("slave.master.port", master.getProperty("admin.port"));
        
        return props;
    }
}
