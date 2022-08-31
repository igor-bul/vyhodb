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
import com.vyhodb.storage.StorageConfig;
import com.vyhodb.storage.pagefile.PageHeader;
import com.vyhodb.storage.space.Dictionary;

/**
 *
 * @author User
 */
public final class RecordManager {

    public static final int[] SLOT_SIZES = {
        128, 256, 512, 1024, 2048, 4096, 8192, 
        16384, 32768, 65536, 131072, 262144, 524288, 1048576,
        2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 
        134217728, 268435456, 536870912, 1073741824};
    
    public static final int MAX_RECORD_SIZE = SLOT_SIZES[SLOT_SIZES.length - 1];
    public static final int MIN_RECORD_SIZE = SLOT_SIZES[0];
    
    private final ExpandableReaderWriter _rw;
    private final BlockManager _block;
    private final Descriptor _descriptor;
    
    public RecordManager(BlockManager blockManager, Descriptor descriptorManager, Dictionary dictionary, StorageConfig config) {
        int initRecordSize = config.getInitRecordBufferSize() << PageHeader.PAGE_SIZE_MULTIPLICATOR;
        int maxRecordSize = config.getMaxRecordSize() << PageHeader.PAGE_SIZE_MULTIPLICATOR;
        
        if (initRecordSize < MIN_RECORD_SIZE || initRecordSize > MAX_RECORD_SIZE)
            throw new IllegalArgumentException("Illegal record buffer initial size");
        
        if (maxRecordSize < MIN_RECORD_SIZE || maxRecordSize > MAX_RECORD_SIZE)
            throw new IllegalArgumentException("Illegal max record size");
        
        _block = blockManager;
        _descriptor = descriptorManager;
        _rw = new ExpandableReaderWriter(initRecordSize, maxRecordSize, dictionary);
    }
       
    public SystemReader readRecord(long recordId) {
        if (recordId >= _block.getAllocator()) 
            return null; // Specified recordId is larger then current space size
        
        // Reads descriptor
        if (_descriptor.read(recordId)) 
        {
            // Prepares record buffer
            final int recordSize = SLOT_SIZES[_descriptor.slotType];
            _rw.clear();
            _rw.ensureCapacity(recordSize);
            _rw.limit(recordSize);
            
            // Reads record
            _block.read(_descriptor.start, _rw.getBuffer());
            _rw.clear();
            _rw.limit(recordSize);
            
            return _rw;
        }
        else
        {
            // preambula is wrong (specified recordId doesn't point to descriptor)
            // or record had been deleted
            return null;  
        }
    }
    
    public long newRecord() {
        return _descriptor.writeNew();
    }
    
    public void remove(long recordId)
    {
        if (_descriptor.read(recordId))
        {
            _descriptor.remove(recordId);
        }
        else
        {
            throw new TransactionRolledbackException("Critical logic error. Removing not existed record" + recordId);
        }
    }
    
    public SystemWriter startUpdate() {
        _rw.clear();
        return _rw;
    }
    
    public void endUpdate(long recordId) {
        // The line below can't take place, because record must be read (or created) before any update
        if (!_descriptor.read(recordId)) throw new TransactionRolledbackException("Record with specified id does not exist. Id:" + recordId);
        
        int newSlotType = getSlotType(_rw.position());
        int size = SLOT_SIZES[newSlotType]; 
        _rw.ensureCapacity(size);             
        _rw.clear();
        _rw.limit(size);
        
        if (_descriptor.slotType < newSlotType)
        {
            // appends new block
            long start = _block.append(_rw.getBuffer());

            // updates descriptor
            _descriptor.write(recordId, start, newSlotType);
        }
        else
        {
            _block.write(_descriptor.start, _rw.getBuffer());
        }
    }

    private static int getSlotType(int size)
    {
        final int length = SLOT_SIZES.length;
        for (int i = 0; i < length; i++) {
            if (size <= SLOT_SIZES[i]) return i;
        }
        throw new IllegalArgumentException("record size exceeds max record size!");
    }
    
    /**
     * Commits transaction
     * 
     */
    public void commit() 
    {
        _block.commit();
    }
    
    /**
     * Rollsback transaction
     */
    public void rollback()
    {
        _block.rollback();
    }
    
}
