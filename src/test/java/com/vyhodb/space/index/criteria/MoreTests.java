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

import org.junit.Test;

import com.vyhodb.space.Order;
import com.vyhodb.space.criteria.More;

public class MoreTests extends AbstractSearchCriteriaTests {

    public static final Integer KEY_EXISTED = 821495276;
    public static final Integer KEY_NOT_EXISTED = 821495280;
    public static final Integer KEY_MIN = Integer.MIN_VALUE;
    public static final Integer KEY_MAX = Integer.MAX_VALUE;
    
    @Test
    public void test_Existed_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new More(KEY_EXISTED), moreTestData.iterator());
    }
    
    @Test
    public void test_Existed_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new More(KEY_EXISTED), moreTestData.descendingIterator());
    }
    
    @Test
    public void test_NotExisted_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new More(KEY_NOT_EXISTED), moreTestData.iterator());
    }
    
    @Test
    public void test_NotExisted_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new More(KEY_NOT_EXISTED), moreTestData.descendingIterator());
    }
    
    @Test
    public void test_Max_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new More(KEY_MAX), EMPTY_TEST_DATA.iterator());
    }
    
    @Test
    public void test_Max_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new More(KEY_MAX), EMPTY_TEST_DATA.descendingIterator());
    }
    
    @Test
    public void test_Min_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new More(KEY_MIN), allNoNullTestData.iterator());
    }
    
    @Test
    public void test_Min_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new More(KEY_MIN), allNoNullTestData.descendingIterator());
    }

}
