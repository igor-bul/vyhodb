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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TreeSet;

import com.vyhodb.space.Criterion;
import com.vyhodb.space.Record;
import com.vyhodb.space.Space;
import com.vyhodb.space.criteria.All;
import com.vyhodb.space.criteria.Equal;
import com.vyhodb.space.criteria.EqualComposite;

public class IndexUtils {

    private static final NUllComparator NULL_COMPARATOR = new NUllComparator();
    
    public static final int SMALL_SET_SIZE = 26;
    public static final int LARGE_SET_SIZE = 1024*1024 + 1000; //256 * 256 * 2 + 1000; //256*256 + 1000;//IndexRoot.M * IndexRoot.M + 1000;
    
    public static final long RANDOM_SEED = 123456L;
    
    public static TreeSet<Integer> doCreateUniqueChildren(Space space, long parentId, String linkName, String fieldName, int initialSize)
    {
        HashSet<Integer> set = new HashSet<Integer>();
        Record child = null;
        Record parent = space.getRecord(parentId);
        Random random = new Random(RANDOM_SEED);
        
        for (int i = 0; i < initialSize; i++) {
            set.add(random.nextInt());
        }
        
        for (Integer integer : set) {
            child = space.newRecord();
            child.setField(fieldName, integer);
            child.setParent(linkName, parent);
        }
        
        return new TreeSet<>(set);
    }
    
    public static List<Integer> doCreateUniqueChildren0(Space space, long parentId, String linkName, String fieldName, int initialSize)
    {
        LinkedList<Integer> set = new LinkedList<Integer>();
        Record child = null;
        Record parent = space.getRecord(parentId);
        Random random = new Random(RANDOM_SEED);
        
        for (int i = 0; i < initialSize; i++) {
            set.add(random.nextInt());
        }
        
        for (Integer integer : set) {
            child = space.newRecord();
            child.setField(fieldName, integer);
            child.setParent(linkName, parent);
        }
        
        return set;
    }
    
    public static ArrayList<Integer> doCreateNotUniqueChildren(Space space, long parentId, String linkName, String fieldName, int initialSize)
    {
        final int iterations = initialSize / 2;
        final int size = iterations * 2;
        ArrayList<Integer> list = new ArrayList<>(size);
        int i;
        
        // Fills list
        Random random = new Random(RANDOM_SEED);
        for (i = 0; i < iterations; i++) {
            list.add(random.nextInt());
        }
        random = new Random(RANDOM_SEED);
        for (; i < size; i++) {
            list.add(random.nextInt());
        }
        
        // Sets null, if required
        list.set(0, null);
        list.set(1, null);
        list.set(3, null);
        
        // Creates children
        Record child = null;
        Record parent = space.getRecord(parentId);
        for (Integer integer : list) {
            child = space.newRecord();
            child.setField(fieldName, integer);
            child.setParent(linkName, parent);
        }
        
        // Sorts result
        Collections.sort(list, NULL_COMPARATOR);
        return list;
    }
    
    @SuppressWarnings("rawtypes")
    public static void compare(String fieldName, Iterator<Record> iterator, Iterator values) {
        Comparable value;
        Comparable recordValue;
        Record record;
        while(iterator.hasNext()) {
            value = (Comparable) values.next();
            record = iterator.next();
            recordValue = record.getField(fieldName);
            
            assertEquals("Wrong element in search result. ", value, recordValue);
        }
        
        // To increase unit test coverage
        {
            assertFalse(iterator.hasNext());
            assertFalse(iterator.hasNext());
        }
        
        assertFalse("Test data has more elements, then search result. ", values.hasNext());
    }
    
    public static Integer generateUniqueValue(Record record, String indexName)
    {
        Integer value;
        Random random = new Random();
                
        do
        {
            value = random.nextInt();
        } 
        while (contains(record, indexName, value));
        
        return value;
    }
    
    private static boolean contains(Record parentRecord, String indexName, Comparable value) {
        return (parentRecord.searchChildren(indexName, new Equal(value))).iterator().hasNext();
    }
    
    public static Integer getNotUniqueValue(Record parent, String indexName, String fieldName)
    {
        final int index = 256;
        return getChild(parent, indexName, index).getField(fieldName);
    }
    
    public static Record getChild(Record parent, String indexName, int index)
    {
        Record child = null;
        Iterator<Record> iterator = parent.searchChildren(indexName, new All()).iterator();
        for (int i = 0; i <= index; i++) {
            child = iterator.next();
        }
        
        return child;
    }
    
    public static boolean childExists(Record record, Iterator<Record> records)
    {
        Record current;
        while(records.hasNext())
        {
            current = records.next();
            if (current.equals(record))
                return true;
        }
        
        return false;
    }
    
    @SuppressWarnings("rawtypes")
    public static boolean childExists(Record parent, Record child, String indexName, Comparable value)
    {
        return childExists(parent, child, indexName, new Equal(value));
    }
    
    public static boolean childExists(Record parent, Record child, String indexName, Map<String, ? extends Comparable> keys)
    {
        return childExists(parent, child, indexName, new EqualComposite(keys));
    }
    
    public static boolean childExists(Record parent, Record child, String indexName, Criterion criterion) {
        Iterator<Record> searchResult = parent.searchChildren(indexName, criterion).iterator();
        return childExists(child, searchResult);
    }
    
    @SuppressWarnings("rawtypes")
    private static class NUllComparator implements Comparator
    {

        @SuppressWarnings("unchecked")
        @Override
        public int compare(Object o1, Object o2) {
            Comparable v1 = (Comparable) o1;
            Comparable v2 = (Comparable) o2;
            
            if (v1 == null && v2 == null) return 0;
            if (v1 == null) return -1;
            if (v2 == null) return 1;
            
            return v1.compareTo(v2);
        }
    }

}
