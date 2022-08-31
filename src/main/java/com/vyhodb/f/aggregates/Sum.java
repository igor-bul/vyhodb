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

import java.math.BigDecimal;
import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.utils.Utils;

public class Sum extends F {

    /**
     * 
     */
    private static final long serialVersionUID = 4914895891888895346L;
    private String _contextKey;
    private SumType _sumType;
    private F _value;

    @Deprecated
    public Sum() {
    }

    public Sum(SumType sumType, String contextKey, F value) {
        if (sumType == null) {
            throw new IllegalArgumentException("[sumType] is null");
        }

        if (contextKey == null) {
            throw new IllegalArgumentException("[contextKey] is null");
        }

        if (value == null) {
            throw new IllegalArgumentException("[value] is null");
        }

        _sumType = sumType;
        _contextKey = contextKey;
        _value = value;
    }

    private Object add(Object sum, Object value) {
        switch (_sumType) {
        case DECIMAL:
            return addDecimal(sum, value);

        case DOUBLE:
            return addDouble(sum, value);

        case LONG:
            return addLong(sum, value);
        }

        throw new IllegalArgumentException("Unknown sum type:" + _sumType);
    }

    private BigDecimal addDecimal(Object sum, Object value) {
        return Utils.toDecimal(sum).add(Utils.toDecimal(value));
    }

    private Double addDouble(Object sum, Object value) {
        return Utils.toDouble(sum) + Utils.toDouble(value);
    }

    private Long addLong(Object sum, Object value) {
        return Utils.toLong(sum) + Utils.toLong(value);
    }

    @Override
    public Object evalTree(Object current, Map<String, Object> context) {
        Object sum = context.get(_contextKey);
        Object value = _value.evalTree(current, context);

        sum = add(sum, value);
        context.put(_contextKey, sum);

        return value;
    }
}
