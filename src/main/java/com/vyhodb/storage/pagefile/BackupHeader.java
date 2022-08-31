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

import com.vyhodb.server.PrimitiveUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
public class BackupHeader implements Header {

    public static final short MAX_VERSION = 0;
    private static final int HEADER_BACKUP_PREAMBULA = 1978170589;
    
    public long start;
    public long backupLength;
    public UUID masterLogId;
    public short version;
    
    @Override
    public void read(ByteBuffer buffer) throws IOException {
        if (HEADER_BACKUP_PREAMBULA != buffer.getInt())
            throw new IOException("Wrong backup file preambula. Backup file is corrupted or isn't a backup file.");
        
        version = buffer.getShort();
        start = buffer.getLong();
        backupLength = buffer.getLong();
        masterLogId = PrimitiveUtils.getUUID(buffer);
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(HEADER_BACKUP_PREAMBULA);
        
        buffer.putShort(version);
        buffer.putLong(start);
        buffer.putLong(backupLength);
        PrimitiveUtils.putUUID(masterLogId, buffer);
    }
    
    public void checkVersion() throws IOException
    {
        if (version > MAX_VERSION)
            throw new IOException("Unsupported backup file version. Max supported version: " + MAX_VERSION + ", current backup version:" + version);
    }
}
