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

package com.vyhodb.space.index;

import static com.vyhodb.space.index.utils.IndexUtils.childExists;
import static com.vyhodb.space.index.utils.IndexUtils.compare;
import static com.vyhodb.space.index.utils.IndexUtils.doCreateNotUniqueChildren;
import static com.vyhodb.space.index.utils.IndexUtils.doCreateUniqueChildren;
import static com.vyhodb.space.index.utils.IndexUtils.doCreateUniqueChildren0;
import static com.vyhodb.space.index.utils.IndexUtils.getChild;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.server.TransactionRolledbackException;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.CriterionFactory;
import com.vyhodb.space.IndexDescriptor;
import com.vyhodb.space.IndexedField;
import com.vyhodb.space.Nullable;
import com.vyhodb.space.Order;
import com.vyhodb.space.Record;
import com.vyhodb.space.Unique;
import com.vyhodb.space.criteria.All;
import com.vyhodb.space.criteria.Null;
import com.vyhodb.space.index.utils.IndexUtils;

public class IndexTests extends AbstractStorageTests {

    public static final All ALL = new All();
    
    public static IndexDescriptor newDescriptor(String indexName, String linkName, String fieldName, Unique unique, Nullable nullable) {
        return new IndexDescriptor(indexName, linkName, unique, new IndexedField(fieldName, Integer.class, nullable));
    }
    
    @Test
    public void testPutField_WrongType()
    {
        final String linkName = "l";
        final String fieldName = "I";
        final String indexName = "testPutField_WrongType";
        
        // Create index and children
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            doCreateUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.SMALL_SET_SIZE);
            parent.createIndex(newDescriptor(indexName, linkName, fieldName, Unique.UNIQUE, Nullable.NULL));
            space.commit();
            
            // Changes record's field to wrong type
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            Record child = getChild(parent, indexName, 5);
            try
            {
                child.setField(fieldName, "Wrong value");
            }
            catch(TransactionRolledbackException tre)
            {
                assertEquals("Wrong field value class. Field name [I], expected class [java.lang.Integer], actual class [java.lang.String]", tre.getMessage());
                return;
            }
                    
            fail("Wrong field type was successfully set");
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void testPutField_Null_NullableIndex()
    {
        final String linkName = "l";
        final String fieldName = "I";
        final String indexName = "testPutField_Null_NullableIndex";
        
        // Create index and children
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            doCreateNotUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.SMALL_SET_SIZE);
            parent.createIndex(newDescriptor(indexName, linkName, fieldName, Unique.DUPLICATE, Nullable.NULL));
            space.commit();
            
            // Changes field value to null
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            Record child = getChild(parent, indexName, 5);
            long childId = child.getId();
            child.setField(fieldName, null);
            space.commit();
            
            // Checks
            space = _storage.startReadTrx();
            child = space.getRecord(childId);
            parent = space.getRecord(parentId);
            assertTrue(childExists(child, parent.searchChildren(indexName, new Null()).iterator()));
            space.rollback();
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void testPutField_Null_NotNullableIndex()
    {
        final String linkName = "l";
        final String fieldName = "I";
        final String indexName = "testPutField_Null_NullableIndex";
        
        // Create index and children
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            doCreateUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.SMALL_SET_SIZE);
            parent.createIndex(newDescriptor(indexName, linkName, fieldName, Unique.UNIQUE, Nullable.NOT_NULL));
            space.commit();
            
            // Changes field value to null
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            Record child = getChild(parent, indexName, 5);
            try
            {
                child.setField(fieldName, null);
            }
            catch(TransactionRolledbackException tre)
            {
                assertEquals("Field [I] can't be null and must be specified.", tre.getMessage());
                return;
            }
            
