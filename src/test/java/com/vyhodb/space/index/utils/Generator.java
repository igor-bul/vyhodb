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

package com.vyhodb.space.index.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class Generator {

    public static final int UNIQUE_SET_SIZE = 256 * 256 + 1000 + 1; // 1 - for null
    public static final int DUBLICATES = 3;
    public static final int DUBLICATE_SET_UNIQUE_ITEMS = UNIQUE_SET_SIZE / DUBLICATES;
    public static final int DUBLICATE_SET_SIZE = DUBLICATE_SET_UNIQUE_ITEMS * 3;
    
    public static final String INTEGER_UNIQUE_UNSORTED = "integer.unique.unsorted.txt";
    public static final String INTEGER_UNIQUE_SORTED = "integer.unique.sort.txt";
    public static final String INTEGER_DUBLICATE_UNSORTED = "integer.dublicate.unsorted.txt";
    public static final String INTEGER_DUBLICATE_SORTED = "integer.dublicate.sort.txt";
    
    public static final String DECIMAL_UNIQUE_UNSORTED = "decimal.unique.unsorted.txt";
    public static final String DECIMAL_UNIQUE_SORTED = "decimal.unique.sort.txt";
    public static final String DECIMAL_DUBLICATE_UNSORTED = "decimal.dublicate.unsorted.txt";
    public static final String DECIMAL_DUBLICATE_SORTED = "decimal.dublicate.sort.txt";
    
    public static final String STRING_UNIQUE_UNSORTED = "string.unique.unsorted.txt";
    public static final String STRING_UNIQUE_SORTED = "string.unique.sort.txt";
    public static final String STRING_DUBLICATE_UNSORTED = "string.dublicate.unsorted.txt";
    public static final String STRING_DUBLICATE_SORTED = "string.dublicate.sort.txt";
    
    public static void main(String[] args) throws IOException {
        generateUniqueSet(INTEGER_UNIQUE_UNSORTED, INTEGER_UNIQUE_SORTED, new IntegerGenerator());
        System.out.println("Integers unique generated");
        
        generateDublicateSet(INTEGER_DUBLICATE_UNSORTED, INTEGER_DUBLICATE_SORTED, new IntegerGenerator());
        System.out.println("Integers dublicate generated");
                
        generateUniqueSet(DECIMAL_UNIQUE_UNSORTED, DECIMAL_UNIQUE_SORTED, new BigDecimalGenerator());
        System.out.println("BigDecimals unique generated");
        
        generateDublicateSet(DECIMAL_DUBLICATE_UNSORTED, DECIMAL_DUBLICATE_SORTED, new BigDecimalGenerator());
        System.out.println("BigDecimals dublicate generated");
        
        generateUniqueSet(STRING_UNIQUE_UNSORTED, STRING_UNIQUE_SORTED, new StringGenerator());
        System.out.println("Strings unique generated");
        
        generateDublicateSet(STRING_DUBLICATE_UNSORTED, STRING_DUBLICATE_SORTED, new StringGenerator());
        System.out.println("Strings dublicate generated");
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void generateUniqueSet(String filename, String sortedFileName, ValueGenerator generator) throws IOException {
        HashSet items = new HashSet(UNIQUE_SET_SIZE);
        
        while (items.size() < UNIQUE_SET_SIZE) {
            items.add(generator.next());
        }
        writeToDataFile(filename, items);
        
        ArrayList sortedItems = new ArrayList<>(items);
        Collections.sort(sortedItems);
        writeToDataFile(sortedFileName, sortedItems);
        
        generator.close();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void generateDublicateSet(String filename, String sortedFileName, ValueGenerator generator) throws IOException {
        HashSet uniqueItems = new HashSet(DUBLICATE_SET_UNIQUE_ITEMS);
        ArrayList items = new ArrayList(DUBLICATE_SET_SIZE);
        
        // Generates unique values
        while(uniqueItems.size() < DUBLICATE_SET_UNIQUE_ITEMS) {
            uniqueItems.add(generator.next());
        }
        // Generates duplicates
        for (int i = 0; i < DUBLICATES; i++) {
            items.addAll(uniqueItems);
        }

        // Writes unsorted values
        writeToDataFile(filename, items);
        
        // Writes sorted values
        Collections.sort(items);
        writeToDataFile(sortedFileName, items);
        
        generator.close();
    }
    
    
    @SuppressWarnings("rawtypes")
    private static void writeToDataFile(String filename, Collection collection) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(filename);
        Iterator iterator = collection.iterator();

        while(iterator.hasNext()) {
            writer.println(iterator.next());
        }
                
        writer.flush();
        writer.close();
    }

}


