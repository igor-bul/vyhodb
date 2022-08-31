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

package com.vyhodb.f.print;

import static com.vyhodb.f.PrintFactory.*;
import static org.junit.Assert.assertEquals;
import static com.vyhodb.f.CommonFactory.*;
import static com.vyhodb.f.NavigationFactory.*;
import static com.vyhodb.space.CriterionFactory.equal;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.f.F;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Record;
import com.vyhodb.utils.DataGenerator;

public class JavaDocTest extends AbstractStorageTests {
    
    @Test
    public void test() {
        TrxSpace space = _storage.startModifyTrx();
        try {
            Record root = space.getRecord(0);
            DataGenerator.generate(root);
            
            F print = 
            startPrint(
                    search("order2root.Customer", equal("Customer 1"),
                            children("item2order",
                                    parent("item2product")
                            )
                    )
            );
            
            System.out.println(print.eval(root));
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }

}
