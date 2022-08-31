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
 * Represents vyhodb storage as a space of records.
 * <p>
 * Space is a record container. It stores records, allows to create new records
 * and retrieve existed by their id.
 * 
 * <p>
 * <b>Root record</b>
 * <p>
 * There is a special record which is called “Root record”. Root record
 * identifier is always zero (0). Root record is created at space creation time
 * and can't be deleted. Root record can be used as start point in space
 * traversal.
 * 
 * <p>
 * <b>Thread safety</b>
 * <p>
 * Objects of this class are not thread safe.
 * 
 * @see Record
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.6.0
 */
public interface Space {

    /**
     * Retrieves record by its identifier.
     * 
     * @param id
     *            record identifier
     * @return record, if record with specified id exist, null otherwise
     */
    public Record getRecord(long id);

    /**
     * Indicated read-only mode.
     * 
     * @return whether current space is in read-only mode or not
     */
    public boolean isReadOnly();

    /**
     * Creates new record.
     * 
     * @return created record
     */
    public Record newRecord();
}
