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

package com.vyhodb.storage.space;

import com.vyhodb.space.Record;
import com.vyhodb.space.*;
import com.vyhodb.space.criteria.All;
import com.vyhodb.storage.rm.SystemReader;
import com.vyhodb.storage.rm.SystemWriter;
import com.vyhodb.storage.rm.ValueSerializer;
import com.vyhodb.storage.space.index.IndexDescriptorInternal;
import com.vyhodb.storage.space.index.IndexRoot;
import com.vyhodb.storage.space.index.iterator.IndexIterator;
import com.vyhodb.storage.space.index.iterator.RangeIterator;
import com.vyhodb.storage.space.index.iterator.RecordIdIterator;

import java.util.*;
import java.util.Map.Entry;

public final class RecordContainer extends Container {

    private final static char FIELD_VALUE_SEPARATOR = '=';
    private final static char[] FIELD_SEPARATOR = new char[]{',', ' '};
    
    public final static String FIELD_NAME_NULL = "[Field name] is null.";
    public final static String FROM_RECORD_NULL = "[from] is null.";
    public final static String FROM_RECORD_WRONG_SPACE = "[from] record is from other space.";
    public final static String LINK_NAME_NULL = "[Link name] is null.";
    public final static String INDEX_NAME_NULL = "[Index name] is null.";
    public final static String INDEX_DESCRIPTOR_NULL = "[Index descriptor] is null.";
    public final static String WRONG_INDEX_NAME = "Index name with specified name [%s] does not exist.";
    public final static String ROOT_RECORD_DELETED = "Root record can't be deleted.";
    public static final String PARENT_RECORD_DELETED = "Parent record has been deleted.";
    
    public HashMap<String, Object> fields = new HashMap<>();
    public HashMap<String, ListRoot> children = new HashMap<>();
    public HashMap<String, ListNode> parents = new HashMap<>();
    public HashMap<String, IndexRoot> indexes = new HashMap<>();
    public HashSet<IndexDescriptorInternal> innerIndexDescriptors = new HashSet<>();
    
    @Override
    public void read(SystemReader reader) {
        String name;
        int size = 0;
        
        // Reads fields
        Object fieldValue;
        size = reader.getShort();
        for (int i = 0; i < size; i++) {
            name = reader.getStringConst();
            fieldValue = reader.getValue();
            fields.put(name, fieldValue);
        }
        
        // Reads children
        ListRoot listRoot;
        size = reader.getInt();
        for (int i = 0; i < size; i++) {
            name = reader.getStringConst();
            listRoot = new ListRoot();
            listRoot.read(reader);
            children.put(name, listRoot);
        }
        
        // Reads parents
        ListNode listNode;
        size = reader.getShort();
        for (int i = 0; i < size; i++) {
            name = reader.getStringConst();
            listNode = new ListNode();
            listNode.read(reader);
            parents.put(name, listNode);
        }
        
        // Reads indexes
        IndexRoot indexRoot = null;
        size = reader.getShort();
        for (int i = 0; i < size; i++) {
            indexRoot = new IndexRoot(_space);
            indexRoot.read(reader);
            indexes.put(indexRoot.getDescriptor().getIndexName(), indexRoot);
        }
        
        // Reads inner index descriptors
        //
        // Optimization for read transactions. 
        // In read transactions we don't use inner index descriptors at all.
        if (! ((Space)_space).isReadOnly()) {
            size = reader.getInt();
            IndexDescriptorInternal innerDescriptor;
            for (int i = 0; i < size; i++) {
                innerDescriptor = new IndexDescriptorInternal();
                innerDescriptor.read(reader);
                innerIndexDescriptors.add(innerDescriptor);
            }
        }
    }

    @Override
    public void write(SystemWriter writer) {
        // Clears empty children
        removeEmptyChildrens();
        
        // Writes fields
        writer.putShort((short)fields.size());
        for (Entry<String, Object> entry : fields.entrySet()) {
            writer.putStringConst(entry.getKey());
            writer.putValue(entry.getValue());
        }
        
        // Writes children
        writer.putInt(children.size());
        for (Entry<String, ListRoot> entry : children.entrySet()) {
            writer.putStringConst(entry.getKey());
            entry.getValue().write(writer);
        }

        // Writes parents
        writer.putShort((short)parents.size());
        for (Entry<String, ListNode> entry : parents.entrySet()) {
            writer.putStringConst(entry.getKey());
            entry.getValue().write(writer);
        }
        
        // Writes Indexes
        writer.putShort((short)indexes.size());
        for (IndexRoot indexRoot : indexes.values()) {
            indexRoot.write(writer);
        }
        
        // Writes inner index descriptors
        writer.putInt(innerIndexDescriptors.size());
        for (IndexDescriptorInternal innerIndexDescriptor : innerIndexDescriptors) {
            innerIndexDescriptor.write(writer);
        }
    }

