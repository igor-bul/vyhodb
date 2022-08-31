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

import com.vyhodb.space.criteria.StartsWith;

public class StartsWithTests extends AbstractSearchCriteriaTests {

    public static final String PREFIX = "m8";
    public static final String NOT_EXISTED_PREFIX = "IIIIIII";
    
    @Test
    public void test_Existed() {
        testCriterion(NAME_INDEX_STRING_UNIQUE, FIELD_STRING, new StartsWith(PREFIX), startswithTestData.iterator());
    }
    
//    @Test
//    public void test_Insensitive_Existed() {
//        testCriterion(NAME_INDEX_STRING_UNIQUE_INSENSITIVE, FIELD_STRING, new StartsWith(PREFIX), startswithInsensitiveTestData.iterator());
//    }
    
    @Test
    public void test_NotExisted() {
        testCriterion(NAME_INDEX_STRING_UNIQUE, FIELD_STRING, new StartsWith(NOT_EXISTED_PREFIX), EMPTY_TEST_DATA.iterator());
    }
    
//    @Test
//    public void test_Insensitive_NotExisted() {
//        testCriterion(NAME_INDEX_STRING_UNIQUE_INSENSITIVE, FIELD_STRING, new StartsWith(NOT_EXISTED_PREFIX), EMPTY_TEST_DATA.iterator());
//    }
    
}
