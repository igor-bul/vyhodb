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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.vyhodb.ts.Utils.compareParents;

@Record
public class Project implements DomainObject {

    @Id
    private long id = -1;
    
    @IsChanged
    private boolean isChanged = false;
    
    @Field(fieldName="Number")
    private int number;
    
    @Field(fieldName="Name")
    private String name;
    
    @Field(fieldName="Description")
    private String description;
    
    @Field(fieldName="TotalCost")
    private BigDecimal totalCost = BigDecimal.ZERO;
    
    @Field(fieldName="TotalTime")
    private double totalTime;
    
    @Parent(linkName="parent_project")
    private Project parentProject;
    
    @Parent(linkName="all_projects")
    private Root root;
    
    @Parent(linkName="top_projects")
    private Root rootTopProject;
    
    @Children(linkName="parent_project")
    private ArrayList<Project> childProjects = new ArrayList<>();
    
    @Children(linkName="activity2project")
    private ArrayList<Activity> activities = new ArrayList<>();
    
    @Children(linkName="unapproved_activity2project")
    private ArrayList<Activity> unapprovedActivities = new ArrayList<>();
    
    @Children(linkName="approved_activity2project")
    private ArrayList<Activity> approvedActivities = new ArrayList<>();
    
    @Children(linkName="assignment2project")
    private ArrayList<Assignment> assignments = new ArrayList<>();
    
    public Project() {}
    
    public Project(Root root, boolean isTopProject, Project parentProject, String name, String description) {
        setRoot(root);
        setRootTopProject(isTopProject ? root : null);
        setParentProject(parentProject);
        
        setName(name);
        setDescription(description);
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        setChanged(true);
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        setChanged(true);
    }
    
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
    
    public double getTotalTime() {
        return totalTime;
    }
    
    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }
    
    public Project getParentProject() {
        return parentProject;
    }

    public void setParentProject(Project parentProject) {
        if (this.parentProject != null) {
            this.parentProject.getChildProjects().remove(this);
        }
        
        this.parentProject = parentProject;
        
        if (this.parentProject != null) {
            this.parentProject.getChildProjects().add(this);
        }
        
        setChanged(true);
    }

    public List<Project> getChildProjects() {
        return childProjects;
    }

    public Collection<Activity> getActivities() {
        return activities;
    }
    
    public Root getRoot() {
        return root;
    }
    
    public void setRoot(Root root) {
        if (this.root != null) {
            this.root.getProjects().remove(this);
        }
        
        this.root = root;
        this.number = root.getNextProjectNumber();
        
        if (this.root != null) {
            this.root.getProjects().add(this);
        }
        
        setChanged(true);
    }
    
    public boolean isToplevel() {
        return rootTopProject != null;
    }
    
    public void setRootTopProject(Root rootTopProject) {
        if (this.rootTopProject != null) {
            this.rootTopProject.getTopProjects().remove(this);
        }
        
        this.rootTopProject = rootTopProject;
        
        if (this.rootTopProject != null) {
            this.rootTopProject.getTopProjects().add(this);
        }
        
        setChanged(true);
    }
    
    public Root getRootTopProject() {
        return rootTopProject;
    }
    
    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }
    
    public Collection<Assignment> getAssignments() {
        return assignments;
    }
    
    public Collection<Activity> getUnapprovedActivities() {
        return unapprovedActivities;
    }
    
    public Collection<Activity> getApprovedActivities() {
        return approvedActivities;
    }
    
    @Override
    public String toString() {
        Long parentProjectId = parentProject == null ? null : parentProject.getId();
        return "Project [id=" + id + ", number=" + number + ", name=" + name + ", description=" + description + ", totalCost=" + totalCost + ", totalTime=" + totalTime + ", parentProject=" + parentProjectId + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Project)) {
            return false;
        }
        
        Project other = (Project) obj;
        
        return 
                id == other.id &&
                number == other.number &&
                name.equals(other.name) &&
                description.equals(other.description) &&
                totalCost.equals(other.totalCost) &&
                totalTime == other.totalTime &&
                
                compareParents(root, other.root) &&
                compareParents(rootTopProject, other.rootTopProject) &&
                compareParents(parentProject, other.parentProject);
    }
}
