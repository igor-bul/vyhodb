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

/**
 * Provides search criteria classes for index search 
 * ({@linkplain com.vyhodb.space.Record#searchChildren(String, com.vyhodb.space.Criterion, com.vyhodb.space.Order) 
 * Record.searchChildren()}).
 * <p>
 * Depending of index type (<b>Simple</b>/<b>Composite</b>) some criteria could be unavailable. 
 * Table below shows which index types are supported by which search criteria.
 * <p>
 * <table border="1" style="text-align: center">
 *  <tr>
 *      <th>Criteria</th>
 *      <th>Simple Index</th>
 *      <th>Composite Index</th>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.All All}</td>
 *      <td>+</td>
 *      <td>+</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.Between Between}</td>
 *      <td>+</td>
 *      <td>&nbsp;</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.BetweenExclusive BetweenExclusive}</td>
 *      <td>+</td>
 *      <td>&nbsp;</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.Equal Equal}</td>
 *      <td>+</td>
 *      <td>&nbsp;</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.EqualComposite EqualComposite}</td>
 *      <td>&nbsp;</td>
 *      <td>+</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.In In}</td>
 *      <td>+</td>
 *      <td>&nbsp;</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.Less Less}</td>
 *      <td>+</td>
 *      <td>&nbsp;</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.LessEqual LessEqual}</td>
 *      <td>+</td>
 *      <td>&nbsp;</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.More More}</td>
 *      <td>+</td>
 *      <td>&nbsp;</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.MoreEqual MoreEqual}</td>
 *      <td>+</td>
 *      <td>&nbsp;</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.NotNull NotNull}</td>
 *      <td>+</td>
 *      <td>&nbsp;</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.Null Null}</td>
 *      <td>+</td>
 *      <td>&nbsp;</td>
 *  </tr>
 *  <tr>
 *      <td>{@link com.vyhodb.space.criteria.StartsWith StartsWith}</td>
 *      <td>+</td>
 *      <td>&nbsp;</td>
 *  </tr>
 * </table>
 * 
 * @see com.vyhodb.space.Record
 * @see com.vyhodb.space.IndexDescriptor
 * 
 */
package com.vyhodb.space.criteria;

