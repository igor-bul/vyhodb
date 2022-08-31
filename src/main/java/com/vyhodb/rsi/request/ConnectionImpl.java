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

package com.vyhodb.rsi.request;

import com.vyhodb.rsi.Connection;
import com.vyhodb.rsi.Implementation;
import com.vyhodb.rsi.RsiClientException;
import com.vyhodb.rsi.Version;

import java.io.IOException;
import java.lang.reflect.Proxy;

public final class ConnectionImpl implements Connection {

    private final RequestProcessor _requestProcessor;
    
    public ConnectionImpl(RequestProcessor processor) {
        _requestProcessor = new ClientContextLayer(processor);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <S> S getService(Class<S> serviceInterface) {
        // Checks that class is interface
        if (! serviceInterface.isInterface())
            throw new RsiClientException("Service class must be an interface class");
        
        // Checks that class has Implementation annotation
        Implementation anImpl = serviceInterface.getAnnotation(Implementation.class);
        if (anImpl == null)
            throw new RsiClientException("Service class must have [" + Implementation.class.getName() +"] annotation");
        if (anImpl.className() == null || anImpl.className().isEmpty())
            throw new RsiClientException("Implementation class name is empty.");
        
        // Retrieves version annotation
        Version anVersion = serviceInterface.getAnnotation(Version.class);
        
        // Creates service proxy
        ServiceProxy sp = new ServiceProxy(
                        anImpl.className(), 
                        (anVersion == null) ? null : anVersion.version(), 
                        _requestProcessor);
        
        // Creates proxy 
        S proxy = (S) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(), 
                new Class<?>[]{serviceInterface}, 
                sp);

        // Set ref to proxy in order to support proxy.equals
        sp.setProxy(proxy);
        
        return proxy;
    }

    @Override
    public void close() throws IOException {
        _requestProcessor.close();
    }
}
