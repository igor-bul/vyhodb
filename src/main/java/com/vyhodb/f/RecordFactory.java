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

import com.vyhodb.f.record.GetChildrenCount;
import com.vyhodb.f.record.GetField;
import com.vyhodb.f.record.GetChildFirst;
import com.vyhodb.f.record.SearchMaxChild;
import com.vyhodb.f.record.SearchMinChild;
import com.vyhodb.f.record.GetChildLast;
import com.vyhodb.f.record.GetParent;
import com.vyhodb.f.record.GetRecord;
import com.vyhodb.f.record.SetField;
import com.vyhodb.f.record.SetParent;
import com.vyhodb.space.IndexDescriptor;
import com.vyhodb.space.Order;
import com.vyhodb.space.Space;

/**
 * Provides static methods which construct functions for working with
 * {@linkplain Record} object which is passed as current object.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class RecordFactory {

    /**
     * Returns the first child record.
     * 
     * @param childLinkName
     *            child link name
     * @return first child record
     * 
     * @see Order
     * @see com.vyhodb.space.Record#getChildFirst(String)
     */
    public static F getChildFirst(String childLinkName) {
        return new GetChildFirst(childLinkName);
    }

    /**
     * Returns the last child record
     * 
     * @param childLinkName
     *            child link name
     * @return last child record
     * 
     * @see Order
     * @see com.vyhodb.space.Record#getChildLast(String)
     */
    public static F getChildLast(String childLinkName) {
        return new GetChildLast(childLinkName);
    }

    /**
     * Returns count of child records.
     * 
     * @param childrenLinkName
     *            child link name
     * @return child records count
     * 
     * @see com.vyhodb.space.Record#getChildrenCount(String)
     */
    public static F getChildrenCount(String childrenLinkName) {
        return new GetChildrenCount(childrenLinkName);
    }

    /**
     * Returns field's value of current record.
     * 
     * @param fieldName
     *            field name
     * @return field's value of null if field is absent.
     * 
     * @see com.vyhodb.space.Record#getField(String)
     */
    public static F getField(String fieldName) {
        return new GetField(fieldName);
    }

    /**
     * Returns parent record of current Record object.
     * 
     * @param linkName
     *            parent link name
     * @return parent Record if current object has parent link with specified
     *         linkName, null - otherwise.
     * 
     * @see com.vyhodb.space.Record#getParent(String)
     */
    public static F getParent(String linkName) {
        return new GetParent(linkName);
    }

    /**
     * Returns record by id.
     * <p>
     * Space for retrieving Record is obtained from current object by using
     * {@linkplain com.vyhodb.space.Record#getSpace()}.
     * 
     * @param recordId
     *            record identifier
     * @return retrieved Record object, or null if Record doesn't exist for
     *         specified id.
     * 
     * @see com.vyhodb.space.Record#getSpace()
     * @see Space#getRecord(long)
     */
    public static F getRecord(long recordId) {
        return new GetRecord(recordId);
    }

    /**
     * Returns child record, which has the maximal indexed field(s) value.
     * 
     * @param indexName
     *            used index to search record with maximal indexed field(s)
     *            value.
     * @return child record with maximal field value
     * 
     * @see IndexDescriptor
     * @see com.vyhodb.space.Record#searchMaxChild(String)
     */
    public static F searchMaxChild(String indexName) {
        return new SearchMaxChild(indexName);
    }

    /**
     * Returns child record, which has the minimal indexed field(s) value.
     * 
     * @param indexName
     *            used index to search record with minimal indexed field(s)
     *            value.
     * @return child record with minimal field value
     * 
     * @see IndexDescriptor
     * @see com.vyhodb.space.Record#searchMinChild(String)
     */
    public static F searchMinChild(String indexName) {
        return new SearchMinChild(indexName);
    }

    /**
     * Evaluates <b>valueF</b> function and sets result as field's value on
     * current record.
     * 
     * @param fieldName
     *            field name
     * @param valueF
     *            function, which result is assigned to current record's field
     * @return valueF evaluation result
     * 
     * @see com.vyhodb.space.Record#setField(String, Object)
     */
    public static F setField(String fieldName, F valueF) {
        return new SetField(fieldName, valueF);
    }

    /**
     * Evaluates <b>parentF</b> function, casts it result to {@linkplain Record}
     * and uses it as parent Record in new link, which is created from current
     * Record (used as child).
     * 
     * @param linkName
     *            parent link name
     * @param parentF
     *            function returns Record object which is used as parent for new
     *            link.
     * @return parentF evaluation result
     * 
     * @see com.vyhodb.space.Record#setParent(String, com.vyhodb.space.Record)
     */
    public static F setParent(String linkName, F parentF) {
        return new SetParent(linkName, parentF);
    }
}
