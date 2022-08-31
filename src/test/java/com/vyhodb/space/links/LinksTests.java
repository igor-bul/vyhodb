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

import static com.vyhodb.space.RecordCommons.doCreateRecord;
import static com.vyhodb.space.links.LinksCommons.*;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Optional;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.server.TransactionRolledbackException;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Order;
import com.vyhodb.space.Record;

public class LinksTests extends AbstractStorageTests {

    
    @Test
    public void testEmptyChildrenIterator()
    {
        final String linkName = "Not existed link";
        
        TrxSpace space = _storage.startModifyTrx();
        checkEmptyChildrenNextFail(space);
        
        space = _storage.startModifyTrx();
        checkEmptyChildrenRemoveFail(space);
        
        space = _storage.startReadTrx();
        checkEmptyChildren(space, 0, linkName);
        space.rollback();
    }
    
    @Test
    public void testEmptyParent()
    {
        final String linkName = "Not existed link";
        
        TrxSpace space = _storage.startModifyTrx();
        checkEmptyParent(space, 0, linkName);
        space.rollback();
        
        space = _storage.startReadTrx();
        checkEmptyParent(space, 0, linkName);
        space.rollback();
    }
    
    @Test
    public void testIterateAsc()
    {
        TrxSpace space = _storage.startModifyTrx();
        doCreateChildren(space, 0, "rootAsc");
        checkGetChildrenAsc(space, "rootAsc");
        space.commit();
        
        space = _storage.startReadTrx();
        checkGetChildrenAsc(space, "rootAsc");
        space.rollback();
    }
    
    @Test
    public void testIterateDesc()
    {
        TrxSpace space = _storage.startModifyTrx();
        doCreateChildren(space, 0, "rootDesc");
        checkGetChildrenDesc(space, "rootDesc");
        space.commit();
        
        space = _storage.startReadTrx();
        checkGetChildrenDesc(space, "rootDesc");
        space.rollback();
    }
    
    @Test
    public void testChildrenCount()
    {
        TrxSpace space = _storage.startModifyTrx();
        doCreateChildren(space, 0, "testCount");
        checkChildrenCount(space, 0, 4, "testCount");
        space.commit();
        
        space = _storage.startReadTrx();
        checkChildrenCount(space, 0, 4, "testCount");
        space.rollback();
    }
    
    @Test
    public void testRemoveChildrenByIterator()
    {
        final String linkName = "removeChildrenByIterator";
        
        // Create and remove in one transaction
        TrxSpace space = _storage.startModifyTrx();
        doCreateChildren(space, 0, linkName);
        doRemoveChildrenByIterator(space, 0, linkName);
        checkChildrenCount(space, 0, 0, linkName);
        space.commit();
        
        // Create in one trx
        space = _storage.startModifyTrx();
        doCreateChildren(space, 0, linkName);
        space.commit();
        // Remove in another
        space = _storage.startModifyTrx();
        doRemoveChildrenByIterator(space, 0, linkName);
        space.commit();
        
        // Check in read transaction
        space = _storage.startReadTrx();
        checkChildrenCount(space, 0, 0, linkName);
        space.commit();
    }
    
    @Test
    public void testRemoveAllChildren()
    {
        final String linkName = "removeAllChildren";
        TrxSpace space = _storage.startModifyTrx();
        doCreateChildren(space, 0, linkName);
        space.commit();
        
        space = _storage.startModifyTrx();
        Record root = space.getRecord(0);
        root.removeChildren(linkName);
        checkChildrenCount(space, 0, 0, linkName);
        space.commit();
    }
    
    @Test
    public void testCreateSelfLink()
    {
        final String linkName = "Self Link";
        
        TrxSpace space = _storage.startModifyTrx();
        long recordId = doCreateRecord(space);
        doSelfLink(space, recordId, linkName);
        checkSelfLink(space, recordId, linkName);
        space.commit();
        
        space = _storage.startReadTrx();
        checkSelfLink(space, recordId, linkName);
        space.commit();
    }
    
