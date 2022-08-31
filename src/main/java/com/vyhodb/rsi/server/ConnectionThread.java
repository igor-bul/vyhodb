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

import com.vyhodb.rsi.message.Message;
import com.vyhodb.rsi.message.MessageProcessor;
import com.vyhodb.rsi.message.MessageUtils;
import com.vyhodb.server.Loggers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author Igor Vykhodtcev
 */
class ConnectionThread implements Runnable {
    private final Logger _logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_RSI);
    private final Socket _socket;
    private final InputStream _in;
    private final OutputStream _out;
    private final MessageProcessor _processor;
    private final Thread _thread;
    private final RsiListener _parentListener;
    
    ConnectionThread(RsiListener parentListener, Socket socket, MessageProcessor processor, String threadName) throws IOException
    {
        _parentListener = parentListener;
        _processor = processor;
        
        _socket = socket;
        _in = _socket.getInputStream();
        _out = _socket.getOutputStream();
        
        _thread = new Thread(this, threadName);
        _thread.setDaemon(true);
    }
    
    public void start()
    {
        _thread.start();
    }
    
    public synchronized void close() {
        try {
            _socket.close();
        } catch (IOException ex1) {
            _logger.debug("Exception occurred during socket closing", ex1);
        }
    }
    
    @Override
    public void run() {
        byte[] header = new byte[MessageUtils.HEADER_SIZE];
        Message messageIn = null;
        Message messageOut;
        
        _logger.debug("Started");
        
        try
        {    
            while (true)
            {
                messageIn = MessageUtils.read(_in, header);
                if (messageIn.isClose)
                    break;
                
                messageOut = _processor.process(messageIn);
                
                if (_socket.isClosed()) break; 
                MessageUtils.send(_out, messageOut);
                if (_socket.isClosed()) break;
            }
        }
        catch(Throwable ex)
        {
            _logger.debug("Exception occurred during rsi call processing", ex);
        }
        finally
        {
            _parentListener.notifyThreadClosed(this);
            close();
        }
        
        _logger.debug("Stopped");
    }
    
}
