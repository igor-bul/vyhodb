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

import static com.vyhodb.f.CollectionFactory.*;
import static com.vyhodb.f.CommonFactory.*;
import static com.vyhodb.f.NavigationFactory.*;
import static com.vyhodb.f.PredicateFactory.*;
import static com.vyhodb.f.RecordFactory.getChildFirst;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.f.F;
import com.vyhodb.f.Predicate;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Record;
import com.vyhodb.ts.SampleBuilder;

public class CollectionTests extends AbstractStorageTests {

    private static final String DEBUG_FLAG = "debug";
    
    @Test
    public void test_Add() {
        ArrayList<Record> employees = new ArrayList<>();
        F f = composite(
                    put("collection", c(employees)),
                    children("all_employees", collectionAdd("collection", current()))
                );
        
        TrxSpace space = _storage.startModifyTrx();
        try {
            long rootId = SampleBuilder.buildAndUpdate(space).getId();
            f.eval(space.getRecord(rootId));
            assertEquals("Wrong collection size after adding elements.", 5, employees.size());
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    @Test
    public void test_Clear() {
        ArrayList<Record> employees = new ArrayList<>();
        F f = composite(
                        put("collection", c(employees)),
                        children("all_employees", collectionAdd("collection", current()))
                    );
        F fclear = composite(
                    put("collection", c(employees)),
                    collectionClear("collection")
                );
        
        TrxSpace space = _storage.startModifyTrx();
        try {
            long rootId = SampleBuilder.buildAndUpdate(space).getId();
            f.eval(space.getRecord(rootId));
            assertEquals("Wrong collection size after adding elements.", 5, employees.size());
            
            fclear.eval(space.getRecord(rootId));
            assertEquals("Wrong collection size after clear.", 0, employees.size());
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    @Test
    public void test_Remove() {
        ArrayList<Record> employees = new ArrayList<>();
        F f = composite(
                put("collection", c(employees)),
                children("all_employees", collectionAdd("collection", current()))
            );
        
        
        F remove = composite(
                    put("collection", c(employees)),
                    collectionRemove("collection", getChildFirst("all_employees"))
                );
        
        TrxSpace space = _storage.startModifyTrx();
        try {
            long rootId = SampleBuilder.buildAndUpdate(space).getId();
            f.eval(space.getRecord(rootId));
            assertEquals("Wrong collection size after adding elements.", 5, employees.size());
            
            remove.eval(space.getRecord(rootId));
            assertEquals("Wrong collection size after removing element.", 4, employees.size());
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    @Test
    public void test_Contains() {
        ArrayList<Record> employees = new ArrayList<>();
        F f = composite(
                put("collection", c(employees)),
                children("all_employees", collectionAdd("collection", current()))
            );
        
        Predicate Fcontains = 
        toBoolean(
                composite(
                    put("collection", c(employees)),
                    collectionContains("collection", getChildFirst("all_employees"))
                )
        );
        
        TrxSpace space = _storage.startModifyTrx();
        try {
            long rootId = SampleBuilder.buildAndUpdate(space).getId();
            f.eval(space.getRecord(rootId));
            assertEquals("Wrong collection size after adding elements.", 5, employees.size());
            assertTrue("Collection does not contain element", (Boolean)Fcontains.eval(space.getRecord(rootId)));
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
//    @Test
//    public void test_Debug_Add() {
//        ArrayList<Record> employees = new ArrayList<>();
//        Predicate f = children("all_employees", debug_add(DEBUG_FLAG, employees, current()));
//        
//        TrxSpace space = _storage.startModifyTrx();
//        try {
//            long rootId = SampleBuilder.buildAndUpdate(space).getId();
//            f.eval(space.getRecord(rootId));
//            assertEquals("Wrong collection size after adding elements.", 5, employees.size());
//        }
//        finally {
//            if (space != null) {
//                space.rollback();
//            }
//        }
//    }
//    
//    @Test
//    public void test_Debug_Remove() {
//        ArrayList<Record> employees = new ArrayList<>();
//        Predicate f = children("all_employees", collectionAdd(employees, current()));
//        Predicate remove = debug_remove(DEBUG_FLAG, employees, getFirst("all_employees"));
//        
//        TrxSpace space = _storage.startModifyTrx();
//        try {
//            long rootId = SampleBuilder.buildAndUpdate(space).getId();
//            f.eval(space.getRecord(rootId));
//            assertEquals("Wrong collection size after adding elements.", 5, employees.size());
//            
//            remove.eval(space.getRecord(rootId));
//            assertEquals("Wrong collection size after removing element.", 4, employees.size());
//        }
//        finally {
//            if (space != null) {
//                space.rollback();
//            }
//        }
//    }
//    
//    @Test
//    public void test_Debug_Contains() {
//        ArrayList<Record> employees = new ArrayList<>();
//        Predicate f = children("all_employees", collectionAdd(employees, current()));
//        Predicate Fcontains = debug_contains(DEBUG_FLAG, employees, getFirst("all_employees"));
//        
//        TrxSpace space = _storage.startModifyTrx();
//        try {
//            long rootId = SampleBuilder.buildAndUpdate(space).getId();
//            f.eval(space.getRecord(rootId));
//            assertEquals("Wrong collection size after adding elements.", 5, employees.size());
//            
//            assertTrue("Collection does not contain element", Fcontains.eval(space.getRecord(rootId)));
//        }
//        finally {
//            if (space != null) {
//                space.rollback();
//            }
//        }
//    }

}
