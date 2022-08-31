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

import com.vyhodb.storage.rm.SystemReader;
import com.vyhodb.storage.rm.SystemSerializable;
import com.vyhodb.storage.rm.SystemWriter;
import com.vyhodb.storage.space.Container;
import com.vyhodb.storage.space.SpaceInternal;
import com.vyhodb.storage.space.index.iterator.TupleIterator;

public final class IndexRoot implements SystemSerializable {

    public static final int M = 64;
    public static final int M21 = M/2 - 1;
    public static final int N = M/2;
    public static final int FULL = M - 1;

    private IndexDescriptorInternal _descriptor;
    
    public int _mod;
    private int _height;
    private long _rootId = SpaceInternal.NULL;
    private final SpaceInternal _space;
         
    /**
     * Read constructor
     */
    public IndexRoot(SpaceInternal space) {
        _space = space;
    }
    
    /**
     * New index constructor
     */
    public IndexRoot(SpaceInternal space, IndexDescriptorInternal indexDescriptor) {
        _space = space;
        _descriptor = indexDescriptor;
    }
    
    @Override
    public void read(SystemReader reader) {
        _mod = reader.getInt();
        _height = reader.getInt();
        _rootId = reader.getLong();
        
        _descriptor = new IndexDescriptorInternal();
        _descriptor.read(reader);
    }
    
    @Override
    public void write(SystemWriter writer) {
        writer.putInt(_mod);
        writer.putInt(_height);
        writer.putLong(_rootId);
        _descriptor.write(writer);
    }
    
    @SuppressWarnings("rawtypes")
    public void insert(Comparable key, long recordId)
    {
        Node root;
        
        // Index is empty
        if (_rootId == SpaceInternal.NULL)
        {
            root = new IndexLeaf(SpaceInternal.NULL, SpaceInternal.NULL, _descriptor.isUnique());
            _space.create((Container)root);
            _rootId = root.getId();
            _height++;
        }
        else
        {
            root = getRoot();
        }
        
        root.lock();
        root.insert(key, recordId);
        if (root.isOverflow())
        {
            SplitResult split = root.split();
            
            IndexNode newRoot = new IndexNode();
            newRoot.lock();
            _space.create(newRoot);
                        
            newRoot._keys[0] = split.key;
            newRoot._children[0] = root.getId();
            newRoot._children[1] = split.newNodeId;
            newRoot._size = 1;
            
            _rootId = newRoot.getId();
            _height++;
            newRoot.unlock();
        }
        root.unlock();
        
        //_size++;
        _mod++;
    }
    
    @SuppressWarnings("rawtypes")
    public void remove(Comparable key, long recordId)
    {
        if (_rootId == SpaceInternal.NULL) return;
        
        Node root = getRoot();
        
        root.remove(key, recordId);
        if (root.size() == 0)
        {
            if (root instanceof IndexLeaf)
            {
                _rootId = SpaceInternal.NULL;
            }
            else
            {
                _rootId = ((IndexNode) root)._children[0];
                
                // Can't happen.But need to check
                if (_rootId == SpaceInternal.NULL)
                {
                    _space.throwTRE("Critical error. IndexNode is empty.");
                }
            }
            
            _height--;
        }
                
        // _size--;
        _mod++;
    }
    
    @SuppressWarnings("rawtypes")
    public TupleIterator search(Comparable searchKey)
    {
        if (_rootId == SpaceInternal.NULL) {
            return TupleIterator.EMPTY_ITERATOR;
        }
        
        Node root = getRoot();
        return root.search(searchKey);
    }
    
    public TupleIterator right()
    {
        if (_rootId == SpaceInternal.NULL) {
            return TupleIterator.EMPTY_ITERATOR;
        }
        
        Node root = getRoot();
        return root.right();
    }
    
    public TupleIterator left()
    {
        if (_rootId == SpaceInternal.NULL) {
            return TupleIterator.EMPTY_ITERATOR;
        }
        
        Node root = getRoot();
        return root.left();
    }
    
    public IndexDescriptorInternal getDescriptor()
    {
        return _descriptor;
    }
    
    private Node getRoot() {
        Node root = (Node) _space.get(_rootId);
        if (root == null) {
            _space.throwTRE("Critical error. Can't find index root node. Id:" + _rootId + ", Index name:" + _descriptor.getIndexName());
        }
            
        return root;
    }
}
