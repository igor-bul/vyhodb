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

package com.vyhodb.storage.space.index.ranges;

import com.vyhodb.storage.space.index.IndexRoot;
import com.vyhodb.storage.space.index.iterator.RangeIterator;
import com.vyhodb.storage.space.index.iterator.Tuple;
import com.vyhodb.storage.space.index.iterator.TupleIterator;

public final class NotNullAsc implements RangeIterator {

    private final TupleIterator _ti;
    
    public NotNullAsc(IndexRoot indexRoot) {
        _ti = indexRoot.left();
    }

    @Override
    public long[] next() {
        Tuple tuple = _ti.next();
        
        if (tuple != null && tuple.key == null) {
            tuple = _ti.next();
        }
        
        if (tuple == null) {
            return null;
        }
        
        return tuple.links;
    }
}
