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

package com.vyhodb.storage.lock;

import com.vyhodb.server.TransactionRolledbackException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author User
 */
public final class LockManagerImpl implements LockManager {
    
    private static final String LOCK_TIMEOUT = "Lock timeout has expired.";
    private static final String LOCK_THREAD_INTERRUPTED = "Thread has been interrupted during waiting for a lock.";
    
    private final Lock _readLock;
    private final Lock _commitLock;
    private final Lock _modifyLock; 
    
    private final int _readTimeout;
    private final int _modifyTimeout;
    private final int _commitTimeout;
    
    public LockManagerImpl(int lockTimeout)
    {
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
        _readLock = rwLock.readLock();
        _commitLock = rwLock.writeLock();
        
        _modifyLock = new ReentrantLock(true);
        
        _readTimeout = lockTimeout;
        _modifyTimeout = lockTimeout;
        _commitTimeout = lockTimeout;
    }
    
    @Override
    public void lockRead() {
        lock0(_readLock, _readTimeout, LOCK_TIMEOUT);
    }

    @Override
    public void unlockRead() {
        _readLock.unlock();
    }

    @Override
    public void lockModify(){
        lock0(_modifyLock, _modifyTimeout, LOCK_TIMEOUT);
    }

    @Override
    public void unlockModify() {
        _modifyLock.unlock();
    }

    @Override
    public void lockCommit(){
        lock0(_commitLock, _commitTimeout, LOCK_TIMEOUT);
    }

    @Override
    public void unlockCommit() {
        _commitLock.unlock();
    }

    private void lock0(Lock lock, int timeout, String timeoutExceptionMessage)
    {
        try {
            if ( !lock.tryLock(timeout, TimeUnit.SECONDS))
                throw new TransactionRolledbackException(timeoutExceptionMessage);
        } catch (InterruptedException ex) {
            throw new TransactionRolledbackException(LOCK_THREAD_INTERRUPTED, ex);
        }
    }
}
