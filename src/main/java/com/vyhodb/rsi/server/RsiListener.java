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

import com.vyhodb.rsi.message.MessageProcessor;
import com.vyhodb.server.Loggers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Igor Vykhodtcev
 */
class RsiListener implements Runnable {
    
    private final Logger _logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_RSI);
    private final MessageProcessor _processor;
    private long _threadCounter;
    private final ServerSocket _server;
    private final Thread _listener;
    private final String _connectionPrefix;
    private final Set<ConnectionThread> _threads;
    private volatile boolean _isClosed = false;
    
    RsiListener(MessageProcessor procesor, ServerSocket server, String listenerPrefix, String connectionPrefix)
    {
        _threads = new HashSet<>();
        _connectionPrefix = connectionPrefix;
        _processor = procesor;
        _server = server;
        
        _listener = new Thread(this, listenerPrefix + ". " + server.getLocalSocketAddress());
        _listener.setDaemon(true);
    }
    
    public void start()
    {
        _listener.start();
    }
    
    public void close() {
        _isClosed = true;
        closeAllThreads();
        try {
            _server.close();
        } catch (IOException ex) {
            _logger.debug("Exception occurred in server socket close", ex);
        }
    }
    
    @Override
    public void run() {
        Socket socket;
        ConnectionThread thread;
        _logger.info("Started");
        
        try
        {    
            while(true)
            {
                socket = _server.accept();
                configure(socket);
                
                thread = new ConnectionThread(this, socket, _processor, newThreadName(socket));
                _threads.add(thread);
                thread.start();
            }
        }
        catch(IOException ex)
        {
            if (_isClosed && ex instanceof SocketException) {
                // Listener has been stopped by storage
                _logger.debug("Exception occured in RsiListener. Listener will be stopped.", ex);
            }
            else {
                _logger.error("Exception occured in RsiListener. Listener will be stopped.", ex);
            }
        }
        finally
        {
            close();
        }
        
        _logger.info("Stopped");
    }
    
    private String newThreadName(Socket socket)
    {
        return (_connectionPrefix + " " + (_threadCounter++) + ". " + socket.getRemoteSocketAddress());
    }
    
    private static void configure(Socket socket) throws SocketException
    {
        socket.setTcpNoDelay(true);
        socket.setSoTimeout(0);
    }
    
    private synchronized void closeAllThreads() {
        for (ConnectionThread thread : _threads) {
            thread.close();
        }
    }
    
    synchronized void notifyThreadClosed(ConnectionThread thread) {
        _threads.remove(thread);
    }
}
