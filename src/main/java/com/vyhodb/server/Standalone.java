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

package com.vyhodb.server;

import com.vyhodb.admin.clu.CluUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class Standalone {
    
    public static final String DEFAULT_CONFIG_FILENAME = "vdb.properties";
    public static final String OPTION_CONFIG = "config";
    public static final String CLI_NAME = "vdb-start";
    
    private static final Logger logger = LoggerFactory.getLogger(Loggers.LOGGER_NAME_SERVER);
    
    @SuppressWarnings("unused")
    private static Server storage;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        CommandLine cl = CluUtils.parseCL(args, getOptions(), CLI_NAME);
        String configFileName = null;
        
        // Wrong parameter or help
        if (cl == null)
            return;
        
        // Checks presence of -config parameter
        if (cl.hasOption(OPTION_CONFIG) && 
                Files.exists(Paths.get(cl.getOptionValue(OPTION_CONFIG)))
            )
        {
            configFileName = cl.getOptionValue(OPTION_CONFIG);
        }

        // Checks default filename
        if (configFileName == null && Files.exists(Paths.get(DEFAULT_CONFIG_FILENAME)))
        {
            configFileName = DEFAULT_CONFIG_FILENAME;
        }
        
        // Configuration file hasn't found
        if (configFileName == null)
        {
            logger.error("Can't find vyhodb config file");
            throw new IOException("Can't find vyhodb config file");
        }
        
        logger.info("vyhodb server starting...\n  Config file: {}\n", Paths.get(configFileName).toAbsolutePath());
        
        Properties properties = loadProperties(configFileName);
        updateDefaultProperties(properties);
        start(properties);
    }
    
    private static Options getOptions()
    {
        Option config = new Option(null, "Specifies vyhodb configuration file name");
        config.setLongOpt(OPTION_CONFIG);
        config.setRequired(false);
        config.setArgs(1);
        config.setArgName("file name");
        config.setOptionalArg(false);
       
        Options options = new Options();
        options.addOption(config);

        return options;
    }
    
    private static Properties loadProperties(String filename) throws IOException {
        Properties properties = new Properties();
        FileInputStream in = new FileInputStream(filename);
        try
        {    
            properties.load(in);
            return properties;
        }
        finally 
        {
            in.close();
        }
    }
    
    private static void updateDefaultProperties(Properties properties) {
        properties.setProperty("exitVmOnCriticalException", "true");
    }
    
    private static void start(Properties properties) throws IOException
    {
        storage = Server.start(properties);
                
        // Wait till Listener's log their start messages 
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        // Just to prevent server from stopping, because all the other threads
        // are daemons.
        while(true)
        {
            try {
                Thread.sleep(360000);   // 1 hour
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
