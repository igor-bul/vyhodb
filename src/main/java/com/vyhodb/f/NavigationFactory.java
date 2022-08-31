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

import static com.vyhodb.f.CommonFactory.composite;
import static com.vyhodb.f.PredicateFactory.trueF;

import com.vyhodb.f.navigation.Break;
import com.vyhodb.f.navigation.BreakException;
import com.vyhodb.f.navigation.Children;
import com.vyhodb.f.navigation.Hierarchy;
import com.vyhodb.f.navigation.Search;
import com.vyhodb.f.navigation.Parent;
import com.vyhodb.space.Criterion;

/**
 * Provides static methods which construct functions for traversing over record
 * graph.
 * <p>
 * Current object, passed to navigation functions, must be of
 * {@linkplain Record} type.
 * 
 * <h5>Record Hierarchy</h5>
 * From functions point of view, record hierarchy is a record structure, where
 * each record has one parent link and (optionally) children links with the same
 * name. Record hierarchy has a "root" record, which doesn't have parent link.
 * 
 * <h5>Stack</h5>
 * All navigation functions get {@linkplain Stack} object from context and, if
 * it isn't null, notify it about traversed records.
 * 
 * @see Stack
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class NavigationFactory {

    /**
     * Stops children record iteration of nearest upper function.
     * <p>
     * Function throws {@linkplain BreakException} which is correctly processed
     * by functions:
     * <ul>
     * <li>{@linkplain #children(String, F...)}</li>
     * <li>{@linkplain #childrenIf(String, Predicate, F...)}</li>
     * <li>{@linkplain #search(String, Criterion, F...)}</li>
     * <li>{@linkplain #searchIf(String, Criterion, Predicate, F...)}</li>
     * </ul>
     * Those functions stop evaluation and return last evaluation result.
     * 
     * @return no result. Function always throws {@linkplain BreakException}.
     * 
     * @see Break
     */
    public static F _break() {
        return Break.SINGLETON;
    }

    /**
     * Retrieves child records and evaluates next functions for each one.
     * 
     * @param linkName
     *            child link name
     * @param next
     *            next functions
     * 
     * @return evaluation result of last <b>next</b> function for last child
     *         record. If there aren't any child records, then <b>null</b> is
     *         returned.
     * 
     * @see com.vyhodb.space.Record#getChildren(String)
     */
    public static F children(String linkName, F... next) {
        return new Children(linkName, trueF(), composite(next));
    }

    /**
     * Retrieves child records and evaluates next functions for those of them,
     * which satisfy predicate function.
     * 
     * @param linkName
     *            child link name
     * @param predicate
     *            child records predicate
     * @param next
     *            next functions
     * 
     * @return evaluation result of last <b>next</b> function for last child
     *         record. If there aren't any child records or none of them satisfy
     *         predicate, then <b>null</b> is returned.
     * 
     * @see com.vyhodb.space.Record#getChildren(String)
     */
    public static F childrenIf(String linkName, Predicate predicate, F... next) {
        return new Children(linkName, predicate, composite(next));
    }

    /**
     * Traverse record hierarchy and evaluates level functions for each
     * traversed record.
     * <p>
     * Current object is treated as hierarchy's root record. Hierarchy is
     * traversed in downward direction: by iterating over children records.
     * 
     * @param childLinkName
     *            hierarchy child link name
     * @param levelF
     *            level functions
     * 
     * @return level function evaluation result of last traversed record.
     * 
     * @see com.vyhodb.space.Record#getChildren(String)
     */
    public static F hierarchy(String childLinkName, F... levelF) {
        return new Hierarchy(childLinkName, trueF(), composite(levelF));
    }

    /**
     * Conditionally traverse record hierarchy and evaluates level functions for
     * each traversed record.
     * <p>
     * Current object is treated as hierarchy's root record. Hierarchy is
     * traversed in downward direction: by iterating over children records.
     * <p>
     * Hierarchy record isn't traversed (including it's subtree) if record
     * doesn't satisfy predicate. Predicate isn't applied to hierarchy root
     * record, so it is always traversed.
     * 
     * @param childLinkName
     *            hierarchy child link name
     * @param levelPredicate
     *            hierarchy record predicate
     * @param levelF
     *            level function
     * 
     * @return level function evaluation result of last traversed record.
     * 
     * @see com.vyhodb.space.Record#getChildren(String)
     */
    public static F hierarchyIf(String childLinkName, Predicate levelPredicate, F... levelF) {
        return new Hierarchy(childLinkName, levelPredicate, composite(levelF));
    }

    /**
     * Retrieves parent record and, if it isn't null, evaluates next functions
     * for it.
     * 
     * @param linkName
     *            parent link name
     * @param next
     *            next functions
     * 
     * @return evaluation result of last <b>next</b> function for parent record.
     *         If parent record is <b>null</b>, then <b>null</b> is returned.
     * 
     * @see com.vyhodb.space.Record#getParent(String)
     */
    public static F parent(String linkName, F... next) {
        return new Parent(linkName, trueF(), composite(next));
    }

    /**
     * Retrieves parent record and, if it isn't null and satisfies predicate
     * function, evaluates next functions for it.
     * 
     * @param linkName
     *            parent link name
     * @param predicate
     *            parent record predicate
     * @param next
     *            next functions
     * 
     * @return evaluation result of last <b>next</b> function for parent record.
     *         If parent record is null or isn't satisfy predicate then null is
     *         returned.
     * 
     * @see com.vyhodb.space.Record#getParent(String)
     */
    public static F parentIf(String linkName, Predicate predicate, F... next) {
        return new Parent(linkName, predicate, composite(next));
    }

    /**
     * Retrieves child records using index search and evaluates next functions
     * for each of them.
     * 
     * @param indexName
     *            name of index
     * @param criterion
     *            search criterion
     * @param next
     *            next functions
     * 
     * @return evaluation result of last <b>next</b> function for last child
     *         record from search result. If search result is empty, then
     *         <b>null</b> is returned.
     * 
     * @see com.vyhodb.space.Record#searchChildren(String, Criterion)
     * @see Criterion
     */
    public static F search(String indexName, Criterion criterion, F... next) {
        return new Search(indexName, criterion, trueF(), composite(next));
    }

    /**
     * Retrieves child records using index search and evaluates next functions
     * for those of them, which satisfy predicate.
     * 
     * @param indexName
     *            name of index
     * @param criterion
     *            search criterion
     * @param predicate
     *            child record predicate
     * @param next
     *            next functions
     * 
     * @return evaluation result of last <b>next</b> function for last child
     *         record from search result. If search result is empty or none of
     *         found child records satisfy predicate, then <b>null</b> is
     *         returned.
     * 
     * @see com.vyhodb.space.Record#searchChildren(String, Criterion)
     * @see Criterion
     */
    public static F searchIf(String indexName, Criterion criterion, Predicate predicate, F... next) {
        return new Search(indexName, criterion, predicate, composite(next));
    }
}
