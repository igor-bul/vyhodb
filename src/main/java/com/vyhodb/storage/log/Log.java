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

package com.vyhodb.storage.log;

import com.vyhodb.admin.LogInfo;
import com.vyhodb.admin.WrongPageIdException;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public interface Log {
    public LogInfo getLogInfo();
    public ByteBuffer getLogBuffer();
    
    public void readAdmin(long fromLogPageId, ByteBuffer page, int offset, int count) throws WrongPageIdException, IOException;
    
    public LogInfo start();
    public void append(long startLogPageId, ByteBuffer buffer, int count) throws IOException;
    public void read(long logPageId, ByteBuffer pageBuffer, int pageIndex) throws IOException;
    public void rollback();
    public void commit(long trxSize) throws IOException;
    
    public void close()  throws IOException;
    public void shutdown() throws IOException;
    
    public void shrink(long shrinkStart) throws WrongPageIdException, IOException;
    public void clearSlave() throws IOException;
}
