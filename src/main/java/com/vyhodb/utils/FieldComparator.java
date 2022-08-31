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

package com.vyhodb.utils;

import java.util.Comparator;

import com.vyhodb.space.Record;

/**
 * Compares two {@linkplain Record} objects using specified fields' values.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class FieldComparator implements Comparator<Record> {

    private String[] _fieldNames;

    /**
     * Deserialization Constructor
     */
    @Deprecated
    public FieldComparator() {
    }

    /**
     * Constructor.
     * 
     * @param fieldNames
     *            field names which participate in comparison
     */
    public FieldComparator(String... fieldNames) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("[fieldNames] is null");
        }

        _fieldNames = fieldNames;
    }

    @SuppressWarnings({ "rawtypes" })
    public int compare(Record o1, Record o2) {
        int result = 0;
        Comparable c1, c2;

        for (String fieldName : _fieldNames) {
            c1 = o1.getField(fieldName);
            c2 = o2.getField(fieldName);
            result = Utils.compare(c1, c2);

            if (result != 0) {
                break;
            }
        }

        return result;
    }
}
