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

import com.vyhodb.f.F;
import com.vyhodb.f.Predicate;
import com.vyhodb.utils.Utils;

public class ComparePredicate extends Predicate {

    private static final long serialVersionUID = -4358899191480913667L;

    private ComparisonType _comparisonType;
    private F[] _values;

    @Deprecated
    public ComparePredicate() {
    }

    public ComparePredicate(ComparisonType comparisonType, F[] values) {
        if (values == null) {
            throw new IllegalArgumentException("[values] is null");
        }

        if (values.length < 2) {
            throw new IllegalArgumentException("[values] length must be >= 2");
        }

        _values = values;
        _comparisonType = comparisonType;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Boolean evalTree(Object current, Map<String, Object> context) {
        Comparable next;
        Comparable prev = (Comparable) _values[0].evalTree(current, context);

        for (int i = 1; i < _values.length; i++) {
            next = (Comparable) _values[i].evalTree(current, context);
            if (isSatisfy(prev, next)) {
                prev = next;
            } else {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings({ "rawtypes" })
    private boolean isSatisfy(Comparable prev, Comparable next) {
        int compResult = Utils.compare(prev, next);

        switch (_comparisonType) {
        case LESS:
            return compResult < 0;

        case MORE:
            return compResult > 0;

        case LESS_EQUAL:
            return compResult <= 0;

        case MORE_EQUAL:
            return compResult >= 0;
        }

        throw new IllegalStateException("Unknown comparison type:" + _comparisonType);
    }
}
