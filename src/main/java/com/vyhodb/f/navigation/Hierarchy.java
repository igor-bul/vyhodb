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

package com.vyhodb.f.navigation;

import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.f.Predicate;
import com.vyhodb.f.Stack;
import com.vyhodb.space.Order;
import com.vyhodb.space.Record;

public class Hierarchy extends RecordNavigation {

    private static final long serialVersionUID = 2377179141527819219L;

    private F _leveF;
    private String _linkName;
    private Order _order;
    private Predicate _predicate;

    @Deprecated
    public Hierarchy() {
    }

    public Hierarchy(String linkName, Predicate predicate, F levelF) {
        this(Stack.DEFAULT_CONTEXT_KEY, linkName, Order.ASC, predicate, levelF);
    }

    public Hierarchy(String stackKey, String linkName, Order order, Predicate predicate, F levelF) {
        super(stackKey);

        if (linkName == null) {
            throw new IllegalArgumentException("[linkName] is null");
        }

        if (predicate == null) {
            throw new IllegalArgumentException("[predicate] is null");
        }

        if (order == null) {
            throw new IllegalArgumentException("[order] is null");
        }

        if (levelF == null) {
            throw new IllegalArgumentException("[levelF] is null");
        }

        _linkName = linkName;
        _predicate = predicate;
        _order = order;
        _leveF = levelF;
    }

    @Override
    protected Object evalRecord(Record current, Map<String, Object> context) {
        return handleLevel(current, context, 0);
    }

    private Object handleLevel(Record current, Map<String, Object> context, int level) {
        Object result = null;
        Stack stack = getStack(context);

        result = _leveF.evalTree(current, context);

        for (Record child : current.getChildren(_linkName, _order)) {
            if (_predicate.evalTree(child, context)) {
                stack.pushChild(_linkName, child);
                try {
                    result = handleLevel(child, context, level + 1);
                } finally {
                    stack.pop();
                }
            }
        }

        return result;
    }
}
