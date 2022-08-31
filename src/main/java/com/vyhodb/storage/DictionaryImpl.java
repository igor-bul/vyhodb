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

package com.vyhodb.storage;

import com.vyhodb.storage.space.Dictionary;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

public final class DictionaryImpl implements Dictionary {

    private static final String FILE_CHARSET = "UTF-8";
    
    private Int2ObjectOpenHashMap<String> _code2string;
    private Object2IntOpenHashMap<String> _string2code;
    
    public DictionaryImpl(StorageConfig config) throws IOException {
        Properties properties = readProperties(config);
       
        _code2string = new Int2ObjectOpenHashMap<>(properties.size());
        _code2string.defaultReturnValue(null);
        
        _string2code = new Object2IntOpenHashMap<>(properties.size());
        _string2code.defaultReturnValue(CODE_NOT_EXIST);
        
        init();
        
        String propName;
        String propValue;
        int code;
        for (Object key : properties.keySet()) {
            propName = ((String) key).intern();
            propValue = properties.getProperty(propName);
                        
            try {
                code = Integer.parseInt(propValue);
                
                if (code < 0) {
                    throw new IOException("Dictionary code must be positive number. Key:" + propName);
                }
                
                put(propName, code);
            }
            catch(NumberFormatException nfe) {
                throw new IOException("Can't read dictionary code for string: " + propName, nfe);
            }
        }
    }
    
    private void init() throws IOException {
        put(String.class.getName(), -1);
        put(Long.class.getName(), -2);
        put(Integer.class.getName(), -3);
        put(Date.class.getName(), -4);
        put(BigDecimal.class.getName(), -5);
        put(BigInteger.class.getName(), -6);
        put(Double.class.getName(), -7);
        put(Float.class.getName(), -8);
        put(Character.class.getName(), -9);
        put(Short.class.getName(), -10);
        put(Byte.class.getName(), -11);
        put(UUID.class.getName(), -12);
    }
    
    private void put(String string, int code) throws IOException {
        if (_string2code.containsKey(string)) {
            throw new IOException("Dictionary contains duplicate key:" + string);
        }
        
        if (_code2string.containsKey(code)) {
            throw new IOException("Dictionary contains duplicate code:" + code);
        }
        
        _code2string.put(code, string);
        _string2code.put(string, code);
    }
    
    private Properties readProperties(StorageConfig config) throws IOException {
        Properties properties = new Properties();
        String dictionaryFilename = config.getDictionaryFilename();
        
        if (dictionaryFilename != null && ! dictionaryFilename.trim().isEmpty() ) {
            FileInputStream fileStream = null; 

            try {
                fileStream = new FileInputStream(dictionaryFilename);
                InputStreamReader reader = new InputStreamReader(fileStream, FILE_CHARSET);
                properties.load(reader);
            }
            finally {
                if (fileStream != null) {
                    fileStream.close();
                }
            }
        }
    
        return properties;
    }

    @Override
    public int resolve(String fieldName) {
        return _string2code.getInt(fieldName);
    }

    @Override
    public String resolve(int fieldNameKey) {
        return _code2string.get(fieldNameKey);
    }

}
