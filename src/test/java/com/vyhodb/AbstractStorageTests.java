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
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.vyhodb.admin.Admin;
import com.vyhodb.server.Server;

public class AbstractStorageTests {

    public static final String LOG_FILENAME = AllTests.getAbsoluteFilename("vyho_log");
    public static final String DATA_FILENAME = AllTests.getAbsoluteFilename("vyho_data");
    protected static Server _storage;
    
    @BeforeClass
    public static void createStorage() throws IOException {
        Admin admin = Admin.getInstance();
        Properties props = generateDefaultProperties(LOG_FILENAME, DATA_FILENAME);
        
        admin.removeStorageFiles(LOG_FILENAME, DATA_FILENAME);
        admin.newStorage(LOG_FILENAME, DATA_FILENAME);
        _storage = Server.start(props);
    }
    
    public static Properties generateDefaultProperties(String logFile, String dataFile) {
        Properties props = new Properties();
        props.setProperty("storage.log", logFile);
        props.setProperty("storage.data", dataFile);
//        props.setProperty("space.record.modifyCacheSize", "5000");
//        props.setProperty("storage.cacheSize", "200000");
//        props.setProperty("storage.modifyBufferSize", "150000");
//        props.setProperty("storage.logBufferSize", "100000");
        
        return props;
    }
    
    @AfterClass
    public static void closeStorage() throws IOException {
        if (_storage != null)
        {
            _storage.close();
        }
    }
}
