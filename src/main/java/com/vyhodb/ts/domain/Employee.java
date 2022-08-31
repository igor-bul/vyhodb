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

import java.util.*;

import static com.vyhodb.ts.Utils.compareParents;

@Record
public class Employee implements DomainObject {

    @Id
    private long id = -1;
    
    @IsChanged
    private boolean isChanged = false;
    
    @Field(fieldName="Number")
    private int number;
    
    @Field(fieldName="FirstName")
    private String firstName;
    
    @Field(fieldName="LastName")
    private String lastName;
    
    @Parent(linkName="employee2role")
    private Role role;
    
    @Parent(linkName="all_employees")
    private Root root;
    
    private Date lastActivityDate;
    
    @Children(linkName="activity2employee")
    private ArrayList<Activity> activities = new ArrayList<>();
    
    @Children(linkName="assignment2employee")
    private ArrayList<Assignment> assignments = new ArrayList<>();
    
    public Employee(){}
    
    public Employee(Root root, Role role, String firstName, String lastName) {
        setRoot(root);
        setRole(role);
        
        setFirstName(firstName);
        setLastName(lastName);
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public int getNumber() {
        return number;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        setChanged(true);
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
        setChanged(true);
    }

    public Collection<Activity> getActivities() {
        return activities;
    }
    
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        if (this.role != null) {
            this.role.getEmployees().remove(this);
        }
        
        this.role = role;
               
        if (this.role != null) {
            this.role.getEmployees().add(this);
        }
        
        setChanged(true);
    }
    
    public Collection<Assignment> getAssignments() {
        return assignments;
    }
    
    public Root getRoot() {
        return root;
    }

    public void setRoot(Root root) {
        if (this.root != null) {
            this.root.getEmployees().remove(this);
        }
        
        this.root = root;
        this.number = root.getNextEmployeeNumber();
        
        if (this.root != null) {
            this.root.getEmployees().add(this);
        }
        
        setChanged(true);
    }
    
    public Date getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(Date lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }
    
    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }

    @Override
    public String toString() {
        Long roleId = role == null ? null: role.getId();
        return "Employee [id=" + id + ", number=" + number + ", firstName=" + firstName + ", lastName=" + lastName + ", role=" + roleId + "]";
    }
    
    public Set<Project> getAssignedProjects() {
        HashSet<Project> projects = new HashSet<>();
        
        if (assignments != null) {
            for (Assignment assignment : assignments) {
                if (assignment.getProject() != null) {
                    projects.add(assignment.getProject());
                }
            }
        }
        
        return projects;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Employee)) {
            return false;
        }
        
        Employee other = (Employee) obj;
        
        return 
                id == other.id &&
                number == other.number &&
                firstName.equals(other.firstName) &&
                lastName.equals(other.lastName) &&
                
                compareParents(root, other.root) &&
                compareParents(role, other.role);
    }

}
