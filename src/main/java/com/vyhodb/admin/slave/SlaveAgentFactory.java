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

package com.vyhodb.admin.slave;

import com.vyhodb.storage.CriticalExceptionHandler;
import com.vyhodb.storage.pagestorage.PageStorage;

/**
 *
 * @author Igor Vykhodtcev
 */
public class SlaveAgentFactory {
    private static final String SLAVE_MODE_REALTIME = "realtime";
    private static final String SLAVE_MODE_CRON = "cron";
    
    public final static AbstractAgent newSlave(PageStorage pageStorage, CriticalExceptionHandler criticalExceptionHandler, SlaveConfig config)
    {
        String mode = config.getSlaveMode();
        
        if (mode.equalsIgnoreCase(SLAVE_MODE_REALTIME))
        {
            return new RealtimeSlaveAgent(pageStorage,  criticalExceptionHandler,
                    "SRA. Realtime", config.getSlaveMasterHost(), config.getSlaveMasterPort(), config.getSlaveCheckTimeout(), config.getSlaveTTL());
        }
        else if (mode.equalsIgnoreCase(SLAVE_MODE_CRON))
        {
            return new CronSlaveAgent(pageStorage, criticalExceptionHandler, 
                    "SRA. Cron", config.getSlaveMasterHost(), config.getSlaveMasterPort(), config.getSlaveCronString());
        }
        
        
        throw new IllegalArgumentException("Unknowing slave mode:" + config.getSlaveMode());
    }
}
