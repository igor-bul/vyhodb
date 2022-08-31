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
import com.vyhodb.space.Criterion;
import com.vyhodb.space.Order;
import com.vyhodb.space.Record;

public class Search extends RecordNavigation {

    private static final long serialVersionUID = 4971434301051764892L;

    private Criterion _criterion;
    private String _indexName;
    private F _next;
    private Order _order;
    private Predicate _predicate;

    @Deprecated
    public Search() {
    }

    public Search(String indexName, Criterion criterion, Predicate predicate, F next) {
        this(Stack.DEFAULT_CONTEXT_KEY, indexName, criterion, Order.ASC, predicate, next);
    }

    public Search(String stackKey, String indexName, Criterion criterion, Order order, Predicate predicate, F next) {
        super(stackKey);

        if (indexName == null) {
            throw new IllegalArgumentException("[linkName] is null");
        }

        if (criterion == null) {
            throw new IllegalArgumentException("[criterion] is null");
        }

        if (predicate == null) {
            throw new IllegalArgumentException("[predicate] is null");
        }

        if (order == null) {
            throw new IllegalArgumentException("[order] is null");
        }

        if (next == null) {
            throw new IllegalArgumentException("[next] is null");
        }

        _indexName = indexName;
        _criterion = criterion;
        _predicate = predicate;
        _order = order;
        _next = next;
    }

    @Override
    protected Object evalRecord(Record current, Map<String, Object> context) {
        Object result = null;
        Stack stack = getStack(context);
        String linkName = current.getIndexDescriptor(_indexName).getIndexedLinkName();

        for (Record child : current.searchChildren(_indexName, _criterion, _order)) {
            if (_predicate.evalTree(child, context)) {
                stack.pushChild(linkName, child);
                try {
                    result = _next.evalTree(child, context);
                } catch (BreakException ex) {
                    break;
                } finally {
                    stack.pop();
                }
            }
        }

        return result;
    }
}
