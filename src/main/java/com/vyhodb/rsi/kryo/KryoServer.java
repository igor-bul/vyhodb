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

package com.vyhodb.rsi.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.vyhodb.rsi.message.Message;
import com.vyhodb.rsi.message.MessageProcessor;
import com.vyhodb.rsi.request.Request;
import com.vyhodb.rsi.request.RequestProcessor;
import com.vyhodb.rsi.request.Response;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class KryoServer implements MessageProcessor {
    
    private final RequestProcessor _requestProcessor;
    
    public KryoServer(RequestProcessor requestProcessor)
    {
        _requestProcessor = requestProcessor;
    }
    
    @Override
    public Message process(Message message) throws Throwable {
        Kryo kryo = KryoFactory.newKryo();
        
        // Deserialize request
        Input input = new Input(message.message, 0, message.length);
        Request request = kryo.readObject(input, Request.class);
        
        input = null;   // We don't need it any more. Help GC.
        
        // Process
        Response response = _requestProcessor.process(request);
        
        // Serialize response
        Output output = new Output(KryoFactory.KRYO_INITIAL_BUFFER_SIZE, KryoFactory.KRYO_MAX_BUFFER_SIZE);
        kryo.writeObject(output, response);
        
        Message messageOut = new Message();
        messageOut.message = output.getBuffer();
        messageOut.length = output.position();
        messageOut.readOnly = message.readOnly;
        messageOut.isClose = false;
        
        return messageOut;
    }
}
