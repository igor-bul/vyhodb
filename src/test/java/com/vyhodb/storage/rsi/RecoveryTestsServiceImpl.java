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

package com.vyhodb.storage.rsi;

import java.util.Random;

import com.vyhodb.space.IndexDescriptor;
import com.vyhodb.space.IndexedField;
import com.vyhodb.space.Nullable;
import com.vyhodb.space.Record;
import com.vyhodb.space.RecordCommons;
import com.vyhodb.space.ServiceLifecycle;
import com.vyhodb.space.Space;

public class RecoveryTestsServiceImpl implements RecoveryTestsService, ServiceLifecycle {

    private Space _space;

    @Override
    public void createIndex() {
        IndexDescriptor descriptor = new IndexDescriptor(INDEX_NAME, LINK_NAME, new IndexedField(RANDOM_FIELD_NAME, Integer.class, Nullable.NULL));
        Record root = _space.getRecord(0L);
        if (! root.containsIndex(INDEX_NAME)) {
            root.createIndex(descriptor);
        }
    }

    @Override
    public void addRecords(int count) {
        Random random = new Random();
        Record root = _space.getRecord(0L);
        Record child;
        
        for (int i = 0; i < count; i++) {
            child = _space.newRecord();
            fillFields(random, child);
            child.setParent(LINK_NAME, root);
        }
    }

    @Override
    public void addRecordsAndPause(int count, long pause) {
        addRecords(count);
        
        try {
            Thread.sleep(pause);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSpace(Space space) {
        _space = space;
    }
    
    private void fillFields(Random random, Record record) {
        RecordCommons.setPrivitiveFields(record);
        record.setField(RANDOM_FIELD_NAME, random.nextInt());
    }

    @Override
    public void addRecordsAndRollback(int count) {
        Random random = new Random();
        Record root = _space.getRecord(0L);
        Record child;
        
        for (int i = 0; i < count; i++) {
            child = _space.newRecord();
            fillFields(random, child);
            child.setParent(LINK_NAME, root);
        }
        
        throw new IllegalArgumentException("Rolled Back");
    }

}
