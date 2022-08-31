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
import com.vyhodb.storage.StorageConfig;
import com.vyhodb.storage.pagefile.PageHeader;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class ModifyTrxImpl implements PageTrx {
    
    private final UUID _trxId;
    
    private long _dataLength;
    private long _dataId = PageHeader.NOT_EXISTED_DATA_PAGE_ID;
    private boolean _isPageDirty = false;
    private final ByteBuffer _page = ByteBuffer.allocate(PageHeader.PAGE_SIZE);
    private final ModifyLogBuffer _modifyLogBuffer;
    
    public ModifyTrxImpl(PageStorage pageStorage, UUID trxId, StorageConfig config)
    {
        _modifyLogBuffer = new ModifyLogBuffer(pageStorage, config);
        _trxId = trxId;
    }
    
    @Override
    public void start() {
        LogInfo header =_modifyLogBuffer.start();
        _dataLength = header.getDataLength();
    }

    @Override
    public void commit() {
        flushPage();
        _modifyLogBuffer.commit();
    }

    @Override
    public void rollback() {
        _modifyLogBuffer.rollback();
    }

    @Override
    public ByteBuffer getPage(long pageId) {
        if (_dataId != pageId)
        {    
            flushPage();
            readPage0(pageId);
        }
        
        _dataId = pageId;
        
        _page.clear();
        _page.limit(PageHeader.PAGE_PAYLOAD);
 
        return _page;
    }

    @Override
    public ByteBuffer getPageForModify(long pageId) {
        if (_dataId != pageId)
        {    
            flushPage();
            readPage0(pageId);
        }
        
        _isPageDirty = true;
        _dataId = pageId;
        
        _page.clear();
        _page.limit(PageHeader.PAGE_PAYLOAD);
 
        return _page;
    }
    
    private void readPage0(long dataId)
    {
        // New page. This case is possible only when invoked from 
        // getPageForModify()
        if (dataId == _dataLength)
        {
            PageHeader.emptyPage(_page, 0);
            PageHeader.setPageId(dataId, _page, 0);
                            
            _dataLength++;
            _isPageDirty = true;
        }
        // Reads page from data
        else
        {    
            _modifyLogBuffer.readPage(dataId, _page);
        }
    }

    private void flushPage()
    {
        if (!_isPageDirty)
            return;
        
        _page.clear();
        PageHeader.setTrxId(_trxId, _page, 0);
        PageHeader.setIntermediate(_page, 0);
        _modifyLogBuffer.writePage(_dataId, _page);

        _isPageDirty = false;
    }
}
