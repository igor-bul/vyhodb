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

import java.util.LinkedList;

import org.junit.Test;

import com.vyhodb.space.Order;
import com.vyhodb.space.criteria.In;

public class InTests extends AbstractSearchCriteriaTests {

    private static LinkedList<Integer> searchCollection;
    private static LinkedList<Integer> expectedResultCollection;
    
    static {
        expectedResultCollection = new LinkedList<>();
        expectedResultCollection.add(-1351119584);
        expectedResultCollection.add(-303580092);
        expectedResultCollection.add(801771549);
        expectedResultCollection.add(2075145073);
        
        searchCollection = new LinkedList<>();
        searchCollection.add(801771549);
        searchCollection.add(-303580092);
        searchCollection.add(2075145073);
        searchCollection.add(-1351119584);
        searchCollection.add(11111111);
        searchCollection.add(-1351119584);
    }
    
    @Test
    public void test_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new In(searchCollection), expectedResultCollection.iterator());
    }

    @Test
    public void test_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new In(searchCollection), expectedResultCollection.descendingIterator());
    }
}
