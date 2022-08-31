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

package com.vyhodb.space;

import static com.vyhodb.space.RecordCommons.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.server.NotActiveTransactionException;
import com.vyhodb.server.TransactionRolledbackException;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Record;

public class RecordTests extends AbstractStorageTests {

    public RecordTests() {
    }
    
    @Test
    public void testTrxTypeCompletion()
    {
        // Modify trx with rollback
        TrxSpace space = _storage.startModifyTrx();
        long id = space.newRecord().getId();
        space.rollback();
        
        // Empty modify trx with commit
        space = _storage.startModifyTrx();
        Record record = space.getRecord(id);
        assertNull(record);
        space.commit();
        
        // Read trx with commit
        space = _storage.startReadTrx();
        record = space.getRecord(id);
        assertNull(record);
        space.commit();
        
        // Read trx with rollback
        space = _storage.startModifyTrx();
        record = space.getRecord(id);
        assertNull(record);
        space.rollback();
    }
    
    @Test(expected=TransactionRolledbackException.class)
    public void testRemoveRoot()
    {
        TrxSpace space = _storage.startModifyTrx();
        Record root = space.getRecord(0);
        root.delete();
    }
    
    @Test
    public void testNewRecord()
    {
        TrxSpace space = _storage.startModifyTrx();
        long id = doCreateRecord(space);
        space.commit();
        
        space = _storage.startModifyTrx();
        checkCreateRecord(space, id);
        space.rollback();
        
        space = _storage.startReadTrx();
        checkCreateRecord(space, id);
        space.commit();
    }

    @Test
    public void testPrimitiveFields()
    {
        TrxSpace space = _storage.startModifyTrx();
        long id = doPrimitiveFields(space);
        space.commit();
        
        space = _storage.startModifyTrx();
        checkPrimitiveFields(space, id);
        space.rollback();
        
        space = _storage.startReadTrx();
        checkPrimitiveFields(space, id);
        space.commit();
    }
    
    @Test
    public void testPrimitiveArrays()
    {
        TrxSpace space = _storage.startModifyTrx();
        long id = doPrimitiveArrays(space);
        space.commit();
        
        space = _storage.startModifyTrx();
        checkPrimitiveArrays(space, id);
        space.rollback();
        
        space = _storage.startReadTrx();
        checkPrimitiveArrays(space, id);
        space.commit();
    }
    
    @Test
    public void testObjectArrays()
    {
        TrxSpace space = _storage.startModifyTrx();
        long id = doObjectArrays(space);
        space.commit();
        
        space = _storage.startModifyTrx();
        checkObjectArrays(space, id);
        space.rollback();
        
        space = _storage.startReadTrx();
        checkObjectArrays(space, id);
        space.commit();
    }
    
    @Test
    public void testWrongFieldClass()
    {
        TrxSpace space = null;
        
        try
        {
            space = _storage.startModifyTrx();
            doWrongFieldClass(space);
        }
        catch(TransactionRolledbackException tre)
        {
            space = _storage.startModifyTrx();
            checkWrongFieldClass(space);
            space.commit();
            
            space = _storage.startReadTrx();
            checkWrongFieldClass(space);
            space.commit();
                        
            return;
        }
        
        fail("Wrong field value has been set");
    }
    
    @Test
    public void testDelete()
    {
        long id;
        TrxSpace space = null;
        Record record = null;
        
        // Create and remove record
        {
            space = _storage.startModifyTrx();
            record = space.newRecord();
            id = record.getId();
            record.delete();
            checkRecordDeleted(space, id);
            space.commit();
        }
        
        // Create record in one and remove in another
        {
            space = _storage.startModifyTrx();
            record = space.newRecord();
            id = record.getId();
            space.commit();
            
            space = _storage.startModifyTrx();
            record = space.getRecord(id);
            record.delete();
            checkRecordDeleted(space, id);
            space.commit();
        }
                
        // Check in read transaction
        {
            space = _storage.startReadTrx();
            checkRecordDeleted(space, id);
            space.commit();
        }
    }
    
