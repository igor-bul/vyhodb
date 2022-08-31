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

package com.vyhodb.onm.impl;

import java.util.HashMap;

import com.vyhodb.onm.Mapping;

public final class AnnotationMapping extends Mapping {

    @SuppressWarnings("rawtypes")
    private HashMap<Class, AnnotationClassDescriptor> _classDescriptors = new HashMap<>();

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized ClassDescriptor getClassDescriptor(Class requiredClass) {
        AnnotationClassDescriptor descriptor = _classDescriptors.get(requiredClass);
        if (descriptor == null) {
            descriptor = new AnnotationClassDescriptor(requiredClass);
            _classDescriptors.put(requiredClass, descriptor);
        }

        return descriptor;
    }
}
