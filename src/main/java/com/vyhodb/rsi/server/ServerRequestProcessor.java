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

import com.vyhodb.rsi.request.Request;
import com.vyhodb.rsi.request.RequestProcessor;
import com.vyhodb.rsi.request.Response;
import com.vyhodb.server.Loggers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Igor Vykhodtcev
 */
public class ServerRequestProcessor implements RequestProcessor {
    private final Logger _logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_RSI);
    private final CallFactory _callFactory;
    
    public ServerRequestProcessor(CallFactory callFactory) {
        _callFactory = callFactory;
    }
    
    @Override
    public Response process(Request request)  {
        Response response = new Response();

        Call call; 
        try {    
            call = _callFactory.newCall(request);
        }
        catch(Throwable ex) {
            _logger.debug("Can't instantiate service", ex);
            response.setException(ex);
            return response;
        }
        
        try {    
            response.result = call.call();
        }
        catch(Throwable ex)
        {
            _logger.debug("Exception occurred during service invocation", ex);
            if (ex instanceof InvocationTargetException) {
                response.setException(((InvocationTargetException)ex).getTargetException());
            }
            else {
                response.setException(ex);
            }
        }
                
        return response;
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
