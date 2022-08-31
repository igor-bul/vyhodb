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

package com.vyhodb.rsi.request;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 *
 * @author User
 */
public final class Response implements KryoSerializable {
    
    public Object result;
    public String exStackTrace;
    public String exMessage;
    public HashMap<String, Object> context;
    
    public void setException(Throwable ex) {
        result = null;
        exMessage = ex.getMessage();
        exStackTrace = getStackTrace(ex);
    }
    
    /**
     * TODO this method might be optimized to increase performance
     * 
     * @param ex
     * @return 
     */
    private static String getStackTrace(Throwable ex)
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        try (PrintWriter writer = new PrintWriter(out)) {
            ex.printStackTrace(writer);
        }
        return new String(out.toByteArray());
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeString(exMessage);
        output.writeString(exStackTrace);
        kryo.writeClassAndObject(output, result);
        kryo.writeObjectOrNull(output, context, HashMap.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        exMessage = input.readString();
        exStackTrace = input.readString();
        result = kryo.readClassAndObject(input);
        context = kryo.readObjectOrNull(input, HashMap.class);
    }
}
