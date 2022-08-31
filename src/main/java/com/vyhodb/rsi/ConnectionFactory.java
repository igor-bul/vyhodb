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

import com.vyhodb.rsi.kryo.KryoClient;
import com.vyhodb.rsi.message.MessageConnection;
import com.vyhodb.rsi.message.MessageConnectionFactory;
import com.vyhodb.rsi.request.ConnectionImpl;

import java.net.URISyntaxException;

/**
 * RSI Connection Factory.
 * 
 * <p>
 * The following connection types are supported:
 * <ol>
 * <li>Local</li>
 * <li>TCP Single</li>
 * <li>TCP Pooled</li>
 * <li>TCP Balancer</li>
 * </ol>
 * 
 * <p>
 * <table border="1" style="width:100%">
 * <tr>
 * <th>Type</th>
 * <th>Description</th>
 * <th>URL example</th>
 * </tr>
 * <tr>
 * <td>Local</td>
 * <td>Starts vyhodb server in local mode.</td>
 * <td>local:file:c:/storage/vdb.properties</td>
 * </tr>
 * <tr>
 * <td>TCP Single</td>
 * <td>Establishes single TCP connection to RSI Server.</td>
 * <td>tcp://localhost:47777</td>
 * </tr>
 * <tr>
 * <td>TCP Pooled</td>
 * <td>Creates pool of TCP connections to RSI Server.</td>
 * <td>
 * <p>
 * tcp://localhost:47777/?pool=true
 * <p>
 * tcp://localhost:47777/?pool=true&poolSize=10&poolTTL=360000</td>
 * </tr>
 * <tr>
 * <td>TCP Balancer</td>
 * <td>Creates many connection pools to different RSI Servers and distributes
 * RSI invocations over them.</td>
 * <td>
 * <p>
 * balancer:file:c:/storage/balancer.properties
 * <p>
 * balancer:http://config.cluster.com/balancer.properties</td>
 * </tr>
 * </table>
 * 
 * <p>
 * <b>Thread safety</b>
 * <p>
 * This class is thread safe.
 * 
 * <p>
 * For more information about connection types and their configuring see
 * "Administration Guide". For information about RSI and RSI API see
 * "Developer Guide".
 * 
 * @see Connection
 * @since vyhodb 0.6.0
 * @author Igor Vykhodtsev
 */
public class ConnectionFactory {

    /**
     * Creates new connection for specified RSI URL.
     * 
     * @param url
     *            vyhodb server URI
     * @return vyhodb connection
     * @throws URISyntaxException
     *             specified string is not a proper URI, or specified connection
     *             type is not supported by vyhodb.
     * @throws RsiClientException
     *             exception occurred during connection creation.
     */
    public static Connection newConnection(String url) throws URISyntaxException, RsiClientException {
        MessageConnection messageConection = MessageConnectionFactory.getInstance().newMessageConnection(url);
        return new ConnectionImpl(new KryoClient(messageConection));
    }
}
