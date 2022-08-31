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

import java.util.HashSet;
import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.f.Predicate;

public class UniquePredicate extends Predicate {

    private static final long serialVersionUID = 5921182490415792957L;

    private F[] _values;

    @Deprecated
    public UniquePredicate() {
    }

    public UniquePredicate(F[] values) {
        if (values == null) {
            throw new IllegalArgumentException("[values] is null");
        }

        if (values.length < 2) {
            throw new IllegalArgumentException("[values] length must be > 1");
        }

        _values = values;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Boolean evalTree(Object current, Map<String, Object> context) {
        Object value;
        HashSet set = new HashSet(_values.length);

        for (int i = 0; i < _values.length; i++) {
            value = _values[i].evalTree(current, context);

            if (set.contains(value)) {
                return false;
            } else {
                set.add(value);
            }
        }

        return true;
    }
}
