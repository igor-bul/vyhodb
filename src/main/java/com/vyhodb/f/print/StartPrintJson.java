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

public class StartPrintJson extends RecordF {

    public static final int DEFAULT_TAB_SIZE = 4;

    private static final long serialVersionUID = -3778573655347116245L;

    private String[] _fieldsFilter;
    private boolean _formatted;
    private F _next;
    private String _stackKey;
    private int _tabSize;

    @Deprecated
    public StartPrintJson() {
    }

    public StartPrintJson(String stackKey, String[] fieldsFilter, int tabSize, boolean formatted, F next) {
        if (stackKey == null) {
            throw new IllegalArgumentException("[stackKey] is null");
        }

        if (next == null) {
            throw new IllegalArgumentException("[next] is null");
        }

        _stackKey = stackKey;
        _tabSize = tabSize;
        _fieldsFilter = fieldsFilter;
        _formatted = formatted;
        _next = next;
    }

    public StartPrintJson(String[] fieldsFilter, boolean formatted, F next) {
        this(Stack.DEFAULT_CONTEXT_KEY, fieldsFilter, DEFAULT_TAB_SIZE, formatted, next);
    }

    @Override
    public Object evalRecord(Record current, Map<String, Object> context) {
        Stack oldStack = (Stack) context.get(_stackKey);
        JsonPrintStack stack = new JsonPrintStack(current, _fieldsFilter, _tabSize, _formatted);

        try {
            context.put(_stackKey, stack);
            _next.evalTree(current, context);
            return stack.print();
        } finally {
            context.put(_stackKey, oldStack);
        }
    }

}
