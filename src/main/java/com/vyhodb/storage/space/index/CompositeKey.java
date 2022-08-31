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
import com.vyhodb.utils.Utils;

public class CompositeKey implements Comparable<CompositeKey>, SystemSerializable {

    @SuppressWarnings("rawtypes")
    public Comparable[] keys;

    /**
     * Read
     */
    public CompositeKey() {
    }
    
    public CompositeKey(Comparable<?>[] keys) {
        this.keys = keys;
    }

    @Override
    public void read(SystemReader reader) {
        int size = reader.getShort();
        keys = new Comparable[size];
        
        for (int i = 0; i < size; i++) {
            keys[i] = (Comparable<?>) reader.getValue();
        }
    }

    @Override
    public void write(SystemWriter writer) {
        writer.putShort((short)keys.length);
        for (int i = 0; i < keys.length; i++) {
            writer.putValue(keys[i]);
        }
    }

    @Override
    public int compareTo(CompositeKey o) {
        int result = 0;
        
        for (int i = 0; i < keys.length; i++) {
            result = Utils.compare(keys[i], o.keys[i]);
            if (result != 0) {
                return result;
            }
        }
        
        return 0;
    }
}