    @Test
    public void testRemoveSelfLink()
    {
        final String linkName = "Self Link";
        
        TrxSpace space = _storage.startModifyTrx();
        long recordId = doCreateRecord(space);
        doSelfLink(space, recordId, linkName);
        space.commit();
        
        space = _storage.startModifyTrx();
        doRemoveSelfLink(space, recordId, linkName);
        space.commit();
        
        space = _storage.startReadTrx();
        checkEmptyParent(space, recordId, linkName);
        checkEmptyChildren(space, recordId, linkName);
        space.commit();
    }
    
    @Test
    public void testDeleteRecordWithLinks()
    {
        final String root2deleted = "root2deleted";
        final String deleted2root = "deleted2root";
        final String selfLink = "deleted2deleted";
        
        // Creates record with links for deletion
        TrxSpace space = _storage.startModifyTrx();
        long id = doCreateRecordForDelete(space, root2deleted, deleted2root, selfLink);
        space.commit();
        
        // Deletes record
        space = _storage.startModifyTrx();
        space.getRecord(id).delete();
        space.commit();
        
        // Checks deletion
        space = _storage.startReadTrx();
        checkDeleteRecord(space, id, root2deleted, deleted2root);
        space.rollback();
    }
    
    @Test
    public void test_Iterator_ConcurrentModification_Remove_Children()
    {
        TrxSpace space = _storage.startModifyTrx();
        try {
            do_Iterator_ConcurrentModification_Remove_Children(space);
            fail("TransactionRolledbackException expected.");
        } catch(TransactionRolledbackException ex) {
            assertEquals("Wrong exception message.",  "Children concurrent modification", ex.getMessage());
        }
    }
    
    @Test
    public void test_Iterator_ConcurrentModification_Delete_Parent()
    {
        TrxSpace space = _storage.startModifyTrx();
        try {
            do_Iterator_ConcurrentModification_Delete_Parent(space);
            fail("TransactionRolledbackException expected.");
        } catch(TransactionRolledbackException ex) {
            assertEquals("Wrong exception message.",  "Parent record has been deleted.", ex.getMessage());
        }
    }
    
    @Test
    public void testGetChildrenParentNames()
    {
        String linkName = "Get Children Parent";
        
        // Create test data
        TrxSpace space = _storage.startModifyTrx();
        long parentId = doCreateRecord(space);
        doCreateChildren(space, parentId, linkName);
        // we get empty iterator below to check clearing empty children at commit
        Iterator<Record> empty = space.getRecord(parentId).getChildren("Not existed").iterator();
        space.commit();
        
        // Check
        space = _storage.startReadTrx();
        checkChildrenNames(space, parentId, linkName);
        checkParentNames(space, parentId, linkName);
        space.rollback();
    }
    
    @Test
    public void testSetParent()
    {
        String parentLinkName = "Set Parent";
        
        // Creates new record and sets link to root
        TrxSpace space = _storage.startModifyTrx();
        long id = doSetParent(space, 0, parentLinkName);
        space.commit();
        
        // Checks link
        space = _storage.startReadTrx();
        Record record = space.getRecord(id);
        Record parent = record.getParent(parentLinkName);
        assertNotNull(parent);
        space.rollback();
    }
    
    @Test
    public void testClearParent()
    {
        String parentLinkName = "Clear Parent";
        
        // Creates new record and sets link to root
        TrxSpace space = _storage.startModifyTrx();
        doSetParent(space, 0, parentLinkName);
        long childId = doSetParent(space, 0, parentLinkName);
        doSetParent(space, 0, parentLinkName);  // Do this to test removing "intermediate" child, not the last one
        space.commit();
        
        // Clears parent
        space = _storage.startModifyTrx();
        doClearParent(space, childId, parentLinkName);
        space.commit();
        
        // Checks
        space = _storage.startReadTrx();
        checkEmptyParent(space, childId, parentLinkName);
        space.rollback();
    }
    
