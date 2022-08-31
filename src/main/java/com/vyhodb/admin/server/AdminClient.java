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
import com.vyhodb.storage.pagefile.IOUtils;
import com.vyhodb.storage.pagefile.Source;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class AdminClient implements Source {

    private final long _startTime;
    private final SocketChannel _sc;
    private final ByteBuffer _smallBuffer = ByteBuffer.allocateDirect(Response.SIZE);
    
    public AdminClient(InetSocketAddress adminAddress, InetSocketAddress localAddress) throws IOException {
        _sc = SocketChannel.open();
        configure(_sc);
        
        _sc.bind(localAddress);
        _sc.connect(adminAddress);
                        
        _startTime = System.currentTimeMillis();
    }
    
    private void configure(SocketChannel sc) throws IOException
    {
        sc.configureBlocking(true);
        sc.setOption(StandardSocketOptions.TCP_NODELAY, true);
        sc.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        sc.setOption(StandardSocketOptions.SO_LINGER, 5);   // 5 seconds
    }
    
    @Override
    public void close() {
        if (_sc.isOpen())
        {
            try {
                Request.newClose().send(_sc, _smallBuffer);
            } catch(IOException ex){
            };
            
            try {
                _sc.close();
            } catch(IOException iex) {
            }
        }
    }
    
    public void readOneLogPage(long logPageId) throws IOException, WrongPageIdException
    {
        Request.newReadOneLogPage(logPageId).send(_sc, _smallBuffer);
        Response.receive(_sc, _smallBuffer);
    }
    
    public long syncLog(long next) throws IOException, WrongPageIdException
    {
        Request.newSyncLog(next).send(_sc, _smallBuffer);
        return Response.receive(_sc, _smallBuffer).next;
    }
    
    public Response readData() throws IOException, WrongPageIdException
    {
        Request.newReadData().send(_sc, _smallBuffer);
        return Response.receive(_sc, _smallBuffer);
    }
    
    public void checkLastLog(UUID masterLogId, long next, UUID lastTrxId, int lastCrc) throws IOException, WrongPageIdException
    {
        send(Request.newCheckLog(masterLogId, next, lastTrxId, lastCrc));
        Response.receive(_sc, _smallBuffer);
    }
    
    public long getStartTime()
    {
        return _startTime;
    }

    @Override
    public int read(ByteBuffer buffer, int offset, int count) throws IOException {
        return IOUtils.readNIO(_sc, buffer, offset, count);
    }

    private void send(Request request) throws IOException
    {
        request.send(_sc, _smallBuffer);
    }
    
    public void shrink(long shrinkStart) throws IOException, WrongPageIdException
    {
        send(Request.newShrink(shrinkStart));
        Response.receive(_sc, _smallBuffer);
    }
    
    public void clearSlave() throws IOException, WrongPageIdException
    {
        send(Request.newClearSlave());
        Response.receive(_sc, _smallBuffer);
    }
    
    public void storageClose(long storageCloseTimeout) throws IOException, WrongPageIdException
    {
        send(Request.newStorageClose(storageCloseTimeout));
        Response.receive(_sc, _smallBuffer);
    }
    
    public LogInfo getLogInfo() throws IOException, WrongPageIdException
    {
        send(Request.newGetLogInfo());
        return Response.receive(_sc, _smallBuffer).getLogInfo();
    }
    
    public void ping() throws IOException, WrongPageIdException
    {
        send(Request.newPing());
        Response.receive(_sc, _smallBuffer);
    }
}
