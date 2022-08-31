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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * TODO
 * 
 * 2. Check count of read/written bytes as a result of _fc.read(), _fc.write()
 * operations.
 * 
 * @author User
 */
public class PageFile implements File {

    private static final String READ_ONLY_MODE = "File is in read-only mode. File name:";
    
    private final FileChannel _fc;
    protected final Crc crc;
    private final String _fileName;
    private long _position;
    private final boolean _readOnly;
   

    /**
     * See 
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6210572
     * http://www.evanjones.ca/software/java-bytebuffers.html
     */
    protected final ByteBuffer directBuffer = ByteBuffer.allocateDirect(PageHeader.PAGE_SIZE);
     
    public PageFile(String fileName, boolean exists) throws IOException {
        this(fileName, exists, false);
    }
    
    public PageFile(String fileName, boolean exists, boolean readOnly) throws IOException
    {
        _fileName = fileName;
        _readOnly = readOnly;
        crc = new Crc();
        _fc = FileChannel.open(Paths.get(fileName), getOpenOptions(exists, _readOnly));
        
        if (!_readOnly) {
            lock();
        }
    }
    
    @Override
    public void close() throws IOException {
        _fc.close();
    }

    @Override
    public void fsync() throws IOException {
        _fc.force(true);
    }
    
    private static OpenOption[] getOpenOptions(boolean exists, boolean readOnly)
    {
        if (readOnly) {
            return  new OpenOption[]{StandardOpenOption.READ};
        }
                
        if (exists)
        {
            return new OpenOption[]{StandardOpenOption.READ, StandardOpenOption.WRITE};
        }
        else
        {
            return new OpenOption[]{StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW};
        }
    }

    @Override
    public void delete() throws IOException {
        if (_readOnly) throw new IOException(READ_ONLY_MODE + _fileName);
        
        close();
        Files.delete(Paths.get(_fileName));
    }

    @Override
    public long size() throws IOException {
        return (_fc.size() >> PageHeader.PAGE_SIZE_MULTIPLICATOR);
    }

    public void lock() throws IOException{
        if (_fc.tryLock(0L, PageHeader.PAGE_SIZE, false) == null)
            throw new IOException("Can't acquire lock on file:" + _fileName);
    }
    
    @Override
    public void truncate(long pageCount) throws IOException {
        if (_readOnly) throw new IOException(READ_ONLY_MODE + _fileName);
        
        _fc.truncate(pageCount << PageHeader.PAGE_SIZE_MULTIPLICATOR);
    }

    @Override
    public int write(ByteBuffer buffer, int offset, int count) throws IOException {
        if (_readOnly) throw new IOException(READ_ONLY_MODE + _fileName);
        
        crc.updateCrc(buffer, offset, count);
                
        _fc.position(_position << PageHeader.PAGE_SIZE_MULTIPLICATOR);
        
        if (buffer.isDirect())
        {
            IOUtils.writeNIO(_fc, buffer, offset, count);
        }
        else
        {
            for (int i = 0; i < count; i++) {
                PageHeader.copyPages(buffer, offset + i, directBuffer, 0, 1);
                IOUtils.writeNIO(_fc, directBuffer, 0, 1);
            }
        }
        
        _position += count;
        return count;
    }
    
    @Override
    public int read(ByteBuffer buffer, int offset, int count) throws IOException {
        _fc.position(_position << PageHeader.PAGE_SIZE_MULTIPLICATOR);
        
        if (buffer.isDirect())
        {
            IOUtils.readNIO(_fc, buffer, offset, count);
        }
        else
        {
            for (int i = 0; i < count; i++) {
                IOUtils.readNIO(_fc, directBuffer, 0, 1);
                PageHeader.copyPages(directBuffer, 0, buffer, offset + i, 1);
            }
         }
        
        // Validates crc
        for (int i = 0; i < count; i++) {
            if ( ! crc.validateCrc(buffer, i + offset))
                throw new IOException(" Wrong page crc . File:" + _fileName + ", pageId:" + (i + _position));
        }
        
        _position += count;
        return count;
    }

    @Override
    public String filename() {
        return _fileName;
    }

    @Override
    public Destination getApplier() {
        return new Applier();
    }

    @Override
    public long position() {
        return _position;
    }

    @Override
    public void position(long newPosition) {
        _position = newPosition;
    }

    @Override
    public void transferFrom(Source source, ByteBuffer buffer, long count) throws IOException {
        IOUtils.copy(source, this, buffer, count);
    }
    
    private class Applier implements Destination
    {

        @Override
        public int write(ByteBuffer buffer, int offset, int count) throws IOException {
            long pageId;
            
            for (int i = 0; i < count; i++) {
                pageId = PageHeader.getPageId(buffer, offset + i);
                position(pageId);
                PageFile.this.write(buffer, offset + i, 1);
            }
            
            return count;
        }

        @Override
        public void transferFrom(Source source, ByteBuffer buffer, long count) throws IOException {
            IOUtils.copy(source, this, buffer, count);
        }

        @Override
        public void close() throws IOException {
            PageFile.this.close();
        }
    }
}
