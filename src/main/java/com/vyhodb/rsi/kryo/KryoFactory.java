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

package com.vyhodb.rsi.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.vyhodb.rsi.request.Request;
import com.vyhodb.rsi.request.Response;

import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class KryoFactory {
    
    private static final String PROPERTY_INITIAL_BUFFER_SIZE = "com.vyhodb.rsi.kryo.init_buffer_size";
    private static final String PROPERTY_MAX_BUFFER_SIZE = "com.vyhodb.rsi.kryo.max_buffer_size";
    
    public static final int KRYO_INITIAL_BUFFER_SIZE;
    public static final int KRYO_MAX_BUFFER_SIZE;
    
    static {
        KRYO_INITIAL_BUFFER_SIZE = Integer.getInteger(PROPERTY_INITIAL_BUFFER_SIZE, 16384);
        KRYO_MAX_BUFFER_SIZE = Integer.getInteger(PROPERTY_MAX_BUFFER_SIZE, 33554432);
    }
    
    public static Kryo newKryo()
    {
        Kryo kryo = new Kryo();
        kryo.setClassLoader(Thread.currentThread().getContextClassLoader());    // Vital line
        kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());
        
        kryo.register(Request.class);
        kryo.register(Response.class);
        return kryo;
    }
}
