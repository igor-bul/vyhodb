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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Properties;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.admin.Admin;
import com.vyhodb.rsi.Connection;
import com.vyhodb.rsi.ConnectionFactory;
import com.vyhodb.rsi.RsiServerException;
import com.vyhodb.server.Server;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.CriterionFactory;
import com.vyhodb.space.Record;
import com.vyhodb.space.Space;
import com.vyhodb.storage.rsi.RecoveryTestsService;

import static com.vyhodb.AllTests.*;

public class RecoveryTests {

    public static final String PROPERTY_FILE_PATH = getAbsoluteFilename("recovery.vyhodb.properties");
    
    /**
     * Tests successful recovery after checkpoint.
     * 
     * Steps:
     * 0. Creates storage.
     * 1. Starts stand-alone server.
     * 2. RSI. Creates two transaction. Modify buffer has the size (80) which trigger checkpoint before applying the second transaction.
     * 3. Kills stand-alone server.
     * 4. Opens storage locally and check data. Storage should recover itself and has added data.
     * 
     * Storage parameters:
     * Small modifyBufferSize allows check checkpoint in this test and recovery after checkpoint.
     * 
     * We need RSI service and appropriate method in it.
     * @throws Exception 
     * @throws ReflectiveOperationException 
     * 
     */
    @Test
    public void test_Recovery_After_Checkpoint_Successful() throws Exception {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        
        Properties props = AbstractStorageTests.generateDefaultProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        props.setProperty("rsi.enabled", "true");
        props.setProperty("storage.cacheSize", "1000");
        props.setProperty("storage.modifyBufferSize", "80");
        props.setProperty("storage.logBufferSize", "1000");
        props.setProperty("space.record.modifyCacheSize", "50");
        
        try (FileOutputStream out = new FileOutputStream(PROPERTY_FILE_PATH)) {
            props.store(out, "");
        }
        
        Process vyhodb = null;
        Server recoveredStorage = null;
        try {
            vyhodb = startVyhodb();
            Thread.sleep(3000);     // wait for starting server
            
            // Obtains connection
            Connection connection = ConnectionFactory.newConnection("tcp://localhost:47777/");
            RecoveryTestsService service = connection.getService(RecoveryTestsService.class);
            
            // Creates index
            service.createIndex();
            
            // Creates records
            service.addRecords(60);
            service.addRecords(60);
            
            // Kills server
            kill(vyhodb);
            
            // Starts storage and checks data
            props.setProperty("rsi.enabled", "false");
            recoveredStorage = Server.start(props);
            TrxSpace space = recoveredStorage.startReadTrx();
            check(space, 120);
            space.rollback();
        }
        finally {
            if (vyhodb != null) {
                kill(vyhodb);
            }
            
            if (recoveredStorage != null) {
                recoveredStorage.close();
            }
        }
    }
    
