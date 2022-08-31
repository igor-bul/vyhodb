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

import com.vyhodb.space.*;
import com.vyhodb.space.criteria.*;
import com.vyhodb.storage.rm.SystemReader;
import com.vyhodb.storage.rm.SystemSerializable;
import com.vyhodb.storage.rm.SystemWriter;
import com.vyhodb.storage.space.RecordContainer;
import com.vyhodb.storage.space.SpaceInternal;
import com.vyhodb.storage.space.index.iterator.CompositeRangeIterator;
import com.vyhodb.storage.space.index.iterator.EqualComparator;
import com.vyhodb.storage.space.index.iterator.RangeIterator;
import com.vyhodb.storage.space.index.ranges.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

public final class IndexDescriptorInternal implements SystemSerializable {

    private long _parentRecordId = Long.MIN_VALUE;
    private String _indexName;
    private String _linkName;
    private Unique _unique;
    private IndexedFieldInternal[] _indexedFields;
    
    public IndexDescriptorInternal(){}
    
    public IndexDescriptorInternal(SpaceInternal space, long parentRecordId, IndexDescriptor descriptor) {
        _parentRecordId = parentRecordId;
        _indexName = descriptor.getIndexName();
        _linkName = descriptor.getIndexedLinkName();
        _unique = descriptor.getUnique();
        IndexedField[] fields = descriptor.getIndexedFields();
        
        // Checks descriptor's data
        {
            if (_indexName == null) {
                space.throwTRE("Can't create index: [index name] is null.");
            }
            
            if (_linkName == null) {
                space.throwTRE("Can't create index: [child link name] is null.");
            }
            
            if (fields == null) {
                space.throwTRE("Can't create index: [indexed fields] is null.");
            }
            
            if (fields.length == 0) {
                space.throwTRE("Can't create index: At least one indexed field must be specified.");
            }
            
            if (fields.length > (int)Short.MAX_VALUE) {
                space.throwTRE("Can't create index: Number of indexed fields can't exceed " + Short.MAX_VALUE);
            }
        }
        
        // Creates indexed fields
        {
            _indexedFields = new IndexedFieldInternal[fields.length];
            for (int i = 0; i < fields.length; i++) {
                _indexedFields[i] = new IndexedFieldInternal(space, fields[i]);
            }
        }
        
        if (containsField(_linkName)) {
            space.throwTRE("Can't create index: Indexed link name can't have the same name as indexed field.");
        }
    }
    
    public IndexDescriptor toIndexDescriptor() {
        IndexedField[] fields = new IndexedField[_indexedFields.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = _indexedFields[i].toIndexedField();
        }
        
        return new IndexDescriptor(_indexName, _linkName, _unique, fields);
    }

    @Override
    public int hashCode() {
        return ((int)_parentRecordId) ^ _indexName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        
        IndexDescriptorInternal other = (IndexDescriptorInternal) obj;
        if (_parentRecordId != other._parentRecordId)
            return false;
        if (!_indexName.equals(other._indexName))
            return false;
        
        return true;
    }

    @Override
    public void read(SystemReader reader) {
        _parentRecordId = reader.getLong();
        _indexName = reader.getStringConst();
        _linkName = reader.getStringConst();
        _unique = getUnique(reader);
        
        // Reads IndexedFields
        int size = reader.getShort();
        _indexedFields = new IndexedFieldInternal[size];
        for (int i = 0; i < size; i++) {
            _indexedFields[i] = new IndexedFieldInternal();
            _indexedFields[i].read(reader);
        }
    }

    @Override
    public void write(SystemWriter writer) {
        writer.putLong(_parentRecordId);
        writer.putStringConst(_indexName);
        writer.putStringConst(_linkName);
        putUnique(writer);
        
        // Writes IndexedFields
        writer.putShort((short) _indexedFields.length);
        for (int i = 0; i < _indexedFields.length; i++) {
            _indexedFields[i].write(writer);
        }
    }
    
    public boolean containsField(String fieldName) {
        for (int i = 0; i < _indexedFields.length; i++) {
            if (_indexedFields[i].getFieldName().equals(fieldName)) {
                return true;
            }
        }
        
        return false;
    }
    
