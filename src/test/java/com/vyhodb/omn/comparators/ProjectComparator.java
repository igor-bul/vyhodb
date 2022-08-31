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
import com.vyhodb.ts.domain.Project;

public class ProjectComparator extends TsComparator<Project> {

    private static ActivityComparator activityComparator = new ActivityComparator();
    private static AssignmentComparator assignmentComparator = new AssignmentComparator();
    
    @Override
    public void onmCompare(Project first, Project second) throws OnmException {
        if (! first.equals(second)) {
            throw new OnmException("Projects are different.\n\tFirst:" + first + "\n\t Second:" + second);
        }

        compareChildren(
                (ArrayList<? extends DomainObject>) first.getAssignments(), 
                (ArrayList<? extends DomainObject>) second.getAssignments(), 
                assignmentComparator);
        
        compareChildren(
                (ArrayList<? extends DomainObject>) first.getActivities(), 
                (ArrayList<? extends DomainObject>) second.getActivities(), 
                activityComparator);
        
        compareChildren(
                (ArrayList<? extends DomainObject>) first.getApprovedActivities(), 
                (ArrayList<? extends DomainObject>) second.getApprovedActivities(), 
                activityComparator);
        
        compareChildren(
                (ArrayList<? extends DomainObject>) first.getUnapprovedActivities(), 
                (ArrayList<? extends DomainObject>) second.getUnapprovedActivities(), 
                activityComparator);
        
        compareChildren(
                (ArrayList<? extends DomainObject>) first.getChildProjects(), 
                (ArrayList<? extends DomainObject>) second.getChildProjects(), 
                this);
    }
}
