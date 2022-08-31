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

import com.vyhodb.f.navigation.Children;
import com.vyhodb.f.navigation.DummyStack;
import com.vyhodb.f.navigation.Parent;
import com.vyhodb.f.navigation.Search;

/**
 * This interface implements Visitor pattern for record graph traversing
 * process.
 * <p>
 * This interface provides the ability to create custom algorithms for traversed
 * objects, using existed traversing approach: {@linkplain NavigationFactory
 * navigation functions}.
 * <p>
 * Navigation functions get Stack object from context and, if it isn't null,
 * notify it about traversed objects (about Records in case of record navigation
 * functions).
 * <p>
 * For instance,
 * {@linkplain NavigationFactory#children(String, com.vyhodb.f.F...)} function
 * invokes {@linkplain #pushChild(String, Object)} method for every child
 * record. At the end of child record traversing, children() function notifies
 * Stack by using {@linkplain #pop()} method.
 * <p>
 * children(), search() and hierarchy() functions use
 * {@linkplain #pushChild(String, Object)} method notification about traversed
 * records. While only parent() function uses
 * {@linkplain #pushParent(String, Object)}.
 * 
 * @see NavigationFactory
 * @see PrintFactory
 * @see Children
 * @see Parent
 * @see Search
 * @see DummyStack
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public interface Stack {

    /**
     * Default context key to which Stack object is associated.
     */
    public static final String DEFAULT_CONTEXT_KEY = "Sys$Stack";

    /**
     * Retrieves stack object state.
     * <p>
     * State is specified by particular implementation.
     * 
     * @return state object
     */
    public Object peek();

    /**
     * Pops previously traversed object from stack.
     */
    public void pop();

    /**
     * Pushes traversed child object into stack
     * 
     * @param linkName
     * @param child
     */
    public void pushChild(String linkName, Object child);

    /**
     * Pushes traversed parent object into stack.
     * 
     * @param linkName
     *            link name
     * @param parent
     *            parent object
     */
    public void pushParent(String linkName, Object parent);
}