    @Test
    public void testIsDeleted() {
        TrxSpace space = _storage.startModifyTrx();
        
        try {
            Record newRecord = space.newRecord();
            assertFalse("isDeleted() returns true for just created record.", newRecord.isDeleted());
            newRecord.delete();
            assertTrue("isDeleted returns false for just deleted record.", newRecord.isDeleted());
            space.commit();
            
            space = _storage.startReadTrx();
            Record root = space.getRecord(0L);
            assertFalse("isDeleted() returns true for existed record in read transaction.", root.isDeleted());
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void testRecordEquals()
    {
        // Check in Modify Trx
        TrxSpace space = _storage.startModifyTrx();
        Record r1 = space.newRecord();
        long id = r1.getId();
        
        Record proxyR1 = space.getRecord(id);
        Record proxyR2 = space.getRecord(id);
        
        assertEquals("Records are not equals", proxyR1, proxyR2);
        space.commit();
        
        // Check in Read Trx
        space = _storage.startReadTrx();
        proxyR1 = space.getRecord(id);
        proxyR2 = space.getRecord(id);
        assertEquals("Records are not equals", proxyR1, proxyR2);
        space.commit();
        
        // Check records from different spaces
        space = _storage.startModifyTrx();
        proxyR1 = space.getRecord(id);
        assertNotEquals("Records from differen spaces are Equals!", proxyR1, proxyR2);
        space.commit();
    }
    
    @Test
    public void testLargeField()
    {
        // Creates new record
        TrxSpace space = _storage.startModifyTrx();
        long id = doCreateRecord(space);
        space.commit();
        
        // Set large field in another modify transaction
        space = _storage.startModifyTrx();
        doLargeField(space, id);
        checkLargeField(space, id);
        space.commit();
        
        // Check in read transaction
        space = _storage.startReadTrx();
        checkLargeField(space, id);
        space.rollback();
    }

    @Test
    public void testRemoveField()
    {
        // Remove field
        TrxSpace space = _storage.startModifyTrx();
        long id = doPrimitiveFields(space);
        doRemoveField(space, id);
        space.commit();
        
        // Checks in read trx
        space = _storage.startReadTrx();
        checkRemoveField(space, id);
        space.rollback();
    }
    
    @Test
    public void testGetFieldNames()
    {
        TrxSpace space = _storage.startModifyTrx();
        long id = doPrimitiveFields(space);
        space.commit();
        
        space = _storage.startModifyTrx();
        checkFieldNames(space, id);
        space.rollback();
        
        space = _storage.startReadTrx();
        checkFieldNames(space, id);
        space.rollback();
    }
    
    @Test(expected = TransactionRolledbackException.class)
    public void testAccessDeletedProxy()
    {
        TrxSpace space = _storage.startModifyTrx();
        long id = doCreateRecord(space);
        Record proxy = space.getRecord(id);
        proxy.delete();
        proxy.getField("some field name");
    }
    
    @Test(expected = TransactionRolledbackException.class)
    public void testAccessDeletedProxy2()
    {
        TrxSpace space = _storage.startModifyTrx();
        long id = doCreateRecord(space);
        space.commit();
        
        space = _storage.startModifyTrx();
        Record proxy = space.getRecord(id);
        proxy.delete();
        proxy.getField("some field name");
    }
    
    @Test(expected = NotActiveTransactionException.class)
    public void testAccessNotActiveTrx()
    {
        TrxSpace space = _storage.startModifyTrx();
        Record proxy = space.newRecord();
        space.rollback();
        
        proxy.setField("Some field", "Some Value");
    }
    
    /**
     * Test should update byte[] field in different transactions so that
     * record's size increased till 10М.
     */
    @Test
    public void testIncreasedRecordSize() {
        int[] arraySizes = new int[]{16, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 
                262144, 524288, 1048576, 2097152, 4194304, 8388608, 0};
        
        TrxSpace space = _storage.startModifyTrx();
        Record testRecord = space.newRecord();
        testRecord.setField("Array", new byte[arraySizes[0]]);
        long id = testRecord.getId();
        space.commit();
        
        for (int i = 1; i < arraySizes.length; i++) {
            space = _storage.startModifyTrx();
            
            testRecord = space.getRecord(id);
            byte[] array = testRecord.getField("Array");
            
            assertNotNull(array);
            assertEquals(arraySizes[i - 1], array.length);
            
            array = new byte[arraySizes[i]];
            testRecord.setField("Array", array);
            
            space.commit();
        }
    }
    
}
