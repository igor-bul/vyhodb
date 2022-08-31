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
import java.util.Date;

import static com.vyhodb.ts.Utils.compareParents;

@Record
public class Activity implements DomainObject {

    @Id
    private long id = -1;
    
    @IsChanged
    private boolean isChanged = false;

    @Field(fieldName="ActivityDate")
    private Date activityDate;
    
    @Field(fieldName="Time")
    private short time;
    
    @Field(fieldName="Cost")
    private BigDecimal cost = BigDecimal.ZERO;
    
    @Field(fieldName="Approved")
    private boolean isApproved = false;
    
    @Parent(linkName="activity2activity_type")
    private ActivityType activityType;
    
    @Parent(linkName="activity2project")
    private Project project;
    
    @Parent(linkName="activity2employee")
    private Employee employee;
    
    @Parent(linkName="unapproved_activity2project")
    private Project unapprovedProject;
    
    @Parent(linkName="approved_activity2project")
    private Project approvedProject;
    
    @Children(linkName="activity_history2activity")
    private ArrayList<ActivityHistory> activityHistories = new ArrayList<>();
    
    public Activity(){}
    
    public Activity(ActivityType activityType, Project project, Employee employee, String activityDate, int time) {
        this(activityType, project, employee, new Date(activityDate), (short)time);
    }
    
    public Activity(ActivityType activityType, Project project, Employee employee, Date activityDate, short time) {
        setActivityType(activityType);
        setEmployee(employee);
        setProject(project);
        
        setActivityDate(activityDate);
        setTime(time);
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public Date getActivityDate() {
        return activityDate;
    }
    
    public void setActivityDate(Date activityDate) {
        this.activityDate = new Date(activityDate.getYear(), activityDate.getMonth(), activityDate.getDate());
        setChanged();
    }
    
    public short getTime() {
        return time;
    }
    
    public void setTime(short hours) {
        this.time = hours;
        recalculateCost();
        setChanged();
    }
    
    public boolean isApproved() {
        return isApproved;
    }
    
    public void approve() {
        this.isApproved = true;
        updateProjectLinks();
        setChanged();
    }
    
    public ActivityType getActivityType() {
        return activityType;
    }
    
    public void setActivityType(ActivityType activityType) {
        if (this.activityType != null) {
            this.activityType.getActivities().remove(this);
        }
        
        this.activityType = activityType;
        
        if (this.activityType != null) {
            this.activityType.getActivities().add(this);
        }
        
        setChanged();
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        if (this.project != null) {
            this.project.getActivities().remove(this);
        }
        
        this.project = project;
        
        if (this.project != null) {
            this.project.getActivities().add(this);
        }
        
        updateProjectLinks();
        setChanged();
    }
    
    public Employee getEmployee() {
        return employee;
    }
    
    public BigDecimal getCost() {
        return cost;
    }
    
    public void setEmployee(Employee employee) {
        if (this.employee != null) {
            this.employee.getActivities().remove(this);
        }
        
        this.employee = employee;
        
        if (this.employee != null) {
            this.employee.getActivities().add(this);
        }
        
        setChanged();
    }
    
    public Collection<ActivityHistory> getActivityHistories() {
        return activityHistories;
    }
    
    public void setChanged() {
        isChanged = true;
    }
    
    public void setChanged(boolean changed) {
        isChanged = changed;
    }
    
    public boolean isChanged() {
        return isChanged;
    }
    
    private void updateProjectLinks() {
        if (isApproved()) {
            setApprovedProject(project);
            setUnApprovedProject(null);
        }
        else {
            setApprovedProject(null);
            setUnApprovedProject(project);
        }
    }
    
    private void setApprovedProject(Project project) {
        if (approvedProject != null) {
            approvedProject.getApprovedActivities().remove(this);
        }
        
        approvedProject = project;
        
        if (approvedProject != null) {
            approvedProject.getApprovedActivities().add(this);
        }
    }
    
    private void setUnApprovedProject(Project project) {
        if (unapprovedProject != null) {
            unapprovedProject.getUnapprovedActivities().remove(this);
        }
        
        unapprovedProject = project;
        
        if (unapprovedProject != null) {
            unapprovedProject.getUnapprovedActivities().add(this);
        }
    }
    
    private void recalculateCost() {
        if (employee != null) {
            Role role = employee.getRole();
            if (role != null) {
                BigDecimal rate = role.getHourRate();
                cost = rate.multiply(new BigDecimal(time));
            }
        }
    }
    
    public void reset() {
        setActivityType(null);
        setProject(null);
        setEmployee(null);
    }
    
    @Override
    public String toString() {
        Long activityTypeId = activityType == null ? null : activityType.getId();
        Long employeeId = employee == null ? null : employee.getId();
        Long projectId = project == null ? null: project.getId();
        
        return "Activity [id=" + id + ", activityType=" + activityTypeId + ", employee=" + employeeId + ", project=" + projectId + ", activityDate=" + activityDate + ", time=" + time + ", cost=" + cost + ", isApproved=" + isApproved + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Activity)) {
            return false;
        }
        
        Activity other = (Activity) obj;
        
        if (id < 0) {
           return other == this; 
        } else {
            return 
                id == other.id &&
                activityDate.equals(other.activityDate) &&
                time == other.time &&
                cost.equals(other.cost) &&
                isApproved == other.isApproved &&
                
                compareParents(employee, other.employee) &&
                compareParents(project, other.project) &&
                compareParents(activityType, other.activityType) &&
                compareParents(approvedProject, other.approvedProject) &&
                compareParents(unapprovedProject, other.unapprovedProject);
        }
    }
}
