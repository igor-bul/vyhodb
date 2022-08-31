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

package com.vyhodb.space.links;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import com.vyhodb.server.TransactionRolledbackException;
import com.vyhodb.space.Order;
import com.vyhodb.space.Record;
import com.vyhodb.space.Space;

import static org.junit.Assert.*;

public class LinksCommons {

    public static void checkChildrenCount(Space space, long parentId, long expectedCount, String linkName)
    {
        Record root = space.getRecord(parentId);
        assertEquals("Children count is wrong",  expectedCount, root.getChildrenCount(linkName));
    }
    
    public static void checkChildrenNames(Space space, long parentId, String linkName)
    {
        Record parent = space.getRecord(parentId);
        Set<String> childrenNames = parent.getChildrenLinkNames();
        assertEquals("Wrong count of children names", 1, childrenNames.size());
        assertEquals("Wrong children name", linkName, childrenNames.iterator().next());
    }
    
    public static void checkChildrenOrder(Iterator<Record> children, int[] values)
    {
        int index = 0;
        while(children.hasNext())
        {
            assertEquals("Wrong children order", (int)values[index], (int)children.next().getField("Integer"));
            index++;
        }
        
        assertEquals("Children has less elements.", values.length, index);
    }
    
    public static void checkDeleteRecord(Space space, long recordId, String rootChildrenLinkName, String rootParentLinkName)
    {
        assertNull("Record is not deleted", space.getRecord(recordId));
        
        Record root = space.getRecord(0);
        assertEquals("Root has a deleted child", 0, root.getChildrenCount(rootChildrenLinkName));
        assertNull("Root has a deleted parent", root.getParent(rootParentLinkName));
    }

    public static void checkEmptyChildren(Space space, long parentId, String linkName)
    {
        Record root = space.getRecord(parentId);
        Iterator<Record> empty = root.getChildren(linkName).iterator();
        assertFalse("Empty children iterator has elements", empty.hasNext());
    }
    
    public static void checkEmptyChildrenNextFail(Space space)
    {
        Record root = space.getRecord(0);
        Iterator<Record> empty = root.getChildren("Not existed link").iterator();
        assertFalse("Empty children iterator has elements", empty.hasNext());
        
        try
        {
            empty.next();
        }
        catch(TransactionRolledbackException tre)
        {
            return;
        }
        
        fail("Empty children iterator can return next element");
    }
    
    public static void checkEmptyChildrenRemoveFail(Space space)
    {
        Record root = space.getRecord(0);
        Iterator<Record> empty = root.getChildren("Not existed link").iterator();
        assertFalse("Empty children iterator has elements", empty.hasNext());
        
        try
        {
            empty.remove();
        }
        catch(TransactionRolledbackException tre)
        {
            return;
        }
        
        fail("remove() method on empty children iterator is invoked without exception");
    }
    
    public static void checkEmptyParent(Space space, long recordId, String linkName)
    {
        Record record = space.getRecord(recordId);
        Record parent = record.getParent(linkName);
        assertNull(parent);
    }
    
    public static void checkGetChildrenAsc(Space space, String linkName)
    {
        final int[] integerOrder = new int[]{1, 2, 3, 4};
        Record root = space.getRecord(0);
        checkChildrenOrder(root.getChildren(linkName).iterator(), integerOrder);
    }
    
    public static void checkGetChildrenDesc(Space space, String linkName)
    {
        final int[] integerOrder = new int[]{4, 3, 2, 1};
        Record root = space.getRecord(0);
        checkChildrenOrder(root.getChildren(linkName, Order.DESC).iterator(), integerOrder);
    }
    
    public static void checkParentNames(Space space, long parentId, String linkName)
    {
        Record parent = space.getRecord(parentId);
        Record child = parent.getChildFirst(linkName);
        
        Set<String> parentNames = child.getParentLinkNames();
        assertEquals("Wrong count of parent names", 1, parentNames.size());
        assertEquals("Wrong parent name", linkName, parentNames.iterator().next());
    }
    
    public static void checkSelfLink(Space space, long recordId, String linkName)
    {
        Record record = space.getRecord(recordId);
        assertEquals("Wrong children count",  1, record.getChildrenCount(linkName));
        assertEquals("Wrong parent", record, record.getParent(linkName));
        assertEquals("Wrong children", record, record.getChildFirst(linkName));
    }
    
    public static void checkSetParent(Space space, long childId, long parentId, String linkName)
    {
        Record child = space.getRecord(childId);
        Record parent = space.getRecord(parentId);
        
        assertEquals("", parent, child.getParent(linkName));
    }
    
