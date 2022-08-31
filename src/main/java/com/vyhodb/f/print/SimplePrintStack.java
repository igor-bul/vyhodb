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
import java.util.Map;

import com.vyhodb.space.Record;

class SimplePrintStack extends AbstractPrintStack {

    private String _lineSeparator = System.lineSeparator();
    private int _slashSize;
    private int _tabSize;

    SimplePrintStack(Record rootRecord, String[] fieldsFilter, int tabSize) {
        super(rootRecord, fieldsFilter);

        _tabSize = tabSize;
        _slashSize = tabSize - 1;
    }

    private void printChildren(StringBuilder builder, String linkName, ArrayList<RecordHolder> childrens, int level) {
        level++;
        printTab(builder, level);
        builder.append('\"').append(linkName).append("\" \u2190").append(_lineSeparator);

        for (RecordHolder child : childrens) {
            printRecord(builder, child, level);
        }
    }

    private void printParent(StringBuilder builder, String linkName, RecordHolder parent, int level) {
        level++;
        printTab(builder, level);
        builder.append('\"').append(linkName).append("\" \u2192").append(_lineSeparator);

        printRecord(builder, parent, level);
    }

    protected void printRecord(StringBuilder builder, RecordHolder record, int level) {
        level++;

        printTab(builder, level);
        record.print(builder);
        builder.append(_lineSeparator);

        // Prints parents
        for (Map.Entry<String, RecordHolder> entryParent : record.parents.entrySet()) {
            printParent(builder, entryParent.getKey(), entryParent.getValue(), level);
        }

        // Prints children
        for (Map.Entry<String, ArrayList<RecordHolder>> entryChildren : record.children.entrySet()) {
            printChildren(builder, entryChildren.getKey(), entryChildren.getValue(), level);
        }
    }

    private void printTab(StringBuilder builder, int level) {
        if (level <= 0)
            return;

        final int tab = (level - 1) * _tabSize;
        for (int i = 0; i < tab; i++) {
            builder.append(' ');
        }

        builder.append(' ');

        for (int i = 0; i < _slashSize; i++) {
            builder.append(' ');
        }
    }
}
