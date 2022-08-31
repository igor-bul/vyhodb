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

package com.vyhodb.admin.server;

import com.vyhodb.storage.pagefile.PageHeader;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 *
 * @author Igor Vykhodtcev
 */
final class BufferPool {
    
    private final int _bufferSize;
    private final LinkedList<ByteBuffer> _pageBuffers = new LinkedList<>();
    
    BufferPool(AdminConfig config) throws OutOfMemoryError
    {
        _bufferSize = config.getAdminBufferSize() << PageHeader.PAGE_SIZE_MULTIPLICATOR;
    }
    
    synchronized ByteBuffer getBuffer()
    {
        if (_pageBuffers.isEmpty())
        {
            return ByteBuffer.allocateDirect(_bufferSize);
        }
        else
        {
            return _pageBuffers.poll();
        }
    }
    
    synchronized void returnBuffer(ByteBuffer buffer)
    {
        _pageBuffers.add(buffer);
    }
}
