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

public class StrEndsWith extends Predicate {

    private static final long serialVersionUID = -2156471778737032182L;
    
    private F _stringValue;
    private String _suffix;

    public StrEndsWith() {
    }

    public StrEndsWith(String suffix, F stringValue) {
        if (suffix == null) {
            throw new IllegalArgumentException("[suffix] is null");
        }

        if (stringValue == null) {
            throw new IllegalArgumentException("[stringValue] is null");
        }

        _suffix = suffix;
        _stringValue = stringValue;
    }

    @Override
    public Boolean evalTree(Object current, Map<String, Object> context) {
        return ((String) _stringValue.evalTree(current, context)).endsWith(_suffix);
    }
}
