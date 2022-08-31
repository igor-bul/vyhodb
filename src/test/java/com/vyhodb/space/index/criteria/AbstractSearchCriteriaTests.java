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

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.admin.Admin;
import com.vyhodb.server.Server;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Criterion;
import com.vyhodb.space.IndexDescriptor;
import com.vyhodb.space.IndexedField;
import com.vyhodb.space.Nullable;
import com.vyhodb.space.Order;
import com.vyhodb.space.Record;
import com.vyhodb.space.Unique;
import com.vyhodb.space.index.utils.AbstractReader;
import com.vyhodb.space.index.utils.BigDecimalReader;
import com.vyhodb.space.index.utils.EOF;
import com.vyhodb.space.index.utils.Generator;
import com.vyhodb.space.index.utils.IndexUtils;
import com.vyhodb.space.index.utils.IntegerReader;
import com.vyhodb.space.index.utils.StringReader;
import com.vyhodb.space.index.utils.ValueReader;

public abstract class AbstractSearchCriteriaTests {

    public static final LinkedList<Integer> EMPTY_TEST_DATA = new LinkedList<>();
    
    public static final String ROOT_LINK_NAME = "SearchCriteriaTests";
    public static final String CHILDREN_UNIQUE = "unique";
    public static final String CHILDREN_DUPLICATE = "duplicate";
    
    public static final String FILE_MORE_UNIQUE = "more.unique.txt";
    public static final String FILE_LESS_UNIQUE = "less.unique.txt";
    public static final String FILE_BETWEEN_UNIQUE = "between.unique.txt";
    public static final String FILE_STARTS_WITH_UNIQUE = "starts.with.unique.txt";
    public static final String FILE_STARTS_WITH_UNIQUE_INSENSITIVE = "starts.with.unique.insensitive.txt";
    
    public static final String FIELD_INTEGER = "I";
    public static final String FIELD_STRING = "S";
    public static final String FIELD_DECIMAL = "D";
    
    public static final String NAME_INDEX_INTEGER_UNIQUE = "Index_Unique_Integer";
    public static final String NAME_INDEX_STRING_UNIQUE = "Index_Unique_S";
    public static final String NAME_INDEX_DECIMAL_UNIQUE = "Index_Unique_D";
    public static final String NAME_INDEX_COMPOSITE_UNIQUE = "Index_Unique_Composite";
       
    public static final IndexDescriptor INDEX_INTEGER_UNIQUE = new IndexDescriptor(NAME_INDEX_INTEGER_UNIQUE, CHILDREN_UNIQUE, Unique.UNIQUE, new IndexedField(FIELD_INTEGER, Integer.class, Nullable.NULL));
    public static final IndexDescriptor INDEX_STRING_UNIQUE = new IndexDescriptor(NAME_INDEX_STRING_UNIQUE, CHILDREN_UNIQUE, Unique.UNIQUE, new IndexedField(FIELD_STRING, String.class, Nullable.NULL));
    public static final IndexDescriptor INDEX_DECIMAL_UNIQUE = new IndexDescriptor(NAME_INDEX_DECIMAL_UNIQUE, CHILDREN_UNIQUE, Unique.UNIQUE, new IndexedField(FIELD_DECIMAL, BigDecimal.class, Nullable.NULL));
    
    public static final IndexDescriptor INDEX_COMPOSITE_UNIQUE = new IndexDescriptor(NAME_INDEX_COMPOSITE_UNIQUE, CHILDREN_UNIQUE, Unique.UNIQUE,  
            new IndexedField(FIELD_INTEGER, Integer.class, Nullable.NULL),
            new IndexedField(FIELD_STRING, String.class, Nullable.NULL),
            new IndexedField(FIELD_DECIMAL, BigDecimal.class, Nullable.NULL)
    );
    
    protected static Server _storage;
    
    protected static LinkedList<Integer> allTestData;
    protected static LinkedList<Integer> allNoNullTestData;
    
    protected static LinkedList<Integer> lessTestData;
    protected static LinkedList<Integer> moreTestData;
   
    protected static LinkedList<Integer> betweenTestData;
    protected static LinkedList<String> startswithTestData;
    protected static LinkedList<String> startswithInsensitiveTestData;
    
    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void createStorage() throws IOException, ReflectiveOperationException {
        _storage = tryToOpen();
        
        if (_storage == null) {
            recreateStorage();
            _storage = open();
            fillStorage();
        }
        
        allTestData = loadValues(new IntegerReader(Generator.INTEGER_UNIQUE_SORTED));
        allNoNullTestData = new LinkedList<Integer>(allTestData);
        allNoNullTestData.remove(null);
        
        lessTestData = loadValues(new IntegerReader(FILE_LESS_UNIQUE));
        moreTestData = loadValues(new IntegerReader(FILE_MORE_UNIQUE));
        betweenTestData = loadValues(new IntegerReader(FILE_BETWEEN_UNIQUE));
        startswithTestData = loadValues(new StringReader(FILE_STARTS_WITH_UNIQUE));
        startswithInsensitiveTestData = loadValues(new StringReader(FILE_STARTS_WITH_UNIQUE_INSENSITIVE));
    }
    