            fail("Sets [null] field value into Not nullable index");
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void testRemoveAll_NotUnique()
    {
        final String linkName = "l";
        final String fieldName = "I";
        final String indexName = "testRemoveAll_NotUnique";
        
        // Create index and children
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            doCreateNotUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.LARGE_SET_SIZE);
            parent.createIndex(newDescriptor(indexName, linkName, fieldName, Unique.DUPLICATE, Nullable.NULL));
            space.commit();
            
            //Remove All children
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            parent.removeChildren(linkName);
            space.commit();
            
            // Check
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            assertFalse("Elements in index after removing all childrens.", parent.searchChildren(indexName, new All()).iterator().hasNext());
            space.rollback();
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void testRemoveAll_Unique()
    {
        final String linkName = "l";
        final String fieldName = "I";
        final String indexName = "testRemoveAll_NotUnique";
        
        // Create index and children
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            doCreateUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.LARGE_SET_SIZE);
            parent.createIndex(newDescriptor(indexName, linkName, fieldName, Unique.UNIQUE, Nullable.NULL));
            space.commit();
            
            //Remove All children
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            parent.removeChildren(linkName);
            space.commit();
            
            // Check
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            assertFalse("Elements in index after removing all childrens.", parent.searchChildren(indexName, new All()).iterator().hasNext());
            space.rollback();
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void testCreate_NotUniue_BeforeChildren()
    {
        final String linkName = "l";
        final String fieldName = "I";
        final String indexName = "testCreate_NotUniue_BeforeChildren";
        
        // Create index
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            parent.createIndex(newDescriptor(indexName, linkName, fieldName, Unique.DUPLICATE, Nullable.NULL));
            space.commit();
            
            // Create children
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            ArrayList<Integer> elements = doCreateNotUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.LARGE_SET_SIZE);
            ArrayList<Integer> reverseElements = (ArrayList<Integer>) elements.clone();
            Collections.reverse(reverseElements);
            space.commit();
            
            // Check in read transaction
            space = _storage.startReadTrx();
            parent = space.getRecord(parentId);
            compare(fieldName, parent.searchChildren(indexName, ALL).iterator(), elements.iterator());
            compare(fieldName, parent.searchChildren(indexName, ALL, Order.DESC).iterator(), reverseElements.iterator());
            space.rollback();
            
