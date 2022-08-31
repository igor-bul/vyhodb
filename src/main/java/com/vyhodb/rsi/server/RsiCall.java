
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

package com.vyhodb.rsi.server;

import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.ServiceLifecycle;
import com.vyhodb.storage.ServerImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
final class RsiCall implements Call {

    private final ServiceLifecycle _service;
    private final Method _method;
    private final Object[] _params;
    private final ServerImpl _storage;
    private final boolean _read;
    private final UUID _trxId;
    
    RsiCall(ServiceLifecycle service, Method method, Object[] params, ServerImpl storage, boolean read, UUID trxId)
    {
        _service = service;
        _method = method;
        _params = params;
        _storage = storage;
        _read = read;
        _trxId = trxId;
    }
    
    @Override
    public Object call() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object result;
        TrxSpace space = startTrx();
        try
        {    
            _service.setSpace(space);
            result = _method.invoke(_service, _params);
            space.commit();
        }
        catch(Throwable th)
        {
            if (! space.isActive()) 
                space.rollback();
            
            throw th;
        }
        
        return result;
    }
    
    private TrxSpace startTrx()
    {
        if (_read)
            return _storage.startReadTrx(_trxId);
        else
            return _storage.startModifyTrx(_trxId);
    }
    
}
