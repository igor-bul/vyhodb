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

import java.util.ArrayList;

public final class IndexLinks extends Container {

    private static final int MAX_LINKS_COUNT = Integer.MAX_VALUE - 5;
    private ArrayList<Long> _links = new ArrayList<>();
    
    @Override
    public void read(SystemReader reader) {
        int size = reader.getInt();
        _links = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            _links.add(reader.getLong());
        }
    }

    @Override
    public void write(SystemWriter writer) {
        int size = _links.size();
        writer.putInt(size);
        for (int i = 0; i < size; i++) {
            writer.putLong(_links.get(i));
        }
    }

    @Override
    public short getType() {
        return CONTAINER_TYPE_INDEX_LINKS;
    }
    
    public void addLink(SpaceInternal space, long link)
    {
        if (_links.size() == MAX_LINKS_COUNT) {
            space.throwTRE("Max count of links in IndexLinks has reached!");
        }
                
        _links.add(link);
        setDirty();
    }
    
    public void removeLink(long link)
    {
        _links.remove(link);
        setDirty();
    }
    
    public long[] getLinks()
    {
        long[] links = new long[_links.size()];
        
        for (int i = 0; i < links.length; i++) {
            links[i] = _links.get(i);
        }
        
        return links;
    }
    
    public int size()
    {
        return _links.size();
    }
}
