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

import static com.vyhodb.space.index.utils.IndexUtils.compare;
import static com.vyhodb.space.index.utils.IndexUtils.doCreateUniqueChildren;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.CriterionFactory;
import com.vyhodb.space.Nullable;
import com.vyhodb.space.Record;
import com.vyhodb.space.Unique;
import com.vyhodb.space.index.utils.IndexUtils;

/**
 * This test is created as a result of critical bug which relates to flow in concatenate/redistribute
 * elements inside index pages.
 * 
 * @author ivykhodtsev
 *
 */
public class IndexBug1Tests extends AbstractStorageTests {
    
    /**
     * Tests do the following:
     * 1. Creates index on large unique set.
     * 2. Removes from index the first 5676 records (by setting null as parent).
     * 3. Adds removed records back.
     * 
     * It must test how elements and pages inside index are allocated and distributed during large modifications.
     * 
     */
    @Test
    public void testCreate_Unique_AfterChildren()
    {
        final int set_size = 5676;
        final String linkName = "1";
        final String fieldName = "I";
        final String indexName = "Bug 1 Index";
        
        // Create children and index
        TrxSpace space = _storage.startModifyTrx();
        try
        {
            Record parent = space.newRecord();
            long parentId = parent.getId();
            TreeSet<Integer> elements = doCreateUniqueChildren(space, parentId, linkName, fieldName, IndexUtils.LARGE_SET_SIZE);
            parent.createIndex(IndexTests.newDescriptor(indexName, linkName, fieldName, Unique.UNIQUE, Nullable.NULL));
            space.commit();
            
            ArrayList<Record> recordSet = new ArrayList<>(set_size);
            
            space = _storage.startModifyTrx();
            parent = space.getRecord(parentId);
            Record record;
            Iterator<Record> children = parent.searchChildren(indexName, CriterionFactory.all()).iterator();
            for (int i = 0; i < set_size; i++) {
                record = children.next();
                recordSet.add(record);
            }
            
            // Clears and removes records from index
            for (int i = set_size - 1; i <= 130; i--) {
                recordSet.get(i).setParent(linkName, null);
            }
            // Removes records back
            for (int i = 130; i < set_size; i++) {
                recordSet.get(i).setParent(linkName, parent);
            }
            
            compare(fieldName, parent.searchChildren(indexName, CriterionFactory.all()).iterator(), elements.iterator());
            
            space.commit();
        }
        finally {
            space.rollback();
        }
    }

}
