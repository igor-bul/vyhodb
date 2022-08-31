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

import com.vyhodb.storage.space.SpaceInternal;
import com.vyhodb.storage.space.index.CompositeKey;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 *
 * @author User
 */
public final class ValueSerializer {
    
    public static final byte NULL = 0;
    public static final byte STRING = 1;
    public static final byte LONG = 2;
    public static final byte INT = 3;
    public static final byte BOOLEAN = 4;
    public static final byte DATE = 5;
    //public static final byte CALENDAR_GRIGORIAN = 6;
    public static final byte BIG_DECIMAL = 7;
    public static final byte BIG_INTEGER = 8;
    public static final byte DOUBLE = 9;
    public static final byte FLOAT = 10;
    public static final byte CHAR = 11;
    public static final byte SHORT = 12;
    public static final byte BYTE = 13;
    public static final byte UUID = 14;
    public static final byte COMPOSITE_KEY = 15;
    public static final byte TIME_ZONE = 16;
    
    public static final byte ARRAY_STRING = -1;
    public static final byte ARRAY_P_LONG = -2;
    public static final byte ARRAY_P_INT = -3;
    public static final byte ARRAY_P_BOOLEAN = -4;
    public static final byte ARRAY_DATE = -5;
    //public static final byte ARRAY_CALENDAR_GREGORIAN= -6;
    public static final byte ARRAY_BIG_DECIMAL = -7;
    public static final byte ARRAY_BIG_INTEGER = -8;
    public static final byte ARRAY_P_DOUBLE = -9;
    public static final byte ARRAY_P_FLOAT = -10;
    public static final byte ARRAY_P_CHAR = -11;
    public static final byte ARRAY_P_SHORT = -12;
    public static final byte ARRAY_P_BYTE = -13;
    public static final byte ARRAY_UUID = -14;
    
    public static Object getValue(SpaceInternal space, SystemReader reader)
    {
        final byte type = reader.getByte();
        
        switch(type)
        {
            case NULL:
                return null;
        
            case STRING:
                return reader.getString();
            
            case LONG:
                return reader.getLong();    
                
            case INT:
                return reader.getInt();
             
            case BOOLEAN:
                return reader.getBoolean();
            
            case DATE:
                Date date = new Date(reader.getLong());
                return date;    
            
//            case CALENDAR_GRIGORIAN:
//                GregorianCalendar calendar = new GregorianCalendar
//                break;
            
            case BIG_DECIMAL:
                return getBigDecimal(reader);
                
            case BIG_INTEGER:
                return getBigInteger(reader);
            
            case DOUBLE:
                return reader.getDouble();
                
            case FLOAT:
                return reader.getFloat();
                
            case CHAR:
                return reader.getChar();
                
            case SHORT:
                return reader.getShort();
                
            case BYTE:
                return reader.getByte();
                
            case UUID:
                return getUUID(reader);
                
            case COMPOSITE_KEY:
                return getCompositeKey(reader);
                
            case TIME_ZONE:
                return TimeZone.getTimeZone(reader.getString());
            
      // Object arrays
            case ARRAY_STRING:
                return getArrayString(reader);
                
            case ARRAY_DATE:
                return getArrayDate(reader);
             
            case ARRAY_BIG_DECIMAL:
                return getArrayBigDecimal(reader);
                
            case ARRAY_BIG_INTEGER:
                return getArrayBigInteger(reader);
                
            case ARRAY_UUID:
                return getArrayUUID(reader);
           
       // Primitive types arrays         
            case ARRAY_P_BYTE:
                return reader.getByteArray();
                
            case ARRAY_P_LONG:
                return getArrayPrimitiveLong(reader);
                
            case ARRAY_P_INT:
                return getArrayPrimitiveInt(reader);

            case ARRAY_P_BOOLEAN:
                return getArrayPrimitiveBoolean(reader);
                
            case ARRAY_P_DOUBLE:
                return getArrayPrimitiveDouble(reader);
                
            case ARRAY_P_FLOAT:
                return getArrayPrimitiveFloat(reader);
                
            case ARRAY_P_CHAR:
                return getArrayPrimitiveChar(reader);
                
            case ARRAY_P_SHORT:
                return getArrayPrimitiveShort(reader);

        }
        
        space.throwTRE("Unknowing object type:" + type);
        return null;    // this line will never be reached, because throwTRE throws TransactionRolledBack Exception
    }
    
