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

package com.vyhodb.f.predicates;

import java.util.Map;

import com.vyhodb.utils.Utils;
import com.vyhodb.space.Record;

public class FieldsEqualPredicate extends RecordPredicate {

    private static final long serialVersionUID = -3114022921503457034L;

    private String[] _keyFieldNames;
    private Object[] _keyValues;

    @Deprecated
    public FieldsEqualPredicate() {
    }

    public FieldsEqualPredicate(String[] keyFieldNames, Object[] keyValues) {
        if (keyFieldNames == null) {
            throw new IllegalArgumentException("[keyFieldNames] is null");
        }

        if (keyValues == null) {
            throw new IllegalArgumentException("[keyValues] is null");
        }

        if (keyFieldNames.length != keyValues.length) {
            throw new IllegalArgumentException("[keyValues] and [keyFieldNames] have different length");
        }

        _keyFieldNames = keyFieldNames;
        _keyValues = keyValues;
    }

    @Override
    protected Boolean evalRecord(Record current, Map<String, Object> context) {
        String keyField;
        Object keyValue;
        Object value;

        for (int i = 0; i < _keyFieldNames.length; i++) {
            keyField = _keyFieldNames[i];
            keyValue = _keyValues[i];
            value = current.getField(keyField);

            if (!Utils.equal(keyValue, value)) {
                return false;
            }
        }

        return true;
    }
}
