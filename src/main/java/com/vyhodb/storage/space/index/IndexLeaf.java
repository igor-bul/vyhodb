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
import static com.vyhodb.utils.Utils.equal;

public final class IndexLeaf extends Container implements Node {

    @SuppressWarnings("rawtypes")
    public Comparable[] _keys;
    long[] _links;
    long _next;
    long _prev;
    public int _size = 0; 
    boolean _isUnique;
    
    @SuppressWarnings("rawtypes")
    @Override
    public void read(SystemReader reader) {
        _isUnique = reader.getBoolean();
        _next = reader.getLong();
        _prev = reader.getLong();
        _size = reader.getInt();
        
        // Reads _keys
        for (int i = 0; i < _size; i++) {
            _keys[i] = (Comparable) reader.getValue();
        }
        
        // Reads _links
        for (int i = 0; i < _size; i++) {
            _links[i] = reader.getLong();
        }
    }

    @Override
    public void write(SystemWriter writer) {
        writer.putBoolean(_isUnique);
        writer.putLong(_next);
        writer.putLong(_prev);
        writer.putInt(_size);
        
        // Writes _keys
        for (int i = 0; i < _size; i++) {
            writer.putValue(_keys[i]);
        }
        
        // Writes _links
        for (int i = 0; i < _size; i++) {
            writer.putLong(_links[i]);
        }
    }

    @Override
    public short getType() {
        return CONTAINER_TYPE_INDEX_LEAF;
    }
    
    /**
     * Constructor is used only for read 
     */
    public IndexLeaf()
    {
        _keys = new Comparable[IndexRoot.M];
        _links = new long[IndexRoot.M];
    }
    
