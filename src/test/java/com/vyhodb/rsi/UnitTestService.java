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

package com.vyhodb.rsi;

import java.util.Map;

import com.vyhodb.rsi.Implementation;
import com.vyhodb.rsi.Modify;
import com.vyhodb.rsi.RsiClientException;
import com.vyhodb.rsi.RsiServerException;
import com.vyhodb.rsi.Read;
import com.vyhodb.rsi.Version;

@Version(version="1.0")
@Implementation(className="com.vyhodb.rsi.UnitTestServiceImpl")
public interface UnitTestService {
    
    public static final String LINK_NAME = "Unit Test Service Parent";
    
    @Modify
    public long addRecord(Map<String, Object> fields);
    
    @Read
    public long count() throws RsiClientException;
    
    @Modify
    public void throwException() throws RsiServerException, RsiClientException;
    
    /**
     * Method tries to modify data under read-only transaction
     * @throws RsiServerException
     * @throws RsiClientException
     */
    @Read
    public void modify() throws RsiServerException, RsiClientException;
}
