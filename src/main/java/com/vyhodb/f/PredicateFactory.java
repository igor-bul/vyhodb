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

import static com.vyhodb.f.CommonFactory.*;

import com.vyhodb.f.predicates.AndPredicate;
import com.vyhodb.f.predicates.ComparePredicate;
import com.vyhodb.f.predicates.ComparisonType;
import com.vyhodb.f.predicates.EqualPredicate;
import com.vyhodb.f.predicates.EveryChildPredicate;
import com.vyhodb.f.predicates.EveryIndexPredicate;
import com.vyhodb.f.predicates.Exists;
import com.vyhodb.f.predicates.ExistsException;
import com.vyhodb.f.predicates.FieldsEqualPredicate;
import com.vyhodb.f.predicates.IsExists;
import com.vyhodb.f.predicates.IsNullPredicate;
import com.vyhodb.f.predicates.NotPredicate;
import com.vyhodb.f.predicates.OrPredicate;
import com.vyhodb.f.predicates.SomeChildrenPredicate;
import com.vyhodb.f.predicates.SomeSearchPredicate;
import com.vyhodb.f.predicates.ToBooleanPredicate;
import com.vyhodb.f.predicates.UniquePredicate;
import com.vyhodb.space.Criterion;
import com.vyhodb.utils.Utils;

