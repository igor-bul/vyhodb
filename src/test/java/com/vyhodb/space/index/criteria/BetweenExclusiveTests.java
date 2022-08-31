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

import static com.vyhodb.space.CriterionFactory.betweenExclusive;

import org.junit.Test;

public class BetweenExclusiveTests extends AbstractSearchCriteriaTests {

    public static final Integer FROM_EXISTED = -905174250;
    public static final Integer FROM_NOT_EXISTED_BORDER = -905148643;
    public static final Integer TO_EXISTED = 821606066;
    public static final Integer TO_NOT_EXISTED_BORDER = 821514624;
    
    public static final Integer FROM_EMPTY_INTERVAL = -5;
    public static final Integer TO_EMPTY_INTERVAL = 5;
    
    public static final Integer KEY_MIN = Integer.MIN_VALUE;
    public static final Integer KEY_MAX = Integer.MAX_VALUE;
    
    @Test
    public void test_Existed_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(FROM_EXISTED, TO_EXISTED), betweenTestData.iterator());
    }
    
    @Test
    public void test_Existed_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(FROM_EXISTED, TO_EXISTED), betweenTestData.descendingIterator());
    }
    
    @Test
    public void test_NotExistedBorders_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(FROM_NOT_EXISTED_BORDER, TO_NOT_EXISTED_BORDER), betweenTestData.iterator());
    }
    
    @Test
    public void test_NotExistedBorders_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(FROM_NOT_EXISTED_BORDER, TO_NOT_EXISTED_BORDER), betweenTestData.descendingIterator());
    }
    
    // -----------------------------
    
    @Test
    public void test_SameBorders_Existed_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(FROM_EXISTED, FROM_EXISTED), EMPTY_TEST_DATA.iterator());
    }
    
    @Test
    public void test_SameBorders_Existed_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(FROM_EXISTED, FROM_EXISTED), EMPTY_TEST_DATA.descendingIterator());
    }
    
    //-----------------------------
    
    @Test
    public void test_SameBorders_NotExisted_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(FROM_NOT_EXISTED_BORDER, FROM_NOT_EXISTED_BORDER), EMPTY_TEST_DATA.iterator());
    }
    
    @Test
    public void test_SameBorders_NotExisted_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(FROM_NOT_EXISTED_BORDER, FROM_NOT_EXISTED_BORDER), EMPTY_TEST_DATA.descendingIterator());
    }
    
    @Test
    public void test_Empty_Interval_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(FROM_EMPTY_INTERVAL, TO_EMPTY_INTERVAL), EMPTY_TEST_DATA.iterator());
    }
    
    @Test
    public void test_Empty_Interval_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(FROM_EMPTY_INTERVAL, TO_EMPTY_INTERVAL), EMPTY_TEST_DATA.descendingIterator());
    }
    
    //-----------------------------
    
    @Test
    public void test_All_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(KEY_MIN, KEY_MAX), allNoNullTestData.iterator());
    }
    
    @Test
    public void test_All_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(KEY_MIN, KEY_MAX), allNoNullTestData.descendingIterator());
    }
    
    // ----------------------------
    
    @Test
    public void test_Min_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(KEY_MIN, LessTests.KEY_EXISTED), lessTestData.iterator());
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(KEY_MIN, LessTests.KEY_NOT_EXISTED), lessTestData.iterator());
    }
    
    @Test
    public void test_Min_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(KEY_MIN, LessTests.KEY_EXISTED), lessTestData.descendingIterator());
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(KEY_MIN, LessTests.KEY_NOT_EXISTED), lessTestData.descendingIterator());
    }
    
    @Test
    public void test_Max_Asc() {
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(MoreTests.KEY_EXISTED, KEY_MAX), moreTestData.iterator());
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(MoreTests.KEY_NOT_EXISTED, KEY_MAX), moreTestData.iterator());
    }
    
    @Test
    public void test_Max_Desc() {
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(MoreTests.KEY_EXISTED, KEY_MAX), moreTestData.descendingIterator());
        testCriterionDesc(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, betweenExclusive(MoreTests.KEY_NOT_EXISTED, KEY_MAX), moreTestData.descendingIterator());
    }
}
