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

package com.vyhodb.server;

import com.vyhodb.rsi.RsiServerException;
import com.vyhodb.rsi.kryo.KryoServer;
import com.vyhodb.rsi.message.Message;
import com.vyhodb.rsi.message.MessageConnection;
import com.vyhodb.rsi.message.MessageProcessor;
import com.vyhodb.rsi.request.RequestProcessor;
import com.vyhodb.rsi.server.RsiCallFactory;
import com.vyhodb.rsi.server.ServerRequestProcessor;
import com.vyhodb.storage.ServerImpl;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class LocalServer implements MessageConnection {

    private final ServerImpl _storage;
    private final MessageProcessor _messageProcessor;
    private final long _startTime;
    
    public LocalServer(Properties properties) throws IOException, RsiServerException
    {
        _storage = (ServerImpl) Server.start(properties);
        
        RequestProcessor requestProcessor = new ServerRequestProcessor(new RsiCallFactory(_storage));
        _messageProcessor = new KryoServer(requestProcessor);
        
        _startTime = System.currentTimeMillis();
    }
    
    @Override
    public void close() throws IOException {
        _storage.close();
    }

    @Override
    public Message process(Message message) throws Throwable {
        return _messageProcessor.process(message);
    }

    @Override
    public long getStartTime() {
        return _startTime;
    }
}
