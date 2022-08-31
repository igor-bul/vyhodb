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

import static org.junit.Assert.*;
import static com.vyhodb.AllTests.*;
import static com.vyhodb.admin.AdminTestDataHelper.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import com.vyhodb.admin.Admin;
import com.vyhodb.admin.LogInfo;
import com.vyhodb.admin.server.AdminClient;
import com.vyhodb.server.Server;
import com.vyhodb.server.TransactionRolledbackException;

public class AdminTests {

    private static final InetSocketAddress masterAddress = newMasterAdminAddress();
    private static final InetSocketAddress localAddress = newLocalAddress();
    
    @Test
    public void test_Admin() throws IOException, InterruptedException {
        Admin admin = Admin.getInstance(localAddress);
        
        admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        admin.newStorage(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        
        Server storage = Server.start(newMasterProperties());
        try {
            // Tests getLogInfo
            LogInfo logInfo = admin.remoteGetLogInfo(masterAddress);
            assertNotNull(logInfo);
 
            // Tests ping
            admin.remotePingAdmin(masterAddress);
            
            // Tests storage closing and exception on admin connection
            {
                AdminClient adminClient = new AdminClient(masterAddress, localAddress);
                adminClient.storageClose(0);
                Thread.sleep(1000);         // Wait for storage closing
                try {
                    adminClient.ping();
                    fail("Admin client connection was not closed.");
                }
                catch(IOException ioe) {
                }
            }
        }
        finally {
            storage.close();
        }
    }
    
    @Test
    public void test_Backup_Online_Master() throws IOException, ReflectiveOperationException {
        test_Backup_Online(false);
    }
    
    @Test
    public void test_Backup_Online_Slave() throws IOException, ReflectiveOperationException {
        test_Backup_Online(true);
    }
    
    private static void test_Backup_Online(boolean slave) throws IOException {
        Admin admin = Admin.getInstance();
        
        Files.deleteIfExists(Paths.get(BACKUP_FILENAME));
        admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        admin.newStorage(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);

        Server storage = Server.start(newMasterProperties());
        try {
            createTestData(storage, 50);
            
            admin.remoteBackup(masterAddress, BACKUP_FILENAME, 64, DummyBackupListener.SINGLETON);
                        
            storage.close();
            
            admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
            admin.restore(BACKUP_FILENAME, MASTER_LOG_FILENAME, MASTER_DATA_FILENAME, 16, slave,  new ConsoleRestoreListener(40));
        
            storage = Server.start(newMasterProperties());
            assertEquals("Restored storage doesn't contain data.", 50, getCount(storage));
            
            // Try to modify data on restored storage
            {
                if (slave) {
                    try {
                        createTestData(storage, 50);
                        fail("Can modify data on slave storage");
                    }
                    catch(TransactionRolledbackException tre) {
                        tre.printStackTrace();
                    }
                    assertEquals(50, getCount(storage));
                }
                else {
                    createTestData(storage, 50);
                    assertEquals(100, getCount(storage));
                }
            }
        }
        finally {
            storage.close();
        }
    }
}
