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

import com.vyhodb.space.criteria.All;
import com.vyhodb.space.criteria.EqualComposite;

/**
 * This class represents index metadata.
 * <p>
 * IndexDescriptor describes index parameters and is used to create new index
 * (see {@linkplain Record#createIndex(IndexDescriptor)}) or to get parameters
 * of existed index (see {@linkplain Record#getIndexDescriptor(String)}).
 * <p>
 * Instances of this class have no connection with records, where indexes are
 * created. One instance of this class can be reused many times to create
 * indexes with the same parameters on different parent records.
 * 
 * <h5>Indexed record restrictions</h5>
 * Created index puts restrictions on child indexed records. Namely their
 * indexed field values should follow rules:
 * <ol>
 * <li>Should be the same class as defined by
 * {@linkplain IndexedField#getFieldClassName()}</li>
 * <li>Should not be <strong>null</strong> if
 * {@linkplain IndexedField#getNullable()} == {@linkplain Nullable#NOT_NULL}</li>
 * <li>Should be unique among all field values of indexed records if
 * {@linkplain IndexDescriptor#getUnique()} == {@linkplain Unique#UNIQUE}</li>
 * </ol>
 * <p>
 * These restrictions prevent creating new child link (and therefore adding new
 * child record) if child records' fields violate them. All child records are
 * also validated for restrictions above before creating new index. If they are
 * violated, then index is not created.
 * 
 * <h5>Simple/Composite</h5>
 * Depends on count of indexed fields, index is considered as <b>Simple</b>
 * (only one indexed field) or as <b>Composite</b> (two or more indexed fields).
 * There are only two search criteria which can be used to search in
 * <b>Composite</b> index:
 * <ol>
 * <li>{@linkplain All}</li>
 * <li>{@linkplain EqualComposite}</li>
 * </ol>
 * 
 * @see Record
 * @see IndexedField
 * @see Criterion
 * @see CriterionFactory
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public final class IndexDescriptor implements Serializable {

    private static final long serialVersionUID = 8542568958236833835L;

    private IndexedField[] _indexedFields;
    private String _indexedLinkName;
    private String _indexName;
    private Unique _unique;

    @Deprecated
    public IndexDescriptor() {
    }

    /**
     * Creates index descriptor.
     * 
     * @param indexName
     *            index name
     * @param indexedLinkName
     *            child link name, which records are indexed
     * @param indexedFields
     *            descriptors of indexed field
     */
    public IndexDescriptor(String indexName, String indexedLinkName, IndexedField... indexedFields) {
        this(indexName, indexedLinkName, Unique.DUPLICATE, indexedFields);
    }

    /**
     * Creates index descriptor.
     * 
     * @param indexName
     *            index name
     * @param indexedLinkName
     *            child link name, which records are indexed
     * @param unique
     *            uniqueness policy
     * @param indexedFields
     *            descriptors of indexed fields
     */
    public IndexDescriptor(String indexName, String indexedLinkName, Unique unique, IndexedField... indexedFields) {
        if (indexName == null) {
            throw new IllegalArgumentException("[indexName] is null");
        }

        if (indexedLinkName == null) {
            throw new IllegalArgumentException("[indexedLinkName] is null");
        }

        if (indexedFields == null) {
            throw new IllegalArgumentException("[indexedFields] is null");
        }

        if (indexedFields.length == 0) {
            throw new IllegalArgumentException("At least one indexed field must be specified.");
        }

        if (indexedFields.length > (int) Short.MAX_VALUE) {
            throw new IllegalArgumentException("Number of indexed fields can't exceed " + Short.MAX_VALUE);
        }

        _indexName = indexName;
        _indexedLinkName = indexedLinkName;
        _unique = unique;
        _indexedFields = indexedFields;

        if (containsField(indexedLinkName)) {
            throw new IllegalArgumentException("Indexed link name can't have the same name as indexed field.");
        }
    }

    /**
     * Checks if current index contains field with specified name among it's
     * indexed fields.
     * 
     * @param fieldName
     *            field name
     * @return true - if field is indexed by current index, false - otherwise
     */
    public boolean containsField(String fieldName) {
        for (int i = 0; i < _indexedFields.length; i++) {
            if (_indexedFields[i].getFieldName().equals(fieldName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns children field names which are indexed by current index.
     * 
     * @return children field names, indexed by index
     */
    public IndexedField[] getIndexedFields() {
        return _indexedFields;
    }

    /**
     * Returns child link name which records are indexed.
     * 
     * @return indexed children link name
     */
    public String getIndexedLinkName() {
        return _indexedLinkName;
    }

    /**
     * Returns index name.
     * 
     * @return index name
     */
    public String getIndexName() {
        return _indexName;
    }

    /**
     * Returns uniqueness policy.
     * 
     * @return uniqueness policy
     */
    public Unique getUnique() {
        return _unique;
    }

    /**
     * Indicates whether current index is a composite or simple index.
     * <p>
     * Composite index is an index which based on two or more fields. Simple
     * index only indexes one field.
     * 
     * @return true - if current index is composite, false - if it simple
     */
    public boolean isComposite() {
        return _indexedFields.length > 1;
    }

    /**
     * Indicates whether index supports only unique filed values.
     * 
     * @return true - if index supports only unique field values. false - if
     *         duplicate field values are allowed
     */
    public boolean isUnique() {
        return Unique.UNIQUE == _unique;
    }
}
