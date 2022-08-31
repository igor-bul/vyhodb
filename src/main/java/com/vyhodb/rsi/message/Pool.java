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

import com.vyhodb.rsi.RsiClientException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author User
 */
public abstract class Pool implements MessageConnection {

    public static final String POOL_CLOSED_EX = "Connection pool is closed.";
    
    private final long _startTime = System.currentTimeMillis();
    
    private final long _ttl;
        
    private volatile boolean _isClosed = false;
    private final boolean _debug;
    private final Semaphore _semaphore;
    private final LinkedList<MessageConnection> _all;
    private final LinkedList<MessageConnection> _available;

    public Pool(int size, long ttl, boolean debug) {
        if (size < 1) {
            throw new RsiClientException("Pool size must be > 1");
        }
        
        if (ttl < 1) {
            throw new RsiClientException("Connection TTL must be > 1");
        }
        
        _ttl = ttl;
        _debug = debug;
        _semaphore = new Semaphore(size);
        _all = new LinkedList<>();
        _available = new LinkedList<>();
    }

    public MessageConnection take() throws InterruptedException, IOException {
        MessageConnection res;
        
        // Pool is closed
        if (_isClosed)
        {
            throw new IOException(POOL_CLOSED_EX);
        }
        
        _semaphore.acquire();
                
        synchronized(this)
        {
            try
            {
                // Pool is closed
                if (_isClosed)
                {
                    throw new IOException(POOL_CLOSED_EX);
                }

                if (_available.size() == 0)
                {
                    res = newMessageConnection();
                    _all.add(res);
                    
                    if (_debug) System.out.println("Pool debug. New connection. " + res);
                }
                else
                {
                    res = _available.remove();

                    // Retrieved connection exceed TTL
                    if ((res.getStartTime() + _ttl) <= System.currentTimeMillis())
                    {
                        // Closes old
                        _all.remove(res);
                        res.close();
                        if (_debug) System.out.println("Pool debug. Connection closed. " + res);
                         
                        // Creates new
                        res = newMessageConnection();
                        _all.add(res);
                        if (_debug) System.out.println("Pool debug. New connection. " + res);
                    }
                    else
                    {
                        if (_debug) System.out.println("Pool debug. Connection taken. " + res);
                    }
                }
            }
            catch (IOException ex)
            {
                _semaphore.release();
                throw ex;
            }
        }
        
        return res;
    }

    public void offer(MessageConnection con) throws IOException {
        synchronized(this)
        {
            if (_isClosed)
            {    
                con.close();
            }
            else {
                _available.add(con);
            }
            
        }
        
        _semaphore.release();
        if (_debug) System.out.println("Pool debug. Connection offered. " + con);
    }

    @Override
    public void close() throws IOException {
        synchronized(this)
        {    
            _isClosed = true;

            for (MessageConnection conn : _all) {
                conn.close();
            }

            _all.clear();
            _available.clear();
        }
    }
    
    public void warmClose() throws IOException {
        synchronized(this)
        {    
            _isClosed = true;

            for (MessageConnection conn : _available) {
                conn.close();
            }

            _all.clear();
            _available.clear();
        }
    }

    public void free(MessageConnection con) throws IOException {
        synchronized(this)
        {
            _all.remove(con);
        }    
        
        _semaphore.release();
        con.close();
        if (_debug) System.out.println("Pool debug. Connection freed. " + con);
    }

    @Override
    public long getStartTime() {
        return _startTime;
    }

    @Override
    public Message process(Message message) throws Throwable {
        Message response = null;
        
        final MessageConnection connection = take();
        try {
            response = connection.process(message);
            offer(connection);
            return response;
        } catch (Throwable ex) {
            free(connection);
            throw ex;
        }
    }
    
    protected abstract MessageConnection newMessageConnection() throws IOException;
}
