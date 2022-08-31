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

package com.vyhodb.utils;

import java.math.BigDecimal;
import java.util.Properties;

/**
 * Core framework utility class.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class Utils {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static int compare(Comparable c1, Comparable c2) {
        if (c1 == null && c2 == null)
            return 0;
        if (c1 == null)
            return -1;
        if (c2 == null)
            return 1;

        return c1.compareTo(c2);
    }

    @SuppressWarnings("rawtypes")
    public static boolean equal(Comparable v1, Comparable v2) {
        return compare(v1, v2) == 0;
    }

    public static boolean equal(Object o1, Object o2) {
        if (o2 == null && o1 == null) {
            return true;
        }

        if (o1 == null) {
            return false;
        }

        return o1.equals(o2);
    }

    public static int getProperty(Properties properties, String popertyName, int defaultValue) {
        String value = properties.getProperty(popertyName);
        if (value == null) {
            return defaultValue;
        } else {
            return Integer.decode(value);
        }
    }

    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    public static boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }

        return false;
    }

    public static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }

        return BigDecimal.ZERO;
    }

    public static double toDouble(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        return 0;
    }

    public static long toLong(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return 0;
    }
}
