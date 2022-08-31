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

package com.vyhodb.admin;

import com.vyhodb.server.Server;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Record;
import com.vyhodb.space.RecordCommons;
import com.vyhodb.space.Space;

public class AdminTestDataHelper {

    public static void createTestData(Server server, int childCount) {
        TrxSpace space = server.startModifyTrx();
        createTestData(space, childCount);
        space.commit();
    }
    
    private static void createTestData(Space space, int childCount) {
        String linkName = "Admin parent link";
        Record child;
        Record root = space.getRecord(0L);
        for (int i = 0; i < childCount; i++) {
            child = space.newRecord();
            RecordCommons.setPrivitiveFields(child);
            child.setParent(linkName, root);
        }
    }
    
    public static int getCount(Server server) {
        TrxSpace space = server.startReadTrx();
        try {
            return getCount(space);
        }
        finally {
            space.rollback();
        }
    }
    
    private static int getCount(Space space) {
        int count = 0;
        
        for (Record child : space.getRecord(0L).getChildren("Admin parent link")) {
            count++;
        }
        
        return count;
    }
}
