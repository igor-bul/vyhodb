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

import com.vyhodb.rsi.Modify;
import com.vyhodb.rsi.RsiClientException;
import com.vyhodb.rsi.RsiServerException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 *
 * @author User
 */
final class ServiceProxy implements InvocationHandler {

    private static final String METHOD_NAME_EQUALS = "equals";
    private static final String METHOD_NAME_TO_STRING = "toString";
    private static final String METHOD_NAME_HASH_CODE = "hashCode";
    
    private final String _implName;
    private final String _version;
    private final RequestProcessor _processor;
    private Object _proxy;

    public ServiceProxy(String implName, String version, RequestProcessor processor) {
        _implName = implName;
        _processor = processor;
        _version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        if (METHOD_NAME_EQUALS.equals(methodName))
        {
            return (_proxy == args[0]);
        }
        if (METHOD_NAME_TO_STRING.equals(methodName))
        {
            return toString();
        }
        if (METHOD_NAME_HASH_CODE.equals(methodName))
        {
            return hashCode();
        }
        
        final Request request = makeRequest(method, args);
        
        Response response = null;
        try {
            response = _processor.process(request);
        }
        catch(Throwable ex) {
            throw new RsiClientException(ex);
        }
        
        checkException(response);
        return response.result;
    }

    private Request makeRequest(Method method, Object[] params) {
        final Request request = new Request();
        request.implName = _implName;
        request.version = _version;
        request.methodName = method.getName();
        request.parameters = params;
        request.types = method.getParameterTypes();
        request.readOnly = (method.getAnnotation(Modify.class) == null);
        request.trxId = UUID.randomUUID();
        return request;
    }

    private void checkException(Response response) throws Throwable {
        if (response.exStackTrace != null) {
            StringBuilder message = (new StringBuilder("\nMessage:")).append(response.exMessage).append("\nStack: ").append(response.exStackTrace);
            throw new RsiServerException(message.toString());
        }
    }
    
    void setProxy(Object proxy)
    {
        _proxy = proxy;
    }
}
