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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static com.vyhodb.space.index.utils.IndexUtils.childExists;
import static com.vyhodb.space.index.utils.IndexUtils.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.IndexDescriptor;
import com.vyhodb.space.IndexedField;
import com.vyhodb.space.Nullable;
import com.vyhodb.space.Record;
import com.vyhodb.space.Space;
import com.vyhodb.space.Unique;
import com.vyhodb.space.criteria.Equal;
import com.vyhodb.space.criteria.EqualComposite;
import com.vyhodb.space.criteria.In;
import com.vyhodb.space.criteria.Null;

public class VirtualLinkIndexTests extends AbstractStorageTests {

    public static final String[] COLORS = {"Red", "Blue", "Green", "White", "Black", "Yellow", "Olive", "Orange"};
    public static final String[] LEVELS = {"Low", "Medium", "High"};
      
    public static final String FIELD_NAME = "Name";
    public static final String FIELD_NUMBER = "Number";
        
    public static final String LINK_COLOR = "Color";
    public static final String LINK_LEVEL = "Level";
    public static final String LINK_ALL_COLORS = "All_Colors";
    public static final String LINK_ALL_LEVELS = "All_Levels";
      
    public static final String NAME_INDEX_COLOR = "Index_Color";
    public static final String NAME_INDEX_COLOR_LEVEL = "Index_Color_Level";
    
    public static final String CHILDREN_NAME = "virtualTests";
  
    public static final IndexDescriptor INDEX_COLOR = new IndexDescriptor(NAME_INDEX_COLOR, CHILDREN_NAME, Unique.DUPLICATE, new IndexedField(LINK_COLOR, Long.class, Nullable.NULL));
    public static final IndexDescriptor INDEX_COLOR_LEVEL = new IndexDescriptor(NAME_INDEX_COLOR_LEVEL, CHILDREN_NAME, Unique.DUPLICATE, 
            new IndexedField(LINK_COLOR, Long.class, Nullable.NULL),
            new IndexedField(LINK_LEVEL, Long.class, Nullable.NULL)
    );
    
