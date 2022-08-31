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

package com.vyhodb.space.criteria;

import com.vyhodb.space.Criterion;

/**
 * Base class for search criteria with single search key.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public abstract class SingleKeyCriterion implements Criterion {

    private static final long serialVersionUID = 455662640434756365L;

    @SuppressWarnings("rawtypes")
    private Comparable _key;

    /**
     * Deserialization constructor.
     */
    protected SingleKeyCriterion() {
    }

    /**
     * Constructor.
     * 
     * @param key
     *            search key
     */
    @SuppressWarnings("rawtypes")
    protected SingleKeyCriterion(Comparable key) {
        if (key == null) {
            throw new IllegalArgumentException("[key] is null");
        }

        _key = key;
    }

    /**
     * Returns search key.
     * 
     * @return search key
     */
    @SuppressWarnings("rawtypes")
    public Comparable getKey() {
        return _key;
    }
}
