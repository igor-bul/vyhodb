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

package com.vyhodb.storage.space.index;

import com.vyhodb.space.IndexedField;
import com.vyhodb.space.Nullable;
import com.vyhodb.storage.rm.SystemReader;
import com.vyhodb.storage.rm.SystemSerializable;
import com.vyhodb.storage.rm.SystemWriter;
import com.vyhodb.storage.space.RecordContainer;
import com.vyhodb.storage.space.SpaceInternal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

public final class IndexedFieldInternal implements SystemSerializable {

    private static HashSet<String> allowedFieldClassNames = new HashSet<>();
    
    static {
        allowedFieldClassNames.add(String.class.getName());
        allowedFieldClassNames.add(Long.class.getName());
        allowedFieldClassNames.add(Integer.class.getName());
        allowedFieldClassNames.add(Date.class.getName());
        allowedFieldClassNames.add(BigDecimal.class.getName());
        allowedFieldClassNames.add(BigInteger.class.getName());
        allowedFieldClassNames.add(Double.class.getName());
        allowedFieldClassNames.add(Float.class.getName());
        allowedFieldClassNames.add(Character.class.getName());
        allowedFieldClassNames.add(Short.class.getName());
        allowedFieldClassNames.add(Byte.class.getName());
        allowedFieldClassNames.add(UUID.class.getName());
    }
    
    private String _fieldName;
    private String _fieldClassName;
    private Nullable _nullable;
    
    public IndexedFieldInternal(){}
    
    public IndexedFieldInternal(SpaceInternal space, IndexedField indexedField) {
        _fieldName = indexedField.getFieldName();
        _fieldClassName = indexedField.getFieldClassName();
        _nullable = indexedField.getNullable();
        
        if (_fieldName == null) {
            space.throwTRE("Can't create index: [field name] is null.");
        }
        
        if (_fieldClassName == null) {
            space.throwTRE("Can't create index: [field class name] is null.");
        }
        
        if (_nullable == null) {
            space.throwTRE("Can't create index: [nullable] is null.");
        }
        
        if (! allowedFieldClassNames.contains(_fieldClassName)) {
            space.throwTRE("Specified fieldType is not supported. Field name [" + _fieldName + "].");
        }
    }
    
    @Override
    public void read(SystemReader reader) {
        _fieldName = reader.getStringConst();
        _fieldClassName = reader.getStringConst();
        _nullable = getNullable(reader);
    }
    
    @Override
    public void write(SystemWriter writer) {
        writer.putStringConst(_fieldName);
        writer.putStringConst(_fieldClassName);
        putNullable(writer);
    }
    
    @SuppressWarnings("rawtypes")
    public Comparable validate(SpaceInternal space, Comparable value) {
        // Checks for null
        if (value == null && _nullable == Nullable.NOT_NULL) {
            space.throwTRE("Field [" + _fieldName + "] can't be null and must be specified.");
        }
        
        // Checks for class
        if (value != null && ! _fieldClassName.equals(value.getClass().getName())) {
            space.throwTRE("Wrong field value class. Field name [" + _fieldName + "], expected class [" + _fieldClassName + "], actual class [" + value.getClass().getName() + "]");
        }
        
        return value;
    }
    
    @SuppressWarnings("rawtypes")
    public Comparable buildKeyField(RecordContainer recordContainer) {
        SpaceInternal space = recordContainer.getSpace();
        
        Object objValue = recordContainer.getField(_fieldName);
        if ( objValue != null && !(objValue instanceof Comparable) ) {
            space.throwTRE("Can't use value of field [" + _fieldName + "] because it does not implement Comparable interface. Field value:" + objValue + ", value class:" + objValue.getClass());
        }
        
        return validate(space, (Comparable) objValue);
    }
    
    public String getFieldName() {
        return _fieldName;
    }
   
    public IndexedField toIndexedField() {
        return new IndexedField(_fieldName, _fieldClassName, _nullable);
    }
    
    private void putNullable(SystemWriter writer) {
        writer.putBoolean(Nullable.NULL == _nullable);
    }
    
    private Nullable getNullable(SystemReader reader) {
        return reader.getBoolean() ? Nullable.NULL : Nullable.NOT_NULL;
    }
}
