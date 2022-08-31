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

package com.vyhodb.f.strings;

import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.f.Predicate;

public class StrMatches extends Predicate {

    private static final long serialVersionUID = -6392544969001306571L;
    
    private String _regExpression;
    private F _value;

    @Deprecated
    public StrMatches() {
    }

    public StrMatches(String regExpression, F value) {
        if (regExpression == null) {
            throw new IllegalArgumentException("[regExpression] is null");
        }

        if (value == null) {
            throw new IllegalArgumentException("[value] is null");
        }

        _regExpression = regExpression;
        _value = value;
    }

    @Override
    public Boolean evalTree(Object current, Map<String, Object> context) {
        return ((String) _value.evalTree(current, context)).matches(_regExpression);
    }
}
