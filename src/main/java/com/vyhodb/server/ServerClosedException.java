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

import com.vyhodb.space.Space;

/**
 * Indicates that current {@linkplain Server} object is closed and no actions
 * are allowed on it.
 * <p>
 * Vyhodb throws this exception as a result of any method invocation (on
 * {@linkplain Server}, {@linkplain Space}, {@linkplain TrxSpace} or
 * {@linkplain Record} objects) when vyhodb server is closed.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class ServerClosedException extends RuntimeException {

    private static final long serialVersionUID = 263130922155669133L;

    public ServerClosedException() {
        super("Server is closed.");
    }

    public ServerClosedException(Throwable cause) {
        super(cause);
    }
}
