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

import java.io.IOException;

/**
 *
 * @author Igor Vykhodtcev
 */
public class HeaderFile extends PageFile {

    public final static long HEADER_PAGE_ID = 0;
    
    public HeaderFile(String fileName, boolean exists, boolean readOnly) throws IOException {
        super(fileName, exists, readOnly);
    }
    
    public HeaderFile(String fileName, boolean exists) throws IOException {
        super(fileName, exists);
    }
    
    public void readHeader(Header header) throws IOException {
        super.position(HEADER_PAGE_ID);
        read(directBuffer, 0, 1);
        
        directBuffer.clear();
        header.read(directBuffer);
    }

    public void writeHeader(Header header) throws IOException {
        // Writes header
        directBuffer.clear();
        header.write(directBuffer);
        
        // Updates crc
        crc.updateCrc(directBuffer, 0, 1);
        
        // Writes page
        super.position(HEADER_PAGE_ID);
        write(directBuffer, 0, 1);
    }

    @Override
    public long size() throws IOException {
        return (super.size() - 1);
    }

    @Override
    public void truncate(long pageCount) throws IOException {
        super.truncate(pageCount + 1);
    }

    @Override
    public void position(long newPosition) {
        super.position(newPosition + 1);
    }

    @Override
    public long position() {
        return (super.position() - 1);
    }
}
