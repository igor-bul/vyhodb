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

package com.vyhodb.storage.space.index.iterator;

import com.vyhodb.storage.space.SpaceInternal;

public final class RecordIdIterator {

    private final SpaceInternal _space;
    private final RangeIterator _iterator;
    private long[] _links;
    private int _linksIndex;
        
    public RecordIdIterator(SpaceInternal space, RangeIterator rangeIterator) {
        _space = space;
        _iterator = rangeIterator;
        _links = _iterator.next();
    }
    
    public boolean hasNext() {
        if (_links == null) 
            return false;
                        
        getNextLinks();
        
        return _links != null;
    }

    public long next() {
        if (_links == null) 
            _space.throwTRE("No more records in index iterator");
                        
        getNextLinks();
        
        if (_links == null) 
            _space.throwTRE("No more records in index iterator");
        
        long id = _links[_linksIndex];
        _linksIndex++;
        
        return id;
    }
    
    private void getNextLinks()
    {
        if (_linksIndex >= _links.length)
        {
            _linksIndex = 0;
            _links = _iterator.next();
        }
    }
}
