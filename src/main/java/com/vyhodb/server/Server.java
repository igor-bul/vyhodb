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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Server API.
 * <p>
 * Class represents vyhodb Server API and is used by custom application for
 * starting vyhodb server in embedded mode.
 * 
 * <p>
 * <b>Close server</b>
 * <p>
 * Server object is not subject of garbage collection when it runs. To free
 * resources (sockets, buffers, files), Server object MUST be explicitly closed
 * by using method {@linkplain #close()}.
 * 
 * <p>
 * <b>Thread safety</b>
 * <p>
 * Objects of this class are thread safe and can be shared by many threads.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public abstract class Server implements Closeable {

    private final static String DEFAULT_STORAGE_IMPL_CLASS = "com.vyhodb.storage.ServerImpl";

    /**
     * Starts vyhodb server in embedded mode.
     * 
     * @param properties
     *            vyhodb configuration properties
     * @return Server API object
     * @throws IOException
     */
    public static Server start(Properties properties) throws IOException {
        try {
            Class<?> storageImplClass = Class.forName(DEFAULT_STORAGE_IMPL_CLASS);
            Constructor<?> constructor = storageImplClass.getConstructor(Properties.class);
            return (Server) constructor.newInstance(properties);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException ex) {
            throw new IOException("Can't start vyhodb server.", ex);
        }
    }

    /**
     * Checks whether vyhodb server is closed.
     * 
     * @return true - if server is closed, false - if server runs
     */
    public abstract boolean isClosed();

    /**
     * Starts modify transaction.
     * 
     * @return transaction Space API object
     */
    public abstract TrxSpace startModifyTrx();

    /**
     * Starts read transaction.
     * 
     * @return transaction Space API object
     */
    public abstract TrxSpace startReadTrx();
}