    @AfterClass
    public static void closeStorage() throws IOException {
        if (_storage != null) {
            _storage.close();
        }
    }
    
    private static void recreateStorage() throws IOException {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
    }
    
    private static Server open() throws IOException {
        Properties props = new Properties();
        props.setProperty("storage.log", AbstractStorageTests.LOG_FILENAME);
        props.setProperty("storage.data", AbstractStorageTests.DATA_FILENAME);
        props.setProperty("space.record.modifyCacheSize", "5000");
        props.setProperty("storage.cacheSize", "200000");
        props.setProperty("storage.modifyBufferSize", "150000");
        props.setProperty("storage.logBufferSize", "100000");
        
        return Server.start(props);
    }
    
    private static Server tryToOpen() throws IOException {
        
        if (
              ! (
                Files.exists(Paths.get(AbstractStorageTests.LOG_FILENAME)) && 
                Files.exists(Paths.get(AbstractStorageTests.DATA_FILENAME))
              )
           ) {
            return null;
        }
        
        Server storage = open();
        TrxSpace space = storage.startReadTrx();

        Record root = space.getRecord(0);
        if (root.getChildren(ROOT_LINK_NAME).iterator().hasNext()) {
            space.rollback();
            return storage;
        }
        else {
            space.rollback();
            storage.close();
            return null;
        }
    }
    
    private static void fillStorage() throws IOException {
        TrxSpace space = _storage.startModifyTrx();
        try
        {
            Record child;
            Record root = space.getRecord(0);
            Record parent = space.newRecord();
            parent.setParent(ROOT_LINK_NAME, root);
            
            // Creates unique children
            for (int i = 0; i < Generator.UNIQUE_SET_SIZE; i++) {
                child = space.newRecord();
                child.setParent(CHILDREN_UNIQUE, parent);
            }
            
            // Fills unique children
            {
                IntegerReader intReader = new IntegerReader(Generator.INTEGER_UNIQUE_UNSORTED);
                fillFields(FIELD_INTEGER, parent.getChildren(CHILDREN_UNIQUE).iterator(), intReader);
                
                BigDecimalReader decimalReader = new BigDecimalReader(Generator.DECIMAL_UNIQUE_UNSORTED);
                fillFields(FIELD_DECIMAL, parent.getChildren(CHILDREN_UNIQUE).iterator(), decimalReader);
                
                StringReader stringReader = new StringReader(Generator.STRING_UNIQUE_UNSORTED);
                fillFields(FIELD_STRING, parent.getChildren(CHILDREN_UNIQUE).iterator(), stringReader);
            }
            
            // Creates unique indexes
            {
                // Field indexes
                parent.createIndex(INDEX_INTEGER_UNIQUE);
                parent.createIndex(INDEX_COMPOSITE_UNIQUE);
                parent.createIndex(INDEX_STRING_UNIQUE);
                parent.createIndex(INDEX_DECIMAL_UNIQUE);
            }
            
            space.commit();
        }
        finally {
            space.rollback();
        }
    }
    
    private static void fillFields(String fieldName, Iterator<Record> records, ValueReader valueReader) throws IOException {
        Record record;
                
        while (records.hasNext()) {
            record = records.next();
            record.setField(fieldName, valueReader.next());
        }
        
        valueReader.close();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected static LinkedList loadValues(AbstractReader reader) throws IOException {
        try {        
            LinkedList list = new LinkedList();
            Object value;
            while ((value = reader.next()) != EOF.SINGLE) {
                list.add(value);
            }
            
            return list;
        }
        finally {
            reader.close();
        }
    }
    
    @SuppressWarnings("rawtypes")
    protected static void testCriterion(String indexName, String compareFieldName, Criterion criterion, Iterator testDataIterator) {
        testCriterionInternal(indexName, compareFieldName, criterion, Order.ASC, testDataIterator);
    }
    
    @SuppressWarnings("rawtypes")
    protected static void testCriterionDesc(String indexName, String compareFieldName, Criterion criterion, Iterator testDataIterator) {
        testCriterionInternal(indexName, compareFieldName, criterion, Order.DESC, testDataIterator);
    }
    
    @SuppressWarnings("rawtypes")
    private static void testCriterionInternal(String indexName, String compareFieldName, Criterion criterion, Order order, Iterator testDataIterator) {
        TrxSpace space = _storage.startReadTrx();
        try {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            
            Iterator<Record> searchResult = parent.searchChildren(indexName, criterion, order).iterator();
            IndexUtils.compare(compareFieldName, searchResult, testDataIterator);
        }
        finally {
            space.rollback();
        }
    }
}
