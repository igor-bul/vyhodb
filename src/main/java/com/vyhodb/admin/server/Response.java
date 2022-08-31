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

import com.vyhodb.admin.LogInfo;
import com.vyhodb.admin.WrongPageIdException;
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
public class Response {
    public static final int PREAMBULA = 891568201;
    public static final int SIZE = 76;
    
    public static final int RESULT_OK = 1;
    public static final int RESULT_EXCEPTION = 2;
    public static final int RESULT_WRONG_LOG_PAGE_ID = 3;
    public static final int RESULT_WRONG_MASTER_LOG_ID = 4;
    public static final int RESULT_WRONG_LAST_PAGE = 5;
    
    public int result;
    
    public UUID logId = PrimitiveUtils.UUID_ZERO;
    public long start;
    public long checkpoint;
    public long next;
    public long dataLength;
    public boolean slave;
    public UUID masterLogId = PrimitiveUtils.UUID_ZERO;
    public boolean successfulStop;
    public short version;
        
    private void read(ByteBuffer buffer) throws IOException, WrongPageIdException
    {
        if (buffer.getInt() != PREAMBULA)
            throw new IOException("Wrong replication response preambula.");
        
        result = buffer.getInt();

        logId = PrimitiveUtils.getUUID(buffer);
        start = buffer.getLong();
        checkpoint = buffer.getLong();
        next = buffer.getLong();
        dataLength = buffer.getLong();
        slave = PrimitiveUtils.getBoolean(buffer);
        masterLogId = PrimitiveUtils.getUUID(buffer);
        successfulStop = PrimitiveUtils.getBoolean(buffer);
        version = buffer.getShort();
        
        checkException();
    }
    
    private void write(ByteBuffer buffer)
    {
        buffer.putInt(PREAMBULA);
        buffer.putInt(result);
        
        PrimitiveUtils.putUUID(logId, buffer);
        buffer.putLong(start);
        buffer.putLong(checkpoint);
        buffer.putLong(next);
        buffer.putLong(dataLength);
        PrimitiveUtils.putBoolean(slave, buffer);
        PrimitiveUtils.putUUID(masterLogId, buffer);
        PrimitiveUtils.putBoolean(successfulStop, buffer);
        buffer.putShort(version);
    }
    
    public void send(SocketChannel sc, ByteBuffer buffer) throws IOException
    {
        buffer.clear();
        write(buffer);
        buffer.clear();
        
        buffer.limit(SIZE);
        IOUtils.writeNIO(sc, buffer);
    }
    
    public static Response receive(SocketChannel sc, ByteBuffer buffer) throws IOException, WrongPageIdException
    {
        buffer.clear();
        buffer.limit(SIZE);
        IOUtils.readNIO(sc, buffer);
        buffer.clear();
        
        Response response = new Response();
        response.read(buffer);
        return response;
    }
    
    
    private void checkException() throws WrongPageIdException, IOException
    {
        if (result == RESULT_WRONG_LOG_PAGE_ID)
            throw new WrongPageIdException("Out of synchronization. Wrong log page id.");
        
        if (result == RESULT_WRONG_LAST_PAGE)
            throw new IOException("Out of synchronization. Wrong last log page.");
        
        if (result == RESULT_WRONG_MASTER_LOG_ID)
            throw new IOException("Wrong master log id. Configure another vyhodb server as master.");
        
        if (result == RESULT_EXCEPTION)
            throw new IOException("Some exception occurred on server side.");
    }
    
    public static Response newReadData(LogInfo logInfo)
    {
        Response response = new Response();
        
        response.result = RESULT_OK;
        response.dataLength = logInfo.getDataLength();
        response.next = logInfo.getNext();
        response.masterLogId = logInfo.getLogId();
        
        return response;
    }
    
    public static Response newSyncLog(LogInfo logInfo)
    {
        Response response = new Response();
        
        response.result = RESULT_OK;
        response.dataLength = logInfo.getDataLength();
        response.next = logInfo.getNext();
        response.masterLogId = logInfo.getLogId();
        
        return response;
    }
    
    public static Response newWrongLogPage()
    {
        Response response = new Response();
        response.result = RESULT_WRONG_LOG_PAGE_ID;
        return response;
    }
    
    public static Response newWrongMaster()
    {
        Response response = new Response();
        response.result = RESULT_WRONG_MASTER_LOG_ID;
        return response;
    }
    
    public static Response newWrongLastPage()
    {
        Response response = new Response();
        response.result = RESULT_WRONG_LAST_PAGE;
        return response;
    }
    
    public static Response newOk()
    {
        Response response = new Response();
        response.result = RESULT_OK;
        return response;
    }
    
    public static Response newException()
    {
        Response response = new Response();
        response.result = RESULT_EXCEPTION;
        return response;
    }
    
    public static Response newGetInfo(LogInfo logInfo)
    {
        Response response = new Response();
        response.result = RESULT_OK;
        
        response.logId = logInfo.getLogId();
        response.start = logInfo.getStart();
        response.checkpoint = logInfo.getCheckpoint();
        response.next = logInfo.getNext();
        response.dataLength = logInfo.getDataLength();
        response.slave = logInfo.isSlave();
        response.masterLogId = logInfo.getMasterLogId();
        response.successfulStop = logInfo.isSuccessfulStop();
        response.version = logInfo.getVersion();
        
        return response;
    }
    
    public LogInfo getLogInfo()
    {
        return new LogInfo(logId, start, checkpoint, next, dataLength, slave, masterLogId, successfulStop, version);
    }
}
