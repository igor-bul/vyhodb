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

package com.vyhodb.f.aggregates;

import java.util.Comparator;
import java.util.Map;

import com.vyhodb.f.F;

public class MinMax extends F {

    private static final long serialVersionUID = 1429052007457852148L;

    @SuppressWarnings("rawtypes")
    private Comparator _comparator;
    private String _contextKey;
    private MinMaxType _type;
    private F _value;

    @Deprecated
    public MinMax() {
    }

    public MinMax(MinMaxType type, String contextKey, Comparator<?> comparator, F value) {
        if (type == null) {
            throw new IllegalArgumentException("[type] is null");
        }

        if (contextKey == null) {
            throw new IllegalArgumentException("[contextKey] is null");
        }

        if (value == null) {
            throw new IllegalArgumentException("[value] is null");
        }

        _type = type;
        _contextKey = contextKey;
        _comparator = comparator;
        _value = value;
    }

    @Override
    public Object evalTree(Object current, Map<String, Object> context) {
        Object minmax = context.get(_contextKey);
        Object value = _value.evalTree(current, context);

        if (value != null && (minmax == null || isSatisfy(value, minmax))) {
            minmax = value;
        }

        context.put(_contextKey, minmax);
        return value;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean isSatisfy(Object value, Object minmax) {
        int compResult = (_comparator == null) ? ((Comparable) value).compareTo(minmax) : _comparator.compare(value, minmax);

        switch (_type) {
        case MIN:
            return compResult < 0;

        case MAX:
            return compResult > 0;
        }

        throw new IllegalArgumentException("Unknown comparison type:" + _type);
    }

}
