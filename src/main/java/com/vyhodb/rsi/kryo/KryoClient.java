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
import com.vyhodb.rsi.message.MessageConnection;
import com.vyhodb.rsi.request.Request;
import com.vyhodb.rsi.request.RequestProcessor;
import com.vyhodb.rsi.request.Response;

import java.io.IOException;

public final class KryoClient implements RequestProcessor {

    private final MessageConnection _connection;
    
    public KryoClient(MessageConnection messageConnection) {
        _connection = messageConnection;
    }

    @Override
    public Response process(Request request) throws Throwable {
        Kryo kryo = KryoFactory.newKryo();
        
        // Serialize request
        Output output = new Output(KryoFactory.KRYO_INITIAL_BUFFER_SIZE, KryoFactory.KRYO_MAX_BUFFER_SIZE);
        kryo.writeObject(output, request);
        
        Message messageOut = new Message();
        messageOut.message = output.getBuffer();
        messageOut.length = output.position();
        messageOut.isClose = false;
        messageOut.readOnly = request.readOnly;
        
        output = null;  // Help GC
        
        // Send message
        Message messageIn = _connection.process(messageOut);
        
        // Deserialize response
        Input input = new Input(messageIn.message, 0, messageIn.length);
        return kryo.readObject(input, Response.class);
    }

    @Override
    public void close() throws IOException {
        _connection.close();
    }
}
