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

import java.util.Iterator;

public final class RecordContainerIterator implements Iterator<RecordContainer> {

    private final boolean _asc;
    private final SpaceInternal _space;
    private final String _linkName;
    
    private long _lastReturned = SpaceInternal.NULL;
    private long _next;
    
    public RecordContainerIterator(String linkName, boolean asc, SpaceInternal space, long next) {
        _asc = asc;
        _linkName = linkName;
        _space = space;
        _next = next;
    }
    
    @Override
    public boolean hasNext() {
        return _next != SpaceInternal.NULL;
    }
    
    @Override
    public RecordContainer next() {
        if (_next == SpaceInternal.NULL) {
            _space.throwTRE("No more records in iterator");
        }
        
        _lastReturned = _next;
        
        // Retrieves next/prev
        RecordContainer nextRC = (RecordContainer) _space.get(_next);
        ListNode nextNode = nextRC.parents.get(_linkName);
        _next = _asc ? nextNode.next : nextNode.prev;

        // TODO check for null here
        return (RecordContainer) _space.get(_lastReturned);
    }
    
    @Override
    public void remove() {
        if (_lastReturned == SpaceInternal.NULL) {
            _space.throwTRE("No more records in iterator");
        }

        RecordContainer rc = (RecordContainer) _space.get(_lastReturned);
        rc.setParent(_linkName, null);
        
        _lastReturned = SpaceInternal.NULL;
    }
}
