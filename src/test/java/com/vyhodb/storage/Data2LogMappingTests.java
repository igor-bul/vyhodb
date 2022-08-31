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

import static com.vyhodb.space.index.utils.IndexUtils.compare;
import static com.vyhodb.space.index.utils.IndexUtils.doCreateUniqueChildren;
import static com.vyhodb.space.index.utils.IndexUtils.doCreateUniqueChildren0;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.AllTests;
import com.vyhodb.admin.Admin;
import com.vyhodb.server.Server;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.IndexDescriptor;
import com.vyhodb.space.IndexedField;
import com.vyhodb.space.Nullable;
import com.vyhodb.space.Order;
import com.vyhodb.space.Record;
import com.vyhodb.space.Unique;
import com.vyhodb.space.criteria.All;
import com.vyhodb.space.index.IndexTests;
import com.vyhodb.space.index.utils.IndexUtils;

/**
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class Data2LogMappingTests {

    private static final String TEMP_DIRECTORY = "ut_temp_map_files";
    
    public static final All ALL = new All();
    
    public static IndexDescriptor newDescriptor(String indexName, String linkName, String fieldName, Unique unique, Nullable nullable) {
        return new IndexDescriptor(indexName, linkName, unique, new IndexedField(fieldName, Integer.class, nullable));
    }
    
    /**
     * This unit test copies index creation unit test from {@linkplain IndexTests#testCreate_Unique_AfterChildren()}, 
     * but starts this logic with extremely low threshold of data2log mapping memory.
     * 
     * @throws IOException
     */
    @Test
    public void test_main() throws IOException {
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        
        Properties props = AbstractStorageTests.generateDefaultProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        props.setProperty("space.mapping.inMemorySize", "100");

        try (Server storage = Server.start(props)) {
            test_Create_Unique_AfterChildren(storage);
        }
    }
    
    /**
     * This unit test copies children creation unit test from {@linkplain IndexTests#testCreate_LargeChildren()}.
     * It tests changing directory for temp mapping files.
     * 
     * @throws IOException
     */
    @Test
    public void test_ChangeDicrectoryToMapFile() throws IOException {
        Path directory = AllTests.resolve(TEMP_DIRECTORY);
        
        Admin admin = Admin.getInstance();
        admin.removeStorageFiles(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        admin.newStorage(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        
        Properties props = AbstractStorageTests.generateDefaultProperties(AbstractStorageTests.LOG_FILENAME, AbstractStorageTests.DATA_FILENAME);
        props.setProperty("space.mapping.inMemorySize", "100");
        props.setProperty("space.mapping.directory", directory.toString());
        
        try (Server storage = Server.start(props)) {
            Files.createDirectories(directory);
            
            try {
                test_Create_LargeChildren(storage);
            } finally {
                Files.delete(directory);
            }
        }
    }
    
    private void test_Create_Unique_AfterChildren(Server storage) {
        final String linkName = "L";
        final String fieldName = "I";
        final String indexName = "Large Integer Index";
        
        // Create children and index
        TrxSpace space = storage.startModifyTrx();
        try
        {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            TreeSet<Integer> elements = doCreateUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.LARGE_SET_SIZE);
            parent.createIndex(newDescriptor(indexName, linkName, fieldName, Unique.UNIQUE, Nullable.NULL));
            space.commit();
            
            // Checks in read transaction
            space = storage.startReadTrx();
            parent = space.getRecord(parentId);
            // Checks ascending
            compare(fieldName, parent.searchChildren(indexName, ALL).iterator(), elements.iterator());
            // Checks descending
            compare(fieldName, parent.searchChildren(indexName, ALL, Order.DESC).iterator(), elements.descendingIterator());
            space.rollback();
            
            // Check in modify transaction
            space = storage.startModifyTrx();
            parent = space.getRecord(parentId);
            // Checks ascending
            compare(fieldName, parent.searchChildren(indexName, ALL).iterator(), elements.iterator());
            // Checks descending
            compare(fieldName, parent.searchChildren(indexName, ALL, Order.DESC).iterator(), elements.descendingIterator());
            space.rollback();
        }
        finally {
            space.rollback();
        }
    }
    
    private void test_Create_LargeChildren(Server storage) {
        final String linkName = "L";
        final String fieldName = "I";
        
        // Create index
        TrxSpace space = storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            List<Integer> elements = doCreateUniqueChildren0(space, parentId, linkName, fieldName, IndexUtils.LARGE_SET_SIZE);
            space.commit();
            
            // Check in read transaction
            space = storage.startReadTrx();
            parent = space.getRecord(parentId);
            // Checks ascending
            compare(fieldName, parent.getChildren(linkName).iterator(), elements.iterator());
            // Checks descending
            //checkElements(fieldName, parent.getIndex(indexName, new AllDesc()), elements.descendingIterator());
            space.rollback();
            
            // Check in modify transaction
            space = storage.startModifyTrx();
            parent = space.getRecord(parentId);
            // Checks ascending
            compare(fieldName, parent.getChildren(linkName).iterator(), elements.iterator());
            // Checks descending
            // checkElements(fieldName, parent.getIndex(indexName, new AllDesc()), elements.descendingIterator());
            space.rollback();
        }
        finally {
            space.rollback();
        } 
    }
}
