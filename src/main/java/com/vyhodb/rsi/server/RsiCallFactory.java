
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

import com.vyhodb.rsi.RsiServerException;
import com.vyhodb.rsi.request.Request;
import com.vyhodb.space.ServiceLifecycle;
import com.vyhodb.storage.ServerImpl;

import java.lang.reflect.Method;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class RsiCallFactory extends AbstractCallFactory {

    private final ServerImpl _storage;
    
    public RsiCallFactory(ServerImpl storage)
    {
        _storage = storage;
    }
    
    @Override
    protected void checkClass(Class<?> implClass) {
        if (! ServiceLifecycle.class.isAssignableFrom(implClass))
            throw new RsiServerException("Implementation class [" + implClass.getName() + "] doesn't implements [com.vyhodb.rsi.server.Service] interface.");
    }

    @Override
    protected Call newCall(Request request, Object service, Method method) {
        return new RsiCall((ServiceLifecycle) service, method, request.parameters, _storage, request.readOnly, request.trxId);
    }
    
}
