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

package com.vyhodb.f.print;

import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.f.Stack;
import com.vyhodb.f.record.RecordF;
import com.vyhodb.space.Record;

public class StartPrintSimple extends RecordF {

    public static final int DEFAULT_TAB_SIZE = 4;
    private static final long serialVersionUID = 6914988395799071915L;

    private String[] _fieldsFilter;
    private F _next;
    private String _stackKey;
    private int _tabSize;

    @Deprecated
    public StartPrintSimple() {
    }

    public StartPrintSimple(String stackKey, String[] fieldsFilter, int tabSize, F next) {
        if (stackKey == null) {
            throw new IllegalArgumentException("[stackKey] is null");
        }

        if (next == null) {
            throw new IllegalArgumentException("[next] is null");
        }

        _stackKey = stackKey;
        _fieldsFilter = fieldsFilter;
        _tabSize = tabSize;
        _next = next;
    }

    public StartPrintSimple(String[] fieldsFilter, F next) {
        this(Stack.DEFAULT_CONTEXT_KEY, fieldsFilter, DEFAULT_TAB_SIZE, next);
    }

    @Override
    public Object evalRecord(Record current, Map<String, Object> context) {
        Stack oldStack = (Stack) context.get(_stackKey);
        SimplePrintStack stack = new SimplePrintStack(current, _fieldsFilter, _tabSize);

        try {
            context.put(_stackKey, stack);
            _next.evalTree(current, context);
            return stack.print();
        } finally {
            context.put(_stackKey, oldStack);
        }
    }
}