    @SuppressWarnings("rawtypes")
    public RangeIterator buildRanges(SpaceInternal space, IndexRoot indexRoot, Criterion criterion, Order order) {
        if (criterion instanceof SingleKeyCriterion) {
            Comparable key =  validateSingleSearchKey(space, ((SingleKeyCriterion)criterion).getKey());
            
            if (criterion instanceof Equal) {
                return new EqualAsc(key, indexRoot);
            }
            
            if (criterion instanceof More) {
                return Order.ASC == order ? new MoreAsc(key, indexRoot) : new MoreDesc(key, indexRoot);
            }
            
            if (criterion instanceof MoreEqual) {
                return Order.ASC == order ? new MoreEqualAsc(key, indexRoot) : new MoreEqualDesc(key, indexRoot);
            }
            
            if (criterion instanceof Less) {
                return Order.ASC == order ? new LessAsc(key, indexRoot) : new LessDesc(key, indexRoot);
            }
            
            if (criterion instanceof LessEqual) {
                return Order.ASC == order ? new LessEqualAsc(key, indexRoot) : new LessEqualDesc(key, indexRoot);
            }
            
            if (criterion instanceof StartsWith) {
                return new StartsWithAsc((String)key, indexRoot);
            }
        }
        
        if (criterion instanceof BetweenExclusive) {
            BetweenExclusive betweenExclusive = (BetweenExclusive) criterion;
            Comparable from = validateSingleSearchKey(space, betweenExclusive.getFrom());
            Comparable to = validateSingleSearchKey(space, betweenExclusive.getTo());
            return Order.ASC == order ? new BetweenExclusiveAsc(indexRoot, from, to) : new BetweenExclusiveDesc(indexRoot, from, to);
        }
        
        if (criterion instanceof Between) {
            Between between = (Between) criterion;
            Comparable from = validateSingleSearchKey(space, between.getFrom());
            Comparable to = validateSingleSearchKey(space, between.getTo());
            return Order.ASC == order ? new BetweenAsc(indexRoot, from, to) : new BetweenDesc(indexRoot, from, to);
        }
        
        if (criterion instanceof All) {
            return new AllRange(indexRoot, Order.ASC == order);
        }
        
        if (criterion instanceof Null) {
            return new EqualAsc(null, indexRoot);
        }
        
        if (criterion instanceof NotNull) {
            return Order.ASC == order ? new NotNullAsc(indexRoot) : new NotNullDesc(indexRoot);
        }
        
        if (criterion instanceof In) {
            In in = (In) criterion;
            TreeSet<Comparable> elements = in.getSearchKeys();
            RangeIterator[] ranges = new RangeIterator[elements.size()];
                        
            Comparable value;
            Iterator<Comparable> iterator = (Order.ASC == order) ? elements.iterator() : elements.descendingIterator();
            
            for (int i = 0; i < ranges.length; i++) {
                value = validateSingleSearchKey(space, iterator.next());
                ranges[i] = new EqualAsc(value, indexRoot);
            }
            
            return new CompositeRangeIterator(ranges);
        }
        
        if (criterion instanceof EqualComposite) {
            EqualComposite equal = (EqualComposite) criterion;
            Comparable key = validateCompositeSearchKey(space, equal.getCompositeKey());
            return new EqualAsc(key, indexRoot);
        }
        
        space.throwTRE("Specified criteria class is not supported. Criteria class: " + criterion.getClass());
        return null;    // Not reachable line
    }
    
    @SuppressWarnings("rawtypes")
    private Comparable validateSingleSearchKey(SpaceInternal space, Comparable key) {
        if (isComposite()) {
            space.throwTRE("Specified search criteria is not supported for composite index. Index name:" + _indexName);
        }

        return _indexedFields[0].validate(space, key);
    }
    
    @SuppressWarnings({ "rawtypes"})
    private CompositeKey validateCompositeSearchKey(SpaceInternal space, Map<String, ? extends Comparable> criteriaKeys) {
        // Checks for field names correctness
        for (String keyName : criteriaKeys.keySet()) {
            if (! containsField(keyName)) {
                space.throwTRE("Index [" + _indexName + "] has no field [" + keyName + "].");
            }
        }
                
        Comparable[] keys = new Comparable[_indexedFields.length];
        LinkedList<String> equalAllFields = new LinkedList<>();
        
        IndexedFieldInternal field;
        Comparable value;
        String fieldName;
        
        for (int i = 0; i < _indexedFields.length; i++) {
            field = _indexedFields[i];
            fieldName = field.getFieldName();
            value = criteriaKeys.get(fieldName);
            
            // Field isn't specified at all. Use equal all comparator
            if (value == null && !criteriaKeys.containsKey(fieldName)) {
                keys[i] = EqualComparator.EQUAL_COMPARATOR;
                equalAllFields.add(fieldName);
                continue;
            }
            else {
                // Gap analysis
                if (! equalAllFields.isEmpty()) {
                    space.throwTRE("The following key fields must be specified: " + equalAllFields + ", or the field [" + fieldName + "] must be omitted.");
                }
            }
            
            keys[i] = field.validate(space, value);
        }
        
        return new CompositeKey(keys);
    }
    
    @SuppressWarnings("rawtypes")
    public Comparable buildKey(RecordContainer recordContainer) {
        if (isComposite()) {
            Comparable[] keys = new Comparable[_indexedFields.length];
            
            for (int i = 0; i < _indexedFields.length; i++) {
                keys[i] = _indexedFields[i].buildKeyField(recordContainer);
            }
            
            return new CompositeKey(keys);
        }
        else {
            return _indexedFields[0].buildKeyField(recordContainer);
        }
    }
    
    public boolean isComposite() {
        return _indexedFields.length > 1;
    }
    
    private void putUnique(SystemWriter writer) {
        writer.putBoolean(Unique.UNIQUE == _unique);
    }
    
    private Unique getUnique(SystemReader reader) {
        return reader.getBoolean() ? Unique.UNIQUE : Unique.DUPLICATE;
    }

    public boolean isUnique() {
        return Unique.UNIQUE == _unique;
    }

    public String getIndexName() {
        return _indexName;
    }

    public String getLinkName() {
        return _linkName;
    }

    public long getParentRecordId() {
        return _parentRecordId;
    }
}
