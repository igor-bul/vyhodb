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

import java.util.Collection;
import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.f.Predicate;
import com.vyhodb.f.Stack;
import com.vyhodb.onm.Mapping;

public class ObjectChildren extends ObjectNavigation {

    private static final long serialVersionUID = -4985886945215100015L;

    private F _next;
    private Predicate _predicate;

    @Deprecated
    public ObjectChildren() {
    }

    public ObjectChildren(String linkName, Predicate predicate, F next) {
        this(Stack.DEFAULT_CONTEXT_KEY, Mapping.DEFAULT_CONTEXT_KEY, linkName, predicate, next);
    }

    public ObjectChildren(String stackKey, String metadataKey, String linkName, Predicate predicate, F next) {
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
        Object result = null;
        Stack stack = getStack(context);

        Collection<Object> children = getChildren(current, context);
        if (children == null) {
            return result;
        }

        for (Object child : children) {
            if (child != null && _predicate.evalTree(child, context)) {
                try {
                    stack.pushChild(linkName, child);
                    result = _next.evalTree(child, context);
                } finally {
                    stack.pop();
                }
            }
        }

        return result;
    }
}
