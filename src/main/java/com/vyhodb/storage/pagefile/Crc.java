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

import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 *
 * @author User
 */
public final class Crc {
    
    private final byte[] crcPageBuffer = new byte[PageHeader.OFFSET_CRC];
    private final Checksum checksum = new CRC32();
    
    private void updateCrc(ByteBuffer buffer, int pageIndex)
    {
        int calcCrc = calculateCrc(buffer, pageIndex);
        
        ByteBuffer page = PageHeader.getPage(buffer, pageIndex);
        page.putInt(page.position() + PageHeader.OFFSET_CRC, calcCrc);
    }
    
    public void updateCrc(ByteBuffer buffer, int offset, int count)
    {
        for (int i = 0; i < count; i++) {
            updateCrc(buffer, i + offset);
        }
    }
    
    private int calculateCrc(ByteBuffer buffer, int pageIndex)
    {
        ByteBuffer page = PageHeader.getPage(buffer, pageIndex);
        checksum.reset();
                
        if (page.hasArray())
        {
            byte[] array = page.array();
            checksum.update(array, page.arrayOffset() + page.position(), PageHeader.OFFSET_CRC);
        }
        else
        {    
            page.get(crcPageBuffer);
            checksum.update(crcPageBuffer, 0, PageHeader.OFFSET_CRC);
        }
                
        return (int)checksum.getValue();
    }
    
    public boolean validateCrc(ByteBuffer buffer, int pageIndex)
    {
        int crc = getPageCrc(buffer, pageIndex);
        int calcCrc = calculateCrc(buffer, pageIndex);
        return (crc == calcCrc);
    }
    
    public static int getPageCrc(ByteBuffer buffer, int pageIndex)
    {
        ByteBuffer page = PageHeader.getPage(buffer, pageIndex);
        return page.getInt(page.position() + PageHeader.OFFSET_CRC);
    }
}