    /**
     * Test successful recovery after first transaction in storage.
     * 
     * Steps:
     * 0. Creates storage.
     * 1. Starts stand-alone server.
     * 2. RSI. Creates transaction which size is less than checkpoint.
     * 3. Kills stand-alone server.
     * 4. Opens storage locally and check data. Storage should recover itself and has added data.
     * 
     * Storage parameters:
     * Small modifyBufferSize allows check checkpoint in this test and recovery after checkpoint.
     * 
     * We need RSI service and appropriate method in it.
     * @throws Exception 
     * @throws ReflectiveOperationException 
     * 
     */
    @Test
    public void test_Recovery_After_Creation_Successful() throws Exception {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        
        Properties props = AbstractStorageTests.generateDefaultProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        props.setProperty("rsi.enabled", "true");
        props.setProperty("storage.cacheSize", "1000");
        props.setProperty("storage.modifyBufferSize", "100");    // Prevents from checkpoint
        props.setProperty("storage.logBufferSize", "1000");
        props.setProperty("space.record.modifyCacheSize", "50");
        
        try (FileOutputStream out = new FileOutputStream(PROPERTY_FILE_PATH)) {
            props.store(out, "");
        }
        
        Process vyhodb = null;
        Server recoveredStorage = null;
        try {
            vyhodb = startVyhodb();
            Thread.sleep(3000);     // wait for starting server
            
            // Obtains connection
            Connection connection = ConnectionFactory.newConnection("tcp://localhost:47777/");
            RecoveryTestsService service = connection.getService(RecoveryTestsService.class);
            
            // Creates index
            service.createIndex();
            
            // Creates records
            service.addRecords(60);
            
            // Kills server
            kill(vyhodb);
            
            // Starts storage and checks data
            props.setProperty("rsi.enabled", "false");
            recoveredStorage = Server.start(props);
            TrxSpace space = recoveredStorage.startReadTrx();
            check(space, 60);
            space.rollback();
        }
        finally {
            if (vyhodb != null) {
                kill(vyhodb);
            }
            
            if (recoveredStorage != null) {
                recoveredStorage.close();
            }
        }
        
    }
    
    
    /**
     * Large uncommitted transaction should be completely rolled back during recovery process.
     * 
     * Preconditions:
     * Small log buffer and large modify buffer (checkpoint window).
     * 
     * Steps:
     * 1. Starts stand-alone server.
     * 2. RSI. Creates list of test records.
     * 3. Starts thread which invokes RSI method.
     * 4. Waits for 2 seconds.
     * 5. Kills stand-alone server.
     * 6. Opens storage locally and checks that existed list of test records has previous records and hasn't been corrupted.
     * 
     * Thread:
     * 1. RSI. Creates large transaction and pause for 3 seconds.
     * @throws Exception 
     * @throws ReflectiveOperationException 
     *  
     */
    @Test
    public void test_Recovery_Failed_NotStop_Transaction() throws Exception {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        
        Properties props = AbstractStorageTests.generateDefaultProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        props.setProperty("rsi.enabled", "true");
        props.setProperty("storage.cacheSize", "1000");
        props.setProperty("storage.modifyBufferSize", "10000");    // Prevents from checkpoint
        props.setProperty("storage.logBufferSize", "50");
        props.setProperty("space.record.modifyCacheSize", "10");  // This parameter is essential for this test.
        
        try (FileOutputStream out = new FileOutputStream(PROPERTY_FILE_PATH)) {
            props.store(out, "");
        }
        
        Process vyhodb = null;
        Server recoveredStorage = null;
        try {
            vyhodb = startVyhodb();
            Thread.sleep(3000);     // wait for starting server

            // Obtains connection
            Connection connection = ConnectionFactory.newConnection("tcp://localhost:47777/");
            final RecoveryTestsService service = connection.getService(RecoveryTestsService.class);
            
            // Creates index
            service.createIndex();
            
            // Creates records
            service.addRecords(60);
            
            // Creates and starts long transaction
            Thread longTrxThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    service.addRecordsAndPause(500, 7000);
                }
            });
            longTrxThread.start();
            Thread.sleep(5000);
            
            // Kills process
            kill(vyhodb);
            
            // Starts storage and checks data
            props.setProperty("rsi.enabled", "false");
            recoveredStorage = Server.start(props);
            TrxSpace space = recoveredStorage.startReadTrx();
            check(space, 60);
            space.rollback();
        }
        finally {
            if (vyhodb != null) {
                kill(vyhodb);
            }
            
            if (recoveredStorage != null) {
                recoveredStorage.close();
            }
        }
    }
    
    
    /**
     * The same as test_Recovery_Successful, but before starting storage locally, it modifies last log page.
     * 
     * At the moment of killing vyhodb process, checkpoint shouldn't take place, so that some pages are still in log 
     * and are required to be recovered (use large modify buffer for this - storage.modifyBufferSize).
     * @throws Exception 
     * 
     * @throws ReflectiveOperationException 
     */
    @Test
    public void test_Recovery_Failed_CRC() throws Exception {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        
        Properties props = AbstractStorageTests.generateDefaultProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        props.setProperty("rsi.enabled", "true");
        props.setProperty("storage.cacheSize", "1000");
        props.setProperty("storage.modifyBufferSize", "1000");
        props.setProperty("storage.logBufferSize", "1000");
        props.setProperty("space.record.modifyCacheSize", "50");
        
        try (FileOutputStream out = new FileOutputStream(PROPERTY_FILE_PATH)) {
            props.store(out, "");
        }
        
        Process vyhodb = null;
        Server recoveredStorage = null;
        try {
            vyhodb = startVyhodb();
            Thread.sleep(3000);     // wait for starting server
            
            // Obtains connection
            Connection connection = ConnectionFactory.newConnection("tcp://localhost:47777/");
            RecoveryTestsService service = connection.getService(RecoveryTestsService.class);
            
            // Creates index
            service.createIndex();
            
            // Creates records
            service.addRecords(60);
            service.addRecords(60);
            
            // Kills server
            kill(vyhodb);
            
            // Modifies last page. Corrupts CRC
            try (FileChannel fc = FileChannel.open(Paths.get(AbstractStorageTests.LOG_FILENAME), StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.allocate(24);
                buffer.clear();
                buffer.putLong(6).putLong(6).putLong(6);
                buffer.clear();
                
                fc.position(fc.size() - 500);
                fc.write(buffer);
            }
            
            // Starts storage and checks data
            props.setProperty("rsi.enabled", "false");
            try {
                recoveredStorage = Server.start(props);
                fail("IOException is expected.");
            }
            catch(IOException ioe) {};
        }
        finally {
            if (vyhodb != null) {
                kill(vyhodb);
            }
            
            if (recoveredStorage != null) {
                recoveredStorage.close();
            }
        }
    }
    
    /**
     * The same as test_Recovery_Successful, but before starting storage locally, it truncates log by 500 bytes.
     * 
     * At the moment of killing vyhodb process, checkpoint shouldn't take place, so that some pages are still in log 
     * and are required to be recovered (use large modify buffer for this - storage.modifyBufferSize).
     * @throws Exception 
     */
    @Test
    public void test_Recovery_Failed_Trancated_Log() throws Exception {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        
        Properties props = AbstractStorageTests.generateDefaultProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        props.setProperty("rsi.enabled", "true");
        props.setProperty("storage.cacheSize", "1000");
        props.setProperty("storage.modifyBufferSize", "1000");
        props.setProperty("storage.logBufferSize", "1000");
        props.setProperty("space.record.modifyCacheSize", "50");
        
        try (FileOutputStream out = new FileOutputStream(PROPERTY_FILE_PATH)) {
            props.store(out, "");
        }
        
        Process vyhodb = null;
        Server recoveredStorage = null;
        try {
            vyhodb = startVyhodb();
            Thread.sleep(3000);     // wait for starting server
            
            // Obtains connection
            Connection connection = ConnectionFactory.newConnection("tcp://localhost:47777/");
            RecoveryTestsService service = connection.getService(RecoveryTestsService.class);
            
            // Creates index
            service.createIndex();
            
            // Creates records
            service.addRecords(60);
            service.addRecords(60);
            
            // Kills server
            kill(vyhodb);
            
            // Truncate log
            try (FileChannel fc = FileChannel.open(Paths.get(AbstractStorageTests.LOG_FILENAME), StandardOpenOption.WRITE)) {
                fc.truncate(fc.size() - 500);
            }
            
            // Starts storage and checks data
            props.setProperty("rsi.enabled", "false");
            recoveredStorage = Server.start(props);
            TrxSpace space = recoveredStorage.startReadTrx();
            check(space, 60);
            space.rollback();
        }
        finally {
            if (vyhodb != null) {
                kill(vyhodb);
            }
            
            if (recoveredStorage != null) {
                recoveredStorage.close();
            }
        }
    }
    
    /**
     * Tests situation when:
     * 1) Start large transaction (size more then log buffer). Many pages are written to Log file
     * 2) Rollback this transaction (pages are still in log, log file hasn't been truncated).
     * 3) Start small modify transaction and commit it.
     * 4) Destroy.
     * 5) Recovery should successfully completed!!!
     * 
     * 6) Start new modify trx and read its result (as option).
     * @throws Exception 
     * 
     */
    @Test
    public void test_Recovery_Failed_AfterRollback() throws Exception {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        
        Properties props = AbstractStorageTests.generateDefaultProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        props.setProperty("rsi.enabled", "true");
        props.setProperty("storage.cacheSize", "1000");
        props.setProperty("storage.modifyBufferSize", "1000");
        props.setProperty("storage.logBufferSize", "32");
        props.setProperty("space.record.modifyCacheSize", "10");
        
        try (FileOutputStream out = new FileOutputStream(PROPERTY_FILE_PATH)) {
            props.store(out, "");
        }
        
        Process vyhodb = null;
        try {
            vyhodb = startVyhodb();
            sleep(3000);     // wait for starting server
            
            // Obtains connection
            Connection connection = ConnectionFactory.newConnection("tcp://localhost:47777/");
            RecoveryTestsService service = connection.getService(RecoveryTestsService.class);
            
            try {
                // Creates index
                service.createIndex();
                // Creates records and roll back trx
                service.addRecordsAndRollback(60000);
            } catch(RsiServerException ex) {
            }
            
            service.addRecords(10);
            
            // Kills server
            kill(vyhodb);
            
            // Starts storage, recover it and do subsequent modify and read
            try(Server recovered = Server.start(props)) {
                TrxSpace space = recovered.startReadTrx();
                check(space, 10);
                space.rollback();
                
                // Adds new data
                connection = ConnectionFactory.newConnection("tcp://localhost:47777/");
                service = connection.getService(RecoveryTestsService.class);
                service.addRecords(60000);
                
                space = recovered.startReadTrx();
                check(space, 60010);
                space.rollback();
            }
        }
        finally {
            if (vyhodb != null) {
                kill(vyhodb);
            }
        }
    }
    
    /**
     * TODO implement it in future.
     * 
     * Does a cycle of the following actions:
     * 1. Starts stand-alone server.
     * 2. Invokes RSI which creates [root test record] and index.
     * 3. Starts Thread.
     * 4. Waits for a timeout.
     * 5. Kills server.
     * 6. Starts storage locally (RSI turned off).
     * 7. Checks data (iterates over children and index).
     * 8. Closes storage.
     * 
     * Thread:
     * 1. Invokes RSI Modify service in infinite cycle. Service adds one new child into [root test record].
     * 
     * Configured parameters:
     * 1. Main thread timeout for kill
     * 2. Iteration count
     * 
     * Storage configuration:
     * 1. Modify Buffer = 500
     * 2. Log Buffer = 80
     * 
     */
//    @Test
//    public void test_Recover_RandomTrx() {
//        throw new UnsupportedOperationException();
//    }
    
    private Process startVyhodb() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(
                "java", 
                "-server", 
                //"-Xbootclasspath:" + System.getProperty("sun.boot.class.path"),
                "-classpath",
                System.getProperty("java.class.path"),
                "-XX:CompileThreshold=1",
                "-XX:MaxDirectMemorySize=2g",
                "com.vyhodb.server.Standalone",
                "-config=" + PROPERTY_FILE_PATH);
        
        builder.inheritIO();
        Process process = builder.start();
        return process;
    }
    
    private void check(Space space, int expectedCount) {
        Record root = space.getRecord(0L);
        Record child;
        
        // Iterates over children
        Iterator<Record> iterator = root.getChildren(RecoveryTestsService.LINK_NAME).iterator();
        for (int i = 0; i < expectedCount; i++) {
            assertTrue(iterator.hasNext());
            child = iterator.next();
            assertNotNull(child);
        }
        assertFalse(iterator.hasNext());
        
        // Iterates over index
        Iterator<Record> indexIter = root.searchChildren(RecoveryTestsService.INDEX_NAME, CriterionFactory.all()).iterator();
        for (int i = 0; i < expectedCount; i++) {
            assertTrue(indexIter.hasNext());
            child = indexIter.next();
            assertNotNull(child);
        }
        assertFalse(indexIter.hasNext());
    }
    
    private void kill(Process process) throws Exception {
//        ProcessHandle handle = process.toHandle();
//        handle.destroyForcibly();
//        handle.onExit().get();

        if (isWindows()) {
            process.destroy();
            process.waitFor();
        } else {
            long pid = getUnixPID(process);
            String killStr = "kill -9 " + pid;
            Runtime.getRuntime().exec(killStr).waitFor();
            process.waitFor();
        }
    }
    
    private static long getUnixPID(Process process) throws Exception {
        // In JDK 1.7 there was access to the private field pid by reflection
        return process.pid();
    }
    
    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Window");
    }
}
