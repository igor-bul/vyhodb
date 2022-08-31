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

package com.vyhodb.storage.pagefile;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 * @author Igor Vykhodtcev
 */
public class IOUtils {

    public static final void copy(Source source, Destination dest, ByteBuffer buffer, long count) throws IOException {
        final long bufferSize = PageHeader.getBufferSize(buffer);
        long copied = 0;
        long n;
        int read;
        int write;
        while (copied < count) {
            read = 0;
            write = 0;
            n = Math.min(bufferSize, count - copied);
            while (read < n) {
                read += source.read(buffer, 0, (int) (n - read));
            }
            while (write < n) {
                write += dest.write(buffer, 0, (int) (n - write));
            }
            copied += n;
        }
    }
    
    public static final int readNIO(ReadableByteChannel channel, ByteBuffer buffer, int offset, int count) throws IOException
    {
        final int off = offset << PageHeader.PAGE_SIZE_MULTIPLICATOR;
        final int size = count << PageHeader.PAGE_SIZE_MULTIPLICATOR;
        
        buffer.clear();
        buffer.limit(off + size);
        buffer.position(off);
        
        int read = 0;
        int n = 0;
        while (read < size)
        {
            n = channel.read(buffer);
            
            if (n < 0)
                throw new EOFException("Channel is closed");
            
            read += n;
        }
        
        return read >> PageHeader.PAGE_SIZE_MULTIPLICATOR;
    }
    
    public static final int readNIO(ReadableByteChannel channel, ByteBuffer buffer) throws IOException
    {
        int read = 0;
        int n = 0;
        while (buffer.hasRemaining())
        {
            n = channel.read(buffer);
            
            if (n < 0)
                throw new EOFException("Channel is closed");
            
            read += n;
        }
        return read;
    }
    
    public static final int writeNIO(WritableByteChannel channel, ByteBuffer buffer) throws IOException
    {
        int written = 0;
        while (buffer.hasRemaining())
        {
            written += channel.write(buffer);
        }
        return written;
    }
    
    public static final int writeNIO(WritableByteChannel channel, ByteBuffer buffer, int offset, int count) throws IOException
    {
        final int off = offset << PageHeader.PAGE_SIZE_MULTIPLICATOR;
        final int size = count << PageHeader.PAGE_SIZE_MULTIPLICATOR;
        
        buffer.clear();
        buffer.limit(off + size);
        buffer.position(off);
        
        int read = 0;
        while (read < size)
        {
            read += channel.write(buffer);
        }
        
        return read >> PageHeader.PAGE_SIZE_MULTIPLICATOR;
    }
    
}
