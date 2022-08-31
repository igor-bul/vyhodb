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

public class ModifyLogBuffer {
    
    private ByteBuffer _logBuffer;
    private final PageStorage _pageStorage;
    
    private LogInfo _logInfo;
    private long _next;
    private int _bufferCapacity;
    private long _bufferStart;
    
    //private Long2LongOpenHashMap _data2log;
    private Data2LogMap _data2log;
    
    public ModifyLogBuffer(PageStorage pageStorage, StorageConfig config) {
        _pageStorage = pageStorage;
        _data2log = new Data2LogMap(config);
    }
    
    public LogInfo start() {
        _logInfo = _pageStorage.startModify();
        _next = _logInfo.getNext();
        
        _logBuffer = _pageStorage.getLogBuffer();
        _logBuffer.clear();
        _bufferCapacity = PageHeader.getBufferSize(_logBuffer);
        _bufferStart = _next;
        
        return _logInfo;
    }
    
    public void commit() {
        if (_next == _logInfo.getNext()) {
            _pageStorage.rollbackModify();
        }
        else {
            // Writes STOP page
            {
                long lastLogPageId = _next - 1;
                ByteBuffer lastPage = ByteBuffer.allocate(PageHeader.PAGE_SIZE);
                
                // Reads last page
                if (inBuffer(lastLogPageId)) {
                    int offset = (int) (lastLogPageId - _bufferStart);
                    PageHeader.copyPages(_logBuffer, offset, lastPage, 0, 1);
                }
                else {
                    _pageStorage.logRead(lastLogPageId, lastPage, 0);
                }
                
                long dataPageId = PageHeader.getPageId(lastPage, 0);
                PageHeader.setStop(lastPage, 0);
                writePage(dataPageId, lastPage);
            }
            
            flush();
            _pageStorage.commitModify(_next - _logInfo.getNext());
        }
        
        _data2log.close();
    }
    
    public void rollback() {
        _pageStorage.rollbackModify();
        _data2log.close();
    }
    
    public void readPage(long pageDataId, ByteBuffer page) {
        long logPageId = _data2log.get(pageDataId);
        
        if (logPageId != Long.MIN_VALUE) {  // Means contains
            if (inBuffer(logPageId)) {
                int offset = (int)(logPageId - _bufferStart);
                PageHeader.copyPages(_logBuffer, offset, page, 0, 1);
            }
            else {
                _pageStorage.logRead(logPageId, page, 0);
            }
        } 
        else {
            _pageStorage.dataRead(pageDataId, page, 0);
        }
    }
    
    public void writePage(long dataPageId, ByteBuffer page) {
        long logPageId = _data2log.get(dataPageId);
        
        if (logPageId != Long.MIN_VALUE) {  // Means contains         
            if (inBuffer(logPageId)) {
                // Updates page in buffer
                int bufferPos = (int)(logPageId - _bufferStart);
                PageHeader.copyPages(page, 0, _logBuffer, bufferPos, 1);
            } 
            else {
                // Allocates new page in buffer
                allocateNewPageInBuffer(dataPageId, page);
            }
        }
        else {
            allocateNewPageInBuffer(dataPageId, page);
        }
    }
    
    private void allocateNewPageInBuffer(long dataPageId, ByteBuffer page) {
        if ( (_next - _bufferStart) >= _bufferCapacity) {
            flush();
        }
        
        long logPageId = _next++;
        int bufferPos = (int)(logPageId - _bufferStart);
        PageHeader.copyPages(page, 0, _logBuffer, bufferPos, 1);
        
        _data2log.put(dataPageId, logPageId);
    }
    
    private void flush() {
        if (_bufferStart != _next) {
            _pageStorage.logAppend(_bufferStart, _logBuffer, (int) (_next - _bufferStart));
            _bufferStart = _next;
        }
    }
    
    private boolean inBuffer(long logPageId) {
        return logPageId >= _bufferStart && logPageId < _next;
    }
}