    public static void putValue(SpaceInternal space, SystemWriter writer, Object value)
    {
        byte type = getType(value, space);
        writer.putByte(type);
        
        switch (type) {

            case NULL:
                return;
    
            case STRING:
                writer.putString((String) value);
                return;
    
            case LONG:
                writer.putLong((Long) value);
                return;
    
            case INT:
                writer.putInt((Integer) value);
                return;
    
            case BOOLEAN:
                writer.putBoolean((Boolean) value);
                return;
    
            case DATE:
                writer.putLong(((Date) value).getTime());
                return;
    
            case BIG_DECIMAL:
                putBigDecimal(writer, (BigDecimal) value);
                return;
    
            case BIG_INTEGER:
                putBigInteger(writer, (BigInteger) value);
                return;
    
            case DOUBLE:
                writer.putDouble((Double) value);
                return;
    
            case FLOAT:
                writer.putFloat((Float) value);
                return;
    
            case CHAR:
                writer.putChar((Character)value);
                return;
                
            case SHORT:
                writer.putShort((Short)value);
                return;
                
            case BYTE:
                writer.putByte((byte)value);
                return;
                
            case UUID:
                putUUID(writer, (UUID) value);
                return;
                
            case COMPOSITE_KEY:
                ((CompositeKey)value).write(writer);
                return;
                
            case TIME_ZONE:
                writer.putString(((TimeZone)value).getID());
                return;
            
            case ARRAY_STRING:
                putArrayString(writer, (String[])value);
                return;
                
            case ARRAY_DATE:
                putArrayDate(writer, (Date[])value);
                return;
                
            case ARRAY_BIG_DECIMAL:
                putArrayBigDecimal(writer, (BigDecimal[])value);
                return;
                
            case ARRAY_BIG_INTEGER:
                putArrayBigInteger(writer, (BigInteger[])value);
                return;
                
            case ARRAY_UUID:
                putArrayUUID(writer, (UUID[])value);
                return;
                
            case ARRAY_P_BYTE:
                writer.putByteArray((byte[])value);
                return;
                
            case ARRAY_P_LONG:
                putArrayPrimitiveLong(writer, (long[])value);
                return;
                
            case ARRAY_P_INT:
                putArrayPrimitiveInt(writer, (int[])value);
                return;
                
            case ARRAY_P_BOOLEAN:
                putArrayPrimitiveBoolean(writer, (boolean[])value);
                return;
                
            case ARRAY_P_DOUBLE:
                putArrayPrimitiveDouble(writer, (double[])value);
                return;
                
            case ARRAY_P_FLOAT:
                putArrayPrimitiveFloat(writer, (float[])value);
                return;
                
            case ARRAY_P_CHAR:
                putArrayPrimitiveChar(writer, (char[])value);
                return;
                
            case ARRAY_P_SHORT:
                putArrayPrimitiveShort(writer, (short[])value);
                return;
        }
    }
    
    /**
     * Checks whether specified object can be stored as a field value.
     * @param value 
     * @return the same object, if object type is immutable, or cloned object
     * if object type is mutable.
     */
    public static Object checkType(SpaceInternal space, Object value)
    {
        byte type = getType(value, space);
        
        switch (type) {
            case NULL:
                return null;
    
            case STRING:
            case LONG:
            case INT:
            case BOOLEAN:
                return value;
    
            case DATE:
                return ((Date)value).clone();
    
            case BIG_DECIMAL:
            case BIG_INTEGER:
            case DOUBLE:
            case FLOAT:
            case CHAR:
            case SHORT:
            case BYTE:
            case UUID:
            case COMPOSITE_KEY:
                return value;
                
            case TIME_ZONE:
                return ((TimeZone) value).clone();
            
            case ARRAY_STRING:
                return ((String[])value).clone();
                
            case ARRAY_DATE:
                Date[] valueDA = (Date[])value;
                Date[] cloned = new Date[valueDA.length];
                for (int i = 0; i < cloned.length; i++) {
                    cloned[i] = (Date) valueDA[i].clone();
                }
                return cloned;
                
            case ARRAY_BIG_DECIMAL:
                return ((BigDecimal[])value).clone();
                
            case ARRAY_BIG_INTEGER:
                return ((BigInteger[])value).clone();
                
            case ARRAY_UUID:
                return ((UUID[])value).clone();
                
            case ARRAY_P_BYTE:
                return ((byte[])value).clone();
                
            case ARRAY_P_LONG:
                return ((long[])value).clone();
                
            case ARRAY_P_INT:
                return ((int[])value).clone();
                
            case ARRAY_P_BOOLEAN:
                return ((boolean[])value).clone();
                
            case ARRAY_P_DOUBLE:
                return ((double[])value).clone();
                
            case ARRAY_P_FLOAT:
                return ((float[])value).clone();
                
            case ARRAY_P_CHAR:
                return ((char[])value).clone();
                
            case ARRAY_P_SHORT:
                return ((short[])value).clone();
        }
        
        return null;     // this line will never be reached, because throwTRE throws TransactionRolledBack Exception
    }
    
