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

package com.vyhodb.admin.clu;

import com.vyhodb.admin.Admin;
import com.vyhodb.admin.ConsoleShrinkListener;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;

import static com.vyhodb.admin.clu.CluUtils.*;

public class RemoteShrink {

    public static final String NAME = "vdb-shrink-remote";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Options options = getOptions();
        CommandLine cl = parseCL(args, options, NAME);
        if (cl != null)
        {
            if (cl.hasOption(OP_NAME_SLAVE_LIST)) {
               shrinkMaster(cl); 
            } else {
                System.out.print(MESSAGE_SHRINK_WARN);
                String answer = System.console().readLine();
                if (answer.trim().toUpperCase().equals("Y")) {
                    shrink(cl);
                }
            }
        }
    }
    
    private static Options getOptions()
    {
        Options options = new Options();
        options.addOption(OP_HOST);
        options.addOption(OP_PORT);
        options.addOption(OP_SLAVE_LIST);
        return options;
    }
    
    private static void shrink(CommandLine cl) throws IOException {
        InetSocketAddress adminSocketAddress = getAdminSocketAddress(cl);
        
        Admin admin = Admin.getInstance();
        admin.remoteShrink(adminSocketAddress, ConsoleShrinkListener.SINGLETON);
    }
    
    private static void shrinkMaster(CommandLine cl) throws IOException {
        InetSocketAddress adminSocketAddress = getAdminSocketAddress(cl);
        
        String slaveListFilename = cl.getOptionValue(OP_NAME_SLAVE_LIST);
        Collection<InetSocketAddress> slaveAddresses = readSlavesAddresses(slaveListFilename);

        Admin admin = Admin.getInstance();
        admin.remoteShrinkMaster(adminSocketAddress, slaveAddresses, ConsoleShrinkListener.SINGLETON);
    }
    
}
