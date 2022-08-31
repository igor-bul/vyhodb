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

import com.vyhodb.storage.cache.Cache;
import com.vyhodb.storage.pagefile.PageHeader;
import com.vyhodb.storage.pagefile.Source;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public class ModifyCacheImpl implements ModifyCache {
    private static final int NOT_EXISTED_VALUE = Integer.MIN_VALUE;
    
    private final SyncDataFile _dataFile;
    private final ByteBuffer _pageBuffer;
    private final ByteBuffer _modifyCache;
    private final Cache _readCache;
    
    private final int _capacity;
    private int _position;
        
    private final Long2IntOpenHashMap _pageMapping;
    
    public ModifyCacheImpl(SyncDataFile dataFile, Cache readCache, int size)
    {
        _dataFile = dataFile;
        _readCache = readCache;
        
        _modifyCache = ByteBuffer.allocateDirect(size << PageHeader.PAGE_SIZE_MULTIPLICATOR);
        _pageBuffer = ByteBuffer.allocateDirect(PageHeader.PAGE_SIZE);
        
        _pageMapping = new Long2IntOpenHashMap(size);
        _pageMapping.defaultReturnValue(NOT_EXISTED_VALUE);
        
        _capacity = size;
        _position = 0;
    }

    @Override
    public synchronized void apply(Source source, long count) throws IOException {
        int index;
        long pageId;
        
        for (long i = 0; i < count; i++) {
            source.read(_pageBuffer, 0, 1);
            
            pageId = PageHeader.getPageId(_pageBuffer, 0);
            index = _pageMapping.get(pageId);
            
            // Page isn't in
            if (index == NOT_EXISTED_VALUE)
            {
                _readCache.removePages(_pageBuffer, 0, 1);
                _pageMapping.put(pageId, _position);
                PageHeader.copyPages(_pageBuffer, 0, _modifyCache, _position, 1);
                _position++;
            }
            // Page is in
            else
            {
                PageHeader.copyPages(_pageBuffer, 0, _modifyCache, index, 1);
            }
        }
    }
    
    @Override
    public void applyDirect(Source source, long count) throws IOException {
        if (! isEmpty()) throw new IllegalStateException("direct apply can be started only on empty modify buffer (after checkpoint)");
        
        for (long i = 0; i < count; i++) {
            source.read(_pageBuffer, 0, 1);
            _readCache.removePages(_pageBuffer, 0, 1);
            _dataFile.apply(_pageBuffer, 0, 1);
        }
    }

    @Override
    public synchronized void checkpoint() throws IOException {
        if (isEmpty()) return;
        
        _readCache.putPages(_modifyCache, 0, _position);
        
        _dataFile.apply(_modifyCache, 0, _position);
        _dataFile.fsync();
        
        _modifyCache.clear();
        _pageMapping.clear();
        _position = 0;
    }

    @Override
    public synchronized boolean getPage(long pageId, ByteBuffer buffer, int pageIndex) {
        if (isEmpty()) return false;
        
        int index = _pageMapping.get(pageId);
        if (index == NOT_EXISTED_VALUE)
            return false;
        
        PageHeader.copyPages(_modifyCache, index, buffer, pageIndex, 1);

        return true;
    }
    
    private boolean isEmpty()
    {
        return _position == 0;
    }

    @Override
    public long getModifyBufferRemaining() {
        return (_capacity - _position);
    }


}
