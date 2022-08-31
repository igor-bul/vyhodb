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
 * Indexed field value is in range.
 * <p>
 * Range is inclusive.
 * <p>
 * For use with simple indexes only.
 * 
 * @see Record
 * @see Criterion
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class Between implements Criterion {

    private static final long serialVersionUID = -1826991751436276928L;

    @SuppressWarnings("rawtypes")
    private Comparable _from;

    @SuppressWarnings("rawtypes")
    private Comparable _to;

    /**
     * Deserialization constructor. For internal use only.
     */
    @Deprecated
    public Between() {
    }

    /**
     * Constructor.
     * 
     * @param from
     *            lower range limit
     * @param to
     *            upper range limit
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Between(Comparable from, Comparable to) {
        if (from == null)
            throw new IllegalArgumentException("[from] is null");
        if (to == null)
            throw new IllegalArgumentException("[to] is null");

        if (!from.getClass().equals(to.getClass()))
            throw new IllegalArgumentException("[from] and [to] classes are different");
        if (from.compareTo(to) > 0)
            throw new IllegalArgumentException("[from] > [to]");

        _from = from;
        _to = to;
    }

    /**
     * Returns lower range limit.
     * 
     * @return lower range limit
     */
    @SuppressWarnings("rawtypes")
    public Comparable getFrom() {
        return _from;
    }

    /**
     * Returns upper range limit.
     * 
     * @return upper range limit
     */
    @SuppressWarnings("rawtypes")
    public Comparable getTo() {
        return _to;
    }
}
