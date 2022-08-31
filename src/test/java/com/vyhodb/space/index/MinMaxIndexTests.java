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

import org.junit.Test;

import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Order;
import com.vyhodb.space.Record;
import com.vyhodb.space.criteria.NotNull;
import com.vyhodb.space.index.criteria.AbstractSearchCriteriaTests;

public class MinMaxIndexTests extends AbstractSearchCriteriaTests {

    public static final Integer KEY_MIN = null;
    public static final Integer KEY_MAX = 2147483119;
    
    public static final Integer FIRST_INDEX_ASC = -2147422948;
    public static final Integer FIRST_INDEX_DESC = 2147483119;
    
    @Test
    public void test_searchMin() {
        TrxSpace space = _storage.startReadTrx();
        try {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            
            Record minRecord = parent.searchMinChild(NAME_INDEX_INTEGER_UNIQUE);
            assertEquals("getIndexMin() returned wrong value.", KEY_MIN, minRecord.getField(FIELD_INTEGER));
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_searchMax() {
        TrxSpace space = _storage.startReadTrx();
        try {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            
            Record maxRecord = parent.searchMaxChild(NAME_INDEX_INTEGER_UNIQUE);
            assertEquals("getIndexMax() returned wrong value.", KEY_MAX, maxRecord.getField(FIELD_INTEGER));
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_searchFirst_Asc() {
        TrxSpace space = _storage.startReadTrx();
        try {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            
            Record firstAsc = parent.searchChildrenFirst(NAME_INDEX_INTEGER_UNIQUE, new NotNull(), Order.ASC);
            assertEquals("getIndexFirst() returned wrong value for ASC order.", FIRST_INDEX_ASC, firstAsc.getField(FIELD_INTEGER));
        }
        finally {
            space.rollback();
        }
    }
    
    @Test
    public void test_searchFirst_Desc() {
        TrxSpace space = _storage.startReadTrx();
        try {
            Record root = space.getRecord(0L);
            Record parent = root.getChildFirst(ROOT_LINK_NAME);
            
            Record firstDesc = parent.searchChildrenFirst(NAME_INDEX_INTEGER_UNIQUE, new NotNull(), Order.DESC);
            assertEquals("getIndexFirst() returned wrong value for DESC order.", FIRST_INDEX_DESC, firstDesc.getField(FIELD_INTEGER));
        }
        finally {
            space.rollback();
        }
    }
}
