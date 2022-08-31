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

package com.vyhodb.storage.space;

import com.vyhodb.space.Order;
import com.vyhodb.space.Record;

import java.util.Iterator;

public final class IterableChildren implements Iterable<Record> {

    @SuppressWarnings("deprecation")
    private final RecordProxyInterface _proxy;
    private final String _linkName;
    private final Order _order;
        
    @SuppressWarnings("deprecation")
    public IterableChildren(RecordProxyInterface proxy, String linkName, Order order) {
        _proxy = proxy;
        _linkName = linkName;
        _order = order;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Iterator<Record> iterator() {
        return _proxy.iteratorChildren(_linkName, _order);
    }

}
