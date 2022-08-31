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

import static com.vyhodb.f.CommonFactory.c;
import static com.vyhodb.f.CommonFactory.nil;
import static com.vyhodb.f.PredicateFactory.and;
import static com.vyhodb.f.PredicateFactory.equal;
import static com.vyhodb.f.PredicateFactory.everyChild;
import static com.vyhodb.f.PredicateFactory.everySearch;
import static com.vyhodb.f.PredicateFactory.falseF;
import static com.vyhodb.f.PredicateFactory.isNotNull;
import static com.vyhodb.f.PredicateFactory.isNull;
import static com.vyhodb.f.PredicateFactory.less;
import static com.vyhodb.f.PredicateFactory.lessEqual;
import static com.vyhodb.f.PredicateFactory.more;
import static com.vyhodb.f.PredicateFactory.moreEqual;
import static com.vyhodb.f.PredicateFactory.not;
import static com.vyhodb.f.PredicateFactory.or;
import static com.vyhodb.f.PredicateFactory.someChildren;
import static com.vyhodb.f.PredicateFactory.someSearch;
import static com.vyhodb.f.PredicateFactory.toBoolean;
import static com.vyhodb.f.PredicateFactory.trueF;
import static com.vyhodb.f.RecordFactory.getField;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.f.Predicate;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.IndexDescriptor;
import com.vyhodb.space.IndexedField;
import com.vyhodb.space.Nullable;
import com.vyhodb.space.Record;
import com.vyhodb.space.Space;
import com.vyhodb.space.criteria.Equal;

public class PredicateTests extends AbstractStorageTests {

    private static final String DEBUG_FLAG = "debug";
    
    @Test
    public void test_toBoolean() {
        evalPredicate("Wrong result.", false, toBoolean(nil()));
        evalPredicate("Wrong result.", false, toBoolean(c(0)));
        evalPredicate("Wrong result.", true, toBoolean(c(10)));
        evalPredicate("Wrong result.", true, toBoolean(c(-10)));
        evalPredicate("Wrong result.", true, toBoolean(c(true)));
        evalPredicate("Wrong result.", false, toBoolean(c(false)));
        evalPredicate("Wrong result.", false, toBoolean(c("True")));
    }
    
    @Test
    public void test_trueF_falseF() {
        evalPredicate("Wrong result.", true, trueF());
        evalPredicate("Wrong result.", false, falseF());
    }
    
    @Test
    public void test_and() {
        evalPredicate("Wrong result.", true, and(trueF(), trueF(), trueF()));
        evalPredicate("Wrong result.", false, and(falseF(), trueF(), trueF()));
    }
    
    @Test
    public void test_or() {
        evalPredicate("Wrong result.", true, or(falseF(), falseF(), trueF()));
        evalPredicate("Wrong result.", false, or(falseF(), falseF(), falseF()));
    }
    
    @Test
    public void test_not() {
        evalPredicate("Wrong result.", true, not(falseF()));
        evalPredicate("Wrong result.", false, not(trueF()));
    }
    
    @Test
    public void test_isNull() {
        evalPredicate("Wrong result.", true, isNull(nil()));
        evalPredicate("Wrong result.", false, isNull(c("Not null")));
    }
    
    @Test
    public void test_isNotNull() {
        evalPredicate("Wrong result.", false, isNotNull(nil()));
    }
    
    @Test
    public void test_equal() {
        evalPredicate("Wrong result.", true, equal(nil(), nil(), nil()));
        evalPredicate("Wrong result.", true, equal(c("Test"), c("Test"), c("Test")));
        evalPredicate("Wrong result.", false, equal(c("Test"), c("Test"), c("Not Test")));
        evalPredicate("Wrong result.", false, equal(nil(), c("Test"), c("Test")));
        evalPredicate("Wrong result.", false, equal(c("Test"), c("Test"), nil()));
    }
    
    @Test
    public void test_more() {
        evalPredicate("Wrong result.", true, more(c(10), c(5), c(3), c(-3), nil()));
        evalPredicate("Wrong result.", false, more(nil(), c(10), c(5), c(3), c(-3), nil()));
        evalPredicate("Wrong result.", false, more(nil(), nil()));
        evalPredicate("Wrong result.", false, more(c(10), c(10)));
    }
    
    @Test
    public void test_moreEqual() {
        evalPredicate("Wrong result.", true, moreEqual(c(10), c(5), c(3), c(-3), nil()));
        evalPredicate("Wrong result.", false, moreEqual(nil(), c(10), c(5), c(3), c(-3), nil()));
        evalPredicate("Wrong result.", true, moreEqual(nil(), nil()));
        evalPredicate("Wrong result.", true, moreEqual(c(10), c(10)));
    }
    
    @Test
    public void test_less() {
        evalPredicate("Wrong result.", true, less(nil(), c(-3), c(0), c(5), c(10)));
        evalPredicate("Wrong result.", false, less(nil(), c(-3), c(0), c(5), c(10), nil()));
        evalPredicate("Wrong result.", false, less(nil(), nil()));
        evalPredicate("Wrong result.", false, less(c(10), c(10)));
    }
    
