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

package com.vyhodb.rsi;

import java.util.Iterator;
import java.util.Map;

import com.vyhodb.rsi.RsiClientException;
import com.vyhodb.rsi.RsiServerException;
import com.vyhodb.rsi.Version;
import com.vyhodb.space.Record;
import com.vyhodb.space.ServiceLifecycle;
import com.vyhodb.space.Space;

@Version(version="1.0")
public class UnitTestServiceImpl implements UnitTestService, ServiceLifecycle {

    private Space _space;

    @Override
    public void setSpace(Space space) {
        _space = space;
    }

    @Override
    public long addRecord(Map<String, Object> fields) {
        Record root = _space.getRecord(0L);
        Record newRecord = _space.newRecord();
        
        for (Map.Entry<String, Object> field : fields.entrySet()) {
            newRecord.setField(field.getKey(), field.getValue());
        }
        
        newRecord.setParent(LINK_NAME, root);
        return newRecord.getId();
    }

    @Override
    public long count() {
        long count = 0;
        
        Record root = _space.getRecord(0L);
        
        for (Record child : root.getChildren(LINK_NAME)) {
            count++;
        }
        
        return count;
    }

    @Override
    public void throwException() throws IllegalStateException {
        Record root = _space.getRecord(0L);
        root.removeChildren(LINK_NAME);
        throw new IllegalStateException("Exception for unit test.");
    }

    
    @Override
    public void modify() throws RsiServerException, RsiClientException {
        Record root = _space.getRecord(0L);
        root.setField("Modify", "Modify");
    }

}
