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

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import com.vyhodb.space.Criterion;

/**
 * Indexed field value is contained in {@link Collection} object.
 * <p>
 * For use with simple indexes only.
 *
 * @see Record
 * @see Criterion
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public final class In implements Criterion {

    private static final long serialVersionUID = 6408194599162626560L;

    @SuppressWarnings("rawtypes")
    private TreeSet<Comparable> _searchKeys;

    /**
     * Deserialization constructor. For internal use only.
     */
    @Deprecated
    public In() {
    }

    /**
     * Constructor.
     * 
     * @param searchKeys
     *            search keys collection
     */
    @SuppressWarnings("rawtypes")
    public In(Collection<? extends Comparable> searchKeys) {
        if (searchKeys == null)
            throw new IllegalArgumentException("[searchKeys] is null");
        if (searchKeys.isEmpty())
            throw new IllegalArgumentException("[searchKeys] is empty");

        _searchKeys = new TreeSet<>();
        _searchKeys.addAll(searchKeys);
    }

    /**
     * Constructor.
     * 
     * @param searchKeys
     *            search keys
     */
    @SuppressWarnings("rawtypes")
    public In(Comparable... searchKeys) {
        this(Arrays.asList(searchKeys));
    }

    /**
     * Returns search keys collection.
     * 
     * @return search keys collection
     */
    @SuppressWarnings("rawtypes")
    public TreeSet<Comparable> getSearchKeys() {
        return _searchKeys;
    }
}
