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

package com.vyhodb.space;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.junit.Assert;

import com.vyhodb.space.Record;
import com.vyhodb.space.Space;

import static org.junit.Assert.*;

public class RecordCommons {

   private static final int LARGE_FIELD_SIZE = 1024 * 1024 * 12; 
   private static final int LARGE_FIELD_CHECK_INDEX = 1024 * 64 + 23;
   private static final byte LARGE_FIELD_CHECK_VALUE = -69;
   
   private static final String[] PRIMITIVE_FIELD_NAMES = new String[] {
       "String", "Long", "Integer", "Boolean", "Date", "BigDecimal", "BigInteger", "Double", "Float",
       "Char", "Short", "Byte", "UUID", "TimeZone"};
   
   private static final Date FIELD_VALUE_DATE = new Date();
   private static final UUID FIELD_VALUE_UUID = UUID.randomUUID();
   private static final TimeZone FIELD_VALUE_TIME_ZONE = TimeZone.getDefault();
   private static final BigDecimal FIELD_VALUE_BIG_DECIMAL = new BigDecimal("-1234567890123456789.123456789");
   private static final BigInteger FIELD_VALUE_BIG_INTEGER = new BigInteger("-1234567890123456789");
   
   private static final byte[] bytes = new byte[] {Byte.MIN_VALUE, 0, Byte.MAX_VALUE};
   private static final short[] shorts = new short[] {1, 2, 3, 0, -1, -2, -3};
   private static final char[] chars = new char[] {'V', 'y', 'h', 'o', 'D', 'b'};
   private static final int[] ints = new int[] {Integer.MIN_VALUE, 0, Integer.MAX_VALUE};
   private static final long[] longs = new long[] {Long.MIN_VALUE, 0, Long.MAX_VALUE};
   private static final float[] floats = new float[] {Float.MIN_VALUE, 0, Float.MAX_VALUE};
   private static final double[] doubles = new double[] {Double.MIN_VALUE, 0, Double.MAX_VALUE};
   private static final boolean[] booleans = new boolean[] {true, false, true};
      
   private static final String[] strings = new String[]{"First", "Second", "Third"};
   private static final Date[] dates = new Date[]{new Date(), new Date(), new Date()};
   private static final BigDecimal[] decimals = new BigDecimal[]{
                           new BigDecimal("-1234567890123456789.123456789"),
                           new BigDecimal(-1234567890L),
                           new BigDecimal(0)
                       };
   private static final BigInteger[] bigIntegers = new BigInteger[]{
                           new BigInteger("-1234567890123456789"),
                           new BigInteger("1234567890123456789"),
                           new BigInteger("0")
                       };
   private static final UUID[] uuids = new UUID[]{UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};

   
   public static long doCreateRecord(Space space)
   {
       Record newRecord = space.newRecord();
       assertNotNull("New record in not created", newRecord);
       return newRecord.getId();
   }
   
   public static void checkCreateRecord(Space space, long id)
   {
       Record createdRecord = space.getRecord(id);
       assertNotNull("Can't find created record", createdRecord);
   }
   
   public static void setPrivitiveFields(Record record) {
       record.setField("String", "String");
       record.setField("Long", Long.MIN_VALUE);
       record.setField("Integer", Integer.MIN_VALUE);
       record.setField("Boolean", true);
       record.setField("Date", FIELD_VALUE_DATE);
       record.setField("BigDecimal", FIELD_VALUE_BIG_DECIMAL);
       record.setField("BigInteger", FIELD_VALUE_BIG_INTEGER);
       record.setField("Double", Double.MIN_VALUE);
       record.setField("Float", Float.MIN_VALUE);
       record.setField("Char", 'i');
       record.setField("Short", Short.MAX_VALUE);
       record.setField("Byte", Byte.MIN_VALUE);
       record.setField("UUID", FIELD_VALUE_UUID);
       record.setField("TimeZone", FIELD_VALUE_TIME_ZONE);
   }
   
   public static long doPrimitiveFields(Space space)
   {
       Record newRecord = space.newRecord();
       setPrivitiveFields(newRecord);
       return newRecord.getId();
   }
   
   public static void checkPrimitiveFields(Space space, long id)
   {
       Record record = space.getRecord(id);
       
       assertEquals("String field is not equal", "String", record.getField("String"));
       assertEquals("Long field is not equal", Long.MIN_VALUE, (long)record.getField("Long"));
       assertEquals("Integer field is not equal", Integer.MIN_VALUE, (int)record.getField("Integer"));
       assertEquals("Boolean field is not equal", true, record.getField("Boolean"));
       assertEquals("Date field is not equal", FIELD_VALUE_DATE, record.getField("Date"));
       assertEquals("BigDecimal field is not equal", FIELD_VALUE_BIG_DECIMAL, record.getField("BigDecimal"));
       assertEquals("BigInteger field is not equal", FIELD_VALUE_BIG_INTEGER, record.getField("BigInteger"));
       assertEquals("Double field is not equal", Double.MIN_VALUE, (double)record.getField("Double"), 0.01);
       assertEquals("Float field is not equal", Float.MIN_VALUE, (float)record.getField("Float"), 0.01);
       assertEquals("Char field is not equal", 'i', (char)record.getField("Char"));
       assertEquals("Short field is not equal", Short.MAX_VALUE, (short)record.getField("Short"));
       assertEquals("Byte field is not equal", Byte.MIN_VALUE, (byte)record.getField("Byte"));
       assertEquals("UUID field is not equal", FIELD_VALUE_UUID, record.getField("UUID"));
       assertEquals("TimeZone field is not equal", FIELD_VALUE_TIME_ZONE, record.getField("TimeZone"));
   }