/**
 * Provides static methods which construct Predicate functions.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class PredicateFactory {

    /**
     * Logical AND.
     * <p>
     * Predicate functions are evaluated till the first FALSE result.
     * 
     * @param predicates
     *            predicate functions
     * @return Boolean.FALSE if at least one of the specified predicates returns
     *         FALSE. Boolean.TRUE - otherwise.
     */
    public static Predicate and(Predicate... predicates) {
        return new AndPredicate(predicates);
    }

    /**
     * Returns Boolean.TRUE if all specified functions return equal results.
     * <p>
     * Specified functions are evaluated till the first unequal result.
     * <p>
     * Method {@linkplain Object#equals(Object)} is used for equality check.
     * 
     * @param values
     *            value functions
     * @return Boolean.TRUE if all specified functions return equal results
     */
    public static Predicate equal(F... values) {
        return new EqualPredicate(values);
    }

    /**
     * Returns Boolean.FALSE if specified predicate returns Boolean.FALSE for at
     * least one child record.
     * <p>
     * Current object, which is passed to current function, must be of
     * {@linkplain com.vyhodb.space.Record} class.
     * <p>
     * Method iterates over current record's children and evaluates predicate
     * function for each of them, until predicate returns Boolean.FALSE.
     * 
     * @param childrenLinkName
     *            child link name
     * @param predicate
     *            predicate function
     * @return Boolean.FALSE if at least one child record doesn't satisfy
     *         predicate, Boolean.TRUE - otherwise. Function also returns
     *         Boolean.TRUE if there aren't any child records for specified link
     *         name.
     * 
     * @see com.vyhodb.space.Record#getChildren(String, com.vyhodb.space.Order)
     */
    public static Predicate everyChild(String childrenLinkName, Predicate predicate) {
        return new EveryChildPredicate(childrenLinkName, predicate);
    }

    /**
     * Returns Boolean.FALSE if specified predicate returns Boolean.FALSE for at
     * least one index search result's record.
     * <p>
     * Current object, which is passed to current function, must be of
     * {@linkplain com.vyhodb.space.Record} class. Index with <b>indexName</b> must exist on
     * current record.
     * <p>
     * Method iterates over index search result records and evaluates predicate
     * function for each of them, until predicate returns Boolean.FALSE.
     * 
     * @param indexName
     *            index name
     * @param criterion
     *            index search criterion
     * @param predicate
     *            predicate function
     * @return Boolean.FALSE if at least one index search result's record
     *         doesn't satisfy predicate, Boolean.TRUE - otherwise. Function
     *         also returns Boolean.TRUE if search result is empty.
     * 
     * @see Criterion
     * @see com.vyhodb.space.Record#searchChildren(String, Criterion)
     */
    public static Predicate everySearch(String indexName, Criterion criterion, Predicate predicate) {
        return new EveryIndexPredicate(indexName, criterion, predicate);
    }

    /**
     * Function is used in conjunction with {@linkplain #isExists(F...)}.
     * Function throws {@linkplain ExistsException} which is handler by
     * {@linkplain #isExists(F...)} function.
     * 
     * @return no result, function always throws {@linkplain ExistsException}
     * 
     * @see Exists
     */
    public static F exists() {
        return Exists.SINGLETON;
    }

    /**
     * Returns Boolean.FALSE
     * 
     * @return Boolean.FALSE
     */
    public static Predicate falseF() {
        return toBoolean(c(Boolean.FALSE));
    }

    /**
     * Returns Boolean.TRUE if current Record has specified field with specified
     * value.
     * <p>
     * Current object, which is passed to current function, must be of
     * {@linkplain com.vyhodb.space.Record} class.
     * <p>
     * It is allowed to specify <b>null</b> value.
     * 
     * @param keyFieldName
     *            field name
     * @param keyValue
     *            expected field value
     * @return Boolean.TRUE if Record object (which is passed as current object)
     *         has specified field with specified value. Boolean.FALSE -
     *         otherwise.
     */
    public static Predicate fieldsEqual(String keyFieldName, Object keyValue) {
        return new FieldsEqualPredicate(new String[] { keyFieldName }, new Object[] { keyValue });
    }

    /**
     * Returns Boolean.TRUE if current Record has specified fields with
     * specified values.
     * <p>
     * Current object, which is passed to current function, must be of
     * {@linkplain com.vyhodb.space.Record} class.
     * <p>
     * Field names and field values are associated to each other by their
     * position in arrays.
     * <p>
     * It is allowed to specify <b>null</b> values.
     * 
     * @param keyFieldNames
     *            field names
     * @param keyValues
     *            expected field values
     * @return Boolean.TRUE if Record object (which is passed as current object)
     *         has specified fields and Record's values are equal to expected
     *         values. Boolean.FALSE - otherwise.
     */
    public static Predicate fieldsEqual(String[] keyFieldNames, Object... keyValues) {
        return new FieldsEqualPredicate(keyFieldNames, keyValues);
    }

    /**
     * Returns Boolean.TRUE if during <b>next</b> functions evaluation, it
     * catches {@linkplain ExistsException}.
     * <p>
     * Function is used in conjunction with {@linkplain #exists()}.
     * <p>
     * For example, the following code:
     * <p>
     * <code>
     * isExists(children("employee2activity", parent("activity2project", exists()));
     * </code>
     * <p>
     * returns Boolean.TRUE if there at least one child record (child link name
     * = "employee2activity") has parent link with name "activity2project".
     * 
     * @param next
     *            next functions
     * @return Boolean.TRUE if it catches ExistsException exception during
     *         <b>next</b> functions evaluation, Boolean.FALSE - otherwise.
     * 
     * @see IsExists
     */
    public static Predicate isExists(F... next) {
        return new IsExists(composite(next));
    }

    /**
     * Returns Boolean.TRUE if specified function returns not <b>null</b>
     * result.
     * 
     * @param valueF
     *            value function
     * @return Boolean.FALSE if valueF function returns <b>null</b>,
     *         Boolean.TRUE - otherwise.
     */
    public static Predicate isNotNull(F valueF) {
        return not(isNull(valueF));
    }

    /**
     * Returns Boolean.TRUE if specified function returns <b>null</b>.
     * 
     * @param valueF
     *            value function
     * @return Boolean.TRUE if valueF function returns <b>null</b>,
     *         Boolean.FALSE - otherwise.
     */
    public static Predicate isNull(F valueF) {
        return new IsNullPredicate(valueF);
    }

    /**
     * Returns Boolean.TRUE if each <b>values</b> function result is LESS then
     * previous one.
     * <p>
     * <b>values</b> functions are evaluated until (
     * <code>values[i - 1] < values[i]</code>) is true.
     * <p>
     * <b>values</b> functions must return {@linkplain Comparable} objects.
     * 
     * @param values
     *            value functions
     * @return Boolean.TRUE if condition (<code>values[i - 1] < values[i]</code>
     *         ) is true for each function (i=1,2,...). Boolean.FALSE -
     *         otherwise.
     */
    public static Predicate less(F... values) {
        return new ComparePredicate(ComparisonType.LESS, values);
    }

    /**
     * Returns Boolean.TRUE if each <b>values</b> function result is LESS OR
     * EQUAL then previous one.
     * <p>
     * <b>values</b> functions are evaluated until (
     * <code>values[i - 1] <= values[i]</code>) is true.
     * <p>
     * <b>values</b> functions must return {@linkplain Comparable} objects.
     * 
     * @param values
     *            value functions
     * @return Boolean.TRUE if condition (
     *         <code>values[i - 1] <= values[i]</code>) is true for each
     *         function (i=1,2,...). Boolean.FALSE - otherwise.
     */
    public static Predicate lessEqual(F... values) {
        return new ComparePredicate(ComparisonType.LESS_EQUAL, values);
    }

    /**
     * Returns Boolean.TRUE if each <b>values</b> function result is MORE then
     * previous one.
     * <p>
     * <b>values</b> functions are evaluated until (
     * <code>values[i - 1] > values[i]</code>) is true.
     * <p>
     * <b>values</b> functions must return {@linkplain Comparable} objects.
     * 
     * @param values
     *            value functions
     * @return Boolean.TRUE if condition (<code>values[i - 1] > values[i]</code>
     *         ) is true for each function (i=1,2,...). Boolean.FALSE -
     *         otherwise.
     */
    public static Predicate more(F... values) {
        return new ComparePredicate(ComparisonType.MORE, values);
    }

    /**
     * Returns Boolean.TRUE if each <b>values</b> function result is MORE OR
     * EQUAL then previous one.
     * <p>
     * <b>values</b> functions are evaluated until (
     * <code>values[i - 1] >= values[i]</code>) is true.
     * <p>
     * <b>values</b> functions must return {@linkplain Comparable} objects.
     * 
     * @param values
     *            value functions
     * @return Boolean.TRUE if condition (
     *         <code>values[i - 1] >= values[i]</code>) is true for each
     *         function (i=1,2,...). Boolean.FALSE - otherwise.
     */
    public static Predicate moreEqual(F... values) {
        return new ComparePredicate(ComparisonType.MORE_EQUAL, values);
    }

    /**
     * Logical NOT.
     * 
     * @param predicate
     * @return inverted predicate evaluation result.
     */
    public static Predicate not(Predicate predicate) {
        return new NotPredicate(predicate);
    }

    /**
     * Logical OR.
     * <p>
     * Predicates functions are evaluated till the first TRUE result.
     * 
     * @param predicates
     *            predicate functions
     * @return Boolean.TRUE if at least one of the specified predicates returns
     *         TRUE. Boolean.FALSE - otherwise.
     */
    public static Predicate or(Predicate... predicates) {
        return new OrPredicate(predicates);
    }

    /**
     * Returns Boolean.TRUE if specified predicate returns Boolean.TRUE for at
     * least one child record.
     * <p>
     * Current object, which is passed to current function, must be of
     * {@linkplain com.vyhodb.space.Record} class.
     * <p>
     * Method iterates over current record's children and evaluates predicate
     * function for each of them, until predicate returns Boolean.TRUE.
     * 
     * @param childrenLinkName
     *            child link name
     * @param predicate
     *            predicate function
     * @return Boolean.TRUE if at least one child record satisfy predicate,
     *         Boolean.FALSE - otherwise. Function also returns Boolean.FALSE if
     *         there aren't any child records for specified link name.
     * 
     * @see com.vyhodb.space.Record#getChildren(String, com.vyhodb.space.Order)
     */
    public static Predicate someChildren(String childrenLinkName, Predicate predicate) {
        return new SomeChildrenPredicate(childrenLinkName, predicate);
    }

    /**
     * Returns Boolean.TRUE if specified predicate returns Boolean.TRUE for at
     * least one index search result's record.
     * <p>
     * Current object, which is passed to current function, must be of
     * {@linkplain com.vyhodb.space.Record} class. Index with <b>indexName</b> must exist on
     * current record.
     * <p>
     * Method iterates over index search result records and evaluates predicate
     * function for each of them, until predicate returns Boolean.TRUE.
     * 
     * @param indexName
     *            index name
     * @param criterion
     *            index search criterion
     * @param predicate
     *            predicate function
     * @return Boolean.TRUE if at least one index search result record satisfy
     *         predicate, Boolean.FALSE - otherwise. Function also returns
     *         Boolean.FALSE if search result is empty.
     * 
     * @see Criterion
     * @see com.vyhodb.space.Record#searchChildren(String, Criterion)
     */
    public static Predicate someSearch(String indexName, Criterion criterion, Predicate predicate) {
        return new SomeSearchPredicate(indexName, criterion, predicate);
    }

    /**
     * Evaluates <b>value</b> function and casts it's result to Boolean.
     * <p>
     * Method {@linkplain Utils#toBoolean(Object)} is used for result casting.
     * 
     * @param value
     * @return Boolean.TRUE only when <b>value</b> function result is:
     *         <ol>
     *         <li>Boolean.TRUE</li> <li>{@linkplain Number} object, which value
     *         != 0</li>
     *         </ol>
     *         Returns Boolean.FALSE - otherwise.
     */
    public static Predicate toBoolean(F value) {
        return new ToBooleanPredicate(value);
    }

    /**
     * Returns Boolean.TRUE.
     * 
     * @return Boolean.TRUE
     */
    public static Predicate trueF() {
        return toBoolean(c(Boolean.TRUE));
    }

    /**
     * Returns Boolean.TRUE if <b>values</b> functions return non equal results.
     * <p>
     * <b>values</b> functions are evaluated till the first pair of equal
     * results.
     * <p>
     * Method {@linkplain Object#equals(Object)} is used for equality check.
     * 
     * @param values
     * @return Boolean.TRUE if <b>values</b> functions return non equal results.
     *         Boolean.FALSE - otherwise
     */
    public static Predicate unique(F... values) {
        return new UniquePredicate(values);
    }
}
