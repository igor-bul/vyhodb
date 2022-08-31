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

package com.vyhodb.onm;

import static com.vyhodb.f.CommonFactory.composite;
import static com.vyhodb.f.PredicateFactory.trueF;

import com.vyhodb.f.F;
import com.vyhodb.f.NavigationFactory;
import com.vyhodb.f.Predicate;
import com.vyhodb.f.Stack;
import com.vyhodb.onm.f.object.clone.StartClone;
import com.vyhodb.onm.f.object.navigation.ObjectChildren;
import com.vyhodb.onm.f.object.navigation.ObjectHierarchy;
import com.vyhodb.onm.f.object.navigation.ObjectParent;
import com.vyhodb.onm.f.read.StartRead;

/**
 * ONM function factory, used for ONM Reading and ONM Cloning.
 * <p>
 * This class provides factory methods for the following function types:
 * <ul>
 * <li>ONM Reading
 * <li>Object Navigation
 * <li>ONM Cloning
 * </ul>
 * 
 * <h5>ONM Reading</h5>
 * To build ONM Reading function do the following steps:
 * <p>
 * <ol>
 * <li>Construct record navigation function which traversal route corresponds to
 * the required object graph structure. See {@linkplain NavigationFactory} for
 * more details.</li>
 * <li>Wrap record navigation function by function from this class:
 * {@linkplain #startRead(Class, Mapping, F...)}.</li>
 * </ol>
 * 
 * <p><b>Example:</b>
 * <pre>
 *   // Creates mapping cache
 *   Mapping mapping = Mapping.newAnnotationMapping();
 * 
 *   // Builds ONM reading function
 *   F onmReadF = 
 *       startRead(Root.class, mapping, 
 *           children("order2root", 
 *               children("item2order", 
 *                   parent("item2product")
 *               )
 *           ), 
 *           children("product2root")
 *       );
 * 
 *   // Evaluates ONM reading function.
 *   Root rootObject = (Root) onmReadF.eval(someRecord);
 * </pre>
 * 
 * <h5>Object Navigation</h5>
 * <p>
 * Object navigation functions are intended for traversing over java object graph. They use fields
 * annotated by {@linkplain Parent @Parent} or {@linkplain Children @Children}
 * for moving from one object to another.
 * <p>
 * Object navigation functions can't be used alone: they must be wrapped by start cloning function (see below).
 * <p>
 * Object navigation functions use {@linkplain Mapping} object for getting
 * information about parent and children fields. Mapping object
 * is retrieved from context ({@linkplain Mapping#DEFAULT_CONTEXT_KEY} is used
 * as a context key).
 * <p>
 * For each traversed object, object navigation functions invoke methods of
 * {@linkplain Stack} object, which is also stored in context (
 * {@linkplain Stack#DEFAULT_CONTEXT_KEY} is used as context key).
 * 
 * <p><b>Object Hierarchy</b>
 * <p>
 * Object hierarchy reflects record hierarchy (see
 * {@linkplain NavigationFactory}). Object hierarchy is an object structure,
 * where each object has @Parent and @Children fields with the same name.
 * Functions {@linkplain #objectHierarchy(String, F...)},
 * {@linkplain #objectHierarchyIf(String, Predicate, F...)} traverse over object
 * hierarchy in descent direction (by iterating over children collection).
 * 
 * <h5>ONM Cloning</h5>
 * <p>
 * Clone functions ({@linkplain #startClone(Mapping, F...)},
 * {@linkplain #startClone(Mapping, Predicate, F...)}) are intended to start
 * cloning of traversed object graph.
 * <p>
 * Start cloning function should wrap object navigation function in order to 
 * clone traversed objects. See example below:
 * 
 * <p><b>Example:</b>
 * 
 * <pre>
 *   Order sourceOrder = ...
 *   
 *   // Creates mapping cache
 *   Mapping mapping = Mapping.newAnnotationMapping();
 *   
 *   // Builds ONM Cloning function
 *   F cloneF = 
 *       startClone(mapping,
 *           objectChildren("item2order", 
 *               objectParent("item2product")
 *           )
 *      );
 *      
 *   // Evaluates ONM Cloning function. Result - cloned object graph.  
 *   Order order = (Order) cloneF.eval(sourceOrder);
 * </pre>
 * 
 * @see F
 * @see Predicate
 * @see Mapping
 * @see NavigationFactory
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 * 
 */
public class OnmFactory {

    /**
     * Starts ONM Reading using annotation mapping.
     * <p>
     * The same as:
     * <code>startRead(rootClass, Mapping.newAnnotationMapping(), next)</code>.
     * (see {@linkplain #startRead(Class, Mapping, F...)})
     * 
     * @param rootClass
     *            java class, which is used to create root object of read graph.
     *            Passed vyhodb Record (as current object) is used for creating
     *            root object.
     * @param next
     *            record navigation functions
     * @return returns object of <b>rootClass</b> class, which is a root object
     *         of read object graph
     * 
     * @see Mapping#newAnnotationMapping()
     */
    public static F startRead(Class<?> rootClass, F... next) {
        return new StartRead(rootClass, Mapping.newAnnotationMapping(), composite(next));
    }

    /**
     * Starts ONM Reading using specified Mapping.
     * <p>
     * Creates new ONM Read Stack object, puts it into context and evaluates
     * <b>next</b> functions, which are expected to be record navigation
     * functions.
     * 
     * @param rootClass
     *            java class, which is used to create root object of read graph.
     *            Passed vyhodb Record (as current object) is used for creating
     *            root object.
     * @param mapping
     *            mapping cache
     * @param next
     *            record navigation functions
     * @return returns object of <b>rootClass</b> class, which is a root object
     *         of read object graph
     * 
     */
    public static F startRead(Class<?> rootClass, Mapping mapping, F... next) {
        return new StartRead(rootClass, mapping, composite(next));
    }
    
