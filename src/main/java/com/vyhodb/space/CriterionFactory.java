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

package com.vyhodb.space;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import com.vyhodb.space.criteria.All;
import com.vyhodb.space.criteria.Between;
import com.vyhodb.space.criteria.BetweenExclusive;
import com.vyhodb.space.criteria.Equal;
import com.vyhodb.space.criteria.EqualComposite;
import com.vyhodb.space.criteria.In;
import com.vyhodb.space.criteria.Less;
import com.vyhodb.space.criteria.LessEqual;
import com.vyhodb.space.criteria.More;
import com.vyhodb.space.criteria.MoreEqual;
import com.vyhodb.space.criteria.NotNull;
import com.vyhodb.space.criteria.Null;
import com.vyhodb.space.criteria.StartsWith;

/**
 * Criteria factory.
 * <p>
 * This class simplify code readability by using <code>static import</code> of
 * it methods.
 * <p>
 * It also contains helper methods which simplify creation of date range
 * criteria (see {@linkplain #date(Date)}, {@linkplain #month(int, int)},
 * {@linkplain #year(int)} ).
 * 
 * @see Criterion
 * @see Record
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class CriterionFactory {

    /**
     * Create All criterion.
     * 
     * @return All criterion
     */
    public static All all() {
        return new All();
    }

    /**
     * Creates Between criterion.
     * 
     * @param from
     *            lower range limit
     * @param to
     *            upper range limit
     * @return Between criterion
     */
    public static Between between(Comparable<?> from, Comparable<?> to) {
        return new Between(from, to);
    }

    /**
     * Creates BetweenExclusive criterion.
     * 
     * @param from
     *            lower range limit
     * @param to
     *            upper range limit
     * @return BetweenExclusive criterion
     */
    public static BetweenExclusive betweenExclusive(Comparable<?> from, Comparable<?> to) {
        return new BetweenExclusive(from, to);
    }

    private static Between buildDateCriterion(int year, int month, int dayOfMonth, int type) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        calendar.clear(Calendar.MILLISECOND);
        Date from = calendar.getTime();

        calendar.add(type, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        Date to = calendar.getTime();

        return new Between(from, to);
    }

    /**
     * Creates criterion for searching {@link Date} values which are within
     * specified date.
     * 
     * @param date
     * @return Between criterion
     */
    @SuppressWarnings("deprecation")
    public static Between date(Date date) {
        return date(date.getYear() + 1900, date.getMonth(), date.getDate());
    }

    /**
     * Creates criterion for searching {@link Date} values which are within
     * specified date.
     * 
     * @param year
     *            year of date
     * @param month
     *            month of date
     * @param dayOfMonth
     *            day of month
     * @return Between criterion
     */
    public static Between date(int year, int month, int dayOfMonth) {
        return buildDateCriterion(year, month, dayOfMonth, Calendar.DAY_OF_YEAR);
    }

    /**
     * Creates Equal criterion.
     * 
     * @param key
     *            search key
     * @return Equal criterion
     */
    public static Equal equal(Comparable<?> key) {
        return new Equal(key);
    }

    /**
     * Creates EqualComposite criterion.
     * 
     * @param key
     *            composite search key
     * @return EqualComposite criterion
     */
    public static EqualComposite equalComposite(Map<String, ? extends Comparable<?>> key) {
        return new EqualComposite(key);
    }

    /**
     * Creates In criterion.
     * 
     * @param keys
     *            search keys
     * @return In criterion
     */
    public static In in(Collection<? extends Comparable<?>> keys) {
        return new In(keys);
    }

    /**
     * Creates In criterion.
     * 
     * @param keys
     *            search keys
     * @return In criterion
     */
    public static In in(Comparable<?>... keys) {
        return new In(keys);
    }

    /**
     * Creates Less criterion.
     * 
     * @param key
     *            search key
     * @return Less criterion
     */
    public static Less less(Comparable<?> key) {
        return new Less(key);
    }

    /**
     * Creates LessEqual criterion.
     * 
     * @param key
     *            search key
     * @return LessEqual criterion
     */
    public static LessEqual lessEqual(Comparable<?> key) {
        return new LessEqual(key);
    }

    /**
     * Creates criterion for search {@link Date} values which are within
     * specified month.
     * 
     * @param year
     *            year of month
     * @param month
     *            month
     * @return Between criterion
     */
    public static Between month(int year, int month) {
        return buildDateCriterion(year, month, 0, Calendar.MONTH);
    }

    /**
     * Creates More criterion.
     * 
     * @param key
     *            search key
     * @return More criterion
     */
    public static More more(Comparable<?> key) {
        return new More(key);
    }

    /**
     * Creates MoreEqual criterion.
     * 
     * @param key
     *            search key
     * @return MoreEqual criterion
     */
    public static MoreEqual moreEqual(Comparable<?> key) {
        return new MoreEqual(key);
    }

    /**
     * Returns NotNull criterion.
     * 
     * @return NotNull criterion
     */
    public static NotNull notNull() {
        return NotNull.SINGLETON;
    }

    /**
     * Returns Null criterion.
     * 
     * @return Null criterion.
     */
    public static Null Null() {
        return Null.SINGLETON;
    }

    /**
     * Creates StartsWith criterion.
     * 
     * @param prefix
     *            string search prefix
     * @return StartsWith criterion
     */
    public static StartsWith startsWith(String prefix) {
        return new StartsWith(prefix);
    }

    /**
     * Creates criterion for searching {@link Date} values which are within
     * specified year.
     * 
     * @param year
     *            year of date
     * @return Between criterion
     */
    public static Between year(int year) {
        return buildDateCriterion(year, 0, 0, Calendar.YEAR);
    }
}
