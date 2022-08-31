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
 * This interface represents transactional "Space API".
 * <p>
 * Objects of this class are created by {@linkplain Server} as a result of
 * starting new transaction.
 * 
 * <p>
 * <b>Thread safety</b>
 * <p>
 * Objects of this class are NOT thread safe. They should be used by only one
 * thread throughout its lifetime.
 * 
 * @see Server
 * @see NotActiveTransactionException
 * @see TransactionRolledbackException
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public interface TrxSpace extends Space {

    /**
     * Commits current transaction.
     */
    public void commit();

    /**
     * Checks whether current transaction is active or not (have already been
     * committed or rolled back).
     * 
     * @return true - current transaction is active, false - otherwise
     */
    public boolean isActive();

    /**
     * Rolls back current transaction.
     */
    public void rollback();
}
