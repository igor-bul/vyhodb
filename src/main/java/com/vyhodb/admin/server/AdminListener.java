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

package com.vyhodb.admin.server;

import com.vyhodb.server.Loggers;
import com.vyhodb.server.Server;
import com.vyhodb.storage.pagestorage.PageStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Igor Vykhodtcev
 */
public class AdminListener implements Runnable {
 
    private final Logger _logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_ADMIN);
    private final ServerSocketChannel _server;
    private final Thread _listener;
    private final String _connectionPrefix;
    private final PageStorage _pageStorage;
    private final BufferPool _pool;
    private final Server _parentStorage;
    private final Set<AdminThread> _threads;
    
    private volatile boolean _isClosed = false;
    private long _threadCounter;
    
    public AdminListener(Server parentStorage, PageStorage pageStorage, String listenerPrefix, String connectionPrefix, AdminConfig config) throws IOException
    {
        _parentStorage = parentStorage;
        _threads = new HashSet<>();
        _connectionPrefix = connectionPrefix;
        _pageStorage = pageStorage;
        
        // Allocates buffer pool
        _pool = new BufferPool(config);
               
        _server = newServerSocket(config.getAdminBindHost(), config.getAdminBindPort(), config.getAdminBacklog());
        _listener = new Thread(this, listenerPrefix + ". " + _server.getLocalAddress());
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
        AdminThread thread;
        SocketChannel socket;
        _logger.info("Started");
        
        try
        {    
            while(true)
            {
                socket = _server.accept();
                configure(socket);
                
                thread = new AdminThread(this, _parentStorage, _pageStorage, socket, _pool, newThreadName(socket));
                _threads.add(thread);
                thread.start();
                thread = null;
            }
        }
        catch(IOException ex)
        {
            if (_isClosed) {
                // Listener has been stopped by storage
                _logger.debug("Exception occured in AdminListener. Listener will be stopped.", ex);
            }
            else {
                _logger.error("Exception occured in AdminListener. Listener will be stopped.", ex);
            }
        }
        finally
        {
            close();
        }
        
        _logger.info("Stopped");
    }
    
    private String newThreadName(SocketChannel socket) throws IOException
    {
        return (_connectionPrefix + " " + (_threadCounter++) + ". " + socket.getRemoteAddress());
    }
    
    private ServerSocketChannel newServerSocket(String bindHost, int bindPort, int backlog) throws IOException
    {
        ServerSocketChannel server = ServerSocketChannel.open();
        
        server.configureBlocking(true);
        server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        
        server.bind(new InetSocketAddress(bindHost, bindPort), backlog);
        return server;
    }
    
    private static void configure(SocketChannel socket) throws IOException
    {
        socket.configureBlocking(true);
        socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
    }
    
    private synchronized void closeAllThreads() {
        for (AdminThread thread : _threads) {
            thread.close();
        }
    }
    
    synchronized void notifyThreadClosed(AdminThread thread) {
        _threads.remove(thread);
    }
}
