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

import static com.vyhodb.utils.Utils.compare;

public class BetweenDesc implements RangeIterator {

    private final TupleIterator _ti;
    
    @SuppressWarnings("rawtypes")
    private final Comparable _to;
    
    @SuppressWarnings("rawtypes")
    private final Comparable _from;
    
    private boolean _first = true;
    
    @SuppressWarnings("rawtypes")
    public BetweenDesc(IndexRoot indexRoot, Comparable from, Comparable to) {
        _from = from;
        _to = to;
        _ti = indexRoot.search(_to);
    }

    @Override
    public long[] next() {
        Tuple tuple = _ti.prev();
        
        if (_first) {
            _first = false;
            
            if (tuple != null && compare(tuple.key, _to) > 0) {
                tuple = _ti.prev();
            }
        }
        
        if (tuple == null) {
            return null;
        }
        
        if (compare(tuple.key, _from) < 0) {
            return null;
        }
        
        return tuple.links;
    }
}
