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

import org.apache.commons.cli.Option;

import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author Igor Vykhodtcev
 */
class OptionComparator implements Comparator<Option> {

    private ArrayList<Option> ops;
    
    public OptionComparator() {
        ops = new ArrayList<>();
        
        ops.add(CluUtils.OP_BACKUP);
        ops.add(CluUtils.OP_LOG);
        ops.add(CluUtils.OP_DATA);
        ops.add(CluUtils.OP_HOST);
        ops.add(CluUtils.OP_PORT);
        ops.add(CluUtils.OP_STORAGE_CLOSE_TIMEOUT);
        ops.add(CluUtils.OP_SLAVE);
        ops.add(CluUtils.OP_PROGRESS_STEP);
        ops.add(CluUtils.OP_FULL_INFO);
        ops.add(CluUtils.OP_SLAVE_LIST);
        ops.add(CluUtils.OP_HELP);
    }
    
    @Override
    public int compare(Option o1, Option o2) {
        int index1 = ops.indexOf(o1);
        int index2 = ops.indexOf(o2);
        
        if (index1 == index2) {
            return 0;
        } 
        
        return index1 > index2 ? 1 : -1;
    }
    
}
