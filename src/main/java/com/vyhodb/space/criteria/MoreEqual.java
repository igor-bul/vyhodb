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
 * Indexed field value is MORE OR EQUAL than specified key.
 * <p>
 * Can be used only with simple indexes.
 * 
 * @see Record
 * @see Criterion
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class MoreEqual extends SingleKeyCriterion {

    private static final long serialVersionUID = -2209559986803285031L;

    /**
     * Deserialization constructor. For internal use only.
     */
    @Deprecated
    public MoreEqual() {
    }

    /**
     * Constructor.
     * 
     * @param key
     *            search key
     */
    @SuppressWarnings("rawtypes")
    public MoreEqual(Comparable key) {
        super(key);
    }
}