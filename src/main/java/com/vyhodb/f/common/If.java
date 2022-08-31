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

package com.vyhodb.f.common;

import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.f.Predicate;

public class If extends F {

    private static final long serialVersionUID = -5532140020705882802L;
    private Predicate _condition;
    private F _false;
    private F _true;

    @Deprecated
    public If() {
    }

    public If(Predicate condition, F trueF, F falseF) {
        if (condition == null) {
            throw new IllegalArgumentException("[condition] is null");
        }

        if (trueF == null) {
            throw new IllegalArgumentException("[trueF] is null");
        }

        if (falseF == null) {
            throw new IllegalArgumentException("[falseF] is null");
        }

        _condition = condition;
        _true = trueF;
        _false = falseF;
    }

    @Override
    public Object evalTree(Object current, Map<String, Object> context) {
        if (_condition.evalTree(current, context)) {
            return _true.evalTree(current, context);
        } else {
            return _false.evalTree(current, context);
        }
    }

}
