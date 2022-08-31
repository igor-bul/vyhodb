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
import java.util.Map;

import com.vyhodb.space.Record;

class JsonPrintStack extends AbstractPrintStack {

    private static final char ARRAY_END = ']';
    private static final char ARRAY_START = '[';
    private static final char COMMA = ',';
    private static final char KEY_VALUE = ':';
    private static final char OBJECT_END = '}';
    private static final char OBJECT_START = '{';
    private static final char QUOTATION = '"';
    private static final char SPACE = ' ';

    private boolean _formatted;
    private String _lineSeparator = System.lineSeparator();
    private int _tabSize;

    public JsonPrintStack(Record rootRecord, String[] fieldsFilter, int tabSize, boolean formatted) {
        super(rootRecord, fieldsFilter);

        _tabSize = tabSize;
        _formatted = formatted;
    }

    private void newLine(StringBuilder builder, int level) {
        if (_formatted) {
            builder.append(_lineSeparator);

            final int tab = level * _tabSize;
            for (int i = 0; i < tab; i++) {
                builder.append(SPACE);
            }
        }
    }

    private void printChildren(StringBuilder builder, String linkName, ArrayList<RecordHolder> childrens, int level) {
        builder.append(COMMA);
        printKey(builder, linkName, level);
        builder.append(ARRAY_START);

        level++;

        boolean first = true;
        for (RecordHolder child : childrens) {
            if (first) {
                first = false;
            } else {
                builder.append(COMMA);
            }

            printRecord(builder, child, level);
        }

        level--;
        newLine(builder, level);
        builder.append(ARRAY_END);
    }

    private void printField(StringBuilder builder, String fieldName, Object value, int level) {
        builder.append(COMMA);

        printKey(builder, fieldName, level);
        printValue(builder, value);
    }

    private void printKey(StringBuilder builder, String keyName, int level) {
        newLine(builder, level);
        builder.append(QUOTATION).append(keyName).append(QUOTATION).append(KEY_VALUE);
    }

    private void printParent(StringBuilder builder, String linkName, RecordHolder parent, int level) {
        builder.append(COMMA);
        printKey(builder, linkName, level);
        level++;

        printRecord(builder, parent, level);
    }

    @Override
    protected void printRecord(StringBuilder builder, RecordHolder record, int level) {
        // Prints {
        newLine(builder, level);
        builder.append(OBJECT_START);
        level++;

        // Prints record id
        newLine(builder, level);
        builder.append("\"id\":").append(record.id);

        // Prints records' fields
        for (Map.Entry<String, Object> entryField : record.fields.entrySet()) {
            printField(builder, entryField.getKey(), entryField.getValue(), level);
        }

        // Prints parents
        for (Map.Entry<String, RecordHolder> entryParent : record.parents.entrySet()) {
            printParent(builder, entryParent.getKey(), entryParent.getValue(), level);
        }

        // Prints children
        for (Map.Entry<String, ArrayList<RecordHolder>> entryChildren : record.children.entrySet()) {
            printChildren(builder, entryChildren.getKey(), entryChildren.getValue(), level);
        }

        level--;
        newLine(builder, level);
        builder.append(OBJECT_END);
    }

    private void printValue(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
            return;
        }

        if (value instanceof String) {
            builder.append(QUOTATION).append(value).append(QUOTATION);
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
            return;
        }

        if (value instanceof Object[]) {
            builder.append(Arrays.toString((Object[]) value));
            return;
        }

        builder.append(QUOTATION).append(value).append(QUOTATION);
    }
}
