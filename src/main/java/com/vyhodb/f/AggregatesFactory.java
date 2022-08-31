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

package com.vyhodb.f;

import static com.vyhodb.f.CommonFactory.clear;
import static com.vyhodb.f.CommonFactory.get;

import java.math.BigDecimal;
import java.util.Comparator;

import com.vyhodb.f.aggregates.Avg;
import com.vyhodb.f.aggregates.ClearAvg;
import com.vyhodb.f.aggregates.Count;
import com.vyhodb.f.aggregates.GetAvg;
import com.vyhodb.f.aggregates.MinMax;
import com.vyhodb.f.aggregates.MinMaxType;
import com.vyhodb.f.aggregates.Sum;
import com.vyhodb.f.aggregates.SumType;
import com.vyhodb.utils.Utils;

/**
 * Provides static methods which construct Aggregate functions.
 * <p>
 * Functions are intended to calculate sum, min, max, avg and count values.
 * <p>
 * Functions operates on value (aggregate) which is stored in context. If
 * context doesn't contain needed aggregate for particular context key, then
 * function, if required, puts new value.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class AggregatesFactory {

    /**
     * Default context key name for storing AVG value.
     */
    public static final String AVG_CONTEXT_NAME = "Sys$Avg";

    /**
     * Default context key name for storing COUNT value.
     */
    public static final String COUNT_CONTEXT_NAME = "Sys$Count";

    /**
     * Default context key name for storing MAX value.
     */
    public static final String MAX_CONTEXT_NAME = "Sys$Max";

    /**
     * Default context key name for storing MIN value.
     */
    public static final String MIN_CONTEXT_NAME = "Sys$Min";

    /**
     * Default context key name for storing SUM value.
     */
    public static final String SUM_CONTEXT_NAME = "Sys$Sum";

    /**
     * Evaluates <b>valueF</b> function and uses it result to recalculate AVG
     * value in context. AVG value in context is stored as Double object.
     * <p>
     * {@linkplain #AVG_CONTEXT_NAME} is used as a context key to store AVG
     * value.
     * 
     * @param countNull
     *            specifies how to treat null result of valueF function. true -
     *            recalculate AVG with 0 value, false - do nothing.
     * @param valueF
     *            value function
     * @return valueF evaluation result
     */
    public static F avg(boolean countNull, F valueF) {
        return avg(AVG_CONTEXT_NAME, countNull, valueF);
    }

    /**
     * Evaluates <b>valueF</b> function and uses it result to recalculate AVG
     * value in context. AVG value in context is stored as Double object.
     * <p>
     * Function does nothing when <b>valueF</b> returns <b>null</b>.
     * <p>
     * {@linkplain #AVG_CONTEXT_NAME} is used as a context key to store AVG
     * value.
     * 
     * @param valueF
     *            value function
     * @return valueF evaluation result
     */
    public static F avg(F valueF) {
        return avg(AVG_CONTEXT_NAME, false, valueF);
    }

    /**
     * Evaluates <b>valueF</b> function and uses it result to recalculate AVG
     * value in context. AVG value in context is stored as Double object.
     * 
     * @param contextKey
     *            context key where AVG value is stored
     * @param countNull
     *            specifies how to treat null result of valueF function. true -
     *            recalculate AVG with 0 value, false - do nothing.
     * @param valueF
     *            value function
     * @return valueF evaluation result
     */
    public static F avg(String contextKey, boolean countNull, F valueF) {
        return new Avg(contextKey, countNull, valueF);
    }

    /**
     * Evaluates <b>valueF</b> function and uses it result to recalculate AVG
     * value in context. AVG value in context is stored as Double object.
     * <p>
     * Function does nothing when <b>valueF</b> returns <b>null</b>.
     * 
     * @param contextKey
     *            context key where AVG value is stored
     * @param valueF
     *            value function
     * @return valueF evaluation result
     */
    public static F avg(String contextKey, F valueF) {
        return avg(contextKey, false, valueF);
    }

    /**
     * Removes AVG value from context and returns it's value.
     * <p>
     * Uses {@linkplain #AVG_CONTEXT_NAME} as a context key.
     * 
     * @return AVG value
     */
    public static F clearAvg() {
        return clearAvg(AVG_CONTEXT_NAME);
    }

    /**
     * Removes AVG value from context and returns it's value.
     * 
     * @param contextKey
     *            context key
     * @return AVG value
     */
    public static F clearAvg(String contextKey) {
        return new ClearAvg(contextKey);
    }

    /**
     * Removes counter value from context and returns it's value.
     * 
     * @return counter value
     * 
     * @see CommonFactory#clear(String)
     */
    public static F clearCount() {
        return clear(COUNT_CONTEXT_NAME);
    }

    /**
     * Removes MAX object from context and returns one.
     * <p>
     * Uses {@linkplain #MAX_CONTEXT_NAME} as a context key.
     * 
     * @return MAX object
     */
    public static F clearMax() {
        return clear(MAX_CONTEXT_NAME);
    }

    /**
     * Removes MAX object from context and returns one.
     * 
     * @param contextKey
     *            context key
     * @return MAX object
     */
    public static F clearMax(String contextKey) {
        return clear(contextKey);
    }

    /**
     * Removes MIN object from context and returns one.
     * <p>
     * Uses {@linkplain #MIN_CONTEXT_NAME} as a context key.
     * 
     * @return MIN object
     */
    public static F clearMin() {
        return clear(MIN_CONTEXT_NAME);
    }

    /**
     * Removes MIN object from context and returns one.
     * 
     * @param contextKey
     *            context key
     * @return MIN object
     */
    public static F clearMin(String contextKey) {
        return clear(contextKey);
    }

    /**
     * Removes SUM value from context and returns it's value.
     * <p>
     * {@linkplain #SUM_CONTEXT_NAME} is used as context key.
     * 
     * @return removed SUM value
     * 
     * @see CommonFactory#clear(String)
     */
    public static F clearSum() {
        return clear(SUM_CONTEXT_NAME);
    }

    /**
     * Removes SUM value from context and returns it's value.
     * 
     * @param contextKey
     *            context key
     * @return removed SUM value
     * 
     * @see CommonFactory#clear(String)
     */
    public static F clearSum(String contextKey) {
        return clear(contextKey);
    }

    /**
     * Increases counter in context.
     * <p>
     * {@linkplain #COUNT_CONTEXT_NAME} is used as context key.
     * 
     * @return new counter value.
     */
    public static F count() {
        return count(COUNT_CONTEXT_NAME);
    }

    /**
     * Increases counter in context.
     * <p>
     * Counter value, which is stored in context, is of {@linkplain Long} type.
     * 
     * @param contextKey
     *            context key
     * @return new counter value
     */
    public static F count(String contextKey) {
        return new Count(contextKey);
    }

    /**
     * Returns AVG value.
     * <p>
     * Uses {@linkplain #AVG_CONTEXT_NAME} as a context key.
     * 
     * @return AVG value
     */
    public static F getAvg() {
        return getAvg(AVG_CONTEXT_NAME);
    }

    /**
     * Returns AVG value.
     * 
     * @param contextKey
     *            context key where AVG value is stored
     * @return AVG value
     */
    public static F getAvg(String contextKey) {
        return new GetAvg(contextKey);
    }

    /**
     * Returns counter value.
     * <p>
     * {@linkplain #COUNT_CONTEXT_NAME} is used as context key.
     * 
     * @return counter value.
     * 
     * @see CommonFactory#get(String)
     */
    public static F getCount() {
        return get(COUNT_CONTEXT_NAME);
    }

    /**
     * Returns MAX object from context.
     * <p>
     * Uses {@linkplain #MAX_CONTEXT_NAME} as a context key.
     * 
     * @return MAX object
     * 
     * @see CommonFactory#get(String)
     */
    public static F getMax() {
        return get(MAX_CONTEXT_NAME);
    }

    /**
     * Returns MAX object from context.
     * 
     * @param contextKey
     *            context key
     * @return MAX object
     * 
     * @see CommonFactory#get(String)
     */
    public static F getMax(String contextKey) {
        return get(contextKey);
    }

    /**
     * Returns MIN object from context.
     * <p>
     * Uses {@linkplain #MIN_CONTEXT_NAME} as a context key.
     * 
     * @return MIN object
     * 
     * @see CommonFactory#get(String)
     */
    public static F getMin() {
        return get(MIN_CONTEXT_NAME);
    }

    /**
     * Returns MIN object from context.
     * 
     * @param contextKey
     *            context key
     * @return MIN object
     * 
     * @see CommonFactory#get(String)
     */
    public static F getMin(String contextKey) {
        return get(contextKey);
    }

    /**
     * Returns SUM value, stored in context.
     * <p>
     * {@linkplain #SUM_CONTEXT_NAME} is used as context key.
     * 
     * @return SUM value, stored in context
     * 
     * @see CommonFactory#get(String)
     */
    public static F getSum() {
        return get(SUM_CONTEXT_NAME);
    }

    /**
     * Returns SUM value, stored in context.
     * 
     * @param contextKey
     *            context key for storing SUM value
     * @return SUM value from context
     * 
     * @see CommonFactory#get(String)
     */
    public static F getSum(String contextKey) {
        return get(contextKey);
    }

    /**
     * Calculates the greater of <b>valueF</b> evaluation result and MAX object
     * stored in context. Saves calculated MAX object in context.
     * <p>
     * If comparator is passed and it isn't null, then it is used to compare
     * values. Otherwise (when comparator is null) <b>valueF</b> must return
     * {@linkplain Comparable} objects.
     * <p>
     * Uses {@linkplain #MAX_CONTEXT_NAME} as a context key for storing MAX
     * object.
     * 
     * @param comparator
     *            used to compare objects, when specified
     * @param valueF
     *            value function
     * @return valueF result
     */
    public static F max(Comparator<?> comparator, F valueF) {
        return max(MAX_CONTEXT_NAME, comparator, valueF);
    }

    /**
     * Calculates the greater of <b>valueF</b> evaluation result and MAX object
     * stored in context. Saves calculated MAX object in context.
     * <p>
     * <b>valueF</b> function must return objects which implements
     * {@linkplain Comparable} interface.
     * <p>
     * Uses {@linkplain #MAX_CONTEXT_NAME} as a context key for storing MAX
     * object.
     * 
     * @param valueF
     *            value function
     * @return valueF result
     */
    public static F max(F valueF) {
        return max(MAX_CONTEXT_NAME, null, valueF);
    }

    /**
     * Calculates the greater of <b>valueF</b> evaluation result and MAX object
     * stored in context. Saves calculated MAX object in context.
     * <p>
     * If comparator is passed and it isn't null, then it is used to compare
     * values. Otherwise (when comparator is null) <b>valueF</b> must return
     * {@linkplain Comparable} objects.
     * 
     * @param contextKey
     *            context key used for storing MAX object
     * @param comparator
     *            used to compare objects, when specified
     * @param valueF
     *            value function
     * @return valueF result
     */
    public static F max(String contextKey, Comparator<?> comparator, F valueF) {
        return new MinMax(MinMaxType.MAX, contextKey, comparator, valueF);
    }

    /**
     * Calculates the greater of <b>valueF</b> evaluation result and MAX object
     * stored in context. Saves calculated MAX object in context.
     * <p>
     * <b>valueF</b> function must return objects which implements
     * {@linkplain Comparable} interface.
     * 
     * @param contextKey
     *            context key used for storing MAX value
     * @param valueF
     *            value function
     * @return valueF result
     */
    public static F max(String contextKey, F valueF) {
        return max(contextKey, null, valueF);
    }

    /**
     * Calculates the smaller of <b>valueF</b> evaluation result and MIN object
     * stored in context. Saves calculated MIN object in context.
     * <p>
     * If comparator is passed and it isn't null, then it is used to compare
     * values. Otherwise (when comparator is null) <b>valueF</b> must return
     * {@linkplain Comparable} objects.
     * <p>
     * Uses {@linkplain #MIN_CONTEXT_NAME} as a context key for storing MIN
     * object.
     * 
     * @param comparator
     *            used to compare objects, when specified
     * @param valueF
     *            value function
     * @return valueF result
     */
    public static F min(Comparator<?> comparator, F valueF) {
        return min(MIN_CONTEXT_NAME, comparator, valueF);
    }

    /**
     * Calculates the smaller of <b>valueF</b> evaluation result and MIN object
     * stored in context. Saves calculated MIN object in context.
     * <p>
     * <b>valueF</b> function must return objects which implements
     * {@linkplain Comparable} interface.
     * <p>
     * Uses {@linkplain #MIN_CONTEXT_NAME} as a context key for storing MIN
     * object.
     * 
     * @param valueF
     *            value function
     * @return valueF result
     */
    public static F min(F valueF) {
        return min(MIN_CONTEXT_NAME, null, valueF);
    }

    /**
     * Calculates the smaller of <b>valueF</b> evaluation result and MIN object
     * stored in context. Saves calculated MIN object in context.
     * <p>
     * If comparator is passed and it isn't null, then it is used to compare
     * values. Otherwise (when comparator is null) <b>valueF</b> must return
     * {@linkplain Comparable} objects.
     * 
     * @param contextKey
     *            context key used for storing MIN object
     * @param comparator
     *            used to compare objects, when specified
     * @param valueF
     *            value function
     * @return valueF result
     */
    public static F min(String contextKey, Comparator<?> comparator, F valueF) {
        return new MinMax(MinMaxType.MIN, contextKey, comparator, valueF);
    }

    /**
     * Calculates the smaller of <b>valueF</b> evaluation result and MIN object
     * stored in context. Saves calculated MIN object in context.
     * <p>
     * <b>valueF</b> function must return objects which implements
     * {@linkplain Comparable} interface.
     * 
     * @param contextKey
     *            context key used for storing MIN value
     * @param valueF
     *            value function
     * @return valueF result
     */
    public static F min(String contextKey, F valueF) {
        return min(contextKey, null, valueF);
    }

    /**
     * Evaluates <b>value</b> function and adds it's result to SUM value, which
     * is stored inside context.
     * <p>
     * <b>value</b> function result is casted to {@linkplain BigDecimal} class
     * using {@linkplain Utils#toDecimal(Object)} method.
     * <p>
     * {@linkplain #SUM_CONTEXT_NAME} is used as context key.
     * 
     * @param valueF
     *            function, which evaluation result is added to SUM value
     * @return value function result
     */
    public static F sum(F valueF) {
        return sum(SumType.DECIMAL, SUM_CONTEXT_NAME, valueF);
    }

    /**
     * Evaluates <b>value</b> function, adds it result to SUM value, which is
     * stored inside context with <b>contextKey</b> key.
     * <p>
     * <b>value</b> function result is casted to {@linkplain BigDecimal} class
     * using {@linkplain Utils#toDecimal(Object)} method.
     * 
     * @param contextKey
     *            context key where SUM value is stored
     * @param valueF
     *            function, which evaluation result is added to SUM value
     * @return value function result
     */
    public static F sum(String contextKey, F valueF) {
        return sum(SumType.DECIMAL, contextKey, valueF);
    }

    /**
     * Evaluates <b>value</b> function, casts it result to type specified by
     * {@linkplain SumType} and adds to SUM value, which is stored inside
     * context.
     * <p>
     * {@linkplain #SUM_CONTEXT_NAME} is used as context key.
     * 
     * @param sumType
     *            specifies class which is used for casting value function
     *            result and storing SUM value.
     * @param valueF
     *            function, which evaluation result is added to SUM value
     * @return function result
     */
    public static F sum(SumType sumType, F valueF) {
        return sum(sumType, SUM_CONTEXT_NAME, valueF);
    }

    /**
     * Evaluates <b>value</b> function, casts it result to type specified by
     * {@linkplain SumType} and adds to SUM value, which is stored inside
     * context with <b>contextKey</b> key.
     * 
     * @param sumType
     *            specifies class which is used for casting value function
     *            result and storing SUM value.
     * @param contextKey
     *            context key where SUM value is stored
     * @param valueF
     *            function, which evaluation result is added to SUM value
     * @return value function result
     */
    public static F sum(SumType sumType, String contextKey, F valueF) {
        return new Sum(sumType, contextKey, valueF);
    }
}
