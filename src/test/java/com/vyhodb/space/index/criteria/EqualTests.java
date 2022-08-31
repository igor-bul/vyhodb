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

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.Test;

import com.vyhodb.server.TransactionRolledbackException;
import com.vyhodb.space.criteria.Equal;

public class EqualTests extends AbstractSearchCriteriaTests {

    @Test
    public void test_Exists_Unique_Integer() {
        Integer key = 2147483119;
        ArrayList<Integer> expectedResult = new ArrayList<>();
        expectedResult.add(key);
        
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new Equal(key), expectedResult.iterator());
    }
    
    @Test
    public void test_NotExists_Unique_Integer() {
        Integer key = 2222222;
        testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new Equal(key), EMPTY_TEST_DATA.iterator());
    }

    @Test
    public void test_Exists_Unique_String() {
        String key = "ёёфg";
        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add(key);
        
        testCriterion(NAME_INDEX_STRING_UNIQUE, FIELD_STRING, new Equal(key), expectedResult.iterator());
    }
    
    @Test
    public void test_NotExists_Unique_String() {
        String key = "22222222";
        testCriterion(NAME_INDEX_STRING_UNIQUE, FIELD_STRING, new Equal(key), EMPTY_TEST_DATA.iterator());
    }
    
    @Test
    public void test_Exists_Unique_Decimal() {
        BigDecimal key = new BigDecimal("9994913.0531431101262569427490234375");
        ArrayList<BigDecimal> expectedResult = new ArrayList<>();
        expectedResult.add(key);
        
        testCriterion(NAME_INDEX_DECIMAL_UNIQUE, FIELD_DECIMAL, new Equal(key), expectedResult.iterator());
    }
    
    @Test
    public void test_NotExists_Unique_Decimal() {
        BigDecimal key = new BigDecimal("22222222");
        testCriterion(NAME_INDEX_DECIMAL_UNIQUE, FIELD_DECIMAL, new Equal(key), EMPTY_TEST_DATA.iterator());
    }
    
    @Test
    public void test_Wrong_FieldClass() {
        String key = "2147483119";
        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add(key);
        
        try
        {
            testCriterion(NAME_INDEX_INTEGER_UNIQUE, FIELD_INTEGER, new Equal(key), expectedResult.iterator());
            fail("Expected exception has not thrown.");
        } catch (TransactionRolledbackException tre) {
            assertEquals("Wrong exception message.", "Wrong field value class. Field name [I], expected class [java.lang.Integer], actual class [java.lang.String]", tre.getMessage());
        }
    }
}
