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

package com.vyhodb.ts;

import com.vyhodb.onm.Mapping;
import com.vyhodb.onm.Writer;
import com.vyhodb.space.Record;
import com.vyhodb.space.*;
import com.vyhodb.ts.domain.*;

import java.math.BigDecimal;
import java.util.Date;

public class SampleBuilder {

    public static final Root build() {
        Root root = new Root();
        
        ActivityType activityTypePlanning = new ActivityType(root, "Planning", "");
        ActivityType activityTypeDesign = new ActivityType(root, "Design", "");
        ActivityType activityTypeCode = new ActivityType(root, "Code", "");
        ActivityType activityTypeTest = new ActivityType(root, "Test", "");
        
        Role roleTeamLeader = new Role(root, "Team Leader", new BigDecimal("50.3"));
        Role roleDeveloper = new Role(root, "Developer", new BigDecimal("40.2"));
        Role roleQA = new Role(root, "QA", new BigDecimal("30.1"));
        
        Employee employeeTeamLeader = new Employee(root, roleTeamLeader, "John", "Smith");
        Employee employeeDeveloper1 = new Employee(root, roleDeveloper, "Harry", "Jones");
        Employee employeeDeveloper2 = new Employee(root, roleDeveloper, "Oliver", "Taylor");
        Employee employeeDeveloper3 = new Employee(root, roleDeveloper, "Jack", "Brown");
        Employee employeeQA = new Employee(root, roleQA, "Amelia", "Williams");
        
        Project projectA = new Project(root, true, null, "Project A", "");
        Project projectADesign = new Project(root, false, projectA, "Project A. Design", "");
        Project projectACode = new Project(root, false, projectA, "Project A. Code", "");
        Project projectATest = new Project(root, false, projectA, "Project A. Test", "");
        Project projectB = new Project(root, true, null, "Project B", "");
        Project projectBDesign = new Project(root, false, projectB, "Project B. Design", "");
        
        // Activities for "Project A"
        new Activity(activityTypePlanning, projectA, employeeTeamLeader, "08/30/2013", 8);
        
        // Activities for "Project A. Design"
        new Activity(activityTypeDesign, projectADesign, employeeTeamLeader, "09/02/2013", 7);
        new Activity(activityTypeDesign, projectADesign, employeeTeamLeader, "09/04/2013", 10);
        new Activity(activityTypeDesign, projectADesign, employeeDeveloper1, "09/04/2013", 8);
        new Activity(activityTypeDesign, projectADesign, employeeQA, "09/05/2013", 3);
        
        // Activities for "Project A. Code"
        new Activity(activityTypeCode, projectACode, employeeDeveloper1, "9/06/2013", 6);
        new Activity(activityTypeCode, projectACode, employeeDeveloper2, "9/06/2013", 6);
        new Activity(activityTypeCode, projectACode, employeeDeveloper3, "9/06/2013", 6);
        new Activity(activityTypeCode, projectACode, employeeTeamLeader, "9/09/2013", 3);
        new Activity(activityTypeCode, projectACode, employeeDeveloper1, "9/09/2013", 7);
        new Activity(activityTypeCode, projectACode, employeeDeveloper2, "9/09/2013", 3);
        new Activity(activityTypeCode, projectACode, employeeDeveloper3, "9/09/2013", 6);

        // Activities for "Project A. Test"
        new Activity(activityTypeTest, projectATest, employeeQA, "09/10/2013", 3);
        new Activity(activityTypeTest, projectATest, employeeQA, "09/11/2013", 4);
        new Activity(activityTypeTest, projectATest, employeeQA, "09/12/2013", 7);
        new Activity(activityTypeTest, projectATest, employeeDeveloper2, "09/11/2013", 6);
        new Activity(activityTypeTest, projectATest, employeeDeveloper3, "09/11/2013", 5);

        // Activities for "Project B"
        new Activity(activityTypeDesign, projectB, employeeTeamLeader, "08/30/2013", 1);
       
        // Activities for "Project B. Design"
        new Activity(activityTypeDesign, projectBDesign, employeeTeamLeader, "9/11/2013", 6);
        new Activity(activityTypeDesign, projectBDesign, employeeQA, "9/10/2013", 6);
        new Activity(activityTypeDesign, projectBDesign, employeeTeamLeader, "9/12/2013", 3);
        
        return root;
    }
    
    public static Root buildAndUpdate(Space space, Mapping metadata) {
        Root root = build();
        Writer.write(metadata, root, space);
        return root;
    }
    
    public static Root buildAndUpdate(Space space) {
        return buildAndUpdate(space, Mapping.newAnnotationMapping());
    }
    
    public static Root buildAndUpdateWithIndexes(Space space) {
        Root root = buildAndUpdate(space);
        createIndexes(space.getRecord(root.getId()));
        return root;
    }
    
    public static Root buildAndUpdateWithIndexes(Space space, Mapping metadata) {
        Root root = buildAndUpdate(space, metadata);
        createIndexes(space.getRecord(root.getId()));
        return root;
    }
    
    public static void createIndexes(Record root) {
        IndexDescriptor allEmployeeNumber = new IndexDescriptor("all_employees.Number", "all_employees", Unique.UNIQUE, new IndexedField("Number", Integer.class, Nullable.NOT_NULL));
        IndexDescriptor activity2EmployeeActivityDate = new IndexDescriptor("activity2employee.ActivityDate", "activity2employee", Unique.DUPLICATE, new IndexedField("ActivityDate", Date.class, Nullable.NOT_NULL));
        IndexDescriptor allProjectsNumber = new IndexDescriptor("all_projects.Number", "all_projects", Unique.UNIQUE, new IndexedField("Number", Integer.class, Nullable.NOT_NULL));
    
        root.createIndex(allEmployeeNumber);
        root.createIndex(allProjectsNumber);
        
        for (Record employee : root.getChildren("all_employees")) {
            employee.createIndex(activity2EmployeeActivityDate);
        }
    }
    
    public static Employee getJohnSmith(Root root) {
        for (Employee employee : root.getEmployees()) {
            if ("John".equals(employee.getFirstName())) {
                return employee;
            }
        }
        
        throw new IllegalStateException("[John Smith] was not found.");
    }
    
    public static Project getProjectACode(Root root) {
        for (Project project : root.getProjects()) {
            if ("Project A. Code".equals(project.getName())) {
                return project;
            }
        }
        
        throw new IllegalStateException("[Project A. Code] was not found.");
    }
    
    public static ActivityType getActivityTypeCode(Root root) {
        for (ActivityType activityType : root.getActivityTypes()) {
            if ("Code".equals(activityType.getName())) {
                return activityType;
            }
        }
        
        throw new IllegalStateException("Activity Type [Code] was not found.");
    }
    
    public static Activity getEmployeeActivityById(Employee employee, long activityId) {
        for (Activity activity : employee.getActivities()) {
            if (activity.getId() == activityId) {
                return activity;
            }
        }
        
        throw new IllegalStateException("Activity was not found.");
    }
    
    public static Role getRoleTeamLeader(Root root) {
        for (Role role : root.getRoles()) {
            if ("Team Leader".equals(role.getName())) {
                return role;
            }
        }
        
        throw new IllegalStateException("Role was not found.");
    }

}
