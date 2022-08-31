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

package com.vyhodb.f.print;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import com.vyhodb.f.Stack;
import com.vyhodb.f.record.RecordExpectedException;
import com.vyhodb.space.Record;

abstract class AbstractPrintStack implements Stack {

    protected class RecordHolder {

        public final HashMap<String, ArrayList<RecordHolder>> children = new HashMap<>();
        public final HashMap<String, Object> fields = new HashMap<>();
        public final long id;
        public final HashMap<String, RecordHolder> parents = new HashMap<>();

        protected RecordHolder(Record record, String[] fieldNames) {
            id = record.getId();

            Collection<String> fieldNames0 = fieldNames == null ? record.getFieldNames() : Arrays.asList(fieldNames);
            for (String fieldName : fieldNames0) {
                Object value = record.getField(fieldName);
                if (value != null) {
                    fields.put(fieldName, value);
                }
            }
        }

        protected void addChild(String linkName, RecordHolder child) {
            ArrayList<RecordHolder> childArray = children.get(linkName);
            if (childArray == null) {
                childArray = new ArrayList<>();
                children.put(linkName, childArray);
            }

            childArray.add(child);
        }

        public void print(StringBuilder builder) {
            builder.append('{');
            printFields(builder);
            builder.append("} id=").append(id);
        }

        private void printFields(StringBuilder builder) {
            boolean first = true;
            for (String fieldName : fields.keySet()) {
                if (first) {
                    first = false;
                } else {
                    builder.append(FIELD_SEPARATOR);
                }

                builder.append(fieldName).append(FIELD_VALUE_SEPARATOR);
                printValue(builder, fields.get(fieldName));
            }
        }

        private void printValue(StringBuilder builder, Object value) {
            if (value instanceof String || value instanceof Date) {
                builder.append('"').append(value).append('"');
            } else {
                builder.append(value);
            }
        }

        protected void setParent(String linkName, RecordHolder parent) {
            parents.put(linkName, parent);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            print(builder);
            return builder.toString();
        }
    }
    private static final int BUILDER_SIZE = 2048;

    private final static char[] FIELD_SEPARATOR = new char[] { ',', ' ' };
    private final static char FIELD_VALUE_SEPARATOR = '=';

    protected static final int TAB_SIZE = 4;
    private String[] _fieldsFilter;
    protected RecordHolder _root;

    protected LinkedList<RecordHolder> _stack;

    AbstractPrintStack(Record record, String[] fieldsFilter) {
        _fieldsFilter = fieldsFilter;
        _root = new RecordHolder(record, _fieldsFilter);
        _stack = new LinkedList<>();
        _stack.push(_root);
    }

    @Override
    public Object peek() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pop() {
        _stack.pop();
    }

    String print() {
        if (_root != null) {
            StringBuilder builder = new StringBuilder(BUILDER_SIZE);
            printRecord(builder, _root, 0);
            return builder.toString();
        }

        return "";
    }

    protected abstract void printRecord(StringBuilder builder, RecordHolder record, int level);

    @Override
    public void pushChild(String linkName, Object child) {
        if (!(child instanceof Record)) {
            throw new RecordExpectedException(child);
        }

        RecordHolder holder = new RecordHolder((Record) child, _fieldsFilter);
        RecordHolder last = _stack.peek();

        last.addChild(linkName, holder);

        _stack.push(holder);
    }

    @Override
    public void pushParent(String linkName, Object parent) {
        if (!(parent instanceof Record)) {
            throw new RecordExpectedException(parent);
        }

        RecordHolder holder = new RecordHolder((Record) parent, _fieldsFilter);
        RecordHolder last = _stack.peek();

        last.setParent(linkName, holder);

        _stack.push(holder);
    }
}
