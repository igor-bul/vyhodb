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

package com.vyhodb.rsi.message;

import com.vyhodb.rsi.socket.SocketFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class SocketMessageConnection implements MessageConnection {

    private final long _start = System.currentTimeMillis();
    private final Socket _socket;
    private final InputStream _in;
    private final OutputStream _out;
    
    public SocketMessageConnection(SocketFactory factory) throws IOException
    {
        _socket = factory.newSocket();
        _in = _socket.getInputStream();
        _out = _socket.getOutputStream();
    }
    
    @Override
    public long getStartTime() {
        return _start;
    }

    @Override
    public synchronized Message process(Message message) throws Throwable {
        MessageUtils.send(_out, message);
        byte[] header = new byte[MessageUtils.HEADER_SIZE];
        return MessageUtils.read(_in, header);
    }

    @Override
    public synchronized void close() throws IOException {
        if (!_socket.isClosed()) {
            if (! _socket.isOutputShutdown()) {
                safeSendClose();
            }
            
            _socket.close();
        }
    }
    
    private void safeSendClose() {
       try {
           MessageUtils.sendClose(_out);
       } catch(Throwable th) {}
    }
    
    @Override
    public String toString() {
        return SocketMessageConnection.class.getSimpleName() + ": " + _socket.toString();
    }
    
}
