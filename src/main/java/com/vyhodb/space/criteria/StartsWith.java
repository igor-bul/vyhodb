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

import com.vyhodb.space.IndexedField;

/**
 * Indexed field value starts with specified String prefix.
 * <p>
 * For use with simple indexes only. Applicable only for String indexed fields.
 * 
 * @see IndexedField
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class StartsWith extends SingleKeyCriterion {

    private static final long serialVersionUID = 6340679152051017233L;

    private static String validatePrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("[prefix] is null");
        }

        if (prefix.isEmpty()) {
            throw new IllegalArgumentException("[prefix] is empty");
        }

        return prefix;
    }

    /**
     * Deserialization constructor. For internal use only.
     */
    @Deprecated
    public StartsWith() {
    }

    /**
     * Constructor.
     * 
     * @param prefix
     *            string prefix
     */
    public StartsWith(String prefix) {
        super(validatePrefix(prefix));
    }
}
