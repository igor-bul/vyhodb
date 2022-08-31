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

package com.vyhodb.onm.f.read;

import java.util.HashMap;
import java.util.LinkedList;

import com.vyhodb.f.Stack;
import com.vyhodb.f.record.RecordExpectedException;
import com.vyhodb.onm.Mapping;
import com.vyhodb.onm.impl.ClassDescriptor;
import com.vyhodb.onm.impl.Holder;
import com.vyhodb.space.Record;

class ReadStack implements Stack {

    private Mapping _metadata;
    private HashMap<Long, Object> _objectCache;
    private Holder _root;
    @SuppressWarnings("rawtypes")
    private Class _rootClass;
    private LinkedList<Holder> _stack;

    @SuppressWarnings("deprecation")
    ReadStack(Class<?> rootClass, Record rootRecord, Mapping metadata) {
        _rootClass = rootClass;
        _metadata = metadata;

        _stack = new LinkedList<>();
        _objectCache = new HashMap<>();

        // Creates root object
        ClassDescriptor classDescriptor = _metadata.getClassDescriptor(_rootClass);
        Object object = getOrCreateObject(rootRecord, classDescriptor);

        // Pushes into stack
        _root = new Holder(classDescriptor, object);
        _stack.push(_root);
    }

    private Object getOrCreateObject(Record record, ClassDescriptor metadata) {
        final long id = record.getId();
        Object object = _objectCache.get(id);

        if (object == null) {
            object = metadata.newObject(record);
            _objectCache.put(id, object);
        }

        return object;
    }

    Object getRoot() {
        if (_root != null) {
            return _root.object;
        }

        return null;
    }

    @Override
    public Object peek() {
        Holder holder = _stack.peek();
        return holder != null ? holder.object : null;
    }

    @Override
    public void pop() {
        _stack.pop();
    }

    @SuppressWarnings({ "rawtypes", "deprecation" })
    private void push(Record record, String linkName, boolean isChildren) {
        Holder prev = _stack.peek();
        Object prevObject = prev.object;
        ClassDescriptor prevDescriptor = prev.descriptor;

        // Creates or locates object
        Class objectClass = isChildren ? prevDescriptor.getChildClass(linkName) : prevDescriptor.getParentClass(linkName);
        ClassDescriptor classDescriptor = _metadata.getClassDescriptor(objectClass);
        Object object = getOrCreateObject(record, classDescriptor);

        // Sets links
        if (isChildren) {
            classDescriptor.setParent(linkName, object, prevObject, false);
            prevDescriptor.addChild(linkName, object, prevObject, true);
        } else {
            classDescriptor.addChild(linkName, prevObject, object, false);
            prevDescriptor.setParent(linkName, prevObject, object, true);
        }

        // Pushes into stack
        Holder holder = new Holder(classDescriptor, object);
        _stack.push(holder);
    }

    @Override
    public void pushChild(String linkName, Object child) {
        if (!(child instanceof Record)) {
            throw new RecordExpectedException(child);
        }

        push((Record)child, linkName, true);
    }

    @Override
    public void pushParent(String linkName, Object parent) {
        if (!(parent instanceof Record)) {
            throw new RecordExpectedException(parent);
        }

        push((Record) parent, linkName, false);
    }
}
