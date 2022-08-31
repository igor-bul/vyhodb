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

package com.vyhodb.f.print;

import static com.vyhodb.f.NavigationFactory.*;
import static com.vyhodb.f.PrintFactory.*;
import static com.vyhodb.space.CriterionFactory.*;
import static org.junit.Assert.*;

import java.io.*;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.f.F;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Record;
import com.vyhodb.utils.DataGenerator;

public class PrintTests extends AbstractStorageTests {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    
    private static String readStrings(Reader reader) throws IOException {
        String readString;
        StringBuilder builder = new StringBuilder();
        
        try (BufferedReader bufReader = new BufferedReader(reader)) {
            while((readString = bufReader.readLine()) != null) {
                builder.append(readString).append(LINE_SEPARATOR);
            }
        }
        return builder.toString();
    }
    
    private static String read(String resourcePath) throws IOException {
        InputStream is = new FileInputStream(resourcePath);
        InputStreamReader reader = new InputStreamReader(is, "UTF-8");
        
        try {
            return readStrings(reader);
        }
        finally {
            is.close();
        }
    }
    
    private static F getNavigation() {
        return 
        search("order2root.Customer", equal("Customer 1"),
                children("item2order",
                        parent("item2product")
                )
        );
    }
    
    private static F getHierarchyNavigation() {
        return 
        children("group2root",
                hierarchy("parent_group",
                        children("product2group")
                )
        );
    }

    @Test
    public void testSimpleHierarchy() throws IOException {
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record root = space.getRecord(0);
            DataGenerator.generateHierarchy(root);
            F print = startPrint(getHierarchyNavigation());
            String result = (String) print.eval(root);
            String expected = read("../../src/test/resources/data/print/simple_hierarchy.txt");

            // Order of fields is not guaranteed that is why this test is for coverage purposes only
            // assertEquals(expected, result);
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    @Test
    public void testSimpleNavigation() throws IOException {
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record root = space.getRecord(0);
            DataGenerator.generate(root);
            F print = startPrint(getNavigation());
            String result = (String) print.eval(root);
            String expected = read("../../src/test/resources/data/print/simple_navigation.txt");

            // Order of fields is not guaranteed that is why this test is for coverage purposes only
            // assertEquals(expected, result);
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    @Test
    public void testSimpleNavigationFilter() throws IOException {
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record root = space.getRecord(0);
            DataGenerator.generate(root);
            F print = startPrint(new String[]{"Customer", "Name", "Cost"}, getNavigation());
            String result = (String) print.eval(root);
            String expected = read("../../src/test/resources/data/print/simple_navigation_fieldfilter.txt");
            
            assertEquals(expected, result);
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    @Test
    public void testJsonHierarchy() throws IOException {
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record root = space.getRecord(0);
            DataGenerator.generateHierarchy(root);
            F print = startPrintJson(getHierarchyNavigation());
            
            String result = (String) print.eval(root);
            result = result + LINE_SEPARATOR;
            
            String expected = read("../../src/test/resources/data/print/json_hierarchy.json");

            // Order of fields is not guaranteed that is why this test is for coverage purposes only
            // assertEquals(expected, result);
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    @Test
    public void testJsonNavigation() throws IOException {
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record root = space.getRecord(0);
            DataGenerator.generate(root);
            F print = startPrintJson(getNavigation());
            
            String result = (String) print.eval(root);
            result = result + LINE_SEPARATOR;
            
            String expected = read("../../src/test/resources/data/print/json_navigation.json");
            // Order of fields is not guaranteed that is why this test is for coverage purposes only
            // assertEquals(expected, result);
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    @Test
    public void testJsonNavigationFilter() throws IOException {
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record root = space.getRecord(0);
            DataGenerator.generate(root);
            F print = startPrintJson(new String[]{"Customer", "Name", "Cost"}, getNavigation());
            
            String result = (String) print.eval(root);
            result = result + LINE_SEPARATOR;
            
            String expected = read("../../src/test/resources/data/print/json_navigation_fieldfilter.json");
            assertEquals(expected, result);
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    @Test
    public void testJsonNavigationFilterUnformatted() throws IOException {
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record root = space.getRecord(0);
            DataGenerator.generate(root);
            F print = startPrintJson(new String[]{"Customer", "Name", "Cost"}, false, getNavigation());
            
            String result = (String) print.eval(root);
            result = result + LINE_SEPARATOR;
            
            String expected = read("../../src/test/resources/data/print/json_navigation_fieldfilter_unformatted.json");
            assertEquals(expected, result);
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
}
