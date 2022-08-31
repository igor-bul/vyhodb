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

package com.vyhodb.rsi;

import java.io.Closeable;
import java.io.IOException;

/**
 * Connection to RSI Server and factory of RSI Service proxy object.
 * <p>
 * Application developer uses this interface for obtaining <strong>service proxy
 * objects</strong> for specific Service contract interface. This service proxy
 * objects are used for remote invocation of service methods by using underlying
 * connection.
 * 
 * <p>
 * <b>Locking and synchronization between threads</b>
 * <p>
 * Physical TCP connection to RSI Server can handle only one RSI invocation per
 * time. So threads, which invokes service proxy object's methods, synchronize
 * access to physical TCP connection. Synchronization depends on connection
 * type.
 * <p>
 * <strong>Local.</strong> For local connection type (local run mode), there is
 * no synchronization at all. All threads invokes RSI methods without locking.
 * However locks are handled at vyhodb server side as result of  
 * transaction locks.
 * <p>
 * <strong>Single TCP.</strong> Only one thread per time can invoke RSI method
 * using this connection type. All other threads are waiting completion of current
 * RSI invocation.
 * <p>
 * <strong>Pooled TCP.</strong> As many threads as maximum pool size can invoke
 * RSI methods simultaneously. Pool creates new connections when there are no
 * free connections in pool until maximum pool size is reached.
 * <p>
 * <strong>Balancer TCP.</strong> The same as pooled TCP. In case of
 * {@link Read} method, count of threads which can simultaneously invoke
 * {@link Read} methods, is a sum of all @Read connection pools' sizes.
 * 
 * <p>
 * <b>Thread safety</b>
 * <p>
 * Implementations of this object are thread safe. Service proxy objects,
 * created by {@link #getService(Class)} method are thread safe as well.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.6.0
 */
public interface Connection extends Closeable {

    /**
     * Closes connection.
     * <p>
     * Shutdowns local vyhodb server in case of local connection type.
     */
    @Override
    public void close() throws IOException;

    /**
     * Creates service proxy object for specified service contract interface.
     * 
     * @param <S>
     * @param serviceContract
     *            service contract interface class
     * @return service proxy object
     */
    public <S> S getService(Class<S> serviceContract);
}
