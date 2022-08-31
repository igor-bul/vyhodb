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

package com.vyhodb.space.index.criteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.vyhodb.server.TransactionRolledbackException;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Record;
import com.vyhodb.space.criteria.EqualComposite;

public class EqualCompositeTests extends AbstractSearchCriteriaTests {

    private static final String KEY_STRING = "тkCюNп";
    private static final Integer KEY_INTEGER = -674266608;
    private static final BigDecimal KEY_DECIMAL = new BigDecimal("876.7915008997769064080785028636455535888671875");
    
    private void validate(Iterator<Record> searchResult, Map<String, Comparable> keys)  {
        Record record = searchResult.next();
        
        assertEquals("String field on found record is wrong.", keys.get(FIELD_STRING), record.getField(FIELD_STRING));
        assertEquals("Decimal field on found record is wrong.",  keys.get(FIELD_DECIMAL), record.getField(FIELD_DECIMAL));
        assertEquals("Integer field on found record is wrong.",  keys.get(FIELD_INTEGER), record.getField(FIELD_INTEGER));
        
        assertFalse("Search result has more lements then expected (1).", searchResult.hasNext());
    }
    
    @Test
    public void test_Unique_Exists_FullKey() {
        HashMap<String, Comparable> keys = new HashMap<>();
        keys.put(FIELD_DECIMAL, KEY_DECIMAL);
        keys.put(FIELD_INTEGER, KEY_INTEGER);
        keys.put(FIELD_STRING, KEY_STRING);
        
        TrxSpace space = _storage.startReadTrx();
        try
        {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            Iterator<Record> searchResults = parent.searchChildren(NAME_INDEX_COMPOSITE_UNIQUE, new EqualComposite(keys)).iterator();
            validate(searchResults, keys);
        }
        finally {
            space.rollback();
        }
    }
    
    
    
    @Test
    public void test_Unique_Exists_PartialKey() {
        HashMap<String, Comparable> keys = new HashMap<>();
        keys.put(FIELD_INTEGER, KEY_INTEGER);
        
        TrxSpace space = _storage.startReadTrx();
        try
        {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            Iterator<Record> searchResults = parent.searchChildren(NAME_INDEX_COMPOSITE_UNIQUE, new EqualComposite(keys)).iterator();
            
            // Adds values for validation
            keys.put(FIELD_DECIMAL, KEY_DECIMAL);
            keys.put(FIELD_STRING, KEY_STRING);
            
            validate(searchResults, keys);
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Unique_NotExists_FullKey() {
        HashMap<String, Comparable> keys = new HashMap<>();
        keys.put(FIELD_DECIMAL, KEY_DECIMAL);
        keys.put(FIELD_INTEGER, KEY_INTEGER);
        keys.put(FIELD_STRING, "Not Existed Key");
        
        TrxSpace space = _storage.startReadTrx();
        try
        {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            Iterator<Record> searchResults = parent.searchChildren(NAME_INDEX_COMPOSITE_UNIQUE, new EqualComposite(keys)).iterator();
            assertFalse("Results has records for not existed key.", searchResults.hasNext());
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Unique_NotExists_PartialKey() {
        HashMap<String, Comparable> keys = new HashMap<>();
        keys.put(FIELD_STRING, KEY_STRING);
        keys.put(FIELD_INTEGER, 11111111);
        
        TrxSpace space = _storage.startReadTrx();
        try
        {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            Iterator<Record> searchResults = parent.searchChildren(NAME_INDEX_COMPOSITE_UNIQUE, new EqualComposite(keys)).iterator();
            assertFalse("Results has records for not existed key.", searchResults.hasNext());
        }
        finally {
            space.rollback();
        }
    }
    
    @Test 
    public void test_Wrong_FieldName() {
        HashMap<String, Comparable> keys = new HashMap<>();
        keys.put("1111111", 11111111);
        
        TrxSpace space = _storage.startReadTrx();
        try
        {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            Iterator<Record> searchResults = parent.searchChildren(NAME_INDEX_COMPOSITE_UNIQUE, new EqualComposite(keys)).iterator();
            
            fail("Expeted TRE was not thrown.");
        }
        catch(TransactionRolledbackException tre) {
            assertEquals("Wrong exception.", "Index [Index_Unique_Composite] has no field [1111111].", tre.getMessage());
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Gap_Validation() {
        HashMap<String, Comparable> keys = new HashMap<>();
        keys.put(FIELD_STRING, KEY_STRING);
        
        TrxSpace space = _storage.startReadTrx();
        try {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            Iterator<Record> searchResults = parent.searchChildren(NAME_INDEX_COMPOSITE_UNIQUE, new EqualComposite(keys)).iterator();
            
            fail("Expected TRE was not thrown.");
        } 
        catch(TransactionRolledbackException tre) {
            assertEquals("Wrong exception.", "The following key fields must be specified: [I], or the field [S] must be omitted.", tre.getMessage());
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Unique_Exists_Null_FullKey() {
        HashMap<String, Comparable> keys = new HashMap<>();
        keys.put(FIELD_INTEGER, null);
        keys.put(FIELD_STRING, "hvKyБёи");
        keys.put(FIELD_DECIMAL, new BigDecimal("175974.25605172189534641802310943603515625"));
        
        TrxSpace space = _storage.startReadTrx();
        try
        {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            Iterator<Record> searchResults = parent.searchChildren(NAME_INDEX_COMPOSITE_UNIQUE, new EqualComposite(keys)).iterator();
            validate(searchResults, keys);
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Unique_Exists_Null_PartialKey() {
        HashMap<String, Comparable> keys = new HashMap<>();
        keys.put(FIELD_INTEGER, null);
        
        TrxSpace space = _storage.startReadTrx();
        try
        {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            Iterator<Record> searchResults = parent.searchChildren(NAME_INDEX_COMPOSITE_UNIQUE, new EqualComposite(keys)).iterator();
            
            // Adds keys for validation
            keys.put(FIELD_STRING, "hvKyБёи");
            keys.put(FIELD_DECIMAL, new BigDecimal("175974.25605172189534641802310943603515625"));
            
            validate(searchResults, keys);
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Unique_NotExists_Null_FullKey() {
        HashMap<String, Comparable> keys = new HashMap<>();
        keys.put(FIELD_DECIMAL, null);
        keys.put(FIELD_INTEGER, null);
        keys.put(FIELD_STRING, null);
        
        TrxSpace space = _storage.startReadTrx();
        try
        {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            Iterator<Record> searchResults = parent.searchChildren(NAME_INDEX_COMPOSITE_UNIQUE, new EqualComposite(keys)).iterator();
            assertFalse("Results has records for not existed key.", searchResults.hasNext());
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Unique_NotExists_Null_PartialKey() {
        HashMap<String, Comparable> keys = new HashMap<>();
        keys.put(FIELD_STRING, "Not Existed");
        keys.put(FIELD_INTEGER, null);
        
        TrxSpace space = _storage.startReadTrx();
        try
        {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            Iterator<Record> searchResults = parent.searchChildren(NAME_INDEX_COMPOSITE_UNIQUE, new EqualComposite(keys)).iterator();
            assertFalse("Results has records for not existed key.", searchResults.hasNext());
        }
        finally {
            space.rollback();
        }
    }
}
