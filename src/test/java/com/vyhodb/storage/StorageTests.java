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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Properties;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.admin.Admin;
import com.vyhodb.server.Server;
import com.vyhodb.server.ServerClosedException;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Record;
import com.vyhodb.space.RecordCommons;

public class StorageTests {

    @Test
    public void test_Corruppted_Storage() throws IOException {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);

        Properties props = AbstractStorageTests.generateDefaultProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        
        // Writes some records into storage
        {
            Server storage = Server.start(props);
            TrxSpace space = storage.startModifyTrx();
            Record root = space.getRecord(0L);
            Record child;
            for (int i = 0; i < 15; i++) {
                child = space.newRecord();
                RecordCommons.setPrivitiveFields(child);
                child.setParent("Corrupted Test Parent Link", root);
            }
            space.commit();
            storage.close();
        }
        
        // Corrupts storage
        corrupt();
        
        // Opens storage and tries to read records
        {
            Server storage = Server.start(props);
            try {
                TrxSpace space = storage.startReadTrx();
                Record root = space.getRecord(0L);
                fail("Storage can operate on corrupted pages.");
            }
            catch(ServerClosedException tre) {
            }
            
            assertTrue(storage.isClosed());
        }
    }
    
    private void corrupt() throws IOException {
        RandomAccessFile file = new RandomAccessFile(AbstractStorageTests.DATA_FILENAME, "rw");
        try {
            file.seek(1750);
            file.write(new byte[]{67, 68, 69, 70, -70, -69, -68, -67});
            file.getFD().sync();
        }
        finally {
            file.close();
        }
    }
}
