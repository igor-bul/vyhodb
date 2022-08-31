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

import com.vyhodb.storage.space.index.IndexLeaf;

public final class TupleIterator {

    public final static TupleIterator EMPTY_ITERATOR = new TupleIterator(null, 0);
    
    private final Tuple _tuple = new Tuple();
    private IndexLeaf _leaf;
    private int _index;
    
    public TupleIterator(IndexLeaf current, int index) {
        _leaf = current;
        _index = index;
    }
    
    public Tuple next()
    {
        if (_leaf == null) return null;
        
        if (_index == _leaf._size)
        {
            _index = 0;
            _leaf = _leaf.getNext();
            if (_leaf == null) return null;
        }
        
        _tuple.key = _leaf._keys[_index];
        _tuple.links = _leaf.getLinks(_index);
        
        _index++;
        
        return _tuple;
    }
    
    public Tuple prev()
    {
        if (_leaf == null) return null;
        
        // This might happen if we searched for a value which is more then the max value in index
        if (_index == _leaf._size) {
            _index--;
        }
        
        if (_index == -1)
        {
            _leaf = _leaf.getPrev();
            if (_leaf != null)
            {
                _index = _leaf._size - 1;
            }
            else
            {
                return null;
            }
        }
        
        _tuple.key = _leaf._keys[_index];
        _tuple.links = _leaf.getLinks(_index);
        
        _index--;

        return _tuple;
    }

}
