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

import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author User
 */
public final class Request implements KryoSerializable {

    public String implName;
    public String methodName;
    public String version;
    public boolean readOnly;
    public Object[] parameters;
    public Class<?>[] types;
    public UUID trxId;
    public HashMap<String, Object> context;

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeString(implName);
        output.writeString(methodName);
        output.writeString(version);
        output.writeBoolean(readOnly);
        
        output.writeLong(trxId.getMostSignificantBits());
        output.writeLong(trxId.getLeastSignificantBits());
        
        kryo.writeObjectOrNull(output, parameters, Object[].class);
        kryo.writeObjectOrNull(output, types, Class[].class);
        kryo.writeObjectOrNull(output, context, HashMap.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        implName = input.readString();
        methodName = input.readString();
        version = input.readString();
        readOnly = input.readBoolean();
        
        trxId = new UUID(input.readLong(), input.readLong());
        
        parameters = kryo.readObjectOrNull(input, Object[].class);
        types = kryo.readObjectOrNull(input, Class[].class);
        context = kryo.readObjectOrNull(input, HashMap.class);
    }

}
