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

package com.vyhodb.storage.space.read;

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

public final class TrxSpaceRead implements TrxSpace, SpaceInternal {

    private boolean _isActive = true;
    private final transient RecordManager _rm;
    
    public TrxSpaceRead(RecordManager rm) {
        _rm = rm;
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
        
        return new ProxyRead(rc);
    }

    @Override
    public Record newRecord() {
        if (!_isActive) throw new NotActiveTransactionException();
        throwTRE(ProxyRead.READ_ONLY);
        return null;
    }
    
    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Container get(long id) {
        if (!_isActive) throw new NotActiveTransactionException();
        
        Container container;
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
    
        return container;
    }

    @Override
    public void create(Container container) {
        if (!_isActive) throw new NotActiveTransactionException();
        throwTRE(ProxyRead.READ_ONLY);
    }

    @Override
    public void delete(Container container) {
        if (!_isActive) throw new NotActiveTransactionException();
        throwTRE(ProxyRead.READ_ONLY);
    }

    @Override
    public void commit() {
        if (!_isActive) throw new NotActiveTransactionException();
        
        _rm.commit();
        _isActive = false;
    }

    @Override
    public void rollback() {
        if (_isActive) {
            _isActive = false;
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
}
