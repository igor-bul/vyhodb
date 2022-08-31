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

package com.vyhodb.f;

import java.util.Collection;

import com.vyhodb.f.collections.CollectionAdd;
import com.vyhodb.f.collections.CollectionClear;
import com.vyhodb.f.collections.CollectionContains;
import com.vyhodb.f.collections.CollectionIsEmpty;
import com.vyhodb.f.collections.CollectionRemove;

import static com.vyhodb.f.CommonFactory.*;

/**
 * Provides static methods which construct functions for working with
 * Collections.
 * <p>
 * {@linkplain Collection} object must be stored in context, so that functions
 * can get it by using appropriate context key.
 * <p>
 * Collection objects can be put into context by using
 * {@linkplain CommonFactory#put(String, Object)} method.
 * 
 * @see CommonFactory
 * @see Collection
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class CollectionFactory {

    /**
     * Adds element, returned by <b>elementF</b> function into collection.
     * 
     * @param contextKey
     *            context key of collection object
     * @param elementF
     *            element function
     * @return elementF result
     * 
     * @see Collection#add(Object)
     */
    public static F collectionAdd(String contextKey, F elementF) {
        return new CollectionAdd(contextKey, elementF);
    }

    /**
     * Adds element into collection.
     * 
     * @param contextKey
     *            context key of collection object
     * @param element
     *            element object
     * @return element object
     * 
     * @see Collection#add(Object)
     */
    public static F collectionAdd(String contextKey, Object element) {
        return new CollectionAdd(contextKey, c(element));
    }

    /**
     * Clears collection.
     * 
     * @param contextKey
     *            context key of collection object
     * @return cleared collection object
     * 
     * @see Collection#clear()
     */
    public static F collectionClear(String contextKey) {
        return new CollectionClear(contextKey);
    }

    /**
     * Checks whether collection contains element, returned by <b>elementF</b>
     * function evaluation.
     * 
     * @param contextKey
     *            context key of collection object
     * @param elementF
     *            element function
     * @return Boolean.TRUE if collection contains object returned by
     *         <b>elementF</b> function, Boolean.FALSE - otherwise.
     * 
     * @see Collection#contains(Object)
     * 
     */
    public static Predicate collectionContains(String contextKey, F elementF) {
        return new CollectionContains(contextKey, elementF);
    }

    /**
     * Checks whether collection contains element.
     * 
     * @param contextKey
     *            context key of collection object
     * @param element
     *            element object
     * @return Boolean.TRUE if collection contains specified element,
     *         Boolean.FALSE - otherwise.
     * 
     * @see Collection#contains(Object)
     */
    public static Predicate collectionContains(String contextKey, Object element) {
        return new CollectionContains(contextKey, c(element));
    }

    /**
     * Checks whether collection is empty.
     * 
     * @param contextKey
     *            context key of collection object
     * @return Boolean.TRUE if collection is empty, Boolean.FALSE otherwise
     * 
     * @see Collection#isEmpty()
     */
    public static Predicate collectionIsEmpty(String contextKey) {
        return new CollectionIsEmpty(contextKey);
    }

    /**
     * Removes element, returned by <b>elementF</b> function from collection.
     * 
     * @param contextKey
     *            context key of collection object
     * @param elementF
     *            element function
     * @return elementF result
     * 
     * @see Collection#remove(Object)
     */
    public static F collectionRemove(String contextKey, F elementF) {
        return new CollectionRemove(contextKey, elementF);
    }

    /**
     * Removes element from collection.
     * 
     * @param contextKey
     *            context key of collection object
     * @param element
     *            element object
     * @return element object
     * 
     * @see Collection#remove(Object)
     */
    public static F collectionRemove(String contextKey, Object element) {
        return new CollectionRemove(contextKey, c(element));
    }
}
