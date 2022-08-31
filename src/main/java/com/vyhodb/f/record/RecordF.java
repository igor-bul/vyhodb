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

package com.vyhodb.f.record;

import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.space.Record;

/**
 * Base class for record functions.
 * <p>
 * Functions which are inherited from this class expect that passed current
 * object is an instance of {@linkplain Record} interface. If it isn't than
 * functions throw {@linkplain RecordExpectedException}.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 * 
 * @see Record
 * @see RecordExpectedException
 */
@SuppressWarnings("serial")
public abstract class RecordF extends F {

    /**
     * Evaluates current Record object.
     * <p>
     * This method must be implemented by record functions.
     * 
     * @param record
     *            current record
     * @param context
     *            evaluation context
     * @return evaluation result
     */
    protected abstract Object evalRecord(Record record, Map<String, Object> context);

    /**
     * Checks whether current object is Record object and invokes
     * {@linkplain #evalRecord(Record, Map)}.
     */
    @Override
    public Object evalTree(Object current, Map<String, Object> context) {
        if (!(current instanceof Record)) {
            throw new RecordExpectedException(current);
        }

        return evalRecord((Record) current, context);
    }
}
