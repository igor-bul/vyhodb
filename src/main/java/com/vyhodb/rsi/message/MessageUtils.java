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

package com.vyhodb.rsi.message;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class MessageUtils {
    
    public static final int HEADER_PREAMBULA = 1773826555;
    public static final int HEADER_SIZE = 10;
    
    private static final int OFFSET_PREAMBULA = 0;
    private static final int OFFSET_SIZE = OFFSET_PREAMBULA + 4;
    private static final int OFFSET_READ_ONLY = OFFSET_SIZE + 4;
    private static final int OFFSET_CLOSE = OFFSET_READ_ONLY + 1;
    
    private static final byte[] HEADER_CLOSE = closeHeader();
    private static final int MAX_CHUNK;
    
    static {
        int defaultChunkSize = (System.getProperty("os.name").startsWith("Window"))? 2048 : 8192;
        MAX_CHUNK = Integer.getInteger("com.vyhodb.rsi.max_chunk", defaultChunkSize);
    }
        
    public static void send(OutputStream out, Message message) throws IOException
    {
        writeArray(out, newHeader(message), HEADER_SIZE);
        writeArray(out, message.message, message.length);
    }
    
    public static void sendClose(OutputStream out) throws IOException
    {
        writeArray(out, HEADER_CLOSE, HEADER_SIZE);
    }
    
    public static Message read(InputStream in, byte[] header) throws IOException
    {
        readArray(in, header);
        
        if (getInt(header, OFFSET_PREAMBULA) != HEADER_PREAMBULA)
            throw new IOException("Wrong message preambula.");
        
        Message message = new Message();
        
        if (getBoolean(header, OFFSET_CLOSE))
        {
            message.isClose = true;
        }
        else
        {    
            message.isClose = false;
            message.readOnly = getBoolean(header, OFFSET_READ_ONLY);
            message.message = new byte[getInt(header, OFFSET_SIZE)];
            message.length = message.message.length;
            
            readArray(in, message.message);
        }
        
        return message;
    }
    
    private static void readArray(InputStream in, byte[] array) throws IOException
    {
        int read = 0;
        int rest = array.length;
        int n;
        while (rest > 0)
        {
            n = in.read(array, read, Math.min(MAX_CHUNK, rest));
            
            if (n < 0)
                throw new EOFException();
            
            read += n;
            rest -= n;
        }
    }
    
    private static void writeArray(OutputStream out, byte[] array, int length) throws IOException
    {
        if (MAX_CHUNK >= length)
        {
            out.write(array, 0, length);
        }
        else
        {
            int written = 0;
            int rest = length;
            int n;
            
            while (rest > 0)
            {
                n = Math.min(MAX_CHUNK, rest);
                out.write(array, written, n);
                
                written += n;
                rest -= n;
            }
        }
    }
    
    
    private static byte[] newHeader(Message message)
    {
        byte[] header = new byte[HEADER_SIZE];
        
        putInt(header, OFFSET_PREAMBULA, HEADER_PREAMBULA);
        putInt(header, OFFSET_SIZE, message.length);
        putBoolean(header, OFFSET_READ_ONLY, message.readOnly);
        putBoolean(header, OFFSET_CLOSE, message.isClose);
                
        return header;
    }
    
    private static byte[] closeHeader()
    {
        byte[] header = new byte[HEADER_SIZE];
        
        putInt(header, OFFSET_PREAMBULA, HEADER_PREAMBULA);
        putInt(header, OFFSET_SIZE, 0);
        putBoolean(header, OFFSET_READ_ONLY, true);
        putBoolean(header, OFFSET_CLOSE, true);
        
        return header;
    }
    
    private static int getInt(byte[] b, int off) {
        return ((b[off + 3] & 0xFF)      ) +
               ((b[off + 2] & 0xFF) <<  8) +
               ((b[off + 1] & 0xFF) << 16) +
               ((b[off    ]       ) << 24);
    }
    
    private static void putInt(byte[] b, int off, int val) {
        b[off + 3] = (byte) (val       );
        b[off + 2] = (byte) (val >>>  8);
        b[off + 1] = (byte) (val >>> 16);
        b[off    ] = (byte) (val >>> 24);
    }
    
    static void putLong(byte[] b, int off, long val) {
        b[off + 7] = (byte) (val       );
        b[off + 6] = (byte) (val >>>  8);
        b[off + 5] = (byte) (val >>> 16);
        b[off + 4] = (byte) (val >>> 24);
        b[off + 3] = (byte) (val >>> 32);
        b[off + 2] = (byte) (val >>> 40);
        b[off + 1] = (byte) (val >>> 48);
        b[off    ] = (byte) (val >>> 56);
    }
    
    private static void putBoolean(byte[] b, int off, boolean val) {
        b[off] = (byte) (val ? 1 : 0);
    }
    
    private static boolean getBoolean(byte[] b, int off) {
        return b[off] != 0;
    }
    
}
