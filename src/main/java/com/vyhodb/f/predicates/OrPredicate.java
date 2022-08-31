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

import com.vyhodb.f.Predicate;

public class OrPredicate extends Predicate {

    private static final long serialVersionUID = -3821734361409035093L;

    private Predicate[] _predicates;

    @Deprecated
    public OrPredicate() {
    }

    public OrPredicate(Predicate[] predicates) {
        if (predicates == null) {
            throw new IllegalArgumentException("[predicates] is null");
        }

        if (predicates.length < 2) {
            throw new IllegalArgumentException("[predicates] length must be > 1");
        }

        _predicates = predicates;
    }

    @Override
    public Boolean evalTree(Object current, Map<String, Object> context) {
        for (int i = 0; i < _predicates.length; i++) {
            if (_predicates[i].evalTree(current, context)) {
                return true;
            }
        }

        return false;
    }
}
