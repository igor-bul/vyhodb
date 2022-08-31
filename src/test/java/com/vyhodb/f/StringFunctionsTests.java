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

package com.vyhodb.f;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.f.F;
import com.vyhodb.f.Predicate;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.ts.SampleBuilder;

import static com.vyhodb.f.AggregatesFactory.*;
import static com.vyhodb.f.CommonFactory.*;
import static com.vyhodb.f.NavigationFactory.*;
import static com.vyhodb.f.PredicateFactory.*;
import static com.vyhodb.f.RecordFactory.*;
import static com.vyhodb.f.StringFactory.*;

/**
 * All tests calculates sum of activities for Harry Jones employee. 
 * His first name ("Harry") is used for string functions evaluation.
 * 
 * @author ivykhodtsev
 *
 */
public class StringFunctionsTests extends AbstractStorageTests {
    
    private static final BigDecimal RESULT = BigDecimal.valueOf(21.0);
    
    @Test
    public void testStrContains() {
        eval(
                RESULT, 
                strContains("rr", getField("FirstName"))
        );
    }
    
    @Test
    public void testStrEndsWith() {
        eval(
                RESULT, 
                strEndsWith("ry", getField("FirstName"))
        );
    }
    
    @Test
    public void testStrIndex() {
        eval(
                RESULT, 
                equal(c(1), strIndex("ar", getField("FirstName")))
        );
    }
    
    @Test
    public void testStrLastIndex() {
        eval(
                RESULT, 
                equal(c(3), strLastIndex("r", getField("FirstName")))
        );
    }
    
    @Test
    public void testStrLength() {
        eval(
                RESULT, 
                equal(c(5), strLength(getField("FirstName")))
        );
    }
    
    @Test
    public void testStrLowerCase() {
        eval(
                RESULT, 
                equal(c("harry"), strLowerCase(getField("FirstName")))
        );
    }
    
    @Test
    public void testStrStartsWith() {
        eval(
                RESULT, 
                strStartsWith("Ha", getField("FirstName"))
        );
    }
    
    @Test
    public void testStrSub() {
        eval(
                RESULT, 
                equal(c("arry"), strSub(c(1), getField("FirstName")))
        );
    }
    
    @Test
    public void testStrTrim() {
        eval(
                RESULT, 
                equal(c("Harry"), strTrim(getField("FirstName")))
        );
    }
    
    @Test
    public void testStrUppderCase() {
        eval(
                RESULT, 
                equal(c("HARRY"), strUpperCase(getField("FirstName")))
        );
    }
    
    @Test
    public void testStrMatches() {
        eval(
                RESULT, 
                strMatches("Harry", getField("FirstName"))
        );
    }
    
    private void eval(Object expectedValue, Predicate employeePredicate) {
        F f = composite(
                childrenIf("all_employees", employeePredicate,
                        children("activity2employee",
                                sum(getField("Time"))
                        )
                ),
                getSum()
        );
        
        TrxSpace space = _storage.startModifyTrx();
        try {
            long rootId = SampleBuilder.buildAndUpdateWithIndexes(space).getId();
            assertEquals("Wrong result.", expectedValue, f.eval(space.getRecord(rootId)));
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    

}
