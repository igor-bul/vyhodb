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

package com.vyhodb.storage.log;

import com.vyhodb.admin.LogInfo;
import com.vyhodb.server.PrimitiveUtils;
import com.vyhodb.storage.data.Data;
import com.vyhodb.storage.pagefile.BackupHeader;
import com.vyhodb.storage.pagefile.Header;
import com.vyhodb.storage.pagefile.HeaderFile;
import com.vyhodb.storage.pagefile.PageHeader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
public class LogFile extends HeaderFile {

    private LogHeader _header;
    
    /**
     * Used in storage openening.
     * 
     * @param fileName
     * @param exists
     * @throws IOException
     */
    public LogFile(String fileName, boolean exists) throws IOException {
        super(fileName, exists);
        
        _header = new LogHeader();
        if (exists) {
            readHeader(_header);
            
            if (_header.version > LogHeader.MAX_VERSION)
                throw new IOException("Unsupported storage version: " + _header.version);
            
            if (_header.checkpoint > size()) 
                throw new IOException("Log file corrupted. It's logical size is less then [checkpoint] pointer.");
        }
    }
    
    /**
     * New log in shrink
     * 
     * @param fileName
     * @param otherLogFile
     * @throws IOException
     */
    public LogFile(String fileName, LogFile otherLogFile) throws IOException {
        super(fileName, false);
        _header = otherLogFile._header.clone();
    }
    
    /**
     * New storage creation
     * 
     * @param fileName
     * @throws IOException
     */
    public LogFile(String fileName) throws IOException {
        super(fileName, false);
        
        _header = new LogHeader();
        _header.logId = UUID.randomUUID();
        _header.slave = false;
        _header.masterLogId = PrimitiveUtils.UUID_ZERO;
        _header.start = _header.checkpoint = _header.next = PageHeader.MIN_LOG_PAGE_ID;
        _header.dataLength = 1;
        _header.successfulStop = true;
        _header.version = 0;
    }
    
    /**
     * Restore from backup
     * 
     * @param fileName
     * @param backupHeader
     * @param isSlave
     * @throws IOException
     */
    public LogFile(String fileName, BackupHeader backupHeader, boolean isSlave) throws IOException {
        super(fileName, false);
        
        _header = new LogHeader();
        _header.logId = UUID.randomUUID();
        _header.successfulStop = true;
        _header.dataLength = backupHeader.backupLength - 1;

        if (isSlave)
        {
            _header.slave = true;
            _header.masterLogId = backupHeader.masterLogId;
            _header.start = backupHeader.start;
        }
        else
        {
            _header.slave = false;
            _header.masterLogId = PrimitiveUtils.UUID_ZERO;
            _header.start = PageHeader.MIN_LOG_PAGE_ID;
        }
        
        _header.checkpoint = _header.next = (_header.start + 1);
    }
    
    public long getStart() {
        return _header.start;
    }
    
    public void setStart(long start) {
        _header.start = start;
    }
    
    public long getNext() {
        return _header.next;
    }
    
    public void incNext(long size) {
        _header.next += size;
    }
    
    public long getCheckpoint() {
        return _header.checkpoint;
    }
    
    public boolean isSuccessfulStop() {
        return _header.successfulStop;
    }
    
    public void setSuccessfulStop(boolean successfulStop) {
        _header.successfulStop = successfulStop;
    }
    
    public UUID getLogId() {
        return _header.logId;
    }
    
    public LogInfo getLogInfo() {
        return _header.getLogInfo();
    }
    
    public long getDataLength() {
        return _header.dataLength;
    }
    
    public void setDataLength(long dataLength) {
        _header.dataLength = dataLength;
    }
    
    public void checkpoint(Data data, boolean succesfulStop) throws IOException {
        fsync();
        data.checkpoint();
        
        _header.checkpoint = _header.next;
        _header.successfulStop = succesfulStop;
        syncHeader();
    }
    
    public void clearSlave() throws IOException {
        _header.slave = false;
        _header.masterLogId = PrimitiveUtils.UUID_ZERO;
        syncHeader();
    }
    
    public void syncHeader() throws IOException {
        super.writeHeader(_header);
        super.fsync();
    }
    
    @Override
    public void truncate(long logPageId) throws IOException {
        super.truncate(logPageId - _header.start);
    }
    
    @Override
    public long size() throws IOException {
        return (super.size() + _header.start);
    }

    @Override
    public long position() {
        return (super.position() + _header.start);
    }

    @Override
    public void position(long newPosition) {
        super.position(newPosition - _header.start);
    }
    
    public void checkDataHeader(UUID dataLogId) throws IOException
    {
        if (! _header.logId.equals(dataLogId))
            throw new IOException("Data file doesn't correspond to log file. Wrong logId. Expected:" + _header.logId + ", Actual:" + dataLogId);
    }
    
    public void checkDataSize(Data data) throws IOException
    {
        if (_header.dataLength > data.size())
            throw new IOException("Data file corrupted. Data file size is more than log's [dataLength] value.");
    }
    
    public void checkRecovery() throws IOException
    {
        if (!_header.successfulStop)
            throw new IOException("Server was not shutdown clearly. Please start vyhodb server to recover log.");
    }
    
    private static final class LogHeader implements Header, Cloneable {
        
        public static final short MAX_VERSION = 0;
        private static final int HEADER_LOG_PREAMBULA = -858993460;
        
        UUID logId;
        
        long start;
        long checkpoint;
        long next;
        long dataLength;
        
        boolean slave;
        UUID masterLogId;
        
        
        boolean successfulStop;
        short version;
        
        LogInfo getLogInfo() {
            return new LogInfo(logId, start, checkpoint, next, dataLength, slave, masterLogId, successfulStop, version);
        }

        @Override
        public void read(ByteBuffer buffer) throws IOException {
            if (HEADER_LOG_PREAMBULA != buffer.getInt())
                throw new IOException("Wrong log file preambula. Log file is corrupted or isn't a log file.");
                
            version = buffer.getShort();
            logId = PrimitiveUtils.getUUID(buffer);
            start = buffer.getLong();
            checkpoint = buffer.getLong();
            next = buffer.getLong();
            dataLength = buffer.getLong();
            slave = PrimitiveUtils.getBoolean(buffer);
            masterLogId = PrimitiveUtils.getUUID(buffer);
            successfulStop = PrimitiveUtils.getBoolean(buffer);
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(HEADER_LOG_PREAMBULA);
            buffer.putShort(version);
            PrimitiveUtils.putUUID(logId, buffer);
            buffer.putLong(start);
            buffer.putLong(checkpoint);
            buffer.putLong(next);
            buffer.putLong(dataLength);
            PrimitiveUtils.putBoolean(slave, buffer);
            PrimitiveUtils.putUUID(masterLogId, buffer);
            PrimitiveUtils.putBoolean(successfulStop, buffer);
        }

        @Override
        public LogHeader clone() {
            try { 
                return (LogHeader) super.clone();
            } catch (CloneNotSupportedException ex) {
            }
            return null;
        }
    }
}
