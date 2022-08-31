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

package com.vyhodb.server.admin;

import com.vyhodb.admin.ProgressListener;
import com.vyhodb.storage.pagefile.Source;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public class SourceProgress implements Source {
    
    private final ProgressListener _listener;
    private final Source _source;
    
    private final double _step;
    private long _counter;
    private double _accumulator;
    private final long _count;

    public SourceProgress(Source source, ProgressListener listener, long count)
    {
        _source = source;
        _listener = listener;
                
        _count = count;
        _step = count / 100 * listener.getStep();
        
        _counter = 0;
        _accumulator = 0;
    }
    
    @Override
    public int read(ByteBuffer buffer, int offset, int count) throws IOException {
        int read = _source.read(buffer, offset, count);
        
        _accumulator += read;
        _counter += read;
        
        if (_counter >= _step)
        {
            _counter = 0;
            _listener.progress((int)(_accumulator / _count * 100));
        }
        
        return read;
    }

    @Override
    public void close() throws IOException {
        _source.close();
    }
}
