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

package com.vyhodb.storage.rm;

import com.vyhodb.server.TransactionRolledbackException;
import com.vyhodb.storage.space.Dictionary;
import com.vyhodb.storage.space.SpaceInternal;

import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class ExpandableReaderWriter implements SystemReader, SystemWriter {

    public static final int DEFAULT_CHAR_BUFFER_SIZE = 50;
    
    public static final byte VALUE_BOOLEAN_TRUE = 0;
    public static final byte VALUE_BOOLEAN_FALSE = 1;
    
    public static final byte CONST_CODE_STRING = 1;
    public static final byte CONST_CODE_CODE = 2;
    
    private final int _maxBufferSize;
    private final Dictionary _dictionary;
    private SpaceInternal _space;
    private char[] _charBuffer = new char[DEFAULT_CHAR_BUFFER_SIZE];
    private ByteBuffer _buffer;
        
    /**
     * 
     * @param bufferSize in bytes
     * @param maxBufferSize in bytes
     * @param dictionary 
     */
    public ExpandableReaderWriter(int bufferSize, int maxBufferSize, Dictionary dictionary)
    {
        _maxBufferSize = maxBufferSize;
        _buffer = ByteBuffer.allocate(bufferSize);
        _dictionary = dictionary;
    }
    
    /**
     * TODO refactor this hack
     */
    public void setSpace(SpaceInternal space) {
        _space = space;
    }
    
    void ensureCapacity(int minCapacity)
    {
        if (minCapacity < 0)
            _space.throwTRE("Record buffer overflow.");
       
        final int oldCapacity = _buffer.capacity();
        
        if (oldCapacity < minCapacity)
        {    
            if (minCapacity > _maxBufferSize)
                _space.throwTRE("Max record buffer size has has reached. Max record buffer size:" + _maxBufferSize);
            
            int newCapacity = minCapacity * 2;
            if (newCapacity < 0)
                _space.throwTRE("Record buffer overflow.");
                        
            if (newCapacity > _maxBufferSize)
            {
                newCapacity = _maxBufferSize;
            }
            
            ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
            
            _buffer.flip();
            newBuffer.put(_buffer);
            _buffer = newBuffer;
        }
    }
    
    private void require(int size) 
    {
        ensureCapacity(_buffer.position() + size);
    }
    
    void clear()
    {
        _buffer.clear();
    }
    
    int position()
    {
        return _buffer.position();
    }
    
    void limit(int limit) {
        _buffer.limit(limit);
    }
    
    @Override
    public final void putDouble(double value) {
        require(8);
        _buffer.putDouble(value);
    }

    @Override
    public final double getDouble() {
        return _buffer.getDouble();
    }

    @Override
    public final void putFloat(float value) {
        require(4);
        _buffer.putFloat(value);
    }
    
    @Override
    public final float getFloat() {
        return _buffer.getFloat();
    }

    @Override
    public final void putShort(short value) {
        require(2);
        _buffer.putShort(value);
    }
    
    @Override
    public final short getShort() {
        return _buffer.getShort();
    }

    @Override
    public void putChar(char value) {
        require(2);
        _buffer.putChar(value);
    }
    
    @Override
    public final char getChar() {
        return _buffer.getChar();
    }

    @Override
    public void putLong(long value) {
        require(8);
        _buffer.putLong(value);
    }
    
    @Override
    public final long getLong() {
        return _buffer.getLong();
    }

    @Override
    public void putInt(int value) {
        require(4);
        _buffer.putInt(value);
    }
    
    @Override
    public final int getInt() {
        return _buffer.getInt();
    }

    @Override
    public void putByteArray(byte[] array) {
        require(4 + array.length);
        _buffer.putInt(array.length);
        _buffer.put(array);
    }
    
    @Override
    public final byte[] getByteArray() {
        final byte[] result = new byte[_buffer.getInt()];
        _buffer.get(result);
        return result;
    }

    @Override
    public final void putByte(byte value) {
        require(1);
        _buffer.put(value);
    }
    
    @Override
    public final byte getByte() {
        return _buffer.get();
    }

    @Override
    public final void putString(String value) {
        require(4 + (value.length() << 1));
        
        final int length = value.length();
        if (length > _charBuffer.length)
        {
            _charBuffer = new char[length];
        }
        
        final char[] cb = _charBuffer;
        value.getChars(0, length, cb, 0);
        
        _buffer.putInt(length);
               
        for (int i = 0; i < length; i++) {
            _buffer.putChar(cb[i]);
        }
    }
    
    @Override
    public final String getString() {
        final int length = _buffer.getInt();
        if (length > _charBuffer.length)
        {
            _charBuffer = new char[length];
        }
        final char[] cb = _charBuffer;
        
        for (int i = 0; i < length; i++) {
            cb[i] = _buffer.getChar();
        }
        
        return new String(cb, 0, length);
    }
    
    @Override
    public void putStringConst(String value) {
        final int id = _dictionary.resolve(value);
        
        if (id == Dictionary.CODE_NOT_EXIST)
        {
            _buffer.put(CONST_CODE_STRING);
            putString(value);
        }
        else
        {
            _buffer.put(CONST_CODE_CODE);
            _buffer.putInt(id);
        }
    }
    
    @Override
    public final String getStringConst() {
        final byte code = _buffer.get();
       
        switch (code)
        {
            case CONST_CODE_CODE:
                final int key = _buffer.getInt();
                final String result = _dictionary.resolve(key);
                if (result == null) throw new TransactionRolledbackException("Unknown string const id:" + key +". Read can't be continued.");
                return result;
                
            case CONST_CODE_STRING:
                return getString();
        }
        
        throw new TransactionRolledbackException("Wrong string const code:" + code);
    }

    @Override
    public boolean getBoolean() {
        return getByte() == VALUE_BOOLEAN_TRUE;
    }

    @Override
    public void putBoolean(boolean value) {
        putByte( value ? VALUE_BOOLEAN_TRUE : VALUE_BOOLEAN_FALSE);
    }

    @Override
    public void putValue(Object value) {
        ValueSerializer.putValue(_space, this, value);
    }

    @Override
    public Object getValue() {
        return ValueSerializer.getValue(_space, this);
    }
    
    public ByteBuffer getBuffer() {
        return _buffer;
    }
}
