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

import com.vyhodb.rsi.RsiClientException;
import com.vyhodb.rsi.message.balancer.BalancerConnection;
import com.vyhodb.rsi.socket.SimpleTcpFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

class MessageConnectionFactoryImpl extends MessageConnectionFactory {
    
    public MessageConnection newMessageConnection(String rsiUrl) throws RsiClientException, URISyntaxException {
        return newConnection(new URI(rsiUrl));
    }
    
    private enum Schema {
        TCP,
        LOCAL,
        BALANCER
    }
    
    private static MessageConnection newConnection(URI uri) throws URISyntaxException, RsiClientException {
        Schema schema = validateSchema(uri);
        
        switch (schema) {
            case TCP:
                return newTcp(uri);
                
            case LOCAL:
                return newLocal(uri);
                
            case BALANCER:
                return newBalancer(uri);
        }
        
        return null;
    }
    
    private static MessageConnection newTcp(URI u) throws URISyntaxException {
        checkHost(u);
        checkPort(u);
        
        Query query = new Query(u.getQuery());
        
        try {    
            SimpleTcpFactory factory = new SimpleTcpFactory(u.getHost(), u.getPort());
            if (query.isPooled()) {
                return new TcpPool(factory, query.getSize(), query.getTTL(), query.isDebug());
            }
            else {
                return new SocketMessageConnection(factory);
            }
        }
        catch(IOException ioe) {
            throw new RsiClientException(ioe);
        }
    }
    
    private static MessageConnection newLocal(URI u) {
        try {
            URL url = new URL(u.getSchemeSpecificPart());
            
            // Loads properties from URL
            Properties properties = new Properties();
            URLConnection connection = url.openConnection();
            connection.connect();
            try (InputStream in = connection.getInputStream()) {
                properties.load(in);
            }
            
            // Starts storage and local RSI
            Class<?> localServerClass = Class.forName("com.vyhodb.server.LocalServer");
            Constructor<?> constructor = localServerClass.getConstructor(Properties.class);
            return (MessageConnection) constructor.newInstance(properties);
        }
        catch(Exception ex) {
            throw new RsiClientException("Can't start vyhodb server in local mode.", ex);
        }
    }
    
    private static MessageConnection newBalancer(URI uri)  {
        try {
            URL configFileURL = new URL(uri.getSchemeSpecificPart());
            return new BalancerConnection(configFileURL);
        }
        catch (IOException ex) {
            throw new RsiClientException(ex);
        }
    }
    
    private static Schema validateSchema(URI u) throws URISyntaxException {
        String schema = u.getScheme();
        
        if ("tcp".equals(schema))
            return Schema.TCP;
        
        if ("local".equals(schema))
            return Schema.LOCAL;
        
        if ("balancer".equals(schema)) {
            return Schema.BALANCER;
        }

        throw new URISyntaxException("", "Unknown schema type:" + u.getScheme());
    }
    
    private static void checkHost(URI u) throws URISyntaxException {
        String host = u.getHost();
        if (host == null || host.trim().equals(""))
            throw new URISyntaxException("", "Host name must be specified");
    }
    
    private static void checkPort(URI u) throws URISyntaxException {
        int port = u.getPort();
        if (port < 0 || port > 65535)
            throw new URISyntaxException("", "Illegal port number");
    }
}
