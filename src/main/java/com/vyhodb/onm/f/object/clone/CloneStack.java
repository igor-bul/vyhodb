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

package com.vyhodb.onm.f.object.clone;

import java.util.HashMap;
import java.util.LinkedList;

import com.vyhodb.f.Stack;
import com.vyhodb.onm.Mapping;
import com.vyhodb.onm.impl.ClassDescriptor;

class CloneStack implements Stack {

    private final HashMap<Object, Object> _clonedObjects = new HashMap<>();
    private final Mapping _metadata;
    private final LinkedList<Object> _stack = new LinkedList<>();

    public CloneStack(Object root, Mapping metadata) {
        _metadata = metadata;
        Object clonedRoot = cloneObject(root);
        _clonedObjects.put(root, clonedRoot);
        _stack.push(clonedRoot);
    }

    @SuppressWarnings("deprecation")
    private Object cloneObject(Object object) {
        if (_clonedObjects.containsKey(object)) {
            return _clonedObjects.get(object);
        }

        final ClassDescriptor classDescriptor = _metadata.getClassDescriptor(object.getClass());

        Object cloned = classDescriptor.cloneObject(object);
        _clonedObjects.put(object, cloned);
        return cloned;
    }

    @Override
    public Object peek() {
        return _stack.peek();
    }

    @Override
    public void pop() {
        _stack.pop();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void pushChild(String linkName, Object child) {
        Object clonedPrev = _stack.peek();
        Object clonedChild = cloneObject(child);

        ClassDescriptor prevCD = _metadata.getClassDescriptor(clonedPrev.getClass());
        ClassDescriptor childCD = _metadata.getClassDescriptor(clonedChild.getClass());

        prevCD.addChild(linkName, clonedChild, clonedPrev, true);
        childCD.setParent(linkName, clonedChild, clonedPrev, false);

        _stack.push(clonedChild);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void pushParent(String linkName, Object parent) {
        Object clonedPrev = _stack.peek();
        Object clonedParent = cloneObject(parent);

        ClassDescriptor prevCD = _metadata.getClassDescriptor(clonedPrev.getClass());
        ClassDescriptor parentCD = _metadata.getClassDescriptor(clonedParent.getClass());

        prevCD.setParent(linkName, clonedPrev, clonedParent, true);
        parentCD.addChild(linkName, clonedPrev, clonedParent, false);

        _stack.push(clonedParent);
    }
}
