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

package com.vyhodb.storage.pagefile;

import com.vyhodb.server.PrimitiveUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *
 * @author User
 */
public final class PageHeader {
    
    public static final byte TRX_POSITION_INTERMEDIATE = 0;
    public static final byte TRX_POSITION_STOP = 1;
    
    public static final long MIN_LOG_PAGE_ID = Long.MIN_VALUE + 1;
    public static final long MIN_DATA_PAGE_ID = 0;
    public static final long NOT_EXISTED_LOG_PAGE_ID = Long.MIN_VALUE;
    public static final long NOT_EXISTED_DATA_PAGE_ID = Long.MIN_VALUE;
    
    public static final int PAGE_HEADER = 29;   // pageId, trx_uuid, trx_pos, crc
    public static final int PAGE_SIZE = 1024;
    public static final int PAGE_SIZE_MULTIPLICATOR = 10;

    public static final int PAGE_PAYLOAD = PAGE_SIZE - PAGE_HEADER;
        
    public static final int CRC_LENGTH = 4;
       
    public static final int OFFSET_PAGE_ID = PAGE_PAYLOAD;
    public static final int OFFSET_TRX_ID = OFFSET_PAGE_ID + 8;
    public static final int OFFSET_TRX_POSITION = OFFSET_TRX_ID + 16;
    public static final int OFFSET_CRC = OFFSET_TRX_POSITION + 1;
    
    public static final byte[] EMPTY_PAGE = new byte[PAGE_SIZE];
    
        
    static {
        for (int i = 0; i < PAGE_SIZE; i++) {
            EMPTY_PAGE[i] = 0;
        }
    }
    
    public static long getPageId(ByteBuffer page, int index)
    {
        return getPage(page, index).
                getLong(OFFSET_PAGE_ID + page.position());
    }
    
    public static void setPageId(long pageId, ByteBuffer buffer, int pageIndex)
    {
        ByteBuffer page = getPage(buffer, pageIndex);
        page.putLong(OFFSET_PAGE_ID + page.position(), pageId);
    }
    
    public static int getPageCrc(ByteBuffer buffer, int pageIndex) {
        ByteBuffer page = getPage(buffer, pageIndex);
        return page.getInt(OFFSET_CRC + page.position());
    }
    
    public static void emptyPage(ByteBuffer buffer, int pageIndex)
    {
        ByteBuffer page = getPage(buffer, pageIndex);
        final int position = page.position();
        page.put(EMPTY_PAGE);
        page.position(position);
    }
    
    public static ByteBuffer getPage(ByteBuffer buffer, int index)
    {
        int pos = index << PageHeader.PAGE_SIZE_MULTIPLICATOR;
        
        buffer.clear();
        buffer.limit(pos + PageHeader.PAGE_SIZE);
        buffer.position(pos);
        
        return buffer;
    }
    
    public static int getBufferSize(ByteBuffer buffer)
    {
        return buffer.capacity() >> PageHeader.PAGE_SIZE_MULTIPLICATOR;
    }
    
    public static void setTrxId(UUID trxId, ByteBuffer buffer, int pageIndex)
    {
        ByteBuffer page = getPage(buffer, pageIndex);
        PrimitiveUtils.putUUID(OFFSET_TRX_ID + page.position(), trxId, page);
    }
    
    public static UUID getTrxId(ByteBuffer buffer, int pageIndex)
    {
        ByteBuffer page = getPage(buffer, pageIndex);
        return PrimitiveUtils.getUUID(OFFSET_TRX_ID + page.position(), page);
    }
    
    public static void setStop(ByteBuffer buffer, int pageIndex)
    {
        ByteBuffer page = getPage(buffer, pageIndex);
        page.put(OFFSET_TRX_POSITION + page.position(), TRX_POSITION_STOP);
    }
    
    public static void setIntermediate(ByteBuffer buffer, int pageIndex)
    {
        ByteBuffer page = getPage(buffer, pageIndex);
        page.put(OFFSET_TRX_POSITION + page.position(), TRX_POSITION_INTERMEDIATE);
    }
    
    public static boolean isStop(ByteBuffer buffer, int pageIndex)
    {
        ByteBuffer page = getPage(buffer, pageIndex);
        return TRX_POSITION_STOP == page.get(OFFSET_TRX_POSITION + page.position());
    }
    
    public static boolean isIntermediate(ByteBuffer buffer, int pageIndex)
    {
        ByteBuffer page = getPage(buffer, pageIndex);
        return TRX_POSITION_INTERMEDIATE == page.get(OFFSET_TRX_POSITION + page.position());
    }
    
    public static void copyPages(ByteBuffer src, int srcOff, ByteBuffer dst, int dstOff, int count)
    {
        final int c = count << PageHeader.PAGE_SIZE_MULTIPLICATOR;
        final int srcOffset = srcOff << PageHeader.PAGE_SIZE_MULTIPLICATOR;
        final int dstOffset = dstOff << PageHeader.PAGE_SIZE_MULTIPLICATOR;
        
        src.limit(srcOffset + c);
        src.position(srcOffset);
        
        dst.limit(dstOffset + c);
        dst.position(dstOffset);
        
        dst.put(src);
    }
}
