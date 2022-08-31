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

import java.nio.ByteBuffer;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class Descriptor {
    
    static final short DESCRIPTOR_PREAMBULA = -21846;
    static final long POINTER_NEW_RECORD = -1L;
    static final byte NEW_SLOT_TYPE = -1;
    static final long POINTER_REMOVED = Long.MIN_VALUE;
    static final int DESCRIPTOR_SIZE = 11;
    
    private final ByteBuffer _descriptorBuffer = ByteBuffer.allocate(DESCRIPTOR_SIZE);
    private final BlockManager _blockManager;
    
    long start;
    byte slotType;
    
    public Descriptor(BlockManager blockManager)
    {
        _blockManager = blockManager;
    }
    
    public boolean read(long recordId)
    {
        // Reads descriptor
        _descriptorBuffer.clear();
        _blockManager.read(recordId, _descriptorBuffer);
        _descriptorBuffer.clear();
        
        // Checks preambula
        short preambula = _descriptorBuffer.getShort();
        if (preambula != DESCRIPTOR_PREAMBULA) 
            return false;   // Wrong preambula
        
        // Check removed
        start = _descriptorBuffer.getLong();
        if (start == POINTER_REMOVED)
            return false;   // Record removed
        
        slotType = _descriptorBuffer.get();
                       
        return true;
    }
    
    public void write(long recordId, long start, int slotType) 
    {
        prepareBufferWrite(start, slotType);
        _blockManager.write(recordId, _descriptorBuffer);
    }
    
    public void remove(long recordId)
    {
        prepareBufferWrite(POINTER_REMOVED, 0);
        _blockManager.write(recordId, _descriptorBuffer);
    }
    
    public long writeNew()
    {
        prepareBufferWrite(POINTER_NEW_RECORD, NEW_SLOT_TYPE);
        return _blockManager.append(_descriptorBuffer);
    }
    
    private void prepareBufferWrite(long start, int slot)
    {
        _descriptorBuffer.clear();
        _descriptorBuffer.putShort(DESCRIPTOR_PREAMBULA);
        _descriptorBuffer.putLong(start);
        _descriptorBuffer.put((byte)slot);
        _descriptorBuffer.clear();
    }
}
