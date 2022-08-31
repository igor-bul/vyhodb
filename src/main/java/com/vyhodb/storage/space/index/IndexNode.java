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
import com.vyhodb.storage.rm.SystemWriter;
import com.vyhodb.storage.space.Container;
import com.vyhodb.storage.space.SpaceInternal;
import com.vyhodb.storage.space.index.iterator.TupleIterator;

import static com.vyhodb.utils.Utils.compare;

public final class IndexNode extends Container implements Node {

    @SuppressWarnings("rawtypes")
    Comparable[] _keys;
    long[] _children;
    int _size = 0;  // remember that for non-leaf nodes size is more for one.
    
    /**
     * Read constructor
     */
    public IndexNode()
    {
        _keys = new Comparable[IndexRoot.M];
        _children = new long[IndexRoot.M + 1];
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void read(SystemReader reader) {
        _size = reader.getInt();

        // Reads _keys
        for (int i = 0; i < _size; i++) {
            _keys[i] = (Comparable) reader.getValue();
        }
        
        // Reads _children
        for (int i = 0; i <= _size; i++) {
            _children[i] = reader.getLong();
        }
    }

    @Override
    public void write(SystemWriter writer) {
        // writer.putBoolean(_ascending);
        writer.putInt(_size);
        
        // Writes _keys
        for (int i = 0; i < _size; i++) {
            writer.putValue(_keys[i]);
        }
        
        // Writes _children
        for (int i = 0; i <= _size; i++) {
            writer.putLong(_children[i]);
        }
    }

    @Override
    public short getType() {
        return CONTAINER_TYPE_INDEX_NODE;
    }
    
    
    @SuppressWarnings("rawtypes")
    @Override
    public void insert(Comparable key, long link) {
        lock();
        
        int index = findIndex(key);
        
        Node child = loadChild(index);
        child.lock();
        child.insert(key, link);
        adjust(child, index);    
        child.unlock();
        unlock();
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void remove(Comparable key, long link) {
        if (_size == 0) {
            return; // For debug perpuses only must be removed
        }
        
        lock();
        
        int index = findIndex(key);
        Node child = loadChild(index);
        
        child.lock();
        child.remove(key, link);
        adjust(child, index);
        child.unlock();
        unlock();
    }
    
    private void adjust(Node child, int index) {
        // Checks for overflow and redistribute/split if needed
        if (child.isOverflow())
        {
            // Retrieves and locks siblings
            Node left = getLeftSibling(index);
            if (left != null) {
                left.lock();
            }
            Node right = getRightSibling(index);
            if (right != null) {
                right.lock();
            }
                        
            // Redistribute Left -> Right
            if (right != null && !right.isFull())
            {
                redistLR(child, right, index);
            }
            // Redistribute Left <- Right
            else if (left != null && !left.isFull()) 
            {
                redistRL(left, child, index - 1);
            }
            // Split
            else 
            {
                SplitResult split = child.split();
                _children[index] = split.newNodeId;
                insertAt(index, split.key, child.getId());
            }
            
            // Unlock siblings
            if (right != null) {
                right.unlock();
            }
            if (left != null) {
                left.unlock();
            }
        }
        else if (child.isUnderflow()) 
        {
            // Retrieves and locks siblings
            Node left = getLeftSibling(index);
            if (left != null) {
                left.lock();
            }
            Node right = getRightSibling(index);
            if (right != null) {
                right.lock();
            }
            
            
            if (left != null && ! left.isOnVergeUnderflow()) 
            {
                redistLR(left, child, index - 1);
            }
            else if (right != null && ! right.isOnVergeUnderflow()) 
            {
                redistRL(child, right, index);
            }
            else
            {
                if (right != null) 
                {
                    concatinate(child, right, index);
                }
                else
                {
                    concatinate(left, child, index - 1);
                }
            }
            
            // Unlock siblings
            if (right != null) {
                right.unlock();
            }
            if (left != null) {
                left.unlock();
            }
        }
    }
    
    private Node getLeftSibling(int index) {
        if (index > 0) {
            return loadChild(index - 1);
        }
        else {
            return null;
        }
    }
    
    private Node getRightSibling(int index) {
        if (index < _size) {
            return loadChild(index + 1);
        }
        else {
            return null;
        }
    }
    
    private void redistLR(Node left, Node right, int leftIndex) {
        _keys[leftIndex] = left.redistLR(_keys[leftIndex], right);
        setDirty();
    }
    
    private void redistRL(Node left, Node right, int leftIndex) {
        _keys[leftIndex] = right.redistRL(_keys[leftIndex], left);
        setDirty();
    }
        
    private void concatinate(Node left, Node right, int leftIndex)
    {
        left.concatinate(right, _keys[leftIndex]);
        _children[leftIndex + 1] = left.getId();
        removeAt(leftIndex);
    }
    
    /**
     * TODO the same method
     */
    @Override
    public boolean isUnderflow() {
        return _size < IndexRoot.M21;
    }
    
    /**
     * TODO the same method
     */
    @Override
    public boolean isOverflow() {
        return _size == IndexRoot.M;
    }
    
    @Override
    public boolean isFull() {
        return _size == IndexRoot.FULL;
    }
    
    @Override
    public boolean isOnVergeUnderflow() {
        return _size == IndexRoot.M21;
    }
    
    @Override
    public SplitResult split() {
        lock();
        
        IndexNode newNode = new IndexNode();
        newNode.lock();
        _space.create(newNode);
        
        System.arraycopy(_keys, IndexRoot.N, newNode._keys, 0, IndexRoot.N);
        System.arraycopy(_children, IndexRoot.N, newNode._children, 0, IndexRoot.N + 1);
        
        newNode._size = IndexRoot.N;
        _size = IndexRoot.M21;
        
        SplitResult result = new SplitResult(newNode.getId(), _keys[IndexRoot.M21]);
        
        trim();
        setDirty();
        
        newNode.unlock();
        unlock();
        
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void concatinate(Node rightSibling, Comparable key) {
        IndexNode right = (IndexNode) rightSibling;
        
        _keys[_size] = key;
        _size++;        
        
        System.arraycopy(right._keys, 0, _keys, _size, right._size);
        System.arraycopy(right._children, 0, _children, _size, right._size + 1);
        
        _size += right._size;
        setDirty();
        // right isn't set as modified, because it is removed (or considered as removed)
        // just after this method invocation.
    }
    
    private void trim()
    {
        for (int i = _size; i < _keys.length; i++) {
            _keys[i] = null;
        }
        
        for (int i = (_size + 1); i < _children.length; i++) {
            _children[i] = SpaceInternal.NULL;
        }
    }
    
    @SuppressWarnings("rawtypes")
    private int findIndex(Comparable key)
    {
        int i = 0;
        for (; i < _size; i++)
        {
            if (compare(key, _keys[i]) < 1)
                break;
        }
        
        return i;
    }
    
    @SuppressWarnings("rawtypes")
    private void insertAt(int index, Comparable key, long childId)
    {
        // Inserts key
        System.arraycopy(_keys, index, _keys, index + 1, _size - index);
        _keys[index] = key;
        
        // Inserts link
        System.arraycopy(_children, index, _children, index + 1, (_size + 1) - index);
        _children[index] = childId;
        
        _size++;
        setDirty();
    }
    
    // TODO this method is the same as in Leaf class.
    private void removeAt(int index)
    {
        System.arraycopy(_keys, index + 1, _keys, index, _keys.length - index - 1);
        System.arraycopy(_children, index + 1, _children, index, _keys.length - index - 1 + 1);
        
        _size--;
        setDirty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public TupleIterator search(Comparable searchKey) {
        try
        {
            lock();
            return loadChild(findIndex(searchKey)).search(searchKey);
        }
        finally
        {
            unlock();
        }
    }


    @Override
    public TupleIterator right() {
        try
        {
            lock();
            return loadChild(_size).right();
        }
        finally
        {
            unlock();
        }
        
    }


    @Override
    public TupleIterator left() {
        try
        {
            lock();
            return loadChild(0).left();
        }
        finally
        {
            unlock();
        }
    }


    @Override
    public int size() {
        return _size;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparable<?> redistLR(Comparable parentKey, Node right) {
        IndexNode rightNode = (IndexNode) right;
        int count = _size - (_size + rightNode._size) / 2;
        
        // TODO check for count - 
        // if count == 1 - there might not be any reasons for redistribution at all.
        // Consider invoking distribution after checking neighbors
        
        // Shifts to right right node
        {
            System.arraycopy(rightNode._keys, 0, rightNode._keys, count, rightNode._size);
            System.arraycopy(rightNode._children, 0, rightNode._children, count, rightNode._size + 1); 
        }
        
        // Sets parent key into ... 
        rightNode._keys[count - 1] = parentKey;
        parentKey = _keys[_size - count];
        
        // Copies to right node
        {
            System.arraycopy(_keys, _size - count + 1, rightNode._keys, 0, count - 1);
            System.arraycopy(_children, _size - count + 1, rightNode._children, 0, count);
        }
        
        // Sizes
        rightNode._size += count;
        _size -= count;
        
        trim();
        setDirty();
        rightNode.setDirty();
        
        return parentKey;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparable<?> redistRL(Comparable parentKey, Node left) {
        IndexNode leftNode = (IndexNode) left;
        int count = _size - (_size + leftNode._size) / 2;
        
        // Sets parents
        leftNode._keys[leftNode._size] = parentKey;
        parentKey = _keys[count - 1];
        
        // Copies to left node
        {
            System.arraycopy(_keys, 0, leftNode._keys, leftNode._size + 1, count - 1);
            System.arraycopy(_children, 0, leftNode._children, leftNode._size + 1, count);
        }
        
        // Shifts
        {
            System.arraycopy(_keys, count, _keys, 0, _size - count);
            System.arraycopy(_children, count, _children, 0, _size - count + 1);
        }
        
        // Sizes
        leftNode._size += count;
        _size -= count;
        
        trim();
        setDirty();
        leftNode.setDirty();
        
        return parentKey;
    }
    
    private Node loadChild(int index)
    {
        return (Node) _space.get(_children[index]);
    }

}
