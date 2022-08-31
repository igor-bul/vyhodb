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

import com.vyhodb.space.Record;
import com.vyhodb.storage.space.RecordContainer;
import com.vyhodb.storage.space.SpaceInternal;
import com.vyhodb.storage.space.index.IndexRoot;

import java.util.Iterator;

public final class IndexIterator implements Iterator<Record> {
    private final SpaceInternal _space;
    private final long _parentId;
    private final String _indexName;
    private final int _mod;
    private final RecordIdIterator _iterator;
    private final boolean _isReadOnly;
    
    public IndexIterator(SpaceInternal space, String indexName, RecordIdIterator iterator, long parentId, int mod) {
        _iterator = iterator;
        
        _indexName = indexName;
        _space = space;
        _parentId = parentId;
        _mod = mod;
        _isReadOnly = _space.isReadOnly();
    }

    @Override
    public boolean hasNext() {
        checkIndexMod();
        return _iterator.hasNext();
    }

    @Override
    public Record next() {
        checkIndexMod();
        return _space.getRecord(_iterator.next());
    }
    
    @Override
    public void remove() {
        _space.throwTRE("Read-only index iterator.");
    }

    private void checkIndexMod() {
        if (_isReadOnly) {
            return;     // We don't need mod check in case of read-only transaction
        }
        
        RecordContainer parent = (RecordContainer) _space.get(_parentId);
        if (parent == null) 
        {
            _space.throwTRE(RecordContainer.PARENT_RECORD_DELETED);
        }
        
        IndexRoot root = parent.indexes.get(_indexName);
        if (root == null || root._mod != _mod)
        {
            _space.throwTRE("Index concurrent modification.");
        }
    }
}
