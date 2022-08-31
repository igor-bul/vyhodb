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

import com.vyhodb.rsi.kryo.KryoServer;
import com.vyhodb.rsi.message.MessageProcessor;
import com.vyhodb.rsi.request.RequestProcessor;
import com.vyhodb.storage.ServerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class RsiServer {
    
    private final RsiListener _listener;
    
    public RsiServer(RsiConfig config, CallFactory factory, ServerImpl storage, String listenerName, String connectionNamePrefix) throws IOException
    {
        // Creates balancer if needed
        RequestProcessor requestProcessor = config.isBalancerEnabled() ? 
                new ServerRequestProcessorBalancer(factory, storage, config) : 
                new ServerRequestProcessor(factory);
                
        MessageProcessor processor = new KryoServer(requestProcessor);
                
        ServerSocket serverSocket = newServerSocket(config.getRsiBindHost(), config.getRsiBindPort(), config.getRsiBacklog());
        _listener = new RsiListener(processor,  serverSocket, listenerName, connectionNamePrefix);
        _listener.start();
    }
    
    private static ServerSocket newServerSocket(String bindHost, int bindPort, int backlog) throws IOException
    {
        ServerSocket server = new ServerSocket();
        server.setReuseAddress(true);
        server.setSoTimeout(0);
        
        server.bind(new InetSocketAddress(bindHost, bindPort), backlog);
        
        return server;
    }
    
    public void close() {
        _listener.close();
    }
}