    /**
     * Retrieves child collection and evaluates next functions for each child
     * object.
     * 
     * @param linkName
     *            child link name
     * @param next
     *            next functions
     * @return evaluation result of last <b>next</b> function for last child
     *         object. If children collection is empty or null, then <b>null</b>
     *         is returned.
     */
    public static F objectChildren(String linkName, F... next) {
        return new ObjectChildren(linkName, trueF(), composite(next));
    }

    /**
     * Retrieves child collection from current object and evaluates next
     * functions for those child objects, which satisfy predicate function.
     * 
     * @param linkName
     *            child link name
     * @param predicate
     *            object predicate
     * @param next
     *            next functions
     * @return evaluation result of last <b>next</b> function for last child
     *         object. If children collection is empty or null, or none of child
     *         objects satisfy predicate, then <b>null</b> is returned.
     */
    public static F objectChildrenIf(String linkName, Predicate predicate, F... next) {
        return new ObjectChildren(linkName, predicate, composite(next));
    }

    /**
     * Traverse object hierarchy and evaluates level functions for each
     * traversed object.
     * <p>
     * Current object is treated as hierarchy's root object. Hierarchy is
     * traversed in downward direction: by iterating over children objects.
     * 
     * @param linkName
     *            hierarchy child link name
     * @param levelF
     *            level function
     * @return level function evaluation result of last traversed object.
     */
    public static F objectHierarchy(String linkName, F... levelF) {
        return new ObjectHierarchy(linkName, trueF(), composite(levelF));
    }

    /**
     * Conditionally traverse object hierarchy and evaluates level functions for
     * each traversed object.
     * <p>
     * Current object is treated as hierarchy's root object. Hierarchy is
     * traversed in downward direction: by iterating over children objects.
     * <p>
     * Hierarchy object isn't traversed (including it's subtree) if object
     * doesn't satisfy predicate. Predicate isn't applied to hierarchy root
     * object, so it is always traversed.
     * 
     * @param linkName
     *            hierarchy child link name
     * @param levelPredicate
     *            hierarchy object predicate
     * @param levelF
     *            level function
     * @return level function evaluation result of last traversed object.
     */
    public static F objectHierarchyIf(String linkName, Predicate levelPredicate, F... levelF) {
        return new ObjectHierarchy(linkName, levelPredicate, composite(levelF));
    }

    /**
     * Retrieves parent object from current one and, if parent isn't null
     * evaluates next functions for it.
     * 
     * @param linkName
     *            parent link name
     * @param next
     *            next functions
     * @return evaluation result of last <b>next</b> function for parent object.
     *         If parent object is <b>null</b>, then <b>null</b> is returned.
     */
    public static F objectParent(String linkName, F... next) {
        return new ObjectParent(linkName, trueF(), composite(next));
    }

    /**
     * Retrieves parent object from current one and, if parent isn't null and
     * satisfies predicate function, evaluates next functions for it.
     * 
     * @param linkName
     *            parent link name
     * @param predicate
     *            parent object predicate
     * @param next
     *            next functions
     * @return evaluation result of last <b>next</b> function for parent object.
     *         If parent object is null or isn't satisfy predicate then
     *         <b>null</b> is returned.
     */
    public static F objectParentIf(String linkName, Predicate predicate, F... next) {
        return new ObjectParent(linkName, predicate, composite(next));
    }

    /**
     * Starts ONM Cloning using annotation Mapping.
     * <p>
     * The same as
     * <code>startClone(Mapping.newAnnotationMapping(), trueF(), next)</code>.
     * See {@linkplain #startClone(Mapping, Predicate, F...)}.
     * 
     * @param next
     *            object navigation functions
     * @return object which is a clone of passed current object and a root of
     *         cloned object graph.
     * 
     * @see Mapping#newAnnotationMapping()
     */
    public static F startClone(F... next) {
        return new StartClone(Mapping.newAnnotationMapping(), trueF(), composite(next));
    }

    /**
     * Starts ONM Cloning.
     * <p>
     * The same as <code>startClone(mapping, trueF(), next)</code>. See
     * {@linkplain #startClone(Mapping, Predicate, F...)}.
     * 
     * @param mapping
     *            mapping object
     * @param next
     *            object navigation functions
     * @return object which is a clone of passed current object and a root of
     *         cloned object graph.
     */
    public static F startClone(Mapping mapping, F... next) {
        return new StartClone(mapping, trueF(), composite(next));
    }

    /**
     * Starts ONM Cloning.
     * <p>
     * Method performs the following steps:
     * <ol>
     * <li>Puts specified mapping object into context.</li>
     * <li>Creates new ONM Cloning stack object and puts it into context.</li>
     * <li>Clones current object which become root object of cloned graph.</li>
     * <li>Evaluates <b>next</b> functions for current object. Those functions
     * are expected to be object navigation functions, which traverse over
     * object graph.</li>
     * <li>Returns cloned root object (see step 3).</li>
     * </ol>
     *
     * <p>
     * If current object doesn't satisfy predicate, then traversing/cloning
     * isn't performed and <b>null</b> is returned.
     * 
     * @param mapping
     *            mapping object
     * @param predicate
     *            current object predicate
     * @param next
     *            object navigation functions
     * @return object which is a clone of passed current object and a root of
     *         cloned object graph. If current object is <b>null</b> or doesn't
     *         satisfy <b>predicate</b> then <b>null</b> is returned.
     */
    public static F startClone(Mapping mapping, Predicate predicate, F... next) {
        return new StartClone(mapping, predicate, composite(next));
    }
}
