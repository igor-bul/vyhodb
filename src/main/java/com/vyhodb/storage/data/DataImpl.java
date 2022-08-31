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

import com.vyhodb.server.ServerClosedException;
import com.vyhodb.storage.StorageConfig;
import com.vyhodb.storage.cache.Cache;
import com.vyhodb.storage.cache.CacheImpl;
import com.vyhodb.storage.pagefile.Source;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
public class DataImpl implements Data {

    private final SyncDataFile _file;
    private final Cache _readCache;
    private final ModifyCache _modifyCache;
    private volatile boolean _isClosed = false;
       
    public DataImpl(StorageConfig config) throws IOException {
        _file = new SyncDataFile(config);
        _readCache = new CacheImpl(config.getCacheSize(), config.getBankCount());
        _modifyCache = new ModifyCacheImpl(_file, _readCache, config.getModifyBufferSize());
    }

    @Override
    public UUID getLogId() {
        return _file.getLogId();
    }

    @Override
    public void readData(long pageId, ByteBuffer buffer, int pageIndex) throws IOException {
        if (_isClosed) 
            throw new ServerClosedException();
        
        if (_modifyCache.getPage(pageId, buffer, pageIndex))
            return;
        
        if (!_readCache.getPage(pageId, buffer, pageIndex))
        {
            _file.readPage(pageId, buffer, pageIndex);
            _readCache.putPage(pageId, buffer, pageIndex);
        }
    }

    /**
     * Tries to read page from cache at first, and from disk at second.
     * Doesn't put read page from disk into cache.
     *
     * @param pageId
     * @param buffer
     * @throws IOException 
     */
    @Override
    public void readDataAdmin(long pageId, ByteBuffer buffer, int pageIndex) throws IOException {
        if (_isClosed) 
            throw new ServerClosedException();
            
        if (_modifyCache.getPage(pageId, buffer, pageIndex))
            return;

        if (_readCache.getPage(pageId, buffer, pageIndex))
            return;

        _file.readPage(pageId, buffer, pageIndex);
    }

    @Override
    public void checkpoint() throws IOException {
        if (_isClosed) 
            throw new ServerClosedException();
        
        _modifyCache.checkpoint();
    }

    @Override
    public synchronized void close() throws IOException  {
        if (!_isClosed)
        {
            _file.close();
            _isClosed = true;
        }
    }
   
    @Override
    public long getModifyBufferRemaining() {
        if (_isClosed) 
            throw new ServerClosedException();
        
        return _modifyCache.getModifyBufferRemaining();
    }

    @Override
    public void apply(Source source, long count) throws IOException {
        if (_isClosed) 
            throw new ServerClosedException();
        
        _modifyCache.apply(source, count);
    }
    
    @Override
    public void applyDirect(Source source, long count) throws IOException {
        if (_isClosed) 
            throw new ServerClosedException();
        
        _modifyCache.applyDirect(source, count);   
    }

    @Override
    public long size() throws IOException {
        if (_isClosed) 
            throw new ServerClosedException();
        
        return _file.size();
    }

}
