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

package com.vyhodb.space;

/**
 * Child records iteration order.
 * <p>
 * Enumerator is used in retrieving child, sibling records and index search
 * result.
 * <p>
 * For child records {@linkplain #ASC} is an order in which child links were
 * created. {@linkplain #DESC} is an backward order. See
 * {@linkplain Record#getChildren(String, Order)}.
 * <p>
 * In index search result, records are arranged based on their indexed field
 * values. {@linkplain #ASC} specifies ascending direction whereas
 * {@linkplain #DESC} specifies descendant direction of iterating. See
 * {@linkplain Record#searchChildren(String, Criterion, Order)}.
 * <p>
 * For sibling records {@linkplain #ASC} is used for iterate over records which
 * parent links were created after current record's link. {@linkplain #DESC} is
 * used to iterate over records which parent links had been created before
 * current record's link. See {@linkplain Record#getSiblings(String, Order)}.
 * 
 * @see Record
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public enum Order {
    /**
     * Ascending order
     */
    ASC,

    /**
     * Descending order
     */
    DESC
}
