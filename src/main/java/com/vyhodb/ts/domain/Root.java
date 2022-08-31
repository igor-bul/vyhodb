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

import com.vyhodb.onm.Record;
import com.vyhodb.onm.*;

import java.util.ArrayList;
import java.util.Date;

@Record
public class Root implements DomainObject {

    @Id
    private long id = -1;
    
    @IsChanged
    private boolean isChanged = false;

    @Field(fieldName="ProjectCount")
    private int projectCount;
    
    @Field(fieldName="EmployeeCount")
    private int employeeCount;
    
    @Field(fieldName="FirstActivityDate")
    private Date firstActivityDate;
    
    @Field(fieldName="LastActivityDate")
    private Date lastActivityDate;
    
    @Children(linkName="all_activity_types")
    private ArrayList<ActivityType> activityTypes = new ArrayList<>();
    
    @Children(linkName="all_roles")
    private ArrayList<Role> roles = new ArrayList<>();
    
    @Children(linkName="all_employees")
    private ArrayList<Employee> employees = new ArrayList<>();
    
    @Children(linkName="all_projects")
    private ArrayList<Project> projects = new ArrayList<>();
    
    @Children(linkName="top_projects")
    private ArrayList<Project> topProjects = new ArrayList<>();

    public Root() {}
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }

    public int getProjectCount() {
        return projectCount;
    }

    public int getNextProjectNumber() {
        setChanged(true);
        return projectCount ++;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }
    
    public int getNextEmployeeNumber() {
        setChanged(true);
        return employeeCount ++;
    }

    public Date getFirstActivityDate() {
        return firstActivityDate;
    }

    public void setFirstActivityDate(Date firstActivityDate) {
        this.firstActivityDate = firstActivityDate;
        setChanged(true);
    }

    public Date getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(Date lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
        setChanged(true);
    }

    public ArrayList<ActivityType> getActivityTypes() {
        return activityTypes;
    }

    public ArrayList<Role> getRoles() {
        return roles;
    }

    public ArrayList<Employee> getEmployees() {
        return employees;
    }

    public ArrayList<Project> getProjects() {
        return projects;
    }

    public ArrayList<Project> getTopProjects() {
        return topProjects;
    }
    
    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }
    
    @Override
    public String toString() {
        return "Root [id=" + id + ", projectCount=" + projectCount + ", employeeCount=" + employeeCount + ", firstActivityDate=" + firstActivityDate + ", lastActivityDate=" + lastActivityDate + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Root)) {
            return false;
        }
        
        Root other = (Root) obj;
        
        return 
                id == other.id &&
                projectCount == other.projectCount &&
                employeeCount == other.employeeCount;
    }
}
