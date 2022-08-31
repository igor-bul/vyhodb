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

import com.vyhodb.storage.pagefile.PageHeader;

import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public class ReadTrxImpl implements PageTrx {

    private final ByteBuffer _page = ByteBuffer.allocate(PageHeader.PAGE_SIZE);
    protected final PageStorage pageStorage;
    private long _current = PageHeader.NOT_EXISTED_DATA_PAGE_ID;
    
    public ReadTrxImpl(PageStorage pageStorage)
    {
        this.pageStorage = pageStorage;
    }
    
    @Override
    public void start() {
        pageStorage.startRead();
    }

    @Override
    public void commit() {
        pageStorage.rollbackRead();
    }

    @Override
    public void rollback() {
        pageStorage.rollbackRead();
    }

    @Override
    public ByteBuffer getPage(long pageId) {
        if (_current != pageId)
        {
            pageStorage.dataRead(pageId, _page, 0);
        }
        
        _current = pageId;
        
        _page.clear();
        _page.limit(PageHeader.PAGE_PAYLOAD);
 
        return _page;
    }

    @Override
    public ByteBuffer getPageForModify(long pageId) {
        throw new IllegalStateException("Read only trx");
    }
    
}
