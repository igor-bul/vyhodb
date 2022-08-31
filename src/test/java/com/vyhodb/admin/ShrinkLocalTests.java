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

import static com.vyhodb.AllTests.MASTER_DATA_FILENAME;
import static com.vyhodb.AllTests.MASTER_LOG_FILENAME;
import static com.vyhodb.AllTests.backupAndRestoreCluster;
import static com.vyhodb.AllTests.getSlavesAddresses;
import static com.vyhodb.AllTests.newMasterAdminAddress;
import static com.vyhodb.AllTests.newMasterProperties;
import static com.vyhodb.AllTests.newSlaveProperties1;
import static com.vyhodb.AllTests.newSlaveProperties2;
import static com.vyhodb.AllTests.sleep;
import static com.vyhodb.admin.AdminTestDataHelper.createTestData;
import static com.vyhodb.admin.AdminTestDataHelper.getCount;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.vyhodb.server.Server;

public class ShrinkLocalTests {


    @Test
    public void test_Shrink_Local_Empty() throws IOException {
        shrink_Empty(true);
    }
    
    @Test
    public void test_Shrink_Remote_Empty() throws IOException {
        shrink_Empty(false);
    }
    
    @Test
    public void test_Shrink_Local_WithData() throws IOException {
        shrink_WithData(true);
    }
    
    @Test
    public void test_Shrink_Remote_WithData() throws IOException {
        shrink_WithData(false);
    }
    
    @Test
    public void test_Shrink_Local_Master() throws IOException {
        shrink_Master(true);
    }
    
    @Test
    public void test_Shrink_Remote_Master() throws IOException {
        shrink_Master(false);
    }
    
    /**
     * Steps:
     * - create storage
     * 
     * - shrink
     * 
     * - open
     * - add data
     * - read and test data
     * 
     * @throws IOException
     */
    private void shrink_Empty(boolean isLocal) throws IOException {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        admin.newStorage(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        
        if (isLocal) {
            admin.shrink(MASTER_LOG_FILENAME, 16, ConsoleShrinkListener.SINGLETON);
            
            try(Server server = Server.start(newMasterProperties())) {
                assertEquals(0, getCount(server));
                createTestData(server, 10);
                assertEquals(10, getCount(server));
            }
        } else {
            try(Server server = Server.start(newMasterProperties())) {
                admin.remoteShrink(newMasterAdminAddress(), ConsoleShrinkListener.SINGLETON);
                
                assertEquals(0, getCount(server));
                createTestData(server, 10);
                assertEquals(10, getCount(server));
            }
        }
    }
    
    /**
     * Steps:
     * - create storage
     * - open
     * - add data
     * - close
     * 
     * - shrink
     * 
     * - open
     * - read and test data
     * - add data
     * - read and test data
     * 
     * @throws IOException 
     */
    private void shrink_WithData(boolean isLocal)  throws IOException {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        admin.newStorage(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        
        try(Server server = Server.start(newMasterProperties())) {
            createTestData(server, 10);
        }
        
        if (isLocal) {
            admin.shrink(MASTER_LOG_FILENAME, 16, ConsoleShrinkListener.SINGLETON);
            
            try(Server server = Server.start(newMasterProperties())) {
                assertEquals(10, getCount(server));
                createTestData(server, 10);
                assertEquals(20, getCount(server));
            }
        } else {
            try(Server server = Server.start(newMasterProperties())) {
                admin.remoteShrink(newMasterAdminAddress(), ConsoleShrinkListener.SINGLETON);
                
                assertEquals(10, getCount(server));
                createTestData(server, 10);
                assertEquals(20, getCount(server));
            }
        }
    }
    
    /**
     * Steps:
     * - create master
     * - open
     * - add data 10
     * - close
     * - creates backup
     * 
     * - open master
     * - add data 10
     * - close
     * 
     * - restore and run slaves
     * - shrink master
     * - start master
     * - add data 10
     * 
     * - wait (for replication)
     * - check data on both slaves - 30
     */
    private void shrink_Master(boolean isLocal) throws IOException {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        admin.newStorage(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        
        try(Server master = Server.start(newMasterProperties())) {
            createTestData(master, 10);
        }
        
        backupAndRestoreCluster(admin);
        
        try(Server master = Server.start(newMasterProperties())) {
            createTestData(master, 10);
        }
        
        Server master = null;
        Server slave1 = null;
        Server slave2 = null;
        
        try {
            slave1 = Server.start(newSlaveProperties1());
            slave2 = Server.start(newSlaveProperties2());
            
            if (isLocal) {
                admin.shrinkMaster(MASTER_LOG_FILENAME, 16, getSlavesAddresses(), ConsoleShrinkListener.SINGLETON);
                master = Server.start(newMasterProperties());
            } else {
                master = Server.start(newMasterProperties());
                admin.remoteShrinkMaster(newMasterAdminAddress(), getSlavesAddresses(), ConsoleShrinkListener.SINGLETON);
            }
                        
            createTestData(master, 10);
            
            sleep(3000);    // Wait for replication
            
            assertEquals(30, getCount(slave1));
            assertEquals(30, getCount(slave2));
        } 
        finally {
            if (master != null) {
                master.close();
            }
            if (slave1 != null) {
                slave1.close();
            }
            if (slave2 != null) {
                slave2.close();
            }
        }
    }
}
