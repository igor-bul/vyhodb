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

package com.vyhodb.onm.f.object.clone;

import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.f.Predicate;
import com.vyhodb.f.Stack;
import com.vyhodb.onm.Mapping;

public class StartClone extends F {

    private static final long serialVersionUID = 6805972383226516737L;

    private Mapping _metadata;
    private String _metadataKey;
    private F _next;
    private Predicate _predicate;
    private String _stackKey;

    @Deprecated
    public StartClone() {
    }

    public StartClone(Mapping metadata, Predicate predicate, F next) {
        this(Stack.DEFAULT_CONTEXT_KEY, Mapping.DEFAULT_CONTEXT_KEY, metadata, predicate, next);
    }

    public StartClone(String stackKey, String metadataKey, Mapping metadata, Predicate predicate, F next) {
        if (stackKey == null) {
            throw new IllegalArgumentException("[stackKey] is null");
        }

        if (metadataKey == null) {
            throw new IllegalArgumentException("[metadataKey] is null");
        }

        if (metadata == null) {
            throw new IllegalArgumentException("[metadata] is null");
        }

        if (predicate == null) {
            throw new IllegalArgumentException("[predicate] is null");
        }

        if (next == null) {
            throw new IllegalArgumentException("[next] is null");
        }

        _stackKey = stackKey;
        _metadataKey = metadataKey;
        _metadata = metadata;
        _predicate = predicate;
        _next = next;
    }

    @Override
    public Object evalTree(Object current, Map<String, Object> context) {
        if (current != null && _predicate.evalTree(current, context)) {
            Object oldStack = context.get(_stackKey);
            Object oldMetadata = context.get(_metadataKey);

            try {
                CloneStack cloneStack = new CloneStack(current, _metadata);
                context.put(_stackKey, cloneStack);
                context.put(_metadataKey, _metadata);
                _next.evalTree(current, context);
                return cloneStack.peek();
            } finally {
                context.put(_stackKey, oldStack);
                context.put(_metadataKey, oldMetadata);
            }
        }

        return null;
    }
}
