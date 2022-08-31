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

package com.vyhodb.onm.f.object.navigation;

import java.util.Collection;
import java.util.Map;

import com.vyhodb.f.F;
import com.vyhodb.f.Stack;
import com.vyhodb.f.navigation.DummyStack;
import com.vyhodb.onm.Mapping;
import com.vyhodb.onm.impl.ClassDescriptor;

@SuppressWarnings("serial")
public abstract class ObjectNavigation extends F {

    protected String linkName;
    protected String metadataKey;
    protected String stackKey;

    protected ObjectNavigation() {
    }

    protected ObjectNavigation(String stackKey, String metadataKey, String linkName) {
        if (stackKey == null) {
            throw new IllegalArgumentException("[stackKey] is null");
        }

        if (metadataKey == null) {
            throw new IllegalArgumentException("[metadataKey] is null");
        }

        if (linkName == null) {
            throw new IllegalArgumentException("[linkName] is null");
        }

        this.stackKey = stackKey;
        this.metadataKey = metadataKey;
        this.linkName = linkName;
    }

    @SuppressWarnings("deprecation")
    protected Collection<Object> getChildren(Object object, Map<String, Object> context) {
        Mapping metadata = (Mapping) context.get(metadataKey);
        ClassDescriptor cl = metadata.getClassDescriptor(object.getClass());
        return cl.getChildren(linkName, object);
    }

    @SuppressWarnings("deprecation")
    protected Object getParent(Object object, Map<String, Object> context) {
        Mapping metadata = (Mapping) context.get(metadataKey);
        ClassDescriptor cl = metadata.getClassDescriptor(object.getClass());
        return cl.getParent(linkName, object);
    }

    protected Stack getStack(Map<String, Object> context) {
        Stack stack = (Stack) context.get(stackKey);
        return stack == null ? DummyStack.SINGLETON : stack;
    }
}