    @Test
    public void test_lessEqual() {
        evalPredicate("Wrong result.", true, lessEqual(nil(), c(-3), c(0), c(5), c(10)));
        evalPredicate("Wrong result.", false, lessEqual(nil(), c(-3), c(0), c(5), c(10), nil()));
        evalPredicate("Wrong result.", true, lessEqual(nil(), nil()));
        evalPredicate("Wrong result.", true, lessEqual(c(10), c(10)));
    }
    
    @Test
    public void test_someChildren() {
        evalChildrenPredicate("Wrong result.", true, someChildren("childrenPredicates", toBoolean(getField("Random"))));
        evalChildrenPredicate("Wrong result.", true, someChildren("childrenPredicates", toBoolean(getField("AllTrue"))));
        evalChildrenPredicate("Wrong result.", false, someChildren("childrenPredicates", toBoolean(getField("AllFalse"))));
    }
    
    @Test
    public void test_everyChild() {
        evalChildrenPredicate("Wrong result.", false, everyChild("childrenPredicates", toBoolean(getField("Random"))));
        evalChildrenPredicate("Wrong result.", true, everyChild("childrenPredicates", toBoolean(getField("AllTrue"))));
        evalChildrenPredicate("Wrong result.", false, everyChild("childrenPredicates", toBoolean(getField("AllFalse"))));
    }
    