    @Test
    public void test_Siblings_Asc() {
        String linkName = "test_Siblings_Asc";
        
        TrxSpace space = _storage.startModifyTrx();
        doCreateChildren(space, 0, linkName);
        check_Siblings_Asc(space, 0, linkName);
        space.commit();
        
        space = _storage.startReadTrx();
        check_Siblings_Asc(space, 0, linkName);
        space.rollback();
    }
    
    @Test
    public void test_Siblings_Desc() {
        String linkName = "test_Siblings_Desc";
        
        TrxSpace space = _storage.startModifyTrx();
        doCreateChildren(space, 0, linkName);
        check_Siblings_Desc(space, 0, linkName);
        space.commit();
        
        space = _storage.startReadTrx();
        check_Siblings_Desc(space, 0, linkName);
        space.rollback();
    }
    
    @Test
    public void test_Siblings_Remove_Asc() {
        String linkName = "test_Siblings_Remove_Asc";
        
        TrxSpace space = _storage.startModifyTrx();
        doCreateChildren(space, 0, linkName);
        
        Record child = LinksCommons.getAt(1, space.getRecord(0L).getChildren(linkName).iterator());
        Iterator<Record> siblings = child.getSiblings(linkName, Order.ASC).iterator();
        siblings.next();
        siblings.remove();
        siblings.next();
        siblings.remove();
        
        LinksCommons.checkChildrenOrder(space.getRecord(0L).getChildren(linkName).iterator(), new int[]{1, 2});
        space.commit();
    }
    
    @Test
    public void test_Siblings_Remove_Desc() {
        String linkName = "test_Siblings_Remove_Desc";
        
        TrxSpace space = _storage.startModifyTrx();
        doCreateChildren(space, 0, linkName);
        
        Record child = LinksCommons.getAt(2, space.getRecord(0L).getChildren(linkName).iterator());
        Iterator<Record> siblings = child.getSiblings(linkName, Order.DESC).iterator();
        siblings.next();
        siblings.remove();
        siblings.next();
        siblings.remove();
        
        LinksCommons.checkChildrenOrder(space.getRecord(0L).getChildren(linkName).iterator(), new int[]{3, 4});
        space.commit();
    }
    
    @Test
    public void test_getFirst() {
        String linkName = "test_getFirst";
        String notExistedLinkName = "Not Existed link";
        
        TrxSpace space = _storage.startModifyTrx();
        try {
            doCreateChildren(space, 0, linkName);
            
            Record root = space.getRecord(0L);
            Record first = root.getChildFirst(linkName);
            assertNotNull("getFirst() returned null.", first);
            assertEquals("getFirst() returned not the first record.", 1, (int)first.getField("Integer"));
            
            Record notExistedFirst = root.getChildFirst(notExistedLinkName);
            assertNull("getFirst() returned not null record for not existed children link name.", notExistedFirst);
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_getLast() {
        String linkName = "test_getLast";
        String notExistedLinkName = "Not Existed link";
        
        TrxSpace space = _storage.startModifyTrx();
        try {
            doCreateChildren(space, 0, linkName);
            
            Record root = space.getRecord(0L);
            Record last = root.getChildLast(linkName);
            assertNotNull("getLast() returned null.", last);
            assertEquals("getLast() returned not the first record.", 4, (int)last.getField("Integer"));
            
            Record notExistedFirst = root.getChildFirst(notExistedLinkName);
            assertNull("getLast() returned not null record for not existed children link name.", notExistedFirst);
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void testSetParent_prevIsNull() {
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record root = space.getRecord(0L);
            Record prev = root.setParent("test_setParent_prevIsNull", root);
            assertNull("Previous parent record isn't null.", prev);
            
            prev = root.setParent("test_setParent_prevIsNull", root);
            assertNotNull("Previous parent record is null.", prev);
            assertTrue(prev.getId() == 0);
        }
        finally {
            space.rollback();
        }
    }
    
}
