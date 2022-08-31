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

package com.vyhodb.storage.cache;

import com.vyhodb.server.Loggers;
import com.vyhodb.storage.pagefile.PageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Important !!!!!
 * 
 * Don't use HashMap approach for calculating index in hash, namely,
 * don't ever never use & operation.
 * 
 * @author Igor Vykhodtcev
 */
public final class CacheImpl implements Cache {

    private static final String STORAGE_CACHE_MAX_SIZE = "Specified cache size is more than max supported cache size. Max cache size (in pages):";
    private static final String BANK_ALLOCATED = "Read cache bank allocated: {} pages";
    
    public static final int MAX_BANK_SIZE = Integer.MAX_VALUE / PageHeader.PAGE_SIZE - 2;
    public static final int MAX_CACHE_SIZE = Integer.MAX_VALUE - 1;   // In pages
   
    private final Logger _logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_SERVER);
    private final int _size;
    private final int _bankSize;
    private final ZeroBuffer _zeroBuffer;
    private final Bank[] _banks;
    
    public CacheImpl(int size, int bankCount) {
        if (size > MAX_CACHE_SIZE) throw new IllegalArgumentException(STORAGE_CACHE_MAX_SIZE + MAX_CACHE_SIZE);
        
        _size = size;
        if (_size == 0)
        {
            _bankSize = 0;
            _banks = null;
            _zeroBuffer = null;
            return;
        }
                
        _zeroBuffer = new ZeroBuffer();
                
        int estimatedBankSize = _size / bankCount;
        _bankSize = estimatedBankSize > MAX_BANK_SIZE ? MAX_BANK_SIZE : estimatedBankSize;
        int reminder = size % _bankSize;
        bankCount = size / _bankSize + ((reminder > 0) ? 1 : 0);
        
        _banks = new Bank[bankCount];
        int counter = size;
        int i = 0;
        while (counter > 0)
        {
            if (counter >= _bankSize)
            {
                _banks[i] = new Bank(i * _bankSize, _bankSize);
                _logger.debug(BANK_ALLOCATED, _bankSize);
            }
            else
            {
                _banks[i] = new Bank(i * _bankSize, counter);
                _logger.debug(BANK_ALLOCATED, counter);
            }

            counter -= _bankSize;
            i++;
        }
    }

    @Override
    public boolean getPage(long pageId, ByteBuffer buffer, int pageIndex) {
        if (_size == 0) {
            return false;
        }
                    
        if (pageId == 0) {
            return _zeroBuffer.getPage(buffer, pageIndex);
        }
        
        int index = (int) (pageId % _size);
        int bankIndex = index / _bankSize;
        
        return _banks[bankIndex].getPage(pageId, index, buffer, pageIndex);
    }

    @Override
    public void putPage(long pageId, ByteBuffer buffer, int pageIndex) {
        if (_size == 0) {
            return;
        }
                
        if (pageId == 0) {
            _zeroBuffer.putPage(buffer, pageIndex);
            return;
        }
        
        int index = (int) (pageId % _size);
        int bankIndex = index / _bankSize;
        
        _banks[bankIndex].putPage(pageId, index, buffer, pageIndex);
    }

    @Override
    public void removePages(ByteBuffer buffer, int offset, int count) {
        if (_size == 0) return;
        
        long pageId;
        int index;
        int bankIndex;
        
        for (int i = offset; i < count; i++) {
            pageId = PageHeader.getPageId(buffer, i);
            
            if (pageId == 0) {
                _zeroBuffer.removePage();
                continue;
            }
            
            index = (int) (pageId % _size);
            bankIndex = index / _bankSize;
            
            _banks[bankIndex].removePage(pageId, index);
        }
    }

    @Override
    public void putPages(ByteBuffer buffer, int offset, int count) {
        if (_size == 0) return;
        
        for (int i = offset; i < count; i++) {
            putPage(
                    PageHeader.getPageId(buffer, i), 
                    buffer, 
                    i
            );
        }
    }

    private class ZeroBuffer {
        private final ByteBuffer _page;
        private boolean _inCache = false;
        
        ZeroBuffer() {
            _page = ByteBuffer.allocate(PageHeader.PAGE_SIZE);
        }
        
        synchronized void putPage(ByteBuffer buffer, int pageIndex) {
            _inCache = true;
            PageHeader.copyPages(buffer, pageIndex, _page, 0, 1);
        }
        
        synchronized boolean getPage(ByteBuffer buffer, int pageIndex) {
            if (_inCache) {
                PageHeader.copyPages(_page, 0, buffer, pageIndex, 1);
                return true;
            }
            else {
                return false;
            }
        }
        
        synchronized void removePage() {
            _inCache = false;
        }
        
    }
}
