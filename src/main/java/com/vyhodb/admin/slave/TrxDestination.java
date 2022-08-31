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

package com.vyhodb.admin.slave;

import com.vyhodb.storage.pagefile.Destination;
import com.vyhodb.storage.pagefile.PageHeader;
import com.vyhodb.storage.pagefile.Source;
import com.vyhodb.storage.pagestorage.PageStorage;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
class TrxDestination implements Destination {

    private final PageStorage _pageStorage;
    private final ByteBuffer _logBuffer;
    private long _next;
    private long _bufferStart;
    private final int _bufferCapacity;
    
    TrxDestination(PageStorage pageStorage, ByteBuffer logBuffer, long next)
    {
        _pageStorage = pageStorage;
        _logBuffer = logBuffer;
        _next = next;
        _bufferStart = next;
        _bufferCapacity = PageHeader.getBufferSize(logBuffer);
    }
    
    @Override
    public int write(ByteBuffer buffer, int offset, int count) throws IOException {
        if (offset != 0 || count != 1) throw new IllegalArgumentException("Can process only page buffer");
        
        if ( (_next - _bufferStart) >= _bufferCapacity) {
            flush();
        }
        
        long logPageId = _next++;
        int bufferPos = (int)(logPageId - _bufferStart);
        PageHeader.copyPages(buffer, 0, _logBuffer, bufferPos, 1);
        
        return 1;
    }
    
    public void flush() {
        if (_bufferStart != _next) {
            _pageStorage.logAppend(_bufferStart, _logBuffer, (int) (_next - _bufferStart));
            _bufferStart = _next;
        }
    }

    @Override
    public void transferFrom(Source source, ByteBuffer buffer, long count) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
