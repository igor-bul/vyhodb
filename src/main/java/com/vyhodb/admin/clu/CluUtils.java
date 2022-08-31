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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vyhodb.admin.clu;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Igor Vykhodtcev
 */
@SuppressWarnings("deprecation")
public class CluUtils {

    public static final String MESSAGE_SHRINK_WARN = "After shrink completion your SLAVE servers will get OUT OF SYNC !!!\n Use option -slavelist for Master server.\n Are you sure to continue ? Y/N";
    
    public static final String OP_NAME_OFFLINE = "offline";
    public static final String OP_NAME_ONLINE = "online";
    public static final String OP_NAME_DATA = "data";
    public static final String OP_NAME_LOG = "log";
    public static final String OP_NAME_BACKUP = "backup";
    public static final String OP_NAME_HOST = "host";
    public static final String OP_NAME_PORT = "port";
    public static final String OP_NAME_HELP = "help";
    public static final String OP_NAME_PROGRESS_STEP = "step";
    public static final String OP_NAME_SLAVE = "slave";
    public static final String OP_NAME_STORAGE_CLOSE_TIMEOUT = "timeout";
    public static final String OP_NAME_FULL_INFO = "full";
    public static final String OP_NAME_SLAVE_LIST = "slavelist";
    
    public static final Option OP_DATA;
    public static final Option OP_LOG;
    public static final Option OP_BACKUP;
    public static final Option OP_HOST;
    public static final Option OP_PORT;
    public static final Option OP_HELP;
    public static final Option OP_PROGRESS_STEP;
    public static final Option OP_SLAVE;
    public static final Option OP_STORAGE_CLOSE_TIMEOUT;
    public static final Option OP_FULL_INFO;
    public static final Option OP_SLAVE_LIST;
    
    public static final int DEFAULT_BUFFER_SIZE = 512;
    static final int DEFAULT_PERCENT = 10;
    static final long DEFAULT_SHUTDOWN_TIMEOUT = 1;
    
    static {
        Option option = new Option(null, "Data file name");
        option.setLongOpt(OP_NAME_DATA);
        option.setRequired(true);
        option.setArgs(1);
        option.setArgName("file name");
        option.setOptionalArg(false);
        OP_DATA = option;
        
        option = new Option(null, "Log file name");
        option.setLongOpt(OP_NAME_LOG);
        option.setRequired(true);
        option.setArgs(1);
        option.setArgName("file name");
        option.setOptionalArg(false);
        OP_LOG = option;
        
        option = new Option(null, "Backup file name");
        option.setLongOpt(OP_NAME_BACKUP);
        option.setRequired(true);
        option.setArgs(1);
        option.setArgName("file name");
        option.setOptionalArg(false);
        OP_BACKUP = option;
        
        option = new Option(null, "Admin component's host name");
        option.setLongOpt(OP_NAME_HOST);
        option.setRequired(true);
        option.setArgs(1);
        option.setArgName("admin host name");
        option.setOptionalArg(false);
        OP_HOST = option;
        
        option = new Option(null, "Admin component's port");
        option.setLongOpt(OP_NAME_PORT);
        option.setRequired(true);
        option.setArgs(1);
        option.setArgName("admin port");
        option.setOptionalArg(false);
        OP_PORT = option;
        
        option = new Option(null, "This help");
        option.setLongOpt(OP_NAME_HELP);
        option.setRequired(false);
        option.setArgs(0);
        OP_HELP = option;
        
        option = new Option(null, "Progress reporting step in percents");
        option.setLongOpt(OP_NAME_PROGRESS_STEP);
        option.setRequired(false);
        option.setArgs(1);
        option.setArgName("percents");
        option.setOptionalArg(false);
        OP_PROGRESS_STEP = option;
        
        option = new Option(null, "Restore storage in slave replication mode");
        option.setLongOpt(OP_NAME_SLAVE);
        option.setRequired(false);
        option.setArgs(0);
        OP_SLAVE = option;
        
        option = new Option(null, "Shutdown timeout");
        option.setLongOpt(OP_NAME_STORAGE_CLOSE_TIMEOUT);
        option.setRequired(false);
        option.setArgs(1);
        option.setArgName("milliseconds");
        option.setOptionalArg(false);
        OP_STORAGE_CLOSE_TIMEOUT = option;
        
        option = new Option(null, "Shows additional Log info fields");
        option.setLongOpt(OP_NAME_FULL_INFO);
        option.setRequired(false);
        option.setArgs(0);
        OP_FULL_INFO = option;
        
        option = new Option(null, "Slave server list file");
        option.setLongOpt(OP_NAME_SLAVE_LIST);
        option.setRequired(false);
        option.setArgs(1);
        option.setArgName("file name");
        option.setOptionalArg(false);
        OP_SLAVE_LIST = option;
    }
    
    private static void printHelp(Options options, String cliName) {
        HelpFormatter help = new HelpFormatter();
        help.setOptionComparator(new OptionComparator());
        help.printHelp(cliName, options, true);
    }

    public static CommandLine parseCL(String[] args, Options options, String cliName) {
        options.addOption(OP_HELP);
        CommandLineParser parser = new GnuParser();
        CommandLine cl;
        try {
            cl = parser.parse(options, args, false);
            if (cl.hasOption(OP_NAME_HELP)) {
                printHelp(options, cliName);
                return null;
            }
            return cl;
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
            printHelp(options, cliName);
            return null;
        }
    }
    
    public static int getPercent(CommandLine cl)
    {
        return cl.hasOption(OP_NAME_PROGRESS_STEP) ? Integer.parseInt(cl.getOptionValue(OP_NAME_PROGRESS_STEP)) : DEFAULT_PERCENT;
    }
    
    public static long getStorageCloseTimeout(CommandLine cl)
    {
        return cl.hasOption(OP_NAME_STORAGE_CLOSE_TIMEOUT) ? Long.parseLong(cl.getOptionValue(OP_NAME_STORAGE_CLOSE_TIMEOUT)) : DEFAULT_SHUTDOWN_TIMEOUT;
    }
    
    public static InetSocketAddress getAdminSocketAddress(CommandLine cl) {
        String host = cl.getOptionValue(OP_NAME_HOST);
        int port = Integer.parseInt(cl.getOptionValue(OP_NAME_PORT));
        return new InetSocketAddress(host, port);
    }
    
    public static Collection<InetSocketAddress> readSlavesAddresses(String filename) throws IOException {
        String CHARSET = "UTF-8";
        
        List<String> strings = Files.readAllLines(Paths.get(filename), Charset.forName(CHARSET));
        ArrayList<InetSocketAddress> result = new ArrayList<>(strings.size());
        for (String addressString : strings) {
            result.add(parseSocketAddress(addressString));
        }
        
        return result;
    }
    
    private static InetSocketAddress parseSocketAddress(String socketAddress) throws IOException {
        socketAddress = socketAddress.trim();
        
        int lastIndex = socketAddress.lastIndexOf(":");
        if (lastIndex < 0) {
            throw new IOException("Wrong slave address format: " + socketAddress);
        }
        
        String[] parsedAddress = new String[2];
        
        parsedAddress[0] = socketAddress.substring(0, lastIndex);
        parsedAddress[1] = socketAddress.substring(lastIndex + 1);
        
        int port = Integer.parseInt(parsedAddress[1]);
        InetSocketAddress address = new InetSocketAddress(parsedAddress[0], port);
        
        return address;
    }
}
