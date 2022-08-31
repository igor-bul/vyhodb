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

package com.vyhodb.rsi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Test;

import static org.junit.Assert.*;

import com.vyhodb.admin.Admin;
import com.vyhodb.rsi.Connection;
import com.vyhodb.rsi.ConnectionFactory;
import com.vyhodb.rsi.RsiClientException;
import com.vyhodb.rsi.RsiServerException;
import com.vyhodb.server.Server;

import static com.vyhodb.AllTests.*;

public class BalancerTests {

    public static final String BALANCER_PROPERTIES_FILENAME = getAbsoluteFilename("balancer.properties");
    public static final String BALANCER_URI = "balancer:" + getURI("balancer.properties");
    
    private static Properties modifyStorageProps = generateMasterProps();
    private static Properties readStorageProps1 = generateSlaveProps1();
    private static Properties readStorageProps2 = generateSlaveProps2();
    
    private static Properties generateMasterProps() {
        Properties props = newMasterProperties();
        addServerBalancer(props);
        return props;
    }
    
    private static Properties generateSlaveProps1() {
        Properties props = newSlaveProperties1();
        
        addServerBalancer(props);
        props.setProperty("slave.mode", "realtime");
        props.setProperty("slave.checkTimeout", "1000");
        
        return props;
    }
    
    private static Properties generateSlaveProps2() {
        Properties props = newSlaveProperties2();
        
        addServerBalancer(props);
        props.setProperty("slave.mode", "realtime");
        props.setProperty("slave.checkTimeout", "1000");
        
        return props;
    }
    
    private static void addServerBalancer(Properties props) {
        props.setProperty("rsi.cluster.enabled", "true");
        props.setProperty("rsi.cluster.probe.attempts", "40");
        props.setProperty("rsi.cluster.probe.timeout", "100");
    }
    
