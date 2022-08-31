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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import com.vyhodb.onm.Id;
import com.vyhodb.onm.IsChanged;
import com.vyhodb.onm.IsDeleted;
import com.vyhodb.onm.OnmException;

public class AnnotationClassDescriptor extends ClassDescriptor {

    @SuppressWarnings("rawtypes")
    public AnnotationClassDescriptor(Class objectClass) {
        _objectClass = objectClass;
        init();
    }

    private void init() {
        com.vyhodb.onm.Record recordAnnot = (com.vyhodb.onm.Record) _objectClass.getAnnotation(com.vyhodb.onm.Record.class);
        if (recordAnnot == null)
            throw new OnmException("Can't support class " + _objectClass.getCanonicalName() + ". Please add @Record annotation");

        for (Field field : _objectClass.getDeclaredFields()) {
            field.setAccessible(true);
            processField(field);
        }

        if (_idField == null)
            throw new OnmException("Id annotation is absent. Class:" + _objectClass);
    }

    @SuppressWarnings("rawtypes")
    private void processField(Field field) {
        // @Id
        Annotation annotation = field.getAnnotation(Id.class);
        if (annotation != null) {
            if (_idField != null)
                throw new OnmException("Only one field can be annotated by @Id. Field:" + field);

            // Checks field type
            if (!(Long.TYPE.equals(field.getType()) || Long.class.equals(field.getType())))
                throw new OnmException("Only long/Long fields can be annotated by @Id. Field:" + field);

            _idField = field;
            return;
        }

        // @IsChanged
        annotation = field.getAnnotation(IsChanged.class);
        if (annotation != null) {
            if (_isChanged != null)
                throw new OnmException("Only one field can be annotated by @IsChanged. Field:" + field);

            // Checks field type
            if (!(Boolean.TYPE.equals(field.getType()) || Boolean.class.equals(field.getType())))
                throw new OnmException("Only boolean/Boolean fields can be annotated by @IsChanged. Field:" + field);

            _isChanged = field;
            return;
        }

        // @IsDeleted
        annotation = field.getAnnotation(IsDeleted.class);
        if (annotation != null) {
            if (_isDeleted != null)
                throw new OnmException("Only one field can be annotated by @IsDeleted. Field:" + field);

            // Checks field type
            if (!(Boolean.TYPE.equals(field.getType()) || Boolean.class.equals(field.getType())))
                throw new OnmException("Only boolean/Boolean fields can be annotated by @IsDeleted. Field:" + field);

            _isDeleted = field;
            return;
        }

        // @Field
        annotation = field.getAnnotation(com.vyhodb.onm.Field.class);
        if (annotation != null) {
            com.vyhodb.onm.Field fann = (com.vyhodb.onm.Field) annotation;
            _fieldNames.put(fann.fieldName(), field);
            return;
        }

        // @Parent
        annotation = field.getAnnotation(com.vyhodb.onm.Parent.class);
        if (annotation != null) {
            com.vyhodb.onm.Parent pann = (com.vyhodb.onm.Parent) annotation;
            _parents.put(pann.linkName(), new LinkField(field, field.getType()));
            return;
        }

        // @Children
        annotation = field.getAnnotation(com.vyhodb.onm.Children.class);
        if (annotation != null) {
            com.vyhodb.onm.Children cann = (com.vyhodb.onm.Children) annotation;

            if (!Collection.class.isAssignableFrom(field.getType()))
                throw new OnmException("Children field must be implement Collection interface. Field:" + field);

            Class childrenClass = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            _children.put(cann.linkName(), new LinkField(field, childrenClass));
            return;
        }
    }
}
