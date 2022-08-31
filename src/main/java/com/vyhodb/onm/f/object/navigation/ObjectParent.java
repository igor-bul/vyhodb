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

package com.vyhodb.onm.f.object.navigation;

import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.f.Predicate;
import com.vyhodb.f.Stack;
import com.vyhodb.onm.Mapping;

public class ObjectParent extends ObjectNavigation {

    private static final long serialVersionUID = -6410056428090027790L;

    private F _next;
    private Predicate _predicate;

    @Deprecated
    public ObjectParent() {
    }

    public ObjectParent(String linkName, Predicate predicate, F next) {
        this(Stack.DEFAULT_CONTEXT_KEY, Mapping.DEFAULT_CONTEXT_KEY, linkName, predicate, next);
    }

    public ObjectParent(String stackKey, String metadataKey, String linkName, Predicate predicate, F next) {
        super(stackKey, metadataKey, linkName);

        if (predicate == null) {
            throw new IllegalArgumentException("[predicate] is null");
        }

        if (next == null) {
            throw new IllegalArgumentException("[next] is null");
        }

        _predicate = predicate;
        _next = next;
    }

    @Override
    public Object evalTree(Object current, Map<String, Object> context) {
        Stack stack = getStack(context);

        Object parent = getParent(current, context);
        if (parent != null && _predicate.evalTree(parent, context)) {
            try {
                stack.pushParent(linkName, parent);
                return _next.evalTree(parent, context);
            } finally {
                stack.pop();
            }
        }

        return null;
    }
}
