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

package com.vyhodb.storage.space.read;

import com.vyhodb.space.Record;
import com.vyhodb.space.*;
import com.vyhodb.storage.space.*;

import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author ivykhodtsev
 *
 */
@SuppressWarnings("deprecation")
public final class ProxyRead implements Record, RecordProxyInterface {

    public final static String READ_ONLY = "Read-only mode";
    
    private final RecordContainer _rc;
    
    public ProxyRead(RecordContainer rc) {
        _rc = rc;
    }

    @Override
    public void removeChildren(String linkName) {
        _rc.getSpace().throwTRE(READ_ONLY);
    }

    @Override
    public Object setField(String fieldName, Object value) {
        _rc.getSpace().throwTRE(READ_ONLY);
        return null;
    }

    @Override
    public void delete() {
        _rc.getSpace().throwTRE(READ_ONLY);
    }
    
    @Override
    public boolean isDeleted() {
        return false;
    }

    @Override
    public void removeIndex(String indexName) {
        _rc.getSpace().throwTRE(READ_ONLY);
    }

    @Override
    public void createIndex(IndexDescriptor descriptor) {
        _rc.getSpace().throwTRE(READ_ONLY);
    }

    @Override
    public Space getSpace() {
        return (Space) _rc.getSpace();
    }

    @Override
    public long getId() {
        return _rc.getId();
    }

    @Override
    public Iterable<Record> getChildren(String linkName) {
        return getChildren(linkName, Order.ASC);
    }

    @Override
    public Iterable<Record> getChildren(String linkName, Order order) {
        return new IterableChildren(this, linkName, order);
    }

    @Override
    public long getChildrenCount(String fieldName) {
        return _rc.getChildrenCount(fieldName);
    }

    @Override
    public Set<String> getChildrenLinkNames() {
        return _rc.getChildrenLinkNames();
    }
    
    @Override
    public Iterable<Record> searchChildren(String indexName, Criterion criterion) {
        return searchChildren(indexName, criterion, Order.ASC);
    }
    
    @Override
    public Iterable<Record> searchChildren(String indexName, Criterion criterion, Order order) {
        return new IterableIndex(this, indexName, criterion, order);
    }

    @Override
    public IndexDescriptor getIndexDescriptor(String indexName) {
        return _rc.getIndexDescriptor(indexName);
    }

    @Override
    public Set<IndexDescriptor> getIndexDescriptors() {
        return _rc.getAllIndexDecsriptors();
    }

    @Override
    public Set<String> getIndexNames() {
        return _rc.getIndexNames();
    }
    
    @Override
    public boolean containsIndex(String indexName) {
        return _rc.containsIndex(indexName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getField(String fieldName) {
        return (T) _rc.getField(fieldName);
    }

    @Override
    public Set<String> getFieldNames() {
        return _rc.getFieldNames();
    }
    
    @Override
    public String toString() {
        return toString(null);
    }

    @Override
    public String toString(String[] fieldNames) {
        return _rc.toString(fieldNames);
    }

    @Override
    public final int hashCode() {
        return (int)_rc.getId();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if ( !(obj instanceof ProxyRead)) return false;
               
        ProxyRead o = (ProxyRead)obj;
        if (o._rc.getId() != _rc.getId()) return false;
        if (o._rc.getSpace() != _rc.getSpace()) return false;
        
        return true;
    }

    @Override
    public Record setParent(String linkName, Record parent) {
        _rc.getSpace().throwTRE(READ_ONLY);
        return null;
    }

    @Override
    public Record getParent(String linkName) {
        return _rc.getParent(linkName);
    }

    @Override
    public Set<String> getParentLinkNames() {
        return _rc.getParentLinkNames();
    }

    @Override
    public Iterable<Record> getSiblings(String linkName) {
        return getSiblings(linkName, Order.ASC);
    }

    @Override
    public Iterable<Record> getSiblings(String linkName, Order order) {
        return new IterableSibling(this, linkName, order);
    }

    @Override
    public Record getChildFirst(String linkName) {
        return _rc.getFirst(linkName);
    }

    @Override
    public Record getChildLast(String linkName) {
        return _rc.getLast(linkName);
    }

    @Override
    public Record searchChildrenFirst(String indexName, Criterion criterion) {
        return _rc.searchFirst(indexName, criterion, Order.ASC);
    }

    @Override
    public Record searchChildrenFirst(String indexName, Criterion criterion, Order order) {
        return _rc.searchFirst(indexName, criterion, order);
    }

    @Override
    public Record searchMinChild(String indexName) {
        return _rc.searchMin(indexName);
    }

    @Override
    public Record searchMaxChild(String indexName) {
        return _rc.searchMax(indexName);
    }

    @Override
    public Iterator<Record> iteratorChildren(String linkName, Order order) {
        return _rc.getChildren(linkName, order);
    }

    @Override
    public Iterator<Record> iteratorSiblings(String linkName, Order order) {
        return _rc.getSiblings(linkName, order);
    }

    @Override
    public Iterator<Record> iteratorSearch(String indexName, Criterion criterion, Order order) {
        return _rc.search(indexName, criterion, order);
    }

}