    @Test
    public void test_someIndex() {
        evalChildrenPredicate("Wrong result.", true, someSearch("childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("Random"))));
        evalChildrenPredicate("Wrong result.", true, someSearch("childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("AllTrue"))));
        evalChildrenPredicate("Wrong result.", false, someSearch("childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("AllFalse"))));
    }
    
    @Test
    public void test_everyIndex() {
        evalChildrenPredicate("Wrong result.", false, everySearch("childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("Random"))));
        evalChildrenPredicate("Wrong result.", true, everySearch("childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("AllTrue"))));
        evalChildrenPredicate("Wrong result.", false, everySearch("childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("AllFalse"))));
    }
    
   
//    @Test
//    public void test_debug_and() {
//        evalPredicate("Wrong result.", true, debug_and(DEBUG_FLAG, trueF(), trueF(), trueF()));
//        evalPredicate("Wrong result.", false, debug_and(DEBUG_FLAG, falseF(), trueF(), trueF()));
//    }
//    
//    @Test
//    public void test_debug_or() {
//        evalPredicate("Wrong result.", true, debug_or(DEBUG_FLAG, falseF(), falseF(), trueF()));
//        evalPredicate("Wrong result.", false, debug_or(DEBUG_FLAG, falseF(), falseF(), falseF()));
//    }
//    
//    @Test
//    public void test_debug_not() {
//        evalPredicate("Wrong result.", true, debug_not(DEBUG_FLAG, falseF()));
//        evalPredicate("Wrong result.", false, debug_not(DEBUG_FLAG, trueF()));
//    }
//    
//    @Test
//    public void test_debug_isNull() {
//        evalPredicate("Wrong result.", true, debug_isNull(DEBUG_FLAG, nil()));
//        evalPredicate("Wrong result.", false, debug_isNull(DEBUG_FLAG, c("Not null")));
//    }
//    
//    @Test
//    public void test_debug_equal() {
//        evalPredicate("Wrong result.", true, debug_equal(DEBUG_FLAG, nil(), nil(), nil()));
//        evalPredicate("Wrong result.", true, debug_equal(DEBUG_FLAG, c("Test"), c("Test"), c("Test")));
//        evalPredicate("Wrong result.", false, debug_equal(DEBUG_FLAG, c("Test"), c("Test"), c("Not Test")));
//        evalPredicate("Wrong result.", false, debug_equal(DEBUG_FLAG, nil(), c("Test"), c("Test")));
//        evalPredicate("Wrong result.", false, debug_equal(DEBUG_FLAG, c("Test"), c("Test"), nil()));
//    }
//    
//    @Test
//    public void test_debug_more() {
//        evalPredicate("Wrong result.", true, debug_more(DEBUG_FLAG, c(10), c(5), c(3), c(-3), nil()));
//        evalPredicate("Wrong result.", false, debug_more(DEBUG_FLAG, nil(), c(10), c(5), c(3), c(-3), nil()));
//        evalPredicate("Wrong result.", false, debug_more(DEBUG_FLAG, nil(), nil()));
//        evalPredicate("Wrong result.", false, debug_more(DEBUG_FLAG, c(10), c(10)));
//    }
//    
//    @Test
//    public void test_debug_moreEqual() {
//        evalPredicate("Wrong result.", true, debug_moreEqual(DEBUG_FLAG, c(10), c(5), c(3), c(-3), nil()));
//        evalPredicate("Wrong result.", false, debug_moreEqual(DEBUG_FLAG, nil(), c(10), c(5), c(3), c(-3), nil()));
//        evalPredicate("Wrong result.", true, debug_moreEqual(DEBUG_FLAG, nil(), nil()));
//        evalPredicate("Wrong result.", true, debug_moreEqual(DEBUG_FLAG, c(10), c(10)));
//    }
//    
//    @Test
//    public void test_debug_less() {
//        evalPredicate("Wrong result.", true, debug_less(DEBUG_FLAG, nil(), c(-3), c(0), c(5), c(10)));
//        evalPredicate("Wrong result.", false, debug_less(DEBUG_FLAG, nil(), c(-3), c(0), c(5), c(10), nil()));
//        evalPredicate("Wrong result.", false, debug_less(DEBUG_FLAG, nil(), nil()));
//        evalPredicate("Wrong result.", false, debug_less(DEBUG_FLAG, c(10), c(10)));
//    }
//    
//    @Test
//    public void test_debug_lessEqual() {
//        evalPredicate("Wrong result.", true, debug_lessEqual(DEBUG_FLAG, nil(), c(-3), c(0), c(5), c(10)));
//        evalPredicate("Wrong result.", false, debug_lessEqual(DEBUG_FLAG, nil(), c(-3), c(0), c(5), c(10), nil()));
//        evalPredicate("Wrong result.", true, debug_lessEqual(DEBUG_FLAG, nil(), nil()));
//        evalPredicate("Wrong result.", true, debug_lessEqual(DEBUG_FLAG, c(10), c(10)));
//    }
//    
//    @Test
//    public void test_debug_someChildren() {
//        evalChildrenPredicate("Wrong result.", true, debug_someChildren(DEBUG_FLAG, "childrenPredicates", toBoolean(getField("Random"))));
//        evalChildrenPredicate("Wrong result.", true, debug_someChildren(DEBUG_FLAG, "childrenPredicates", toBoolean(getField("AllTrue"))));
//        evalChildrenPredicate("Wrong result.", false, debug_someChildren(DEBUG_FLAG, "childrenPredicates", toBoolean(getField("AllFalse"))));
//    }
//    
//    @Test
//    public void test_debug_everyChild() {
//        evalChildrenPredicate("Wrong result.", false, debug_everyChild(DEBUG_FLAG, "childrenPredicates", toBoolean(getField("Random"))));
//        evalChildrenPredicate("Wrong result.", true, debug_everyChild(DEBUG_FLAG, "childrenPredicates", toBoolean(getField("AllTrue"))));
//        evalChildrenPredicate("Wrong result.", false, debug_everyChild(DEBUG_FLAG, "childrenPredicates", toBoolean(getField("AllFalse"))));
//    }
//    
//    @Test
//    public void test_debug_someIndex() {
//        evalChildrenPredicate("Wrong result.", true, debug_someIndex(DEBUG_FLAG, "childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("Random"))));
//        evalChildrenPredicate("Wrong result.", true, debug_someIndex(DEBUG_FLAG, "childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("AllTrue"))));
//        evalChildrenPredicate("Wrong result.", false, debug_someIndex(DEBUG_FLAG, "childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("AllFalse"))));
//    }
//    
//    @Test
//    public void test_debug_everyIndex() {
//        evalChildrenPredicate("Wrong result.", false, debug_everyIndex(DEBUG_FLAG, "childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("Random"))));
//        evalChildrenPredicate("Wrong result.", true, debug_everyIndex(DEBUG_FLAG, "childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("AllTrue"))));
//        evalChildrenPredicate("Wrong result.", false, debug_everyIndex(DEBUG_FLAG, "childrenPredicates.Name", new Equal("Some value"), toBoolean(getField("AllFalse"))));
//    }
   
    private void evalPredicate(String message, Object expectedValue, Predicate predicate) {
        TrxSpace space = _storage.startModifyTrx();
        try {
            assertEquals(message, expectedValue, predicate.eval(space.getRecord(0L)));
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    private void evalChildrenPredicate(String message, Object expectedValue, Predicate predicate) {
        TrxSpace space = _storage.startModifyTrx();
        try {
            createChildrenPredicateSpace(space.getRecord(0L));
            assertEquals(message, expectedValue, predicate.eval(space.getRecord(0L)));
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    private void createChildrenPredicateSpace(Record root) {
        Space space = root.getSpace();
        
        String childrenLinkName = "childrenPredicates";
        Record[] children = new Record[4];
        
        
        for (int i = 0; i < children.length; i++) {
            children[i] = space.newRecord();
            children[i].setParent(childrenLinkName, root);
            children[i].setField("AllTrue", true);
            children[i].setField("AllFalse", false);
            children[i].setField("Random", true);
            children[i].setField("Name", "Some value");
        }
        
        children[3].setField("Random", false);
        
        IndexDescriptor descriptor = new IndexDescriptor("childrenPredicates.Name", "childrenPredicates", new IndexedField("Name", String.class, Nullable.NULL));
        root.createIndex(descriptor);
    }
}
