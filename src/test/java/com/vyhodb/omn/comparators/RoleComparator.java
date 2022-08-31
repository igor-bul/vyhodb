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

package com.vyhodb.omn.comparators;

import java.util.ArrayList;

import com.vyhodb.onm.OnmException;
import com.vyhodb.ts.domain.DomainObject;
import com.vyhodb.ts.domain.Role;

public class RoleComparator extends TsComparator<Role> {

    private static EmployeeComparator employeeComparator = new EmployeeComparator();
    
    @Override
    public void onmCompare(Role first, Role second) throws OnmException {
        if (! first.equals(second)) {
            throw new OnmException("Roles are different.\n\tFirst:" + first + "\n\t Second:" + second);
        }

        compareChildren(
                (ArrayList<? extends DomainObject>) first.getEmployees(), 
                (ArrayList<? extends DomainObject>) second.getEmployees(), 
                employeeComparator);
    }
    
}