    public static void do_Iterator_ConcurrentModification_Remove_Children(Space space)
    {
        String linkName = "Concurred Mod";
        
        // Creates new record and children
        Record record = space.newRecord();
        doCreateChildren(space, record.getId(), linkName);
        
        // Gets iterator
        Iterator<Record> children = record.getChildren(linkName).iterator();
        children.next();
        
        // Removes children
        record.removeChildren(linkName);
        
        // Must throw TRE Exception
        children.next();
    }
    
    public static void do_Iterator_ConcurrentModification_Delete_Parent(Space space)
    {
        String linkName = "Concurred Mod";
        
        // Creates new record and children
        Record record = space.newRecord();
        doCreateChildren(space, record.getId(), linkName);
        
        // Gets iterator
        Iterator<Record> children = record.getChildren(linkName).iterator();
        children.next();
        
        // Removes children
        record.delete();
        
        // Must throw TRE Exception
        children.next();
    }
    
    public static void doCreateChildren(Space space, long parentId, String linkName)
    {
        Record parent = space.getRecord(parentId);
        
        Record r1 = space.newRecord();
        r1.setField("String", "1");
        r1.setField("Integer", 1);
        r1.setField("BigDecimal", new BigDecimal("1.1"));
        r1.setParent(linkName, parent);
        
        Record r2 = space.newRecord();
        r2.setField("String", "2");
        r2.setField("Integer", 2);
        r2.setField("BigDecimal", new BigDecimal("2.2"));
        r2.setParent(linkName, parent);
        
        Record r3 = space.newRecord();
        r3.setField("String", "3");
        r3.setField("Integer", 3);
        r3.setField("BigDecimal", new BigDecimal("3.3"));
        r3.setParent(linkName, parent);
        
        Record r4 = space.newRecord();
        r4.setField("String", "4");
        r4.setField("Integer", 4);
        r4.setField("BigDecimal", new BigDecimal("4.4"));
        r4.setParent(linkName, parent);
    }
    
    public static long doCreateRecordForDelete(Space space, String rootChildrenLinkName, String rootParentLinkName, String selfLinkName)
    {
        Record record = space.newRecord();
        Record root = space.getRecord(0);
        long id = record.getId();
        
        doCreateChildren(space, id, "Children");
        doSelfLink(space, id, selfLinkName);
        
        record.setParent(rootChildrenLinkName, root);
        root.setParent(rootParentLinkName, record);
        
        return id;
    }
    
    public static void doRemoveAllChildren(Space space, long parentId, String linkName)
    {
        Record record = space.getRecord(parentId);
        record.removeChildren(linkName);
    }
    
    public static void doRemoveChildrenByIterator(Space space, long parentId, String linkName)
    {
        Record record = space.getRecord(parentId);
        Iterator<Record> iterator = record.getChildren(linkName).iterator();
        while(iterator.hasNext())
        {
            iterator.next();
            iterator.remove();
        }
    }
    
    public static void doRemoveSelfLink(Space space, long recordId, String linkName)
    {
        Record record = space.getRecord(recordId);
        record.setParent(linkName, null);
    }
    
    public static void doSelfLink(Space space, long recordId, String linkName)
    {
        Record record = space.getRecord(recordId);
        record.setParent(linkName, record);
    }
    
    public static long doSetParent(Space space, long parentId, String linkName)
    {
        Record newRecord = space.newRecord();
        newRecord.setParent(linkName, space.getRecord(parentId));
        return newRecord.getId();
    }
    
    public static void doClearParent(Space space, long childId, String linkName)
    {
        Record child = space.getRecord(childId);
        child.setParent(linkName, null);
    }
    
    public static void check_Siblings_Asc(Space space, long parentId, String linkName) {
       Record parent = space.getRecord(parentId);
        
        // Gets 2rd record
       Record from = getAt(1, parent.getChildren(linkName).iterator());
       
       // Gets from iterator
       Iterator<Record> fromIterator = from.getSiblings(linkName).iterator();
       checkChildrenOrder(fromIterator, new int[] {3, 4});
    }
    
    public static void check_Siblings_Desc(Space space, long parentId, String linkName) {
        Record parent = space.getRecord(parentId);
        
        // Gets 2rd record
       Record from = getAt(1, parent.getChildren(linkName).iterator());
       
       // Gets from iterator
       Iterator<Record> fromIterator = from.getSiblings(linkName, Order.DESC).iterator();
       checkChildrenOrder(fromIterator, new int[] {1});
    }
    
    public static Record getAt(long index, Iterator<Record> iterator) {
        Record record = null;
        
        for (long i = 0; i <= index; i++) {
            record = iterator.next();
        }
        
        return record;
    }
}
