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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Base function class.
 * 
 * <p>
 * <b>Evaluation Context (or Context)</b>
 * <p>
 * During function tree evaluation, root function (which is top level function
 * in function tree and which method {@linkplain #eval(Object)} is invoked for
 * tree evaluation) creates so called <b>evaluation context</b> which is
 * actually a Map<String, Object> object. Evaluation context is used as shared
 * memory by all functions in function tree.
 * 
 * <p>
 * <b>Thread safety</b>
 * <p>
 * It is expected that all functions would be safe thread. It means that they
 * should have references only to thread safe objects or immutable objects which
 * can be shared by multiple threads. All not thread safe objects, which are
 * used by functions during evaluation (for instance {@linkplain Record}),
 * should be passed using evaluation context.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
@SuppressWarnings("serial")
public abstract class F implements Serializable {

    /**
     * Starts function tree evaluation.
     * <p>
     * Method creates new context and invokes
     * {@linkplain #evalTree(Object, Map)}
     * 
     * @param current
     *            current object
     * @return evaluation result
     */
    public final Object eval(Object current) {
        return evalTree(current, new HashMap<String, Object>());
    }

    /**
     * Evaluates function as part of function tree evaluation.
     * 
     * @param current
     *            current object
     * @param context
     *            evaluation context
     * @return evaluation result
     */
    public abstract Object evalTree(Object current, Map<String, Object> context);
}