    private void removeEmptyChildrens() {
        Iterator<Entry<String, ListRoot>> iterator = children.entrySet().iterator();
        Entry<String, ListRoot> entry;
        while (iterator.hasNext())
        {
            entry = iterator.next();
            if (entry.getValue().size == 0)
                iterator.remove();
        }
    }

    @Override
    public short getType() {
        return CONTAINER_TYPE_RECORD;
    }
    
    
    public Object getField(String fieldName) {
        if (fieldName == null) {
            _space.throwTRE(FIELD_NAME_NULL);
        }
        
        Object value = fields.get(fieldName);
        
        // Virtual link field
        if (value == null) {
            ListNode listNode = parents.get(fieldName);
            if (listNode != null) {
                value = listNode.parent;
            }
        }
                
        return ValueSerializer.checkType(_space, value);
    }
    
    public Object setField(String fieldName, Object value) {
        if (fieldName == null) {
            _space.throwTRE(FIELD_NAME_NULL);
        }
        
        lock();
        setDirty();
        
        Object oldValue = null;
        value = ValueSerializer.checkType(_space, value);
        
        // Removing
        {
            updateParentIndexesRemove(fieldName);
            
            oldValue = fields.get(fieldName);
            fields.remove(fieldName);
        }
        
        // Adding
        {
            if (value != null) {
                fields.put(fieldName, value);
            }
            
            updateParentIndexesAdd(fieldName);
        }

        unlock();
        
        // Checks for max field count
        if (fields.size() > Short.MAX_VALUE) {
            _space.throwTRE("Maximum fields (" + Short.MAX_VALUE + ") per record has exceeded.");
        }
        
        return oldValue;
    }
    
    private void updateParentIndexesRemove(String removingField) {
        RecordContainer parent;
        IndexRoot indexRoot;
        
        for (IndexDescriptorInternal descriptor : innerIndexDescriptors) {
            if (descriptor.containsField(removingField)) {
                parent = (RecordContainer) _space.get(descriptor.getParentRecordId());
                
                parent.lock();
                parent.setDirty();
                
                indexRoot = parent.indexes.get(descriptor.getIndexName());
                indexRoot.remove(descriptor.buildKey(this), _id);
                              
                parent.unlock();
            }
        }
    }
    
    private void updateParentIndexesAdd(String addingField) {
        RecordContainer parent;
        IndexRoot indexRoot;
        
        for (IndexDescriptorInternal descriptor : innerIndexDescriptors) {
            if (descriptor.containsField(addingField)) {
                parent = (RecordContainer) _space.get(descriptor.getParentRecordId());
                
                parent.lock();
                parent.setDirty();
                
                indexRoot = parent.indexes.get(descriptor.getIndexName());
                indexRoot.insert(descriptor.buildKey(this), _id);
                              
                parent.unlock();
            }
        }
    }
    
    public Record getParent(String linkName) {
        if (linkName == null) {
            _space.throwTRE(LINK_NAME_NULL);
        }
        
        ListNode listNode = parents.get(linkName);
        if (listNode != null) {
            return ((Space) _space).getRecord(listNode.parent);
        }
        else {
            return null;
        }
    }
    
    public Record setParent(String linkName, Record parent) {
        if (linkName == null) {
            _space.throwTRE(LINK_NAME_NULL);
        }
        
        lock();
        setDirty();

        RecordContainer oldRC = linkRemove(linkName);
                
        RecordContainer parentRC = null;
        if (parent != null) {
            if (parent.getSpace() != _space) {
                _space.throwTRE("Can't create link to the record from other space.");
            }
            
            parentRC = (RecordContainer) _space.get(parent.getId());
            if (parentRC == null) {
                _space.throwTRE("Can't find parent record in space. Record might have been deleted.");
            }
            
            parentRC.lock();
        }
        
        linkAdd(parentRC, linkName);
        
        if (parentRC != null) {
            parentRC.unlock();
        }
        unlock();
        
        if (oldRC != null) {
            return _space.getRecord(oldRC._id);
        } else {
            return null;
        }
    }
    
