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

package com.vyhodb.omn.comparators;

import java.util.ArrayList;

import com.vyhodb.onm.OnmException;
import com.vyhodb.ts.domain.DomainObject;

public abstract class TsComparator<T extends DomainObject> {
    public abstract void onmCompare(T first, T second) throws OnmException;
    
    public static void compareChildren(ArrayList<? extends DomainObject> firstChildren, ArrayList<? extends DomainObject> secondChildren, TsComparator comparator) {
        if (firstChildren.size() != secondChildren.size()) {
            throw new OnmException("Wrong children size.");
        }
        
        int secondIndex;
        int size = firstChildren.size();
        for (int i = 0; i < size; i++) {
            secondIndex = secondChildren.indexOf(firstChildren.get(i));
            if (secondIndex == -1) {
                throw new OnmException("Second does not have corresponding children.");
            }
            
            comparator.onmCompare(firstChildren.get(i), secondChildren.get(secondIndex));
        }
    }
}
