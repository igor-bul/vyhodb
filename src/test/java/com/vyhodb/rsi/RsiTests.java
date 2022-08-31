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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.admin.Admin;
import com.vyhodb.rsi.Connection;
import com.vyhodb.rsi.ConnectionFactory;
import com.vyhodb.rsi.RsiClientException;
import com.vyhodb.rsi.RsiException;
import com.vyhodb.rsi.RsiServerException;
import com.vyhodb.server.Server;

import static com.vyhodb.AllTests.*;

public class RsiTests {

    public static final String LOCAL_RSI_PROP_FILENAME =  getAbsoluteFilename("local_rsi.properties");
    public static final String URL_LOCAL_RSI_PROP_FILENAME = "local:" + getURI("local_rsi.properties");
    
    @Test
    public void test_Rsi_Server_Single() throws IOException, RsiClientException, URISyntaxException {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);

        Properties props = generateRsiProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        test_Rsi_Server(props, "tcp://localhost:50000/", 0);
    }
    
    @Test
    public void test_Rsi_Local() throws IOException, RsiClientException, URISyntaxException {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        
        // Save properties
        {
            Properties props = AbstractStorageTests.generateDefaultProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
            try(FileOutputStream out = new FileOutputStream(LOCAL_RSI_PROP_FILENAME)) {
                props.store(out, "");
            }
        }
        
        Map<String, Object> fields = generateFields();
        Connection connection = null;
        try {
            connection = ConnectionFactory.newConnection(URL_LOCAL_RSI_PROP_FILENAME);
            UnitTestService service = connection.getService(UnitTestService.class);
    
            // Tests read and modify trx
            {
                for (int i = 0; i < 5; i++) {
                    service.addRecord(fields);
                }
                assertEquals("Record count is different.", 5, service.count());
            }
            
            // Tests wrong transaction type annotation
            {
                try {
                    service.modify();
                    fail("Modify transaction completed under @Read method.");
                }
                catch(RsiServerException ex) {}
                assertEquals("Record count is different.", 5, service.count());
            }
            
            // Tests exception in modify trx
            {
                try {
                    service.throwException();
                    fail("IllegalStateException is expected.");
                }
                catch(RsiServerException rse) {
                }
                assertEquals("Record count is different.", 5, service.count());
            }
            
            // Tests closing storage
            {
                connection.close();
                                
                try {
                    service.count();
                    fail("Can call rsi service on stopped storage");
                }
                catch(RsiException ex) {
                }
            }
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
    
    @Test
    public void test_Rsi_Server_Pool() throws IOException, RsiClientException, URISyntaxException {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);

        Properties props = generateRsiProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        test_Rsi_Server(props, "tcp://localhost:50000/?pool=true&poolSize=10&poolTTL=360000", 0);
    }
    
    @Test
    public void test_Rsi_Server_Pool_TTL() throws IOException, RsiClientException, URISyntaxException {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);

        Properties props = generateRsiProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        test_Rsi_Server(props, "tcp://localhost:50000/?pool=true&poolSize=10&poolTTL=500&poolDebug=true", 1000);
    }
    
    @Test
    public void test_Rsi_WrongVersion() throws IOException, RsiClientException, URISyntaxException {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        Properties props = generateRsiProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        
        Server storage = Server.start(props);
        try
        {
            Connection connection = ConnectionFactory.newConnection("tcp://localhost:50000/?pool=true&poolSize=10&poolTTL=360000");

            UnitTestService service= connection.getService(UnitTestService.class);
            UnitTestServiceWrongVersion serviceWrongVersion = connection.getService(UnitTestServiceWrongVersion.class);

            // Tests wrong version exception
            try {
                serviceWrongVersion.addRecord(generateFields());
                fail("Service method was invoked through interface with Wrong version.");
            }
            catch(RsiServerException ex) {}

            // This line must be successfully executed
            service.addRecord(generateFields());
            
            // Close connection
            connection.close();
        }
        finally {
            storage.close();
        }
    }
    
    private static Properties generateRsiProperties(String logfilename, String datafilename) {
        Properties props = AbstractStorageTests.generateDefaultProperties(logfilename, datafilename);
        props.setProperty("rsi.enabled", "true");
        props.setProperty("rsi.port", "50000");
        return props;
    }
    
    private static Map<String, Object> generateFields() {
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("Name", "Test Name");
        fields.put("Description", "fwrferferferf frferfer ferferferf erfervvmgmbpjyhotyhj trg0rj ghrjhiryjihgtr gtrjg");
        fields.put("Long", Long.MIN_VALUE);
        return fields;
    }
   
    private static void test_Rsi_Server(Properties properties, String serverURL, long timeout) throws IOException, RsiClientException, URISyntaxException {
        Map<String, Object> fields = generateFields();

        Server storage = Server.start(properties);
        try
        {
            Connection connection = ConnectionFactory.newConnection(serverURL);
            UnitTestService service = connection.getService(UnitTestService.class);
    
            // Tests read and modify trx
            {
                for (int i = 0; i < 5; i++) {
                    service.addRecord(fields);
                    sleep(timeout);
                }
                assertEquals("Record count is different.", 5, service.count());
            }
            
            // Tests wrong transaction type annotation
            {
                try {
                    service.modify();
                    fail("Modify transaction completed under @Read method.");
                }
                catch(RsiServerException ex) {}
                assertEquals("Record count is different.", 5, service.count());
            }
            
            // Tests closing rsi connection. For increase coverage purpose
            {
                connection.close();
                connection = ConnectionFactory.newConnection(serverURL);
                service = connection.getService(UnitTestService.class);
            }
                    
            // Tests exception in modify trx
            {
                try {
                    service.throwException();
                    fail("IllegalStateException is expected.");
                }
                catch(RsiServerException rse) {
                }
                assertEquals("Record count is different.", 5, service.count());
            }
            
            // Tests closing storage
            {
                storage.close();
                assertTrue("Storage is not closed", storage.isClosed());
                
                try {
                    service.count();
                    fail("Can call rsi service on stopped storage");
                }
                catch(RsiClientException ex) {
                }
            }
        }
        finally {
            storage.close();
        }
    }
}
