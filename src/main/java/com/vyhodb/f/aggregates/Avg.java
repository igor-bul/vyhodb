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

package com.vyhodb.f.aggregates;

import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.utils.Utils;

public class Avg extends F {

    private static final long serialVersionUID = 5190104363585744916L;

    private String _contextKey;
    private boolean _countNull;
    private F _value;

    @Deprecated
    public Avg() {
    }

    public Avg(String contextKey, boolean countNull, F value) {

        if (contextKey == null) {
            throw new IllegalArgumentException("[contextKey] is null");
        }

        if (value == null) {
            throw new IllegalArgumentException("[value] is null");
        }

        _contextKey = contextKey;
        _countNull = countNull;
        _value = value;
    }

    @Override
    public Object evalTree(Object current, Map<String, Object> context) {
        AvgState state = (AvgState) context.get(_contextKey);
        if (state == null) {
            state = new AvgState();
        }

        Object value = _value.evalTree(current, context);
        if (_countNull || value != null) {
            state.addValue(Utils.toDouble(value));
            context.put(_contextKey, state);
        }

        return value;
    }

}