    private RecordContainer linkRemove(String linkName) {
        ListNode listNode = parents.get(linkName);
        
        updateParentIndexesRemove(linkName);
        
        if (listNode != null)
        {
            RecordContainer parent = (RecordContainer) _space.get(listNode.parent);
            
            parent.lock();
            parent.setDirty();
            
            // Removes values from parent indexes
            // Removes inner descriptors
            IndexDescriptorInternal descriptor;
            List<IndexRoot> parentIndexes = parent.getParentIndexes(linkName);
            for (IndexRoot indexRoot : parentIndexes) {
                descriptor = indexRoot.getDescriptor();
                indexRoot.remove(descriptor.buildKey(this), _id);
                innerIndexDescriptors.remove(descriptor);
            }
            
            // Remove ListNode
            listUnlink(parent, linkName, listNode);
            parents.remove(linkName);
            
            parent.unlock();
            
            return parent;
        }
        else {
            return null;
        }
    }
    
    private void linkAdd(RecordContainer parent, String linkName) {
        if (parent != null) {
            parent.lock();
            parent.setDirty();
            
            // Creates and insert ListNode
            ListNode childNode = new ListNode();
            listAddLast(parent, linkName, childNode);
            parents.put(linkName, childNode);
                    
            // Get parent's indexes
            List<IndexRoot> parentIndexes = parent.getParentIndexes(linkName);
            
            // Adds inner indexes
            IndexDescriptorInternal descriptor;
            for (IndexRoot indexRoot : parentIndexes) {
                descriptor = indexRoot.getDescriptor();
                innerIndexDescriptors.add(descriptor);
                indexRoot.insert(descriptor.buildKey(this), _id);
            }
            
            parent.unlock();
        }
        
        updateParentIndexesAdd(linkName);
    }
    
       
    
    private List<IndexRoot> getParentIndexes(String childLinkName) {
        if (indexes.isEmpty()) return Collections.emptyList();
        
        ArrayList<IndexRoot> result = new ArrayList<>();
        for (IndexRoot indexRoot : indexes.values()) {
            if (indexRoot.getDescriptor().getLinkName().equals(childLinkName)) {
                result.add(indexRoot);
            }
        }
        
        return result;
    }
    
    public void indexCreate(IndexDescriptor descriptor) {
        if (descriptor == null) {
            _space.throwTRE(INDEX_DESCRIPTOR_NULL);
        }
        
        // Check index name
        String indexName = descriptor.getIndexName();
        if (indexes.containsKey(indexName)) {
            _space.throwTRE("Index with specified name already exists. Index name:" + indexName);
        }
        
        // Checks index count
        if (indexes.size() == Short.MAX_VALUE) {
            _space.throwTRE("Maximum index count (" + Short.MAX_VALUE + ") per record has exceeded");
        }
        
        
        IndexDescriptorInternal desc = new IndexDescriptorInternal(_space, _id, descriptor);
        
        lock();
        setDirty();
                
        // Creates index root
        IndexRoot indexRoot = new IndexRoot(_space, desc);
        indexes.put(indexName, indexRoot);
        
        // Modifies children and inserts field values into index
        RecordContainer childRC;
        Iterator<RecordContainer> children = getRcIterator(desc.getLinkName(), Order.ASC);
        while(children.hasNext())
        {
            childRC = children.next();
            
            childRC.lock();
            childRC.setDirty();
            
            childRC.innerIndexDescriptors.add(desc);
            indexRoot.insert(desc.buildKey(childRC), childRC.getId());
            
            childRC.unlock();
        }
        
        unlock();
    }
    
    public void indexDelete(String indexName) {
        if (indexName == null) {
            _space.throwTRE(INDEX_NAME_NULL);
        }
        
        IndexRoot indexRoot = indexes.get(indexName);
        if (indexRoot == null) {
            _space.throwTRE(String.format(WRONG_INDEX_NAME, indexName));
        }
        
        IndexDescriptorInternal desc = indexRoot.getDescriptor();
        
        lock();
        setDirty();
        
        // Modifies children and removes field values from index
        RecordContainer childRC;
        Iterator<RecordContainer> childIter = getRcIterator(desc.getLinkName(), Order.ASC);
        while(childIter.hasNext())
        {
            childRC = childIter.next();
            
            childRC.lock();
            childRC.setDirty();
            
            childRC.innerIndexDescriptors.remove(desc);
            
            // For performance reasons we don't modify index. 
            // This means, that indexs' containers won't be marked as deleted. 
            // However index root's leaf won't be referenced by current record container.
            //
            // indexRoot.remove(childRC.buildKey(desc), childRC.getId());
            
            childRC.unlock();
        }
        
        // Removes index root
        indexes.remove(indexName);
        
        unlock();
    }
    
