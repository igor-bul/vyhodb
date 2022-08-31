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

import com.vyhodb.f.common.Case;
import com.vyhodb.f.common.Clear;
import com.vyhodb.f.common.Composite;
import com.vyhodb.f.common.Const;
import com.vyhodb.f.common.Current;
import com.vyhodb.f.common.Get;
import com.vyhodb.f.common.If;
import com.vyhodb.f.common.Loop;
import com.vyhodb.f.common.Nil;
import com.vyhodb.f.common.OmitContext;
import com.vyhodb.f.common.Print;
import com.vyhodb.f.common.Put;
import com.vyhodb.f.common.When;

/**
 * Provides static methods which construct Common functions.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class CommonFactory {

    /**
     * Evaluates <b>valueF</b> function and passes it result through
     * {@linkplain When} substitution chain.
     * <p>
     * See method {@linkplain #when(Object, Object)} to construct When objects.
     * 
     * @param valueF
     *            function, which evaluation result is passed through
     *            <b>When</b> chain.
     * @param whens
     *            substitution chain
     * @return substitution chain result
     */
    public static F _case(F valueF, When... whens) {
        return new Case(valueF, whens);
    }

    /**
     * Evaluates <b>valueF</b> function and if it result is null, then returns
     * <b>nullReplaceValue</b> object.
     * 
     * @param valueF
     *            function
     * @param nullReplaceValue
     * @return nullReplaceValue object if <b>valueF</b> is null. valueF result
     *         itself - otherwise.
     */
    public static F _caseNull(F valueF, Object nullReplaceValue) {
        return _case(valueF, when(null, nullReplaceValue));
    }

    /**
     * Evaluates predicate and, if it returns {@linkplain Boolean#TRUE},
     * evaluates <b>trueF</b> functions.
     * 
     * @param predicate
     *            predicate function
     * @param trueF
     *            function list for true result
     * @return evaluation result of last <b>trueF</b> function, when predicate's
     *         result is true. When predicate's result is false - returns
     *         <b>null</b>.
     */
    public static F _if(Predicate predicate, F... trueF) {
        return new If(predicate, composite(trueF), nil());
    }

    /**
     * Evaluates predicate function and, depends on result, evaluates either
     * trueF function or falseF function.
     * 
     * @param predicate
     *            predicate function
     * @param trueF
     *            function which is evaluated when predicate returns
     *            {@linkplain Boolean#TRUE}
     * @param falseF
     *            function which is evaluated when predicate returns
     *            {@linkplain Boolean#FALSE}
     * @return evaluation result of <b>trueF</b> or <b>falseF</b> function.
     */
    public static F _if_else(Predicate predicate, F trueF, F falseF) {
        return new If(predicate, trueF, falseF);
    }

    /**
     * Returns value object.
     * 
     * @param value
     *            object
     * @return value object
     */
    public static F c(Object value) {
        return new Const(value);
    }

    /**
     * Removes value from context.
     * 
     * @param contextKey
     *            context key
     * @return removing object
     */
    public static F clear(String contextKey) {
        return new Clear(contextKey);
    }

    /**
     * Subsequently evaluates passed functions.
     * <p>
     * This function uses the same current object to evaluate all functions.
     * <p>
     * Factory method returns different function objects depending of function
     * list size:
     * <ul>
     * <li>Nil - when 0</li>
     * <li>parameter function - when 1</li>
     * <li>Composite - when > 1</li>
     * </ul>
     * 
     * @param functions
     *            list of functions
     * @return result of last function evaluation.
     */
    public final static F composite(F... functions) {
        if (functions == null)
            return nil();

        switch (functions.length) {
        case 0:
            return nil();

        case 1:
            return functions[0];

        default:
            return new Composite(functions);
        }
    }

    /**
     * Returns current object.
     * 
     * @return current object
     */
    public static F current() {
        return new Current();
    }

    /**
     * Returns context's value.
     * 
     * @param contextKey
     *            context key
     * @return value corresponding to context key
     */
    public static F get(String contextKey) {
        return new Get(contextKey);
    }

    /**
     * Returns Loop function. Loop function is used to create cycles of
     * functions. See "Function Reference" for more details.
     * 
     * @return new Loop function object
     */
    public static Loop loop() {
        return new Loop();
    }

    /**
     * Always returns <b>null</b>.
     * 
     * @return <b>null</b>
     */
    public static final F nil() {
        return Nil.SINGLETON;
    }

    /**
     * Temporary removes object from context for a period of <b>next</b>
     * functions evaluation.
     * 
     * @param contextKey
     *            context key which object is removed/restored.
     * @param next
     *            functions
     * @return evaluation result of last <b>next</b> function
     */
    public static F omitContext(String contextKey, F... next) {
        return new OmitContext(contextKey, composite(next));
    }

    /**
     * Prints to console function evaluation result.
     * 
     * @param valueF
     *            function which result is printed
     * @return valueF evaluation result
     */
    public static F print(F valueF) {
        return new Print(valueF);
    }

    /**
     * Prints to console object.
     * 
     * @param value
     *            object which is printed to console
     * @return value object
     */
    public static F print(Object value) {
        return print(c(value));
    }

    /**
     * Prints current object to console.
     * 
     * @return current object
     */
    public static F printCurrent() {
        return print(current());
    }

    /**
     * Puts function's evaluation result into context.
     * 
     * @param contextKey
     *            context key
     * @param valueF
     *            function, which evaluation result is put into context
     * @return evaluation result of <b>valueF</b> function
     */
    public static F put(String contextKey, F valueF) {
        return new Put(contextKey, valueF);
    }

    /**
     * Puts object into context.
     * 
     * @param contextKey
     *            context key
     * @param value
     *            object
     * @return value object
     */
    public static F put(String contextKey, Object value) {
        return new Put(contextKey, c(value));
    }

    /**
     * Substitutes passed object (see {@linkplain When#eval(Object)}) by
     * <b>when</b> object if passed object equals to <b>when</b> object.
     * 
     * @param when
     *            when object
     * @param then
     *            then object
     * @return <b>then</b> if passed object equals to <b>when</b> object,
     *         otherwise return passed object.
     */
    public static When when(Object when, Object then) {
        return new When(when, then);
    }
}