    private static void putUUID(SystemWriter writer, UUID value)
    {
        writer.putLong(value.getMostSignificantBits());
        writer.putLong(value.getLeastSignificantBits());
    }
    
    private static UUID getUUID(SystemReader reader)
    {
        return new UUID(reader.getLong(), reader.getLong());
    }
    
    private static void putBigInteger(SystemWriter writer, BigInteger value)
    {
        byte[] bytes = value.toByteArray();
        writer.putByteArray(bytes);
    }
    
    private static BigInteger getBigInteger(SystemReader reader)
    {
        return new BigInteger(reader.getByteArray());
    }
    
    private static void putBigDecimal(SystemWriter writer, BigDecimal value)
    {
        putBigInteger(writer, value.unscaledValue());
        writer.putInt(value.scale());
    }
    
    private static BigDecimal getBigDecimal(SystemReader reader)
    {
        return new BigDecimal(getBigInteger(reader), reader.getInt());
    }
    
    private static String[] getArrayString(SystemReader reader)
    {
        final int size = reader.getInt();
        final String[] result = new String[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = reader.getString();
        }
        
        return result;
    }
    
    private static void putArrayString(SystemWriter writer, String[] value)
    {
        writer.putInt(value.length);
        for (String string : value) {
            writer.putString(string);
        }
    }
    
    private static Date[] getArrayDate(SystemReader reader)
    {
        final int size = reader.getInt();
        final Date[] result = new Date[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = new Date(reader.getLong());
        }
        
        return result;
    }
    
    private static void putArrayDate(SystemWriter writer, Date[] value)
    {
        writer.putInt(value.length);
        for (Date date : value) {
            writer.putLong(date.getTime());
        }
    }
    
    private static BigDecimal[] getArrayBigDecimal(SystemReader reader)
    {
        final int size = reader.getInt();
        final BigDecimal[] result = new BigDecimal[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = getBigDecimal(reader);
        }
        
        return result;
    }
    
    private static void putArrayBigDecimal(SystemWriter writer, BigDecimal[] value)
    {
        writer.putInt(value.length);
        for (BigDecimal bigDecimal : value) {
            putBigDecimal(writer, bigDecimal);
        }
    }
    
    private static BigInteger[] getArrayBigInteger(SystemReader reader)
    {
        final int size = reader.getInt();
        final BigInteger[] result = new BigInteger[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = getBigInteger(reader);
        }
        
        return result;
    }
    
    private static void putArrayBigInteger(SystemWriter writer, BigInteger[] value)
    {
        writer.putInt(value.length);
        for (BigInteger bigInteger : value) {
            putBigInteger(writer, bigInteger);
        }
    }
    
    private static UUID[] getArrayUUID(SystemReader reader)
    {
        final int size = reader.getInt();
        final UUID[] result = new UUID[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = getUUID(reader);
        }
        
        return result;
    }
    
    private static void putArrayUUID(SystemWriter writer, UUID[] value)
    {
        writer.putInt(value.length);
        for (UUID uuid : value) {
            putUUID(writer, uuid);
        }
    }
    
    private static long[] getArrayPrimitiveLong(SystemReader reader)
    {
        final int size = reader.getInt();
        final long[] result = new long[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = reader.getLong();
        }
        
        return result;
    }
    
    private static void putArrayPrimitiveLong(SystemWriter writer, long[] value)
    {
        writer.putInt(value.length);
        for (long l : value) {
            writer.putLong(l);
        }
    }
    
    private static int[] getArrayPrimitiveInt(SystemReader reader)
    {
        final int size = reader.getInt();
        final int[] result = new int[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = reader.getInt();
        }
        
        return result;
    }
    
    private static void putArrayPrimitiveInt(SystemWriter writer, int[] value)
    {
        writer.putInt(value.length);
        for (int l : value) {
            writer.putInt(l);
        }
    }
    
    private static boolean[] getArrayPrimitiveBoolean(SystemReader reader)
    {
        final int size = reader.getInt();
        final boolean[] result = new boolean[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = reader.getBoolean();
        }
        
        return result;
    }
    
    private static void putArrayPrimitiveBoolean(SystemWriter writer, boolean[] value)
    {
        writer.putInt(value.length);
        for (boolean b : value) {
            writer.putBoolean(b);
        }
    }
    
