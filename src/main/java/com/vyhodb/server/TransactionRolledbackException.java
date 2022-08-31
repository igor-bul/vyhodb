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

/**
 * Indicates that current transaction is rolled back because of error.
 * 
 * <p>
 * This exception is thrown in two cases:
 * <ol>
 * <li>Internal system fault has happened like IO error</li>
 * <li>Wrong actions from custom application code side, like index constraint
 * violation, wrong field value class, etc.</li>
 * <ol>
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class TransactionRolledbackException extends RuntimeException {

    private static final long serialVersionUID = 802518628043166427L;

    public TransactionRolledbackException(String message) {
        super(message);
    }

    public TransactionRolledbackException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionRolledbackException(Throwable cause) {
        super(cause);
    }

}
