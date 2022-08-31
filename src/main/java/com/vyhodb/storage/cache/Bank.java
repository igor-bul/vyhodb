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

package com.vyhodb.storage.cache;

import com.vyhodb.storage.pagefile.PageHeader;

import java.nio.ByteBuffer;

final class Bank {

    public static final int LONG_MULTIPLICATOR = 3;
    public static final long EMPTY = -1L;
    
    private final int _start;
    private final ByteBuffer _buffer;
    private final ByteBuffer _index;
    
    Bank(int start, int size) {
        _start = start;
        _index = ByteBuffer.allocateDirect(size << LONG_MULTIPLICATOR);
        _buffer = ByteBuffer.allocateDirect(size << PageHeader.PAGE_SIZE_MULTIPLICATOR);
        
        fillLong(_index, EMPTY, size);
    }
    
    synchronized boolean getPage(long pageId, int index, ByteBuffer page, int pageIndex)
    {
        final int pageOffset = index - _start;
        final int keyOffset = pageOffset << LONG_MULTIPLICATOR;
        final long key = _index.getLong(keyOffset);
                
        if (key == pageId)
        {
            PageHeader.copyPages(_buffer, pageOffset, page, pageIndex, 1);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    synchronized void putPage(long pageId, int index, ByteBuffer page, int pageIndex)
    {
        final int pageOffset = index - _start;
        PageHeader.copyPages(page, pageIndex, _buffer, pageOffset, 1);
        
        final int keyOffset = pageOffset << LONG_MULTIPLICATOR;
        _index.putLong(keyOffset, pageId);
    }
    
    synchronized boolean removePage(long pageId, int index)
    {
        final int pageOffset = index - _start;
        final int keyOffset = pageOffset << LONG_MULTIPLICATOR;
        final long key = _index.getLong(keyOffset);
        
        if (key == pageId) {
            _index.putLong(keyOffset, EMPTY);
            return true;
        }
        else {
            return false;
        }
    }
    
    private static void fillLong(ByteBuffer buffer, long value, int count) {
        buffer.clear();
        for (int i = 0; i < count; i++) {
            buffer.putLong(value);
        }
        buffer.clear();
    }
}
