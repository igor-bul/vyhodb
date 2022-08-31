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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Igor Vykhodtcev
 */
class Query {

    private static final String PROPERTY_POOL_SIZE = "com.vyhodb.rsi.pool.size";
    private static final String PROPERTY_POOL_TTL = "com.vyhodb.rsi.pool.ttl";
    private static final String PROPERTY_POOL_DEBUG = "com.vyhodb.rsi.pool.debug";

    private static final int DEFAULT_POOL_SIZE;
    private static final long DEFAULT_POOL_TTL;   
    private static final boolean DEFAULT_POOL_DEBUG;
    
    static {
        DEFAULT_POOL_SIZE = Integer.getInteger(PROPERTY_POOL_SIZE, 1);
        DEFAULT_POOL_TTL = Long.getLong(PROPERTY_POOL_TTL, 86400000);
        DEFAULT_POOL_DEBUG = Boolean.getBoolean(PROPERTY_POOL_DEBUG);
    }

    private final Map<String, String> props;

    Query(String query) {
        props = new HashMap<>();
        
        if (query != null)
        {    
            String[] parsedPair;
            for (String pair : query.split("&")) {
                parsedPair = pair.split("=");

                if (parsedPair.length == 2) {
                    props.put(parsedPair[0], parsedPair[1]);
                }
            }
        }
    }

    boolean isPooled() {
        return Boolean.valueOf(props.get("pool"));
    }

    boolean isDebug() {
        String v = props.get("poolDebug");
        return (v == null) ? DEFAULT_POOL_DEBUG : Boolean.parseBoolean(v);
    }

    int getSize() {
        String v = props.get("poolSize");
        return (v == null) ? DEFAULT_POOL_SIZE : Integer.parseInt(v);
    }

    long getTTL() {
        String v = props.get("poolTTL");
        return (v == null) ? DEFAULT_POOL_TTL : Long.parseLong(v);
    }
}