            // Check in modify transaction
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            compare(fieldName, parent.searchChildren(indexName, ALL).iterator(), elements.iterator());
            compare(fieldName, parent.searchChildren(indexName, ALL, Order.DESC).iterator(), reverseElements.iterator());
            space.rollback();
        } 
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void testCreate_NotUniue_AfterChildren()
    {
        final String linkName = "l";
        final String fieldName = "I";
        final String indexName = "testCreate_NotUniue_BeforeChildren";
        
        // Create children
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            ArrayList<Integer> elements = doCreateNotUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.LARGE_SET_SIZE);
            ArrayList<Integer> reverseElements = (ArrayList<Integer>) elements.clone();
            Collections.reverse(reverseElements);
            space.commit();
            
            // Create index
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            parent.createIndex(newDescriptor(indexName, linkName, fieldName, Unique.DUPLICATE, Nullable.NULL));
            space.commit();
            
            // Check in read transaction
            space = _storage.startReadTrx();
            parent = space.getRecord(parentId);
            compare(fieldName, parent.searchChildren(indexName, ALL).iterator(), elements.iterator());
            compare(fieldName, parent.searchChildren(indexName, ALL, Order.DESC).iterator(), reverseElements.iterator());
            space.rollback();
            
            // Check in modify transaction
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            compare(fieldName, parent.searchChildren(indexName, ALL).iterator(), elements.iterator());
            compare(fieldName, parent.searchChildren(indexName, ALL, Order.DESC).iterator(), reverseElements.iterator());
            space.rollback();
        }
        finally {
            space.rollback();
        }
    }
    
    /**
     * Creates index (unique, nullable) and after and subsequently adds children
     */
    @Test
    public void testCreate_Unique_BeforeChildren()
    {
        final String linkName = "L";
        final String fieldName = "I";
        final String indexName = "Add children after index creation";
        
        // Create index
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            parent.createIndex(newDescriptor(indexName, linkName, fieldName, Unique.UNIQUE, Nullable.NOT_NULL));
            space.commit();
            
            // Create children
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            TreeSet<Integer> elements = doCreateUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.LARGE_SET_SIZE);
            space.commit();
            
            // Check in read transaction
            space = _storage.startReadTrx();
            parent = space.getRecord(parentId);
            // Checks ascending
            compare(fieldName, parent.searchChildren(indexName, ALL).iterator(), elements.iterator());
            // Checks descending
            compare(fieldName, parent.searchChildren(indexName, ALL, Order.DESC).iterator(), elements.descendingIterator());
            space.rollback();
            
            // Check in modify transaction
            space = _storage.startModifyTrx();
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
    
    @Test
    public void testCreate_LargeChildren()
    {
        final String linkName = "L";
        final String fieldName = "I";
        
        // Create index
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            List<Integer> elements = doCreateUniqueChildren0(space, parentId, linkName, fieldName, IndexUtils.LARGE_SET_SIZE);
            space.commit();
            
            // Check in read transaction
            space = _storage.startReadTrx();
            parent = space.getRecord(parentId);
            // Checks ascending
            compare(fieldName, parent.getChildren(linkName).iterator(), elements.iterator());
            // Checks descending
            //checkElements(fieldName, parent.getIndex(indexName, new AllDesc()), elements.descendingIterator());
            space.rollback();
            
            // Check in modify transaction
            space = _storage.startModifyTrx();
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
    
    /**
     * Creates index ((unique, nullable) on existed children
     */
    @Test
    public void testCreate_Unique_AfterChildren()
    {
        final String linkName = "L";
        final String fieldName = "I";
        final String indexName = "Large Integer Index";
        
        // Create children and index
        TrxSpace space = _storage.startModifyTrx();
        try
        {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            TreeSet<Integer> elements = doCreateUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.LARGE_SET_SIZE);
            parent.createIndex(newDescriptor(indexName, linkName, fieldName, Unique.UNIQUE, Nullable.NULL));
            space.commit();
            
            // Checks in read transaction
            space = _storage.startReadTrx();
            parent = space.getRecord(parentId);
            // Checks ascending
            compare(fieldName, parent.searchChildren(indexName, ALL).iterator(), elements.iterator());
            // Checks descending
            compare(fieldName, parent.searchChildren(indexName, ALL, Order.DESC).iterator(), elements.descendingIterator());
            space.rollback();
            
            // Check in modify transaction
            space = _storage.startModifyTrx();
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
    
    /**
     * Creates index on not uniqie field value
     * 
     */
    @Test
    public void testCreate_Unique_AfterChildrenFail()
    {
        final String linkName = "L";
        final String fieldName = "I";
        final String indexName = "Unique Index Fail";
        
        // Create children and index
        TrxSpace space = _storage.startModifyTrx();
        try
        {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            TreeSet<Integer> elements = doCreateUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.LARGE_SET_SIZE);
            
            // Creates not unique element
            Record notUniqueRecord = space.newRecord();
            notUniqueRecord.setField(fieldName, elements.iterator().next());
            notUniqueRecord.setParent(linkName, parent);
            space.commit();
            
            // Try to create unique index on not unique field values
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            try
            {
                parent.createIndex(newDescriptor(indexName, linkName, fieldName, Unique.UNIQUE, Nullable.NULL));
            }
            catch(TransactionRolledbackException tre)
            {
                assertTrue(tre.getMessage().startsWith("Unique index constraint violation. Specified value already exists:"));
                return;
            }
            
            fail("Unique index has been created");
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void testGetEmptyIndex()
    {
        final String indexName = "Not existed index";
        
        // In @Read
        TrxSpace space = _storage.startReadTrx();
        try
        {
            Record root = space.getRecord(0);
            Iterator<Record> index = root.searchChildren(indexName, new All()).iterator();
            fail();
        }
        catch(TransactionRolledbackException tre)
        {
            assertEquals("Index name with specified name [Not existed index] does not exist.", tre.getMessage());
        }
        space.rollback();
        
        // In @Modify
        space = _storage.startModifyTrx();
        try
        {
            Record root = space.getRecord(0);
            Iterator<Record> index = root.searchChildren(indexName, new All()).iterator();
            fail();
        }
        catch(TransactionRolledbackException tre)
        {
            assertEquals("Index name with specified name [Not existed index] does not exist.", tre.getMessage());
        }
        space.rollback();
        
    }
    
    @Test
    public void testDelete_Index()
    {
        final String linkName = "l";
        final String fieldName = "I";
        final String indexName = "testDelete_Index";
        
        IndexDescriptor descriptor = newDescriptor(indexName, linkName, fieldName, Unique.DUPLICATE, Nullable.NULL);
        
        // Create index and children
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            doCreateNotUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.SMALL_SET_SIZE);
            parent.createIndex(descriptor);
            space.commit();
            
            //Delete index
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            assertTrue("Created index does not exist.", parent.containsIndex(indexName));
            parent.removeIndex(indexName);
            space.commit();
            
            // Check
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            assertFalse("Elements in index after index deletion.", parent.containsIndex(indexName));
            space.rollback();
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void testIterator_Parent_Deleted()
    {
        final String linkName = "l";
        final String fieldName = "I";
        final String indexName = "testIterator_Parent_Deleted";
        
        IndexDescriptor descriptor = newDescriptor(indexName, linkName, fieldName, Unique.DUPLICATE, Nullable.NULL);
        
        // Create index and children
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            doCreateNotUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.SMALL_SET_SIZE);
            parent.createIndex(descriptor);
            space.commit();
            
            //Deletes parent
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            Iterator<Record> children = parent.searchChildren(indexName, CriterionFactory.all()).iterator();
            assertTrue(children.hasNext());
            assertTrue(children.next() != null);
            parent.delete();
            try {
                children.next();
                fail("TransactionRolledbackException expected.");
            }
            catch(TransactionRolledbackException ex) {
                assertEquals("Wrong exception message.", "Parent record has been deleted.", ex.getMessage());
            }
            
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void testIterator_Index_Changed()
    {
        final String linkName = "l";
        final String fieldName = "I";
        final String indexName = "testIterator_Index_Changed";
        
        IndexDescriptor descriptor = newDescriptor(indexName, linkName, fieldName, Unique.DUPLICATE, Nullable.NULL);
        
        // Create index and children
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            doCreateNotUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.SMALL_SET_SIZE);
            parent.createIndex(descriptor);
            space.commit();
            
            // Changes existed child
            Record child = null;
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            Iterator<Record> children = parent.searchChildren(indexName, CriterionFactory.all(), Order.DESC).iterator();
            assertTrue(children.hasNext());
            assertTrue((child = children.next()) != null);
            // Actual change
            child.setField(fieldName, Integer.MIN_VALUE);
            try {
                children.next();
                fail("TransactionRolledbackException expected.");
            }
            catch(TransactionRolledbackException ex) {
                assertEquals("Index concurrent modification.", ex.getMessage());
            }
            
            // Removes existed child
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            children = parent.searchChildren(indexName, CriterionFactory.all()).iterator();
            assertTrue(children.hasNext());
            assertTrue((child = children.next()) != null);
            child.setParent(linkName, null);
            try {
                children.next();
                fail("TransactionRolledbackException expected.");
            }
            catch(TransactionRolledbackException ex) {
                assertEquals("Index concurrent modification.", ex.getMessage());
            }
            
        }
        finally {
            space.rollback();
        }
    }
}
