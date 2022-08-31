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

package com.vyhodb.f.predicates;

import java.util.Map;

import com.vyhodb.f.Predicate;
import com.vyhodb.space.Criterion;
import com.vyhodb.space.Order;
import com.vyhodb.space.Record;

public class EveryIndexPredicate extends RecordPredicate {

    private static final long serialVersionUID = -4099333314652218041L;

    private Criterion _criterion;
    private String _indexName;
    private Order _order;
    private Predicate _predicate;

    @Deprecated
    public EveryIndexPredicate() {
    }

    public EveryIndexPredicate(String indexName, Criterion criterion, Order order, Predicate predicate) {
        if (indexName == null) {
            throw new IllegalArgumentException("[indexName] is null");
        }

        if (criterion == null) {
            throw new IllegalArgumentException("[criterion] is null");
        }

        if (order == null) {
            throw new IllegalArgumentException("[order] is null");
        }

        if (predicate == null) {
            throw new IllegalArgumentException("[predicate] is null");
        }

        _indexName = indexName;
        _criterion = criterion;
        _order = order;
        _predicate = predicate;
    }

    public EveryIndexPredicate(String indexName, Criterion criterion, Predicate predicate) {
        this(indexName, criterion, Order.ASC, predicate);
    }

    @Override
    protected Boolean evalRecord(Record current, Map<String, Object> context) {
        for (Record child : current.searchChildren(_indexName, _criterion, _order)) {
            if (!_predicate.evalTree(child, context)) {
                return false;
            }
        }

        return true;
    }
}
