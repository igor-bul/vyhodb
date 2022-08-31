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

import com.vyhodb.storage.rm.SystemReader;
import com.vyhodb.storage.rm.SystemSerializable;
import com.vyhodb.storage.rm.SystemWriter;
import com.vyhodb.storage.space.index.IndexLeaf;
import com.vyhodb.storage.space.index.IndexLinks;
import com.vyhodb.storage.space.index.IndexNode;

public abstract class Container implements SystemSerializable {

    // If we need new Container version, for instance for new record serialization mechanism or
    // new index version - just add new const and serialization/deserialization logic.
    
    public static final short CONTAINER_TYPE_RECORD = 1;
    public static final short CONTAINER_TYPE_INDEX_NODE = 2;
    public static final short CONTAINER_TYPE_INDEX_LEAF = 3;
    public static final short CONTAINER_TYPE_INDEX_LINKS = 4;
    
    protected long _id = SpaceInternal.NULL;
    protected SpaceInternal _space;
    private boolean _isDirty = false;
    private boolean _freed = false;
    private int _lock = Integer.MIN_VALUE;
    
    public abstract short getType();
    
    public long getId() {
        return _id;
    }
    
    public void setId(long id) {
        _id = id;
    }
    
    public void setSpace(SpaceInternal space) {
        _space = space;
    }
    
    public SpaceInternal getSpace() {
        return _space;
    }
    
    public void setDirty() {
        if (_freed) {
            _space.throwTRE("Freed");
        }
        
        _isDirty = true;
    }
    
    public boolean isDirty() {
        return _isDirty;
    }
    
    public boolean isLocked() {
        return _lock != Integer.MIN_VALUE;
    }
    
    public void setFreed() {
        _freed = true;
    }
    
    public void lock() {
        if (_lock == Integer.MAX_VALUE) {
            _space.throwTRE("Cache lock overflow");
        }
        
        _lock++;
    }
    
    public void unlock() {
        if (_lock == Integer.MIN_VALUE) {
            _space.throwTRE("Cache lock underflow");
        }
        
        _lock--;
    }
    
    public final static void writeContainer(SystemWriter writer, Container container)
    {
        writer.putShort(container.getType());
        container.write(writer);
    }
    
    public final static Container readContainer(SpaceInternal space, SystemReader reader, long id)
    {
        Container container;
        short type = reader.getShort();
        
        switch (type) {
            case CONTAINER_TYPE_RECORD:
                container = new RecordContainer(); 
                break;
                
            case CONTAINER_TYPE_INDEX_NODE:
                container = new IndexNode();
                break;

            case CONTAINER_TYPE_INDEX_LEAF:
                container = new IndexLeaf();
                break;
                
            case CONTAINER_TYPE_INDEX_LINKS:
                container = new IndexLinks();
                break;
                
            default:
                return null;
        }
        
        container.setSpace(space);
        container.setId(id);
        container.read(reader);
        
        return container;
    }
}
