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

package com.vyhodb.storage.space.readcache;

import com.vyhodb.server.NotActiveTransactionException;
import com.vyhodb.server.TransactionRolledbackException;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Record;
import com.vyhodb.storage.rm.ExpandableReaderWriter;
import com.vyhodb.storage.rm.RecordManager;
import com.vyhodb.storage.rm.SystemReader;
import com.vyhodb.storage.space.Container;
import com.vyhodb.storage.space.RecordContainer;
import com.vyhodb.storage.space.SpaceInternal;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.LinkedList;

public final class TrxSpaceReadCache implements TrxSpace, SpaceInternal {

    private boolean _isActive = true;
    private final transient RecordManager _rm;
    
    private final int _maxCacheSize = 200;
    private transient Long2ObjectOpenHashMap<Container> _cache;
    
    public TrxSpaceReadCache(RecordManager rm) {
        _rm = rm;
        _cache = new Long2ObjectOpenHashMap<>(_maxCacheSize);
    }
    
    @Override
    public Record getRecord(long id) {
        Container container = get(id);
        if (container != null && (container.getType() == Container.CONTAINER_TYPE_RECORD))
            return getRecord((RecordContainer)container);
        else
            return null;
    }
    
    private Record getRecord(RecordContainer rc) {
        if (rc == null) {
            return null;
        }
        
        return new ProxyReadCache(rc.getId(), this);
    }

    @Override
    public Record newRecord() {
        if (!_isActive) throw new NotActiveTransactionException();
        throwTRE(ProxyReadCache.READ_ONLY);
        return null;
    }
    
    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Container get(long id) {
        if (!_isActive) throw new NotActiveTransactionException();
        
        Container container = _cache.get(id);
        if (container == null)
        {
            try
            {
                final SystemReader reader = _rm.readRecord(id);
                            
                // Record with specified id doesn't exists
                if (reader == null) 
                    return null;
                
                // TODO remove this hack
                ((ExpandableReaderWriter)reader).setSpace(this);
                
                container = Container.readContainer(this, reader, id);
                
                // Record exists but it's type is unknown.
                if (container == null)
                    return null;
                
                putInCache(container);
            }
            catch(TransactionRolledbackException tre)
            {
                rollback();
                throw tre;
            }
            catch(Throwable th)
            {
                rollback();
                throw new TransactionRolledbackException(th);
            }
        }
    
        return container;
    }

    @Override
    public void create(Container container) {
        if (!_isActive) throw new NotActiveTransactionException();
        throwTRE(ProxyReadCache.READ_ONLY);
    }

    @Override
    public void delete(Container container) {
        if (!_isActive) throw new NotActiveTransactionException();
        throwTRE(ProxyReadCache.READ_ONLY);
    }

    @Override
    public void commit() {
        if (!_isActive) throw new NotActiveTransactionException();
        
        flushCache(true);
        _rm.commit();
        _isActive = false;
    }

    @Override
    public void rollback() {
        if (_isActive) {
            _isActive = false;
            _cache = null;
            _rm.rollback();
        }
    }

    @Override
    public boolean isActive() {
        return _isActive;
    }

    @Override
    public void throwTRE(String message) {
        rollback();
        throw new TransactionRolledbackException(message);
    }

    @Override
    public void throwTRE(String message, Throwable throwable) {
        rollback();
        throw new TransactionRolledbackException(message, throwable);
    }
    
    @Override
    public void throwTRE(Throwable throwable) {
        rollback();
        throw new TransactionRolledbackException(throwable);
    }
    
    private void flushCache(boolean isCommit)
    {
        LinkedList<Container> lockedContainers = new LinkedList<>();
                
        for (Container container : _cache.values()) {
            if (container.isLocked()) {
                if (isCommit) {
                    throwTRE("Record container is locked during commit. Container:" + container + ", id:" + container.getId());
                }
                
                lockedContainers.add(container);
            }
            else
            {
                container.setFreed();
            }
        }
        
        // Creating new arrays is better than filling them.
        // See Long2ObjectOpenHashMap source code for clear() method
        if (isCommit)
        {    
            _cache = null;
        }
        else 
        {
            _cache = new Long2ObjectOpenHashMap<>(_maxCacheSize);
            
            for (Container container : lockedContainers) {
                _cache.put(container.getId(), container);
            }
        }
    }
    
    private void putInCache(Container container) {
        if (_cache.size() >= _maxCacheSize) {
            flushCache(false);
        }
        _cache.put(container.getId(), container);
    }
}
