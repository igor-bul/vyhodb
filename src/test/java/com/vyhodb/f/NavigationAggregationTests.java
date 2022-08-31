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

import static com.vyhodb.f.AggregatesFactory.*;
import static com.vyhodb.f.CommonFactory.*;
import static com.vyhodb.f.NavigationFactory.*;
import static com.vyhodb.f.PredicateFactory.*;
import static com.vyhodb.f.RecordFactory.getField;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.f.F;
import com.vyhodb.f.Predicate;
import com.vyhodb.f.aggregates.SumType;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.criteria.Equal;
import com.vyhodb.space.criteria.More;
import com.vyhodb.ts.SampleBuilder;

public class NavigationAggregationTests extends AbstractStorageTests {

    public static F computeProjectAggregate(Predicate activityPredicate, F aggregateF, F resultF) {
        return
                composite(  
                    children("top_projects", 
                                  hierarchy("parent_project",  
                                              childrenIf("activity2project", activityPredicate,
                                                      aggregateF
                                              )
                                  )
                     ),
                     resultF
                );
    }
    
    public static F activitySum(int employeeNumber, SumType sumType, String activityFieldName) {
        return 
                composite(
                    search("all_employees.Number", new Equal(employeeNumber), 
                            children("activity2employee", 
                                    sum(sumType, getField(activityFieldName))
                            )
                    ),
                    getSum()
                );
    }
    
    @Test
    public void test_All_Activity_Cost_Min() {
        F aggregateF = min(getField("Cost"));
        F resultF = getMin();
        evalF("Wrong min cost.", new BigDecimal("50.3"), computeProjectAggregate(trueF(), aggregateF, resultF));
    }
    
    @Test
    public void test_All_Activity_Cost_Max() {
        F aggregateF = max(getField("Cost"));
        F resultF = getMax();
        evalF("Wrong max cost.", new BigDecimal("503.0"), computeProjectAggregate(trueF(), aggregateF, resultF));
    }
    
    @Test
    public void test_All_Activity_Count() {
        F aggregateF = count();
        F resultF = getCount();
        evalF("Wrong activity count.", Long.valueOf(21), computeProjectAggregate(trueF(), aggregateF, resultF));
    }
    
    @Test
    public void test_All_Activity_Time_Avg() {
        F aggregateF = avg(getField("Time"));
        F resultF = getAvg();
        evalF("Wrong activity avg.", ((double)114/21), computeProjectAggregate(trueF(), aggregateF, resultF));
    }
    
    @Test
    public void test_All_Activity_Time_Sum() {
        Predicate activityPredicate = equal(c("Planning"), parent("activity2activity_type", getField("Name")));
        
        evalF("Wrong time sum for planning activities.", (double) 8, computeProjectAggregate(activityPredicate, sum(SumType.DOUBLE, getField("Time")), getSum()));
        evalF("Wrong time sum for planning activities.", (long) 8, computeProjectAggregate(activityPredicate, sum(SumType.LONG, getField("Time")), getSum()));
        evalF("Wrong time sum for planning activities.", new BigDecimal("8.0"), computeProjectAggregate(activityPredicate, sum(SumType.DECIMAL, getField("Time")), getSum()));
    }
    
    @Test
    public void test_Employee_Sum_Time() {
        evalF("Wrong time sum for employee.", (long)38, activitySum(0, SumType.LONG, "Time"));
    }
    
    @Test
    public void test_Break() {
        F breakHierarchy = 
        composite(
            children("top_projects",
                    hierarchy("parent_project", 
                            _if(fieldsEqual("Name", "Project A. Test"),
                                    children("activity2project",
                                            sum(getField("Time"))
                                    ),
                                    _break()
                            )
                    )
            ),
            getSum()
        );
        
        evalF("_break() doesn't work for children", new BigDecimal("25.0"), breakHierarchy);
        
        F breakIndex = 
        composite(
            search("all_projects.Number", new More(2),
                    _if(fieldsEqual("Name", "Project A. Test"),
                            children("activity2project",
                                    sum(getField("Time"))
                            ),
                            _break()
                    )
            ),
            getSum()
        );
        
        evalF("_break() doesn't work for search", new BigDecimal("25.0"), breakHierarchy);
    }
    
    private void evalF(String message, Object expectedValue, F function) {
        TrxSpace space = _storage.startModifyTrx();
        try {
            long rootId = SampleBuilder.buildAndUpdateWithIndexes(space).getId();
            assertEquals(message, expectedValue, function.eval(space.getRecord(rootId)));
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
}