   public static long doPrimitiveArrays(Space space)
   {
       Record newRecord = space.newRecord();
       
       newRecord.setField("byte[]", bytes);
       newRecord.setField("short[]", shorts);
       newRecord.setField("char[]", chars);
       newRecord.setField("int[]", ints);
       newRecord.setField("long[]", longs);
       newRecord.setField("float[]", floats);
       newRecord.setField("double[]", doubles);
       newRecord.setField("boolean[]", booleans);
       
       return newRecord.getId();
   }
   
   public static void checkPrimitiveArrays(Space space, long id)
   {
       Record record = space.getRecord(id);
       
       assertArrayEquals("byte[] field is not equal", bytes, (byte[]) record.getField("byte[]"));
       assertArrayEquals("short[] field is not equal", shorts, (short[]) record.getField("short[]"));
       assertArrayEquals("char[] field is not equal", chars, (char[]) record.getField("char[]"));
       assertArrayEquals("int[] field is not equal", ints, (int[]) record.getField("int[]"));
       assertArrayEquals("long[] field is not equal", longs, (long[]) record.getField("long[]"));
       
       Assert.assertTrue("float[] field is not equal", Arrays.equals(floats, (float[]) record.getField("float[]")));
       Assert.assertTrue("double[] field is not equal", Arrays.equals(doubles, (double[]) record.getField("double[]")));
       Assert.assertTrue("boolean[] field is not equal", Arrays.equals(booleans, (boolean[]) record.getField("boolean[]")));
   }
   
   public static long doObjectArrays(Space space)
   {
       Record newRecord = space.newRecord();
       
       newRecord.setField("String[]", strings);
       newRecord.setField("Date[]", dates);
       newRecord.setField("BigDecimal[]", decimals);
       newRecord.setField("BigInteger[]", bigIntegers);
       newRecord.setField("UUID[]", uuids);
       
       return newRecord.getId();
   }
   
   public static void checkObjectArrays(Space space, long id)
   {
       Record record = space.getRecord(id);
       
       assertArrayEquals("String[] field is not equal", strings, (String[])record.getField("String[]"));
       assertArrayEquals("Date[] field is not equal", dates, (Date[])record.getField("Date[]"));
       assertArrayEquals("BigDecimal[] field is not equal", decimals, (BigDecimal[])record.getField("BigDecimal[]"));
       assertArrayEquals("BigInteger[] field is not equal", bigIntegers, (BigInteger[])record.getField("BigInteger[]"));
       assertArrayEquals("UUID[] field is not equal", uuids, (UUID[])record.getField("UUID[]"));
   }
   
   public static void doWrongFieldClass(Space space)
   {
       Record root = space.getRecord(0);
       root.setField("Wrong field", new LinkedList());
   }
   
   public static void checkWrongFieldClass(Space space)
   {
       Record root = space.getRecord(0);
       assertNull("Wrong field value has been saved", root.getField("Wrong field"));
   }
   
   public static void checkRecordDeleted(Space space, long id)
   {
       Record record = space.getRecord(id);
       assertNull("Record has not been removed", record);
   }
   
   public static void doLargeField(Space space, long id)
   {
       byte[] array = new byte[LARGE_FIELD_SIZE];
       array[LARGE_FIELD_CHECK_INDEX] = LARGE_FIELD_CHECK_VALUE;
       
       Record record = space.getRecord(id);
       record.setField("Large Field", array);
   }
   
   public static void checkLargeField(Space space, long id)
   {
       Record record = space.getRecord(id);
       byte[] array = record.getField("Large Field");
       
       assertEquals("Large field size is wrong",  LARGE_FIELD_SIZE, array.length);
       assertEquals("Large field context is wrong",  LARGE_FIELD_CHECK_VALUE, array[LARGE_FIELD_CHECK_INDEX]);
   }
   
   public static void doRemoveField(Space space, long id)
   {
       Record record = space.getRecord(id);
       record.setField("String", null);
   }
   
   public static void checkRemoveField(Space space, long id)
   {
       Record record = space.getRecord(id);
       assertNull("Fields has not been removed", record.getField("String"));
   }
   
   public static void checkFieldNames(Space space, long id)
   {
       Record record = space.getRecord(id);
       List<String> etalonNames = new ArrayList(Arrays.asList(PRIMITIVE_FIELD_NAMES));
       Set<String> fieldNames = record.getFieldNames();
       
       assertEquals("Field count is different", fieldNames.size(), etalonNames.size());
       etalonNames.removeAll(fieldNames);
       assertEquals("Field names are different", etalonNames.size(), 0);
   }
   
   public static void updateFields(Record record, Map<String, Object> fields) {
       for (Map.Entry<String, Object> field : fields.entrySet()) {
           record.setField(field.getKey(), field.getValue());
       }
   }
}
