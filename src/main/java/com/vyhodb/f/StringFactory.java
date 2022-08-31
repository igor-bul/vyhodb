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

import com.vyhodb.f.strings.StrContains;
import com.vyhodb.f.strings.StrEndsWith;
import com.vyhodb.f.strings.StrIndex;
import com.vyhodb.f.strings.StrLastIndex;
import com.vyhodb.f.strings.StrLength;
import com.vyhodb.f.strings.StrLowerCase;
import com.vyhodb.f.strings.StrMatches;
import com.vyhodb.f.strings.StrStartsWith;
import com.vyhodb.f.strings.StrSub;
import com.vyhodb.f.strings.StrTrim;
import com.vyhodb.f.strings.StrUpperCase;

/**
 * Provides static methods which construct functions for operating on strings.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class StringFactory {

    public static Predicate strContains(String pattern, F stringValueF) {
        return new StrContains(pattern, stringValueF);
    }

    public static Predicate strEndsWith(String suffix, F stringValueF) {
        return new StrEndsWith(suffix, stringValueF);
    }

    public static F strIndex(String pattern, F stringValueF) {
        return new StrIndex(pattern, stringValueF);
    }

    public static F strLastIndex(String pattern, F stringValueF) {
        return new StrLastIndex(pattern, stringValueF);
    }

    public static F strLength(F stringValueF) {
        return new StrLength(stringValueF);
    }

    public static F strLowerCase(F stringValueF) {
        return new StrLowerCase(stringValueF);
    }

    public static Predicate strMatches(String regExpression, F stringValueF) {
        return new StrMatches(regExpression, stringValueF);
    }

    public static Predicate strStartsWith(String prefix, F stringValueF) {
        return new StrStartsWith(prefix, stringValueF);
    }

    public static F strSub(F beginF, F stringValueF) {
        return new StrSub(beginF, null, stringValueF);
    }

    public static F strSub(F beginF, F endF, F stringValueF) {
        return new StrSub(beginF, endF, stringValueF);
    }

    public static F strTrim(F stringValueF) {
        return new StrTrim(stringValueF);
    }

    public static F strUpperCase(F stringValueF) {
        return new StrUpperCase(stringValueF);
    }
}