    private static float[] getArrayPrimitiveFloat(SystemReader reader)
    {
        final int size = reader.getInt();
        final float[] result = new float[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = reader.getFloat();
        }
        
        return result;
    }
    
    private static void putArrayPrimitiveFloat(SystemWriter writer, float[] value)
    {
        writer.putInt(value.length);
        for (float f : value) {
            writer.putFloat(f);
        }
    }
    
    private static double[] getArrayPrimitiveDouble(SystemReader reader)
    {
        final int size = reader.getInt();
        final double[] result = new double[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = reader.getDouble();
        }
        
        return result;
    }
    
    private static void putArrayPrimitiveDouble(SystemWriter writer, double[] value)
    {
        writer.putInt(value.length);
        for (double d : value) {
            writer.putDouble(d);
        }
    }
    
    private static char[] getArrayPrimitiveChar(SystemReader reader)
    {
        final int size = reader.getInt();
        final char[] result = new char[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = reader.getChar();
        }
        
        return result;
    }
    
    private static void putArrayPrimitiveChar(SystemWriter writer, char[] value)
    {
        writer.putInt(value.length);
        for (char c : value) {
            writer.putChar(c);
        }
    }
    
    private static short[] getArrayPrimitiveShort(SystemReader reader)
    {
        final int size = reader.getInt();
        final short[] result = new short[size];
        
        for (int i = 0; i < size; i++) {
            result[i] = reader.getShort();
        }
        
        return result;
    }
    
    private static void putArrayPrimitiveShort(SystemWriter writer, short[] value)
    {
        writer.putInt(value.length);
        for (short s : value) {
            writer.putShort(s);
        }
    }
    
    private static CompositeKey getCompositeKey(SystemReader reader) {
        CompositeKey key = new CompositeKey();
        key.read(reader);
        return key;
    }
    
    public static byte getType(Object value, SpaceInternal space) {
        if (value == null)
        {
            return NULL;
        }
        
        if (value instanceof String)
        {
            return STRING;
        }
        
        if (value instanceof Long)
        {
            return LONG;
        }
        
        if (value instanceof Integer)
        {
            return INT;
        }
        
        if (value instanceof Boolean)
        {
            return BOOLEAN;
        }
        
        if (value instanceof Date)
        {
            return DATE;
        }
        
        if (value instanceof BigDecimal)
        {
            return BIG_DECIMAL;
        }
        
        if (value instanceof BigInteger)
        {
            return BIG_INTEGER;
        }
        
        if (value instanceof Double)
        {
            return DOUBLE;
        }
        
        if (value instanceof Float)
        {
            return FLOAT;
        }
        
        if (value instanceof Character)
        {
            return CHAR;
        }
        
        if (value instanceof Short)
        {
            return SHORT;
        }
        
        if (value instanceof Byte)
        {
            return BYTE;
        }
        
        if (value instanceof UUID)
        {
            return UUID;
        }
        
        if (value instanceof CompositeKey) {
            return COMPOSITE_KEY;
        }
        
        if (value instanceof TimeZone) {
            return TIME_ZONE;
        }
        
        if (value instanceof String[])
        {
            return ARRAY_STRING;
        }
        
        if (value instanceof Date[])
        {
            return ARRAY_DATE;
        }
        
        if (value instanceof BigDecimal[])
        {
            return ARRAY_BIG_DECIMAL;
        }
        
        if (value instanceof BigInteger[])
        {
            return ARRAY_BIG_INTEGER;
        }
        
        if (value instanceof UUID[])
        {
            return ARRAY_UUID;
        }
        
        if (value instanceof byte[])
        {
            return ARRAY_P_BYTE;
        }
        
        if (value instanceof long[])
        {
            return ARRAY_P_LONG;
        }
        
        if (value instanceof int[])
        {
            return ARRAY_P_INT;
        }
        
        if (value instanceof boolean[])
        {
            return ARRAY_P_BOOLEAN;
        }
        
        if (value instanceof double[])
        {
            return ARRAY_P_DOUBLE;
        }
        
        if (value instanceof float[])
        {
            return ARRAY_P_FLOAT;
        }
        
        if (value instanceof char[])
        {
            return ARRAY_P_CHAR;
        }
        
        if (value instanceof short[])
        {
            return ARRAY_P_SHORT;
        }
        
        space.throwTRE("Specified object class does is not supported. Class:" + value.getClass());
        return Byte.MIN_VALUE;  // Unreachable line.
    }
}
