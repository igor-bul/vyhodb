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

package com.vyhodb.storage.pagestorage;

import com.vyhodb.storage.pagefile.Source;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class SourceBackupData implements Source {

    private final PageStorage _pageStorage;
    private long _position;
    
    public SourceBackupData(PageStorage pageStorage)
    {
        this(pageStorage, 0);
    }
    
    public SourceBackupData(PageStorage pageStorage, long position)
    {
        _pageStorage = pageStorage;
        _position = position;
    }
    
    @Override
    public int read(ByteBuffer buffer, int offset, int count) throws IOException {
        for (int i = 0; i < count; i++) {
            _pageStorage.adminDataRead(_position ++, buffer, i + offset);
        }
        return count;
    }

    @Override
    public void close() throws IOException {
    }
}
