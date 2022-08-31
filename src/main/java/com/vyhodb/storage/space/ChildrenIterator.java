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

import com.vyhodb.space.Record;

import java.util.Iterator;

public final class ChildrenIterator implements Iterator<Record> {

    private final SpaceInternal _space;
    private final Iterator<RecordContainer> _iterator;
    private final long _parentId;
    private final String _linkName;
    private final boolean _isReadOnly;
    
    private int _mod;
    
    public ChildrenIterator(SpaceInternal space, String linkName, Iterator<RecordContainer> iterator, long parentId, int mod) {
        _iterator = iterator;
        _space = space;
        _linkName = linkName;
        _parentId = parentId;
        _mod = mod;
        _isReadOnly = _space.isReadOnly();
    }
    
    @Override
    public boolean hasNext() {
        checkConcurrentMod();
        return _iterator.hasNext();
    }

    @Override
    public Record next() {
        checkConcurrentMod();
        return _space.getRecord(_iterator.next().getId());
    }

    @Override
    public void remove() {
        if (_isReadOnly) {
            _space.throwTRE("Read-only iterator.");
        }
        
        checkConcurrentMod();
        _iterator.remove();
        _mod ++;
    }

    private void checkConcurrentMod()
    {
        if (_isReadOnly) {
            return;
        }
                
        RecordContainer parent = (RecordContainer) _space.get(_parentId);
        if (parent == null) 
        {
            _space.throwTRE(RecordContainer.PARENT_RECORD_DELETED);
        }
        
        ListRoot root = parent.children.get(_linkName);
        if (root == null || root.mod != _mod)
        {
            _space.throwTRE("Children concurrent modification");
        }
    }
}
