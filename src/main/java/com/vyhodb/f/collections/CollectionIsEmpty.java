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

package com.vyhodb.f.collections;

import java.util.Collection;
import java.util.Map;

import com.vyhodb.f.Predicate;

public class CollectionIsEmpty extends Predicate {

    private static final long serialVersionUID = 1867389265953459947L;
    private String _contextKey;

    public CollectionIsEmpty() {
    }

    public CollectionIsEmpty(String contextKey) {
        if (contextKey == null) {
            throw new IllegalArgumentException("[contextKey] is null");
        }

        _contextKey = contextKey;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Boolean evalTree(Object current, Map<String, Object> context) {
        Collection collection = (Collection) context.get(_contextKey);
        return collection.isEmpty();
    }
}
