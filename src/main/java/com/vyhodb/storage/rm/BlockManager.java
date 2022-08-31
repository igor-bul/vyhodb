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

package com.vyhodb.storage.rm;

import com.vyhodb.storage.pagefile.PageHeader;
import com.vyhodb.storage.pagestorage.PageTrx;

import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public class BlockManager {
    private static final int OFFSET_ALLOCATOR = 8;
    
    private final PageTrx _pageTrx;
    
    private long _allocator;
    private boolean _allocatorChanged = false;
    
    
    public BlockManager(PageTrx pageTrx)
    {
        _pageTrx = pageTrx;
        _allocator = readAllocator();
    }
    
    /**
     * limit equal to size must be set on destBuffer before this method invoketion
     * 
     * @param start
     * @param destBuffer 
     */
    public void read(long start, ByteBuffer destBuffer)
    {
        start += OFFSET_ALLOCATOR;
        
        long pageId = start / PageHeader.PAGE_PAYLOAD;
        int offset = (int) (start % PageHeader.PAGE_PAYLOAD);
        ByteBuffer page;
                
        while(destBuffer.remaining() > 0)
        {
            page = _pageTrx.getPage(pageId);
            if (offset > 0)
            {
                page.position(page.position() + offset);
                offset = 0;
            }
            
            if (page.remaining() > destBuffer.remaining())
            {
                page.limit(page.position() + destBuffer.remaining());
            }
            
            destBuffer.put(page);
            
            pageId++;
        }
    }
    
    public long append(ByteBuffer buffer)
    {
        final int size = buffer.remaining();
        final long oldAlloc = _allocator;
        
        write(oldAlloc, buffer);
        
        _allocator += size;
        _allocatorChanged = true;
        
        return oldAlloc;
    }
    
    public void write(long start, ByteBuffer sourceBuffer) 
    {
        start += OFFSET_ALLOCATOR;
        
        long pageId = start / PageHeader.PAGE_PAYLOAD;
        int offset = (int) (start % PageHeader.PAGE_PAYLOAD);
        int limit = sourceBuffer.limit();
        ByteBuffer page;
                
        while(sourceBuffer.remaining() > 0)
        {
            page = _pageTrx.getPageForModify(pageId);
            if (offset > 0)
            {
                page.position(page.position() + offset);
                offset = 0;
            }
            
            if (sourceBuffer.remaining() > page.remaining())
            {
                limit = sourceBuffer.limit();
                sourceBuffer.limit(sourceBuffer.position() + page.remaining());
            }
            
            page.put(sourceBuffer);
            sourceBuffer.limit(limit);
            
            pageId++;
        }
    }
    
    public long getAllocator()
    {
        return _allocator;
    }
    
    public void commit() 
    {
        if (_allocatorChanged)
        {
            updateAllocator();
        }
        
        _pageTrx.commit();
    }
    
    public void rollback()
    {
        _pageTrx.rollback();
    }
    
    private long readAllocator()
    {
        return _pageTrx.getPage(0L).getLong();
    }
    
    private void updateAllocator() 
    {
        _pageTrx.getPageForModify(0L).putLong(_allocator);
    }
}