    public void removeChildren(String childLinkName) {
        if (childLinkName == null) {
            _space.throwTRE(FIELD_NAME_NULL);
        }
        
        ListRoot listRoot = children.get(childLinkName);
        if (listRoot == null) {
            return;
        }
        
        lock();
        setDirty();
        
        // Modifies children and removes field values from index
        Iterator<RecordContainer> childIter = getRcIterator(childLinkName, Order.ASC);
        while(childIter.hasNext())
        {
            childIter.next();
            childIter.remove();
        }
        
        // Removes list root
        children.remove(childLinkName);
        
        unlock();
    }
    
    public Set<String> getFieldNames() {
        return Collections.unmodifiableSet(
                new HashSet<String>(
                            fields.keySet()
                        )
                );
    }
    
    public Set<String> getParentLinkNames() {
        return Collections.unmodifiableSet(
                new HashSet<String>(
                            parents.keySet()
                        )
                );
    }
    
    public Set<String> getChildrenLinkNames() {
        return Collections.unmodifiableSet(
                new HashSet<String>(
                            children.keySet()
                        )
                );
    }
    
    public Set<String> getIndexNames() {
        return Collections.unmodifiableSet(
                new HashSet<String>(
                            indexes.keySet()
                        )
                );
    }
    
    public boolean containsIndex(String indexName) {
        return indexes.containsKey(indexName);
    }
    
    public IndexDescriptor getIndexDescriptor(String indexName) {
        if (indexName == null) {
            _space.throwTRE(INDEX_NAME_NULL);
        }
        
        IndexRoot indexRoot = indexes.get(indexName);
        if (indexRoot == null) {
            _space.throwTRE(String.format(WRONG_INDEX_NAME, indexName));
        }
        
        return indexRoot.getDescriptor().toIndexDescriptor();
    }
    
    public Set<IndexDescriptor> getAllIndexDecsriptors() {
        HashSet<IndexDescriptor> descs = new HashSet<>();
        
        for (IndexRoot indexRoot : indexes.values()) {
            descs.add(indexRoot.getDescriptor().toIndexDescriptor());
        }
                
        return Collections.unmodifiableSet(descs);
    }
    
    public void delete() {
        if (_id == 0) {
            _space.throwTRE(ROOT_RECORD_DELETED);
        }
        
        // Removes parents
        for (String linkName : getParentLinkNames()) {
            setParent(linkName, null);
        }
        
        // Removes children
        for (String fieldName : getChildrenLinkNames()) {
            removeChildren(fieldName);
        }
        
        _space.delete(this);
    }
    
    public long getChildrenCount(String fieldName) {
        if (fieldName == null) {
            _space.throwTRE(FIELD_NAME_NULL);
        }
        
        ListRoot listRoot = children.get(fieldName);
        return (listRoot == null) ? 0 : listRoot.size;
    }
    
    private void listUnlink(RecordContainer parent, String fieldName, ListNode childNode) {
        final long nextId = childNode.next;
        final long prevId = childNode.prev;
                                
        // Retrieve and modify list root
        final ListRoot listRoot = parent.children.get(fieldName);          
                    
        // Prev
        if (prevId == SpaceInternal.NULL) {
            listRoot.first = nextId;
        }
        else {
            RecordContainer prev = (RecordContainer) _space.get(prevId);
            
            prev.lock();
            prev.setDirty();
            
            ListNode prevNode = prev.parents.get(fieldName);
            prevNode.next = nextId;
            
            prev.unlock();
        }
            
        // Next
        if (nextId == SpaceInternal.NULL) {
            listRoot.last = prevId;
        }
        else {
            RecordContainer next = (RecordContainer) _space.get(nextId);
            
            next.lock();
            next.setDirty();
            
            ListNode nextNode = next.parents.get(fieldName);
            nextNode.prev = prevId;
            
            next.unlock();
        }
        
        listRoot.size--;
        listRoot.mod++;
    }
    
    private void listAddLast(RecordContainer parent, String linkName, ListNode childNode) {
        ListRoot root = parent.children.get(linkName);
        if (root == null) {
            root = new ListRoot();
            parent.children.put(linkName, root);
        }
        
        if (root.size == Long.MAX_VALUE) {
            _space.throwTRE("Max children count has reached.");
        }
        
        childNode.parent = parent.getId();
        childNode.next = SpaceInternal.NULL;
        childNode.prev = root.last;
        
        // Updates root
        if (root.first == SpaceInternal.NULL)
        {
            root.first = _id;
        }
        else
        {
            // Updates last if exist
            RecordContainer last = (RecordContainer) _space.get(root.last);
            
            last.lock();
            last.setDirty();
            
            ListNode lastNode = last.parents.get(linkName);
            lastNode.next = _id;
            
            last.unlock();
        }
        
        root.last = _id;
        root.size++;
        root.mod++;
    }
    
