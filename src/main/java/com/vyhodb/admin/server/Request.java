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

package com.vyhodb.admin.server;

import com.vyhodb.server.PrimitiveUtils;
import com.vyhodb.storage.pagefile.IOUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class Request {
    public static final int PREAMBULA = -19134761; 
    public static final int SIZE = 68;
    
    public static final int COMMAND_CLOSE = 1;
    public static final int COMMAND_READ_DATA = 2;
    public static final int COMMAND_SYNC_LOG = 3;
    public static final int COMMAND_CHECK_LOG = 4;
    public static final int COMMAND_READ_ONE_LOG_PAGE = 5;
    public static final int COMMAND_GET_LOG_INFO = 6;
    public static final int COMMAND_SHRINK = 7;
    public static final int COMMAND_CLEAR_SLAVE = 8;
    public static final int COMMAND_STORAGE_CLOSE = 9;
    public static final int COMMAND_PING = 10;
    
    public int command;
    public UUID masterLogId = PrimitiveUtils.UUID_ZERO;
    public long next;
    public UUID lastTrxId = PrimitiveUtils.UUID_ZERO;
    public int lastCrc;
    
    public long shrinkStart;
    public long storageCloseTimeout;
    
    private void read(ByteBuffer buffer) throws IOException
    {
        if (buffer.getInt() != PREAMBULA)
            throw new IOException("Wrong replication request preambula.");
        
        command = buffer.getInt();
        masterLogId = PrimitiveUtils.getUUID(buffer);
        next = buffer.getLong();
        lastTrxId = PrimitiveUtils.getUUID(buffer);
        lastCrc = buffer.getInt();
        shrinkStart = buffer.getLong();
        storageCloseTimeout = buffer.getLong();
    }
    
    private void write(ByteBuffer buffer)
    {
        buffer.putInt(PREAMBULA);
        
        buffer.putInt(command);
        PrimitiveUtils.putUUID(masterLogId, buffer);
        buffer.putLong(next);
        PrimitiveUtils.putUUID(lastTrxId, buffer);
        buffer.putInt(lastCrc);
        buffer.putLong(shrinkStart);
        buffer.putLong(storageCloseTimeout);
    }
    
    public static Request receive(SocketChannel sc, ByteBuffer buffer) throws IOException
    {
        buffer.clear();
        buffer.limit(SIZE);
        IOUtils.readNIO(sc, buffer);
        buffer.clear();
        
        Request request = new Request();
        request.read(buffer);
        return request;
    }
    
    public void send(SocketChannel sc, ByteBuffer buffer) throws IOException
    {
        buffer.clear();
        write(buffer);
        buffer.clear();
        
        buffer.limit(SIZE);
        IOUtils.writeNIO(sc, buffer);
    }

    public static Request newReadData()
    {
        Request request = new Request();
        request.command = COMMAND_READ_DATA;
        return request;
    }
    
    public static Request newReadOneLogPage(long logPageId)
    {
        Request request = new Request();
        request.command = COMMAND_READ_ONE_LOG_PAGE;
        request.next = logPageId;
        return request;
    }
    
    public static Request newSyncLog(long next)
    {
        Request request = new Request();
        request.command = COMMAND_SYNC_LOG;
        request.next = next;
        return request;
    }
    
    public static Request newCheckLog(UUID masterLogId, long next, UUID lastTrxId, int lastCrc)
    {
        Request request = new Request();
        request.command = COMMAND_CHECK_LOG;
        request.masterLogId = masterLogId;
        request.next = next;
        request.lastTrxId = lastTrxId;
        request.lastCrc = lastCrc;
        return request;
    }
    
    public static Request newClose()
    {
        Request request = new Request();
        request.command = COMMAND_CLOSE;
        return request;
    }
    
    public static Request newGetLogInfo()
    {
        Request request = new Request();
        request.command = COMMAND_GET_LOG_INFO;
        return request;
    }
    
    public static Request newShrink(long shringPageId)
    {
        Request request = new Request();
        request.command = COMMAND_SHRINK;
        request.shrinkStart = shringPageId;
        return request;
    }
    
    public static Request newPing()
    {
        Request request = new Request();
        request.command = COMMAND_PING;
        return request;
    }
    
    public static Request newClearSlave()
    {
        Request request = new Request();
        request.command = COMMAND_CLEAR_SLAVE;
        return request;
    }
    
    public static Request newStorageClose(long storageCloseTimeout)
    {
        Request request = new Request();
        request.command = COMMAND_STORAGE_CLOSE;
        request.storageCloseTimeout = storageCloseTimeout;
        return request;
    }
}
