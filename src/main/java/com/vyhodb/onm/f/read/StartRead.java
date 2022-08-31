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

package com.vyhodb.onm.f.read;

import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.f.Stack;
import com.vyhodb.f.record.RecordF;
import com.vyhodb.onm.Mapping;
import com.vyhodb.space.Record;

public class StartRead extends RecordF {

    private static final long serialVersionUID = 364740846241862912L;

    private Mapping _metadata;
    private F _next;
    private Class<?> _rootClass;
    private String _stackKey;

    @Deprecated
    public StartRead() {
    }

    public StartRead(Class<?> rootClass, Mapping metadata, F next) {
        this(Stack.DEFAULT_CONTEXT_KEY, rootClass, metadata, next);
    }

    public StartRead(String stackKey, Class<?> rootClass, Mapping metadata, F next) {
        if (stackKey == null) {
            throw new IllegalArgumentException("[stackKey] is null");
        }

        if (rootClass == null) {
            throw new IllegalArgumentException("[rootClass] is null");
        }

        if (metadata == null) {
            throw new IllegalArgumentException("[metadata] is null");
        }

        if (next == null) {
            throw new IllegalArgumentException("[next] is null");
        }

        _stackKey = stackKey;
        _rootClass = rootClass;
        _metadata = metadata;
        _next = next;
    }

    @Override
    public Object evalRecord(Record current, Map<String, Object> context) {
        Stack oldStack = (Stack) context.get(_stackKey);

        ReadStack onmStack = new ReadStack(_rootClass, current, _metadata);
        context.put(_stackKey, onmStack);
        _next.evalTree(current, context);
        context.put(_stackKey, oldStack);

        return onmStack.getRoot();
    }
}