    public Iterator<Record> getSiblings(String linkName, Order order) {
        if (linkName == null) {
            _space.throwTRE(LINK_NAME_NULL);
        }
        
        ListNode listNode = parents.get(linkName);
        if (listNode == null) {
            return Collections.emptyIterator();
        }

        // No locks are required because of read method
        RecordContainer parentRC = (RecordContainer) _space.get(listNode.parent);
       
        // Creates RecordContainer iterator
        boolean ascending = ( Order.ASC == order );
        Iterator<RecordContainer> rcIterator = new RecordContainerIterator(
                linkName, 
                ascending, 
                _space, 
                ascending ? listNode.next : listNode.prev);
                
        // Creates children iterator
        return new ChildrenIterator(
                _space, 
                linkName, 
                rcIterator, 
                listNode.parent, 
                parentRC.children.get(linkName).mod
        );
        
    }
    
    private Iterator<RecordContainer> getRcIterator(String linkName, Order order) {
        if (linkName == null) {
            _space.throwTRE(LINK_NAME_NULL);
        }
        
        ListRoot listRoot = children.get(linkName);
        if (listRoot == null) {
            listRoot = new ListRoot();
            children.put(linkName, listRoot);
        }
        
        boolean ascending = (Order.ASC == order);
        return new RecordContainerIterator(
                linkName, 
                ascending, 
                _space, 
                ascending ? listRoot.first : listRoot.last
        );
    }
    
    public Iterator<Record> getChildren(String linkName, Order order) {
        Iterator<RecordContainer> rcIterator = getRcIterator(linkName, order);
        return new ChildrenIterator(
                _space, 
                linkName, 
                rcIterator, 
                _id, 
                children.get(linkName).mod
        );
    }
    
    public IndexIterator search(String indexName, Criterion criterion, Order order) {
        IndexRoot indexRoot = indexes.get(indexName);
        if (indexRoot == null) {
            _space.throwTRE(String.format(WRONG_INDEX_NAME, indexName));
        }
        
        IndexDescriptorInternal descriptor = indexRoot.getDescriptor();
        RangeIterator rangeIterator = descriptor.buildRanges(_space, indexRoot, criterion, order);
        RecordIdIterator recordIdIterator = new RecordIdIterator(_space, rangeIterator);
        return new IndexIterator(_space, indexName, recordIdIterator, _id, indexRoot._mod);
    }
    
    public Record getFirst(String linkName) {
        Iterator<Record> children = getChildren(linkName, Order.ASC);
        return children.hasNext() ? children.next() : null;
    }

    public Record getLast(String linkName) {
        Iterator<Record> children = getChildren(linkName, Order.DESC);
        return children.hasNext() ? children.next() : null;
    }

    public Record searchFirst(String indexName, Criterion criterion, Order order) {
        Iterator<Record> indexIterator = search(indexName, criterion, order);
        return indexIterator.hasNext() ? indexIterator.next() : null;
    }

    public Record searchMin(String indexName) {
        Iterator<Record> indexed = search(indexName, new All(), Order.ASC);
        return indexed.hasNext() ? indexed.next() : null;
    }

    public Record searchMax(String indexName) {
        Iterator<Record> indexed = search(indexName, new All(), Order.DESC);
        return indexed.hasNext() ? indexed.next() : null;
    }
    
    public String toString(String[] fieldNames) {
        StringBuilder builder = new StringBuilder();
        
        builder.append('{');
        printFields(builder, fieldNames);
        builder.append("} id=").append(_id);
        
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return toString(null);
    }
    
    private void printFields(StringBuilder builder, String[] fieldNames)
    {
        Collection<String> fieldNamesForPrint = fieldNames == null ? fields.keySet() : Arrays.asList(fieldNames);
        
        boolean first = true;
        for (String fieldName : fieldNamesForPrint) {
            if (first) {
                first = false;
            }
            else {
                builder.append(FIELD_SEPARATOR);
            }
            
            builder.append(fieldName).append(FIELD_VALUE_SEPARATOR);
            printValue(builder, fields.get(fieldName));
        }
    }
    
    private static void printValue(StringBuilder builder, Object value)
    {
        if (value instanceof String || value instanceof Date) {
            builder.append('"').append(value).append('"');
        }
        else {
            builder.append(value);
        }
    }
}