    private void createTestData() {
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record root = space.getRecord(0L);
            // Test Data has been created
            if (root.getChildrenLinkNames().contains(CHILDREN_NAME)) {
                return;
            }
            
            root.createIndex(INDEX_COLOR);
            root.createIndex(INDEX_COLOR_LEVEL);
                        
            // Creates dictionaries
            Map<String, Record> colors = null;
            Map<String, Record> levels = null;
            {
                colors = createDictionary(root, FIELD_NAME, LINK_ALL_COLORS, COLORS);
                levels = createDictionary(root, FIELD_NAME, LINK_ALL_LEVELS, LEVELS);
            }
            
            addChild(root, 1, "Red", "Low");
            addChild(root, 2, "Red", "Low");
            addChild(root, 3, "Red", "Normal");
            addChild(root, 4, "Red", "null");
            addChild(root, 5, "Green", "Low");
            addChild(root, 6, "null", "null");
            addChild(root, 7, "Blue", "High");
            addChild(root, 8, "Blue", "Low");
        }
        finally {
            if (space.isActive()) {
                space.commit();
            }
        }
        
    }
    
    private Record addChild(Record parent, int number, String color, String level) {
        Record record = parent.getSpace().newRecord();
        
        record.setParent(CHILDREN_NAME, parent);
        record.setField(FIELD_NUMBER, number);
        
        Record colorRecord = search(parent, LINK_ALL_COLORS, FIELD_NAME, color);
        record.setParent(LINK_COLOR, colorRecord);
        
        Record levelRecord = search(parent, LINK_ALL_LEVELS, FIELD_NAME, level);
        record.setParent(LINK_LEVEL, levelRecord);
        
        return record;
    }

    private Map<String, Record> createDictionary(Record parent, String fieldName, String linkName, String[] values) {
        HashMap<String, Record> result = new HashMap<>();
        Space space = parent.getSpace();
        Record child;

        for (String value : values) {
            child = space.newRecord();
            child.setField(fieldName, value);
            child.setParent(linkName, parent);
            result.put(value, child);
        }

        return result;
    }
    
    private Record search(Record parent, String linkName, String fieldName, String value) {
        Iterator<Record> children = parent.getChildren(linkName).iterator();
        Record child;
        while(children.hasNext()) {
            child = children.next();
            if (child.getField(fieldName) != null && child.getField(fieldName).equals(value)) {
                return child;
            }
        }
        
        return null;
    }
    
    private Record searchColor(Record parent, String color) {
        return search(parent, LINK_ALL_COLORS, FIELD_NAME, color);
    }
    
    private Record searchLevel(Record parent, String level) {
        return search(parent, LINK_ALL_LEVELS, FIELD_NAME, level);
    }
    
    @Test
    public void test_In() {
        createTestData();
        
        ArrayList<Integer> expectedResult = new ArrayList<>();
        // Red
        expectedResult.add(1);
        expectedResult.add(2);
        expectedResult.add(3);
        expectedResult.add(4);
        
        // Blue
        expectedResult.add(7);
        expectedResult.add(8);
        
        TrxSpace space = _storage.startReadTrx();
        try {
            Record root = space.getRecord(0L);
            long redId = searchColor(root, "Red").getId();
            long blueId = searchColor(root, "Blue").getId();
            Iterator<Record> searchResult = root.searchChildren(NAME_INDEX_COLOR, new In(redId, blueId)).iterator();
            
            compare(FIELD_NUMBER, searchResult, expectedResult.iterator());
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Equal_Null() {
        createTestData();
        
        ArrayList<Integer> expectedResult = new ArrayList<>();
        expectedResult.add(6);
        
        TrxSpace space = _storage.startReadTrx();
        try {
            Record root = space.getRecord(0L);
            Iterator<Record> searchResult = root.searchChildren(NAME_INDEX_COLOR, new Null()).iterator();
            
            compare(FIELD_NUMBER, searchResult, expectedResult.iterator());
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Equal() {
        createTestData();
        
        ArrayList<Integer> expectedResult = new ArrayList<>();
        expectedResult.add(1);
        expectedResult.add(2);
        expectedResult.add(3);
        expectedResult.add(4);
        
        TrxSpace space = _storage.startReadTrx();
        try {
            Record root = space.getRecord(0L);
            long redRecordId = searchColor(root, "Red").getId();
            Iterator<Record> searchResult = root.searchChildren(NAME_INDEX_COLOR, new Equal(redRecordId)).iterator();
            
            compare(FIELD_NUMBER, searchResult, expectedResult.iterator());
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Equal_Composite_Full() {
        createTestData();
        
        ArrayList<Integer> expectedResult = new ArrayList<>();
        expectedResult.add(7);
                
        TrxSpace space = _storage.startReadTrx();
        try {
            Record root = space.getRecord(0L);
            long blueRecordId = searchColor(root, "Blue").getId();
            long highRecordId = searchLevel(root, "High").getId();
            HashMap<String, Long> keys = new HashMap<>();
            keys.put(LINK_COLOR, blueRecordId);
            keys.put(LINK_LEVEL, highRecordId);
            
            Iterator<Record> searchResult = root.searchChildren(NAME_INDEX_COLOR_LEVEL, new EqualComposite(keys)).iterator();
            
            compare(FIELD_NUMBER, searchResult, expectedResult.iterator());
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Equal_Composite_Partial() {
        createTestData();
        
        ArrayList<Integer> expectedResult = new ArrayList<>();
        expectedResult.add(8);
        expectedResult.add(7);
                
        TrxSpace space = _storage.startReadTrx();
        try {
            Record root = space.getRecord(0L);
            long redRecordId = searchColor(root, "Blue").getId();
            HashMap<String, Long> keys = new HashMap<>();
            keys.put(LINK_COLOR, redRecordId);

            Iterator<Record> searchResult = root.searchChildren(NAME_INDEX_COLOR_LEVEL, new EqualComposite(keys)).iterator();
            
            compare(FIELD_NUMBER, searchResult, expectedResult.iterator());
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_Record_Create_Move_Delete() {
        createTestData();
        
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record root = space.getRecord(0L);
            
            Record redColorRecord = searchColor(root, "Red");
            Record blueColorRecord = searchColor(root, "Blue");
            Record mediumLevelRecord = searchLevel(root, "Medium");
            
            long redColorId = redColorRecord.getId();
            long blueColorId = blueColorRecord.getId();
            long mediumlevelId = mediumLevelRecord.getId();
            
            HashMap<String, Long> redMediumKey = new HashMap<>();
            redMediumKey.put(LINK_COLOR, redColorId);
            redMediumKey.put(LINK_LEVEL, mediumlevelId);
            
            HashMap<String, Long> blueMediumKey = new HashMap<>();
            blueMediumKey.put(LINK_COLOR, blueColorId);
            blueMediumKey.put(LINK_LEVEL, mediumlevelId);
            
            Record child = addChild(root, 16, "null", "null");
            assertFalse("Color index contains not added link.", childExists(root, child, NAME_INDEX_COLOR, redColorId));
            assertFalse("Level index contains not added link.", childExists(root, child, NAME_INDEX_COLOR, blueColorId));
            
            // Sets parents and checks
            child.setParent(LINK_COLOR, redColorRecord);
            child.setParent(LINK_LEVEL, mediumLevelRecord);
            assertTrue("Color index does not contain record.",  childExists(root, child, NAME_INDEX_COLOR, redColorId));
            assertTrue("Color-Level index does not contain record.", childExists(root, child, NAME_INDEX_COLOR_LEVEL, redMediumKey));
            
            // Change color and check
            child.setParent(LINK_COLOR, blueColorRecord);
            assertTrue("Color index does not contain record.",  childExists(root, child, NAME_INDEX_COLOR, blueColorId));
            assertTrue("Color-Level index does not contain record", childExists(root, child, NAME_INDEX_COLOR_LEVEL, blueMediumKey));
            assertFalse("Color index contains record.",  childExists(root, child, NAME_INDEX_COLOR, redColorId));
            assertFalse("Color-Level index contains record", childExists(root, child, NAME_INDEX_COLOR_LEVEL, redMediumKey));
            
            // Deletes and checks
            child.delete();
            assertFalse("Deleted record is in Color index.", childExists(root, child, NAME_INDEX_COLOR, blueColorId));
            assertFalse("Deleted record is in Color-Level index.", childExists(root, child, NAME_INDEX_COLOR_LEVEL, blueMediumKey));
        }
        finally {
            space.rollback();
        }
    }
    
    /**
     * This test do the 
     * 
     */
    @Test
    public void test_Iterator_Remove() {
        String linkNameTestCase = "test_Iterator_Remove";
        String linkNameParentA = "parentA";
        String linkNameParentB = "parentB";
        String linkNameChild = "child";
        String linkNameRemoved = "Removed";
        String linkNameIndexed = "Indexed";
        
        IndexDescriptor descriptor = new IndexDescriptor("Index_Iterator_Remove", linkNameIndexed, Unique.DUPLICATE, 
                new IndexedField(linkNameRemoved, Long.class, Nullable.NULL));
        
        
        TrxSpace space = _storage.startModifyTrx();
        
        Record root = space.getRecord(0L);
        
        Record testCaseRecord = space.newRecord();
        testCaseRecord.setParent(linkNameTestCase, root);
        
        Record parentA = space.newRecord();
        parentA.setParent(linkNameParentA, testCaseRecord);
        parentA.createIndex(descriptor);
        
        Record parentB = space.newRecord();
        parentB.setParent(linkNameParentB, testCaseRecord);
        
        Record child = space.newRecord();
        child.setParent(linkNameChild, testCaseRecord);
        child.setParent(linkNameRemoved, parentB);
        child.setParent(linkNameIndexed, parentA);
        
        // Removed link "Removed"
        Iterator<Record> parentBchildren = parentB.getChildren(linkNameRemoved).iterator();
        while(parentBchildren.hasNext()) {
            parentBchildren.next();
            parentBchildren.remove();
        }
        
        // Test actually
        assertTrue(child.getField("Removed") == null);
        assertTrue(childExists(parentA, child, "Index_Iterator_Remove", new Null()));
        
        space.commit();
    }
}