    private static void createCluster() throws IOException {
        Admin admin = Admin.getInstance();
        
        Files.deleteIfExists(Paths.get(BACKUP_FILENAME));
        
        admin.removeStorageFiles(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        admin.newStorage(MASTER_LOG_FILENAME, MASTER_DATA_FILENAME);
        
        backupAndRestoreCluster(admin);
    }
    
    private static void saveBalancerProperties(Properties properties, String filename) throws FileNotFoundException, IOException {
        try(FileOutputStream out = new FileOutputStream(filename)) {
            properties.store(out, "");
        }
    }
    
    private Properties getBalancerProperties() {
        Properties modify = newMasterProperties();
        Properties slave1 = newSlaveProperties1();
        Properties slave2 = newSlaveProperties2();
        
        Properties properties = new Properties();
        
        properties.setProperty("refreshConfigTimeout", "2000");
        properties.setProperty("debug", "true");
        properties.setProperty("read.poolCount", "2");
        
        properties.setProperty("error.attempts", "8");
        properties.setProperty("error.timeout", "500");
                
        properties.setProperty("modify.host", modify.getProperty("rsi.host"));
        properties.setProperty("modify.port", modify.getProperty("rsi.port"));
        
        properties.setProperty("read.0.host", slave1.getProperty("rsi.host"));
        properties.setProperty("read.0.port", slave1.getProperty("rsi.port"));
        
        properties.setProperty("read.1.host",  slave2.getProperty("rsi.host"));
        properties.setProperty("read.1.port", slave2.getProperty("rsi.port"));
        properties.setProperty("read.1.poolSize", "1");
        properties.setProperty("read.1.poolTTL", "10000");
        
        return properties;
    }
    
    private Properties getReadOnlyBalancerProperties() {
        Properties slave0 = newMasterProperties();
        Properties slave1 = newSlaveProperties1();
        Properties slave2 = newSlaveProperties2();
        
        Properties properties = new Properties();
        
        properties.setProperty("refreshConfigTimeout", "2000");
        properties.setProperty("debug", "true");
        properties.setProperty("read.poolCount", "3");
        
        properties.setProperty("error.attempts", "8");
        properties.setProperty("error.timeout", "500");
                
        properties.setProperty("read.0.host", slave1.getProperty("rsi.host"));
        properties.setProperty("read.0.port", slave1.getProperty("rsi.port"));
        
        properties.setProperty("read.1.host", slave2.getProperty("rsi.host"));
        properties.setProperty("read.1.port", slave2.getProperty("rsi.port"));
        properties.setProperty("read.1.poolSize", "1");
        properties.setProperty("read.1.poolTTL", "10000");
        
        // Modify vdb server
        properties.setProperty("read.2.host", slave0.getProperty("rsi.host")); 
        properties.setProperty("read.2.port", slave0.getProperty("rsi.port"));
        
        return properties;
    }
    
   
    /**
     * Main goal - test basic balancer functionality and waiting needed [Next] parameter on server side.
     * 
     * Steps:
     * 1. Starts cluster (one modify, two reads).
     * 2. Cycle 6 times: one modify, one read.
     * 3. Closes connection.
     * 4. Closes storages.
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws URISyntaxException 
     * @throws RsiClientException 
     * @throws ReflectiveOperationException 
     * 
     */
    @Test
    public void test_Next_Wait() throws FileNotFoundException, IOException, RsiClientException, URISyntaxException, ReflectiveOperationException {
        saveBalancerProperties(getBalancerProperties(), BALANCER_PROPERTIES_FILENAME);
        
        createCluster();
        
        Server master = null;
        Server slave1 = null;
        Server slave2 = null;
        
        try {
            master = Server.start(modifyStorageProps);
            slave1 = Server.start(readStorageProps1);
            slave2 = Server.start(readStorageProps2);
            
            Connection connection = ConnectionFactory.newConnection(BALANCER_URI);
            BalancerService service = connection.getService(BalancerService.class);
            
            for (int i = 1; i <= 6; i++) {
                service.addRecord(Integer.toString(i));
                assertEquals(i, service.getCount());
            }
            
            connection.close();
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
    
    /**
     * Main goal test pauses on client side after failing establish new connections.
     * 
     * Main thread:
     * 1. Start service thread.
     * 2. Cycle 6 times: one modify, one read.
     * 3. Closes connection.
     * 4. Closes storages.
     * 
     * Service thread:
     * 1. Waits 2 second
     * 2. Starts Modify server
     * 3. Waits 2 seconds
     * 4. Starts Read nodes.
     * 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws URISyntaxException 
     * @throws RsiClientException 
     * @throws ReflectiveOperationException 
     * 
     */
    @Test
    public void test_Recover_NewConnection() throws FileNotFoundException, IOException, RsiClientException, URISyntaxException, ReflectiveOperationException {
        saveBalancerProperties(getBalancerProperties(), BALANCER_PROPERTIES_FILENAME);
        
        createCluster();
        
        Server[] modifyStorage = new Server[1];
        Server[] readStorages = new Server[2];
        Properties[] readStorageProperties = new Properties[] {readStorageProps1, readStorageProps2};
        
        try {
            Thread threadStartModify = new Thread(new StartStorages(modifyStorage, new Properties[] {modifyStorageProps}, 3000));
            Thread threadStartRead = new Thread(new StartStorages(readStorages, readStorageProperties, 6000));
            
            threadStartModify.start();
            threadStartRead.start();
            
            Connection connection = ConnectionFactory.newConnection(BALANCER_URI);
            BalancerService service = connection.getService(BalancerService.class);
            
            for (int i = 1; i <= 6; i++) {
                service.addRecord(Integer.toString(i));
                assertEquals(i, service.getCount());
            }
            
            connection.close();
        }
        finally {
            if (modifyStorage != null) {
                modifyStorage[0].close();
            }
            
            if (readStorages != null) {
                readStorages[0].close();
                readStorages[1].close();
            }
        }
    }
    
    /**
     * New RSI Service must be written. Which is waiting for specified period of time.
     * 
     * Main thread:
     * 1. Starts cluster.
     * 2. Invokes waitRead method. Timeout: 0
     * 3. Starts service thread.
     * 4. Invokes waitRead method. Timeout: 4 sec.
     * 5. Closes connection.
     * 6. Closes storages.
     * 
     * Service thread:
     * 1. Waits for 2 second.
     * 2. Stops all read storages.
     * 3. Waits for 2 second.
     * 4. Starts all read storages.
     * 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws URISyntaxException 
     * @throws RsiClientException 
     * @throws ReflectiveOperationException 
     * 
     */
    @Test
    public void test_Recover_Read_OnExecution() throws FileNotFoundException, IOException, RsiClientException, URISyntaxException, ReflectiveOperationException {
        saveBalancerProperties(getBalancerProperties(), BALANCER_PROPERTIES_FILENAME);
        
        createCluster();
        
        Server modify = null;
        
        Server[] readStorages = new Server[2];
        Properties[] readStorageProperties = new Properties[] {readStorageProps1, readStorageProps2};
        
                
        try {
            modify = Server.start(modifyStorageProps);
            readStorages[0] = Server.start(readStorageProps1);
            readStorages[1] = Server.start(readStorageProps2);
            
            Thread threadStopRead = new Thread(new StopStorages(readStorages, 2000));
            Thread threadStartRead = new Thread(new StartStorages(readStorages, readStorageProperties, 4000));
            
            threadStopRead.start();
            threadStartRead.start();
            
            Connection connection = ConnectionFactory.newConnection(BALANCER_URI);
            BalancerService service = connection.getService(BalancerService.class);
            
            service.waitRead(0);
            service.waitRead(4000);
            
            connection.close();
        }
        finally {
            if (modify != null) {
                modify.close();
            }
            
            if (readStorages != null) {
                readStorages[0].close();
                readStorages[1].close();
            }
        }
    }
    
    /**
     * New RSI Service must be written. Which is waiting for specified period of time.
     * 
     * Main thread:
     * 1. Starts modify server.
     * 2. Invokes Modify method. Timeout: 0
     * 3. Starts service thread.
     * 4. Invokes Modify method. Timeout: 4 sec.
     * 5. Catch RsiClientException
     * 5. Closes connection.
     * 6. Closes storage.
     * 
     * Service thread:
     * 1. Waits for 2 second.
     * 2. Stops storage.
     * 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws URISyntaxException 
     * @throws RsiClientException 
     * @throws ReflectiveOperationException 
     * 
     */
    @Test
    public void test_Fail_Modify_OnExecution() throws FileNotFoundException, IOException, RsiClientException, URISyntaxException, ReflectiveOperationException {
        saveBalancerProperties(getBalancerProperties(), BALANCER_PROPERTIES_FILENAME);
        
        createCluster();
        
        Server modify = null;
                
        try {
            modify = Server.start(modifyStorageProps);
            
            Thread threadStopModify = new Thread(new StopStorages(new Server[] {modify}, 2000));
            threadStopModify.start();

            Connection connection = ConnectionFactory.newConnection(BALANCER_URI);
            BalancerService service = connection.getService(BalancerService.class);
            
            service.waitModify(0);
            
            try {
                service.waitModify(4000);
                fail("RSIClientException is expected.");
            }
            catch(RsiClientException rex) {}
            
            connection.close();
        }
        finally {
            if (modify != null) {
                modify.close();
            }
        }
    }
    
    /**
     * New RSI Service must be written. Which is waiting for specified period of time.
     * 
     * Main thread:
     * 1. Starts two read servers.
     * 2. Invokes Read method. Timeout: 0
     * 3. Starts service thread.
     * 4. Invokes Read method. Timeout: 5 sec.
     * 5. Catch RsiClientException
     * 5. Closes connection.
     * 6. Closes storage.
     * 
     * Service thread:
     * 1. Waits for 2 second.
     * 2. Stops read nodes storage.
     * 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws URISyntaxException 
     * @throws RsiClientException 
     * @throws ReflectiveOperationException 
     * 
     */
    @Test
    public void test_Fail_Read_OnExecution() throws FileNotFoundException, IOException, RsiClientException, URISyntaxException, ReflectiveOperationException {
        saveBalancerProperties(getBalancerProperties(), BALANCER_PROPERTIES_FILENAME);
        
        createCluster();
        
        Server modify = null;
        Server[] readStorages = new Server[2];
                       
        try {
            modify = Server.start(modifyStorageProps);
            readStorages[0] = Server.start(readStorageProps1);
            readStorages[1] = Server.start(readStorageProps2);
            
            Thread threadStopRead = new Thread(new StopStorages(readStorages, 2000));
            threadStopRead.start();

            Connection connection = ConnectionFactory.newConnection(BALANCER_URI);
            BalancerService service = connection.getService(BalancerService.class);
            
            service.waitRead(0);
            
            try {
                service.waitRead(5000);
                fail("RSIClientException is expected.");
            }
            catch(RsiClientException rex) {}
            
            connection.close();
        }
        finally {
            if (modify != null) {
                modify.close();
            }
        }
    }
    
    /**
     * Tests reloading new config, closing old pools and creating new ones.
     * 
     * 1. Starts cluster.
     * 2. Invokes addRecord() method (@Modify).
     * 3. Invokes getCount() method (@Read).
     * 4. Stops Modify node.
     * 5. Starts Modify with new RSI port.
     * 6. Changes balancer properties (to new Modify RSI port).
     * 7. Waits 4 seconds for balancer properties refresh.
     * 8. Invokes addRecord method (@Modify).
     * 9. Invokes getCount() method (@Read) and compares it result (== 2).
     * 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws URISyntaxException 
     * @throws RsiClientException 
     * @throws ReflectiveOperationException 
     * 
     */
    @Test
    public void test_Reload_Balancer_Config() throws FileNotFoundException, IOException, RsiClientException, URISyntaxException, ReflectiveOperationException {
        saveBalancerProperties(getBalancerProperties(), BALANCER_PROPERTIES_FILENAME);
        
        createCluster();
        
        Server master = null;
        Server slave1 = null;
        Server slave2 = null;
        
        try {
            master = Server.start(modifyStorageProps);
            slave1 = Server.start(readStorageProps1);
            slave2 = Server.start(readStorageProps2);
            
            Connection connection = ConnectionFactory.newConnection(BALANCER_URI);
            BalancerService service = connection.getService(BalancerService.class);
            
            // Service invocations
            {
                service.addRecord("1");
                assertEquals(1, service.getCount());
            }
            
            // Restarts Modify node with new RSI port
            {
                Properties masterProps = (Properties) modifyStorageProps.clone();
                masterProps.setProperty("rsi.port", "50010");
                
                master.close();
                master = Server.start(masterProps);
            }
            
            // Saves new balancer properties
            {
                Properties balancerProps = getBalancerProperties();
                balancerProps.setProperty("modify.port", "50010");
                saveBalancerProperties(balancerProps, BALANCER_PROPERTIES_FILENAME);
            }
            
            // Service invocation
            {
                sleep(4000);
                service.addRecord("1");
                assertEquals(2, service.getCount());
            }
                        
            connection.close();
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

    /**
     * Test failure during waiting for required [next storage] on read node.
     * 
     * 1. Starts Modify
     * 2. Starts Reads, but without replication
     * 3. Invokes addRecord()
     * 4. Invokes getCount(). Handle RSIServerException.
     * 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws URISyntaxException 
     * @throws RsiClientException 
     * @throws ReflectiveOperationException 
     * 
     */
    @Test
    public void test_Fail_Next_Wait() throws FileNotFoundException, IOException, RsiClientException, URISyntaxException, ReflectiveOperationException {
        saveBalancerProperties(getBalancerProperties(), BALANCER_PROPERTIES_FILENAME);
        
        createCluster();
                
        Server master = null;
        Server slave1 = null;
        Server slave2 = null;
        
        try {
            Properties read1 = (Properties) readStorageProps1.clone();
            read1.setProperty("slave.enabled", "false");
            
            Properties read2 = (Properties) readStorageProps2.clone();
            read2.setProperty("slave.enabled", "false");
            
            master = Server.start(modifyStorageProps);
            slave1 = Server.start(read1);
            slave2 = Server.start(read2);
            
            Connection connection = ConnectionFactory.newConnection(BALANCER_URI);
            BalancerService service = connection.getService(BalancerService.class);
            
            service.addRecord("1");
            
            try {
                service.getCount();
                fail("RSIServerException expected.");
            } catch(RsiServerException rex) {}
            
            connection.close();
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
    
    /**
     * Tests attempts on establishing new connection and throwing exception as a result of failure.
     * Cluster nodes are neither created nor started. 
     * 
     * @throws FileNotFoundException
     * @throws IOException
     * @throws RsiClientException
     * @throws URISyntaxException
     */
    @Test
    public void test_Fail_NewConnection() throws FileNotFoundException, IOException, RsiClientException, URISyntaxException {
        saveBalancerProperties(getBalancerProperties(), BALANCER_PROPERTIES_FILENAME);

        Connection connection = ConnectionFactory.newConnection(BALANCER_URI);
        BalancerService service = connection.getService(BalancerService.class);
        
        try {
            service.addRecord("1");
            fail("RSIClientException is expected.");
        }
        catch(RsiClientException cex) {}
        
        try {
            service.getCount();
            fail("RSIClientException is expected.");
        }
        catch(RsiClientException cex) {}
        
        connection.close();
    }
    
    /**
     * Tests configuration for read only nodes. Modify node is considered as Read.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     * @throws RsiClientException
     * @throws URISyntaxException
     * @throws ReflectiveOperationException
     */
    @Test
    public void test_ReadOnlyBalancer() throws FileNotFoundException, IOException, RsiClientException, URISyntaxException, ReflectiveOperationException {
        saveBalancerProperties(getReadOnlyBalancerProperties(), BALANCER_PROPERTIES_FILENAME);
        
        createCluster();
        
        Server master = null;
        Server slave1 = null;
        Server slave2 = null;
        
        try {
            master = Server.start(modifyStorageProps);
            slave1 = Server.start(readStorageProps1);
            slave2 = Server.start(readStorageProps2);
            
            Connection connection = ConnectionFactory.newConnection(BALANCER_URI);
            BalancerService service = connection.getService(BalancerService.class);
            
            for (int i = 1; i <= 12; i++) {
                assertEquals(0, service.getCount());
            }
            
            // Read-only balancer prevents @Modify transactions
            try {
                service.addRecord("2");
            } catch (RsiClientException rce) {
                assertEquals("com.vyhodb.rsi.RsiClientException: No modify pool is configured for balancer connection.", rce.getMessage());
            }
            
            connection.close();
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
    
    private class StartStorages implements Runnable {
        
        private final Server[] _storages;
        private final Properties[] _props;
        private final long _timeout;
        
        StartStorages(Server[] storages, Properties[] props, long timeout) {
            _storages = storages;
            _props = props;
            _timeout = timeout;
        }

        @Override
        public void run() {
            // Waits
            sleep(_timeout);
            
            // Starts
            try {
                for (int i = 0; i < _props.length; i++) {
                    _storages[i] = Server.start(_props[i]);
                }
            }
            catch(Throwable th) {
                th.printStackTrace();
            }
        }
    }
    
    private class StopStorages implements Runnable {
        
        private final Server[] _storages;
        private final long _timeout;
        
        StopStorages(Server[] storages, long timeout) {
            _storages = storages;
            _timeout = timeout;
        }

        @Override
        public void run() {
            // Waits
            sleep(_timeout);
            
            // Starts
            try {
                for (int i = 0; i < _storages.length; i++) {
                    _storages[i].close();
                }
            }
            catch(Throwable th) {
                th.printStackTrace();
            }
        }
    }
}