    /**
     * Constructor is used for new leafs creation.
     * 
     * @param prev
     * @param next
     * @param unique
     */
    public IndexLeaf(long prev, long next, boolean unique) {
        this();
        _next = next;
        _prev = prev;
        _isUnique = unique;
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
    private void insertAt(int index, Comparable key, long link)
    {
        // Inserts key
        System.arraycopy(_keys, index, _keys, index + 1, _size - index);
        _keys[index] = key;
        
        // Inserts link
        System.arraycopy(_links, index, _links, index + 1, _size - index);
        _links[index] = link;
        
        _size++;
        setDirty();
    }
    
    private void removeAt(int index)
    {
        System.arraycopy(_keys, index + 1, _keys, index, _keys.length - index - 1);
        System.arraycopy(_links, index + 1, _links, index, _links.length - index - 1);
        
        _size--;
        setDirty();
    }
    
    /* (non-Javadoc)
     * @see bplus.Node#insert(java.lang.Comparable, java.lang.Object)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void insert(Comparable key, long link)
    {
        lock();
        int index = findIndex(key);
        
        if (_isUnique)
        {
            // Checks for unique key
            if (_size > 0 && equal(key, _keys[index]))
            {
                _space.throwTRE("Unique index constraint violation. Specified value already exists: " + key);
            }
                
            insertAt(index, key, link);
        }
        else
        {
            // Update
            if (_size > 0 && equal(key, _keys[index]))
            {
                IndexLinks links = (IndexLinks) _space.get(_links[index]);
                links.addLink(_space, link);                
            }
            // New Links
            else
            {
                IndexLinks links = new IndexLinks();
                links.addLink(_space, link);
                _space.create(links);
                
                insertAt(index, key, links.getId());
            }
        }

        unlock();
    }
    
    /* (non-Javadoc)
     * @see bplus.Node#remove(java.lang.Comparable)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void remove(Comparable key, long link)
    {
        lock();
        int index = findIndex(key);
        
        // Checks for key presence
        // This case can't occur, included for check
        // TODO refactoring might require for this check. It might be simply removed
        if (index == _size || ! equal(key, _keys[index])) 
        {
            _space.throwTRE("Key doesn't exist");
            
        }
        
        if (_isUnique)
        {
            removeAt(index);
        }
        else
        {
            IndexLinks links = (IndexLinks) _space.get(_links[index]);
            links.removeLink(link);
            
            if (links.size() == 0)
            {
                _space.delete(links);
                removeAt(index);
            }
        }

        unlock();
    }
    
    /* (non-Javadoc)
     * @see bplus.Node#getMaxKey()
     */
    @SuppressWarnings("rawtypes")
    private Comparable getMaxKey()
    {
        return _keys[_size - 1];
    }
    
    /* (non-Javadoc)
     * @see bplus.Node#isUnderflow()
     */
    @Override
    public boolean isUnderflow()
    {
        return _size < IndexRoot.M21;
    }
    
    /* (non-Javadoc)
     * @see bplus.Node#isOverflow()
     */
    @Override
    public boolean isOverflow()
    {
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
    
    /* (non-Javadoc)
     * @see bplus.Node#split()
     */
    @Override
    public SplitResult split()
    {
        //System.out.println("Leaf.split");
        
        lock();
        
        IndexLeaf newLeaf = new IndexLeaf(_id, _next, _isUnique);
        newLeaf.lock();
        _space.create(newLeaf);
                    
        if (_next != SpaceInternal.NULL)
        {    
            IndexLeaf next = (IndexLeaf) _space.get(_next);
            next.lock();
            
            next._prev = newLeaf._id;
            
            next.setDirty();
            next.unlock();
        }
            
        _next = newLeaf._id;
        
        System.arraycopy(_keys, IndexRoot.N, newLeaf._keys, 0, IndexRoot.N);
        System.arraycopy(_links, IndexRoot.N, newLeaf._links, 0, IndexRoot.N);
        newLeaf._size = IndexRoot.N;
        _size = IndexRoot.N;
        
        trim();
        setDirty();
        
        newLeaf.unlock();
        unlock();
        
        return new SplitResult(newLeaf._id, getMaxKey());
    }
    
    private void trim()
    {
        for (int i = _size; i < IndexRoot.M; i++) {
            _keys[i] = null;
        }
        for (int i = _size; i < IndexRoot.M; i++) {
            _links[i] = SpaceInternal.NULL;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void concatinate(Node rightSibling, Comparable key) {
        lock();
        IndexLeaf right = (IndexLeaf) rightSibling;
        right.lock();
        
        System.arraycopy(right._keys, 0, _keys, _size, right._size);
        System.arraycopy(right._links, 0, _links, _size, right._size);
        
        _size += right._size;
        
        _next = right._next;
        if (_next != SpaceInternal.NULL) 
        {
            IndexLeaf next = (IndexLeaf) _space.get(_next);
            next.lock();
            
            next._prev = _id;
            
            next.setDirty();
            next.unlock();
        }
        
        setDirty();
        right.unlock();
        unlock();
                
        // right isn't set as modified, because it is removed (or considered as removed)
        // just after this method invocation.
    }

    @SuppressWarnings("rawtypes")
    @Override
    public TupleIterator search(Comparable searchKey) {
        return new TupleIterator(this, findIndex(searchKey));
    }

    @Override
    public TupleIterator right() {
        return new TupleIterator(this, _size - 1);
    }

    @Override
    public TupleIterator left() {
        return new TupleIterator(this, 0);
    }

    @Override
    public int size() {
        return _size;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparable<?> redistLR(Comparable parentKey, Node right) {
        //System.out.println("Leaf.redistLR");
        
        IndexLeaf rightLeaf = (IndexLeaf) right;
        int count = _size - (_size + rightLeaf._size) / 2;
        
        lock();
        rightLeaf.lock();
        
        // TODO check for count - 
        // if count == 1 - there might not be any reasons for redistribution at all.
        // Consider invoking distribution after checking neighbors
        
        // Shifts to right right node
        {
            System.arraycopy(rightLeaf._keys, 0, rightLeaf._keys, count, rightLeaf._size);
            System.arraycopy(rightLeaf._links, 0, rightLeaf._links, count, rightLeaf._size); 
        }
        
        // Copies to right node
        {
            System.arraycopy(_keys, _size - count, rightLeaf._keys, 0, count);
            System.arraycopy(_links, _size - count, rightLeaf._links, 0, count);
        }
        
        // Sizes
        rightLeaf._size += count;
        _size -= count;
        
        trim();
        rightLeaf.setDirty();
        setDirty();
        
        rightLeaf.unlock();
        unlock();
        
        return getMaxKey();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparable<?> redistRL(Comparable parentKey, Node left) {
        //System.out.println("Leaf.redistRL");
        
        IndexLeaf leftLeaf = (IndexLeaf) left;
        int count = _size - (_size + leftLeaf._size) / 2;
        
        lock();
        leftLeaf.lock();
        
        // Copies to left node
        {
            System.arraycopy(_keys, 0, leftLeaf._keys, leftLeaf._size, count);
            System.arraycopy(_links, 0, leftLeaf._links, leftLeaf._size, count);
        }
        
        // Shifts
        {
            System.arraycopy(_keys, count, _keys, 0, _size - count);
            System.arraycopy(_links, count, _links, 0, _size - count);
        }
        
        // Sizes
        leftLeaf._size += count;
        _size -= count;
        
        trim();
        setDirty();
        leftLeaf.setDirty();
        
        leftLeaf.unlock();
        unlock();
        
        return leftLeaf.getMaxKey();
    }
    
    public long[] getLinks(int index)
    {
        if (_isUnique)
        {
            return new long[]{_links[index]};
        }
        else
        {
            lock();
            IndexLinks links = (IndexLinks) _space.get(_links[index]);
            unlock();
            
            return links.getLinks();
        }
    }

    public IndexLeaf getNext()
    {
        IndexLeaf indexLeaf = null;
        
        if (_next != SpaceInternal.NULL) {
            lock();
            indexLeaf = (IndexLeaf) _space.get(_next);
            unlock();
        }
        
        return indexLeaf;
    }
    
    public IndexLeaf getPrev()
    {
        IndexLeaf indexLeaf = null;
        
        if (_prev != SpaceInternal.NULL) {
            lock();
            indexLeaf = (IndexLeaf) _space.get(_prev);
            unlock();
        }
        
        return indexLeaf;
    }
}
