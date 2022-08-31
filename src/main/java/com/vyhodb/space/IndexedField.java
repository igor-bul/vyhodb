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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

/**
 * This class represents metadata of indexed field.
 * <p>
 * Objects of this class have no connection to either records nor
 * {@linkplain IndexDescriptor} and can be reused by many
 * {@linkplain IndexDescriptor} objects.
 * 
 * <h5>Supported indexed field classes</h5> Table below shows classes, which
 * objects can be used as indexed field values:
 * <p>
 * <table border="1">
 * <tr>
 * <td>{@linkplain String}</td>
 * <td>{@linkplain Long}</td>
 * <td>{@linkplain Integer}</td>
 * </tr>
 * <tr>
 * <td>{@linkplain Date}</td>
 * <td>{@linkplain BigDecimal}</td>
 * <td>{@linkplain BigInteger}</td>
 * </tr>
 * <tr>
 * <td>{@linkplain Double}</td>
 * <td>{@linkplain Float}</td>
 * <td>{@linkplain Character}</td>
 * </tr>
 * <tr>
 * <td>{@linkplain Short}</td>
 * <td>{@linkplain Byte}</td>
 * <td>{@linkplain UUID}</td>
 * </tr>
 * </table>
 * 
 * @see IndexDescriptor
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public final class IndexedField implements Serializable {

    private static final long serialVersionUID = 8093056451849241464L;

    private String _fieldClassName;
    private String _fieldName;
    private Nullable _nullable;

    @Deprecated
    public IndexedField() {
    }

    /**
     * Creates indexed field descriptor.
     * 
     * @param fieldName
     *            indexed field name
     * @param fieldClass
     *            indexed field's class
     */
    public IndexedField(String fieldName, Class<?> fieldClass) {
        this(fieldName, fieldClass, Nullable.NULL);
    }

    /**
     * Creates indexed field descriptor.
     * 
     * @param fieldName
     *            indexed field name
     * @param fieldClass
     *            indexed field's class
     * @param nullable
     *            nullable policy
     */
    public IndexedField(String fieldName, Class<?> fieldClass, Nullable nullable) {
        this(fieldName, fieldClass.getName(), nullable);
    }

    /**
     * Creates indexed field descriptor.
     * 
     * @param fieldName
     *            indexed field name
     * @param fieldClassName
     *            indexed field's class name
     * @param nullable
     *            nullable policy
     */
    public IndexedField(String fieldName, String fieldClassName, Nullable nullable) {
        if (fieldName == null) {
            throw new IllegalArgumentException("[fieldName] is null");
        }

        if (fieldClassName == null) {
            throw new IllegalArgumentException("[fieldClassName] is null");
        }

        if (nullable == null) {
            throw new IllegalArgumentException("[nullable] is null");
        }

        _fieldName = fieldName;
        _fieldClassName = fieldClassName;
        _nullable = nullable;
    }

    /**
     * Returns indexed field class name.
     * 
     * @return indexed field class name
     */
    public String getFieldClassName() {
        return _fieldClassName;
    }

    /**
     * Returns indexed field name.
     * 
     * @return indexed field name
     */
    public String getFieldName() {
        return _fieldName;
    }

    /**
     * Returns <strong>null</strong> policy for indexed field
     * 
     * @return null policy
     */
    public Nullable getNullable() {
        return _nullable;
    }

    /**
     * Indicates whether <strong>null</strong> values are allowed for indexed
     * field.
     * 
     * @return whether null values are allowed or not.
     */
    public boolean isNullable() {
        return Nullable.NULL == _nullable;
    }
}
