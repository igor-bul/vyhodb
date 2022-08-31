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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import com.vyhodb.onm.OnmException;
import com.vyhodb.onm.impl.xml.XmlChildren;
import com.vyhodb.onm.impl.xml.XmlClass;
import com.vyhodb.onm.impl.xml.XmlField;
import com.vyhodb.onm.impl.xml.XmlParent;
import com.vyhodb.utils.Utils;

public class XmlClassDescriptor extends ClassDescriptor {

    public XmlClassDescriptor(XmlClass xmlClass) throws ClassNotFoundException, NoSuchFieldException {
        init(xmlClass);
    }

    @SuppressWarnings("rawtypes")
    public Class getObjectClass() {
        return _objectClass;
    }

    @SuppressWarnings("rawtypes")
    private void init(XmlClass xmlClass) throws ClassNotFoundException, NoSuchFieldException {
        _objectClass = Class.forName(xmlClass.name);

        // Id
        _idField = _objectClass.getDeclaredField(xmlClass.id);
        if (!(Long.TYPE.equals(_idField.getType()) || Long.class.equals(_idField.getType())))
            throw new OnmException("Only long/Long fields can be used as id");
        _idField.setAccessible(true);

        // isChanged
        if (!Utils.isEmpty(xmlClass.isChanged)) {
            _isChanged = _objectClass.getDeclaredField(xmlClass.isChanged);
            _isChanged.setAccessible(true);
        }

        // isDeleted
        if (!Utils.isEmpty(xmlClass.isDeleted)) {
            _isDeleted = _objectClass.getDeclaredField(xmlClass.isDeleted);
            _isDeleted.setAccessible(true);
        }

        Field field;

        // Field Set
        if (xmlClass.fieldSet != null) {
            for (XmlField xmlField : xmlClass.fieldSet) {
                field = _objectClass.getDeclaredField(xmlField.name);
                field.setAccessible(true);
                _fieldNames.put(xmlField.fieldName, field);
            }
        }

        // Children Set
        if (xmlClass.childrenSet != null) {
            for (XmlChildren xmlChildren : xmlClass.childrenSet) {
                field = _objectClass.getDeclaredField(xmlChildren.name);
                field.setAccessible(true);

                if (!Collection.class.isAssignableFrom(field.getType()))
                    throw new OnmException("Children field must be a Collection!");

                Class childrenClass = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                _children.put(xmlChildren.linkName, new LinkField(field, childrenClass));
            }
        }

        // Parent Set
        if (xmlClass.parentSet != null) {
            for (XmlParent xmlParent : xmlClass.parentSet) {
                field = _objectClass.getDeclaredField(xmlParent.name);
                field.setAccessible(true);
                _parents.put(xmlParent.linkName, new LinkField(field, field.getType()));
            }
        }
    }
}
