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

package com.vyhodb.storage.pagestorage;

import com.vyhodb.admin.LogInfo;
import com.vyhodb.admin.WrongPageIdException;

import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public interface PageStorage {

    public LogInfo getLogInfo();
    public void adminDataRead(long pageId, ByteBuffer buffer, int pageIndex);
    public void adminLogRead(long logPageId, ByteBuffer buffer, int offset, int count) throws WrongPageIdException;
    
    public void startRead();
    public void rollbackRead();
    public void dataRead(long pageId, ByteBuffer buffer, int pageIndex);
    
    public LogInfo startModify();
    public void logAppend(long startLogPageId, ByteBuffer buffer, int count);
    public void logRead(long logPageId, ByteBuffer buffer, int pageIndex);
    public void rollbackModify();
    public void commitModify(long trxSize);
    
    public ByteBuffer getLogBuffer();
    
    public void close();
    
    public void shrink(long startLogPageId) throws WrongPageIdException;
    public void clearSlave();
}
