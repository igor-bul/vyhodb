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

package com.vyhodb.f.navigation;

import com.vyhodb.f.Stack;

/**
 * Dummy Stack implementation.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class DummyStack implements Stack {

    /**
     * Singleton pattern field.
     */
    public static final DummyStack SINGLETON = new DummyStack();

    /**
     * Method throws {@link UnsupportedOperationException}.
     */
    @Override
    public Object peek() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pop() {
    }

    @Override
    public void pushChild(String linkName, Object child) {
    }

    @Override
    public void pushParent(String linkName, Object parent) {
    }
}
