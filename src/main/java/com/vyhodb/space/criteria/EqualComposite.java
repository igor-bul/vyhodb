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

import java.util.Map;

import com.vyhodb.space.Criterion;
import com.vyhodb.space.IndexDescriptor;

/**
 * Values of indexed fields are equal to specified keys.
 * <p>
 * For use only with Composite indexes.
 * <p>
 * Field names and their search key values are specified by <b>Composite Search
 * Key</b>. <b>Composite Search Key</b> is a map where:
 * <ul>
 * <li><b>Key</b> is an indexed field name</li>
 * <li><b>Value</b> is a required field value</li>
 * </ul>
 * <p>
 * Order of indexed fields in composite index is important, because it defines
 * structure of composite keys and composite search key. For instance, the last
 * field values can be omitted in composite search key (so called partial search
 * key). However field gaps are not allowed for first fields or intermidiate
 * fields.
 * <p>
 * Consider composite index with four indexed fields (note their order): A, B,
 * C, D. So EqualComposite criteria with the following field combinations are
 * correct:
 * <ul>
 * <li>A, B, C, D</li>
 * <li>A, B, C</li>
 * <li>A, B</li>
 * <li>A</li>
 * </ul>
 * <p>
 * The following field combinations in EqualComposite criteria are incorrect:
 * <ul>
 * <li>B, C, D</li>
 * <li>A, D</li>
 * <li>C, D</li>
 * <li>D</li>
 * </ul>
 * 
 * @see IndexDescriptor
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class EqualComposite implements Criterion {

    private static final long serialVersionUID = -7054321633921339329L;

    @SuppressWarnings("rawtypes")
    private Map<String, ? extends Comparable> _compositeKey;

    /**
     * Deserialization constructor. For internal use only.
     */
    @Deprecated
    public EqualComposite() {
    }

    /**
     * Constructor.
     * 
     * @param compositeKey
     *            composite search key (field name -> required field value)
     */
    @SuppressWarnings("rawtypes")
    public EqualComposite(Map<String, ? extends Comparable> compositeKey) {
        if (compositeKey == null) {
            throw new IllegalArgumentException("[compositeKey] is null");
        }

        if (compositeKey.isEmpty()) {
            throw new IllegalArgumentException("[compositeKey] is empty");
        }

        _compositeKey = compositeKey;
    }

    /**
     * Returns composite search key.
     * 
     * @return composite search key
     */
    @SuppressWarnings("rawtypes")
    public Map<String, ? extends Comparable> getCompositeKey() {
        return _compositeKey;
    }
}
