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
import com.vyhodb.rsi.request.Response;
import com.vyhodb.storage.ServerImpl;

import java.util.HashMap;

public class ServerRequestProcessorBalancer extends ServerRequestProcessor {

    public static final String CONTEXT_KEY_NEXT = "Sys$Next";
    
    private int _attempts;
    private long _timeout;
    private ServerImpl _server;
    
    public ServerRequestProcessorBalancer(CallFactory callFactory, ServerImpl server, RsiConfig config) {
        super(callFactory);
        
        _server = server;
        _attempts = config.getBalancerAttempts();
        _timeout = config.getBalancerTimeout();
    }

    @Override
    public Response process(Request request) {
        Response response;
        
        // Gets Context
        HashMap<String, Object> context = request.context;
        if (context == null) {
            context = new HashMap<>();
        }
                
        // Gets [next] from Context
        long requestNext = Long.MIN_VALUE;
        if (context.containsKey(CONTEXT_KEY_NEXT)) {
            requestNext = (Long) context.get(CONTEXT_KEY_NEXT);
        }
        
        // Tries to wait for synchronization
        int attemp = 0;
        long next;
        do {
            attemp ++;
            next = _server.getNext();
            if (next >= requestNext) {
                break;
            }
            sleep();
        } while (attemp <= _attempts);
        
        // Composes response message
        if (next < requestNext) {
            RsiServerException rsi = new RsiServerException("Server balancer failed to wait required storage's [next] parameter. Required: " + requestNext + ", last available:" + next + ". Try to decrease replication timeout or increase server balancer timeout or check replication configuration.");
            response = new Response();
            response.setException(rsi);
        } else {
            response = super.process(request);
            context.put(CONTEXT_KEY_NEXT, _server.getNext());
        }
        
        response.context = context;
        return response;
    }

    private void sleep() {
        try {
            Thread.sleep(_timeout);
        } catch (InterruptedException e) {
        }
    }
}
