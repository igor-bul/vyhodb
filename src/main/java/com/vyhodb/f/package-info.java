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

/**
 * Provides <b>"Functions API"</b> classes: basic function classes and function Factories.
 * <p>
 * Working with functions consists of two steps:
 * <ol>
 * <li>Function construction.
 * <li>Function evaluation.
 * </ol>
 * <p>
 * Function object is an object, which class is inherited from {@linkplain com.vyhodb.f.F}. For constructing function tree Factory 
 * classes are created. There are different Factories for different purposes.
 * <p>
 * By using <code>import static</code> directive and combining many Factory methods' invocations, we get code which creates 
 * function tree and looks like functional programming language (see example below).
 * <p>
 * Once the function tree is built (which is actually an object graph) we can evaluate function tree by invoking method {@linkplain com.vyhodb.f.F#eval(Object)}
 * of root function and passing <b>current object</b>. Each function accepts current object (usually {@linkplain com.vyhodb.space.Record}),
 * process it (depends on function logic it can just do nothing with it), modify it and passes to next function. Some functions 
 * (like record navigation) pass to next functions other current object. For instance children function gets child records
 * from current record and evaluates next function for each of child record.
 * <p>
 * Factory methods' javadocs describe functions which they create rather than methods themselves.
 * 
 * <h5>Example</h5>
 * Example below shows method which constructs and evaluates function. It uses data sample which can be created by {@linkplain com.vyhodb.utils.DataGenerator#generate(com.vyhodb.space.Record)}.
 * Function sumF traverse over item records (by two children relations: "product2root", "item2product") and calculates sum of their
 * "Cost" field:
 * <p>
 * <pre>
 * import static com.vyhodb.f.AggregatesFactory.sum;
 * import static com.vyhodb.f.AggregatesFactory.getSum;
 * import static com.vyhodb.f.CommonFactory.composite;
 * import static com.vyhodb.f.NavigationFactory.children;
 * import static com.vyhodb.f.RecordFactory.getField;
 * ...
 * private static BigDecimal calcSum(Record root) {
        // Constructs function tree
        F sumF = 
        composite(
            children("product2root", 
                children("item2product",
                    sum(getField("Cost"))
                )
            ),
            getSum()
        );

        // Evaluates function tree
        return (BigDecimal) sumF.eval(root);
    }
 * ...
 * </pre>
 * 
 * @see com.vyhodb.utils.DataGenerator
 */
package com.vyhodb.f;

