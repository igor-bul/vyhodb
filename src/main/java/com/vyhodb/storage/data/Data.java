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

package com.vyhodb.storage.data;

import com.vyhodb.storage.pagefile.Source;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
public interface Data {
    /**
     * 
     * @return data size (minus header page)
     */
    public long size()throws IOException;
    public UUID getLogId();
    public void readData(long pageId, ByteBuffer buffer, int pageIndex)  throws IOException;
    public void readDataAdmin(long pageId, ByteBuffer buffer, int pageIndex)  throws IOException;
    
    public long getModifyBufferRemaining();
    public void apply(Source source, long count) throws IOException;
    public void applyDirect(Source source, long count) throws IOException;
    public void checkpoint()  throws IOException;
    
    public void close()  throws IOException;
}
