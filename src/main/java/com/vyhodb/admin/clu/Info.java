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

import com.vyhodb.admin.Admin;
import com.vyhodb.admin.LogInfo;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.IOException;

import static com.vyhodb.admin.clu.CluUtils.*;

/**
 *
 * @author Igor Vykhodtcev
 */
public class Info {

    private static final String NAME = "vdb-info";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Options options = getOptions();
        CommandLine cl = parseCL(args, options, NAME);
        
        if (cl != null)
        {
            info(cl);
        }
    }
     
    private static Options getOptions()
    {
        Options options = new Options();
        options.addOption(OP_LOG);
        options.addOption(OP_FULL_INFO);
        return options;
    }
    
    private static void info(CommandLine cl) throws IOException
    {
        System.out.println();
        Admin admin = Admin.getInstance();
        LogInfo logInfo = admin.getLogInfo(cl.getOptionValue(OP_NAME_LOG));
        
        if (cl.hasOption(OP_NAME_FULL_INFO)) {
            System.out.println(logInfo.toFullString());
        } else {
            System.out.println(logInfo);
        }
    }
}
