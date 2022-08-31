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

package com.vyhodb.ts.domain;

import com.vyhodb.onm.Id;
import com.vyhodb.onm.IsChanged;
import com.vyhodb.onm.Parent;
import com.vyhodb.onm.Record;

import static com.vyhodb.ts.Utils.compareParents;

@Record
public class Assignment implements DomainObject {
    
    @Id
    private long id = -1;
    
    @IsChanged
    private boolean isChanged = false;
    
    @Parent(linkName="assignment2employee")
    private Employee employee;
    
    @Parent(linkName="assignment2project")
    private Project project;
    
    public Assignment(){}
    
    public Assignment(Employee employee, Project project) {
        setEmployee(employee);
        setProject(project);
    }

    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        if (this.employee != null) {
            this.employee.getAssignments().remove(this);
        }
        
        this.employee = employee;
        
        if (this.employee != null) {
            this.employee.getAssignments().add(this);
        }
        
        setChanged();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        if (this.project != null) {
            this.project.getAssignments().remove(this);
        }
        
        this.project = project;
        
        if (this.project != null) {
            this.project.getAssignments().add(this);
        }
        
        setChanged();
    }
    
    public void setChanged() {
        isChanged = true;
    }

    @Override
    public String toString() {
        Long employeeId = employee == null ? null : employee.getId();
        Long projectId = project == null ? null: project.getId();
        
        return "Assignment [id=" + id + ", employee=" + employeeId + ", project=" + projectId + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Assignment)) {
            return false;
        }
        
        Assignment other = (Assignment) obj;
        
        return 
                id == other.id &&
                compareParents(employee, other.employee) &&
                compareParents(project, other.project);
    }
}
