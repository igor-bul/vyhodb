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
import com.vyhodb.rsi.Version;
import com.vyhodb.rsi.request.Request;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Igor Vykhodtcev
 */
public abstract class AbstractCallFactory implements CallFactory {

    private final static int INIT_IMPL_CACHE_SIZE = 64;
    
    private final Map<String, ImplHolder> _implementations = new HashMap<>(INIT_IMPL_CACHE_SIZE);
    
    @Override
    public final Call newCall(Request request) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Try to get class from cache
        ImplHolder holder = getCached(request.implName);
       
        // Load implementation class
        if (holder == null)
            holder = load(request.implName);

        // Checks version
        if (holder.version != null && 
                !holder.version.equals(request.version))
            throw new RsiServerException("Wrong service version. Expected:" + holder.version + ", actual:" + request.version);
        
        final Class<?> implClass = holder.implementationClass;
        final Method method = implClass.getMethod(request.methodName, request.types);
        final Object service = implClass.newInstance();
        
        return newCall(request, service, method);
    }
    
    private synchronized ImplHolder getCached(String implementationName)
    {
        return _implementations.get(implementationName);
    }
    
    private synchronized ImplHolder load(String implementationName) throws ClassNotFoundException
    {
        // This check because of synchronization nature. While current thread was waiting for lock, others might have already load service class.
        if (_implementations.containsKey(implementationName))
            return _implementations.get(implementationName);
        
        Class<?> impl = Class.forName(implementationName);
        checkClass(impl);
        
        ImplHolder descriptor = new ImplHolder(getVersion(impl), impl);
        _implementations.put(implementationName, descriptor);
        return descriptor;
    }
    
    private String getVersion(Class<?> impl)
    {
        Version version = impl.getAnnotation(Version.class);
        if (version == null)
            return null;
        else
            return version.version();
    }
    
    protected abstract void checkClass(Class<?> implClass);
    protected abstract Call newCall(Request request, Object service, Method method);
    
    private class ImplHolder
    {
        final String version;
        final Class<?> implementationClass;
        
        ImplHolder(String version, Class<?> implementationClass)
        {
            this.version = version;
            this.implementationClass = implementationClass;
        }
    }
}
