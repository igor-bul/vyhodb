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

package com.vyhodb.omn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.vyhodb.AbstractStorageTests;
import com.vyhodb.f.F;
import com.vyhodb.omn.comparators.RootComparator;
import com.vyhodb.onm.Mapping;
import com.vyhodb.onm.Writer;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Record;
import com.vyhodb.space.Space;
import com.vyhodb.ts.SampleBuilder;
import com.vyhodb.ts.domain.Activity;
import com.vyhodb.ts.domain.ActivityHistory;
import com.vyhodb.ts.domain.ActivityType;
import com.vyhodb.ts.domain.Employee;
import com.vyhodb.ts.domain.Project;
import com.vyhodb.ts.domain.Role;
import com.vyhodb.ts.domain.Root;

import static com.vyhodb.f.NavigationFactory.*;
import static com.vyhodb.onm.OnmFactory.*;


/**
 * TODO - Using pushParent/pushChild before push()
 * TODO - Using sebsequent push()/push()
 * 
 * @author ivykhodtsev
 *
 */
public abstract class AbstractUpdaterTests extends AbstractStorageTests {

    protected abstract Mapping getSampleMetadata();
    
    /**
     * Simple model has neither @Delete nor @IsChanged annotations
     * 
     * @return
     */
    protected abstract Mapping getSimpleMetadata();
    
    /**
     * Tests building sample, persisting it, reading and validating.
     * 
     * 1. Builds sample
     * 2. Persists sample
     * 3. Loads (by functions) sample
     * 4. Compares (by onmEquals()) built and loaded samples.
     */
    @Test
    public void test_Sample_Build() {
        TrxSpace space = _storage.startModifyTrx();
        
        try {
            Root root = buildAndUpdateWithIndexes(space, getSampleMetadata());
            Root loadedRoot = loadRoot(space.getRecord(root.getId()));
            RootComparator.compare(root, loadedRoot);
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    
    protected Root buildAndUpdateWithIndexes(Space space, Mapping metadata) {
        return SampleBuilder.buildAndUpdateWithIndexes(space, metadata);
    }
    
    protected void update(Root object, Space space) {
        Writer.write(getSampleMetadata(), object, space);
    }
    
    protected Root loadRoot(Record rootRecord) {
        F loadSampleF = getLoadSampleFunction(getSampleMetadata());
        return (Root) loadSampleF.eval(rootRecord);
    }
    
    public static F getLoadSampleFunction(Mapping metadata) {
       return 
           startRead(Root.class, metadata,
                // Top Projects
                children("top_projects", 
                        hierarchy("parent_project")
                ),

                // All projects
                children("all_projects"),
                
                // Employees
                children("all_employees", 
                        
                        // Roles
                        parent("employee2role"),
                        
                        // Assignments
                        children("assignment2employee", 
                                parent("assignment2project")
                        ),
                        
                        // Activities
                        children("activity2employee", 
                                parent("activity2project"),
                                parent("unapproved_activity2project"),
                                parent("activity2activity_type"),
                                children("activity_history2activity")
                        )
                 ),
                 
                 // Test for adding duplicates into collection
                 children("all_employees",
                         children("activity2employee")
                 ),
                 
                 // All Roles
                 children("all_roles"),
                 
                 // All Activity Types
                 children("all_activity_types")
           );
    }
    
    
    /**
     * Tests adding new object into read sample and subsequent sample persisting.
     * 
     * 1. Builds sample
     * 2. Persists sample
     * 3. Adds Activity, persists sample
     * 4. Loads sample, compares it with sample from step 3.
     * 
     */
    @Test
    public void test_Sample_New() {
        TrxSpace space = _storage.startModifyTrx();
        
        try {
            // Creates sample
            Root root = buildAndUpdateWithIndexes(space, getSampleMetadata());
            root = loadRoot(space.getRecord(root.getId()));
            
            // Creates new Activity
            Employee employee = SampleBuilder.getJohnSmith(root);
            Project project = SampleBuilder.getProjectACode(root);
            ActivityType activityType = SampleBuilder.getActivityTypeCode(root);
            Activity newActivity = new Activity(activityType, project, employee, "09/15/2013", 12);
            ActivityHistory newActivityHistory = new ActivityHistory(newActivity, "added");
            update(root, space);
            
            // Checks id
            assertNotEquals("new activity's id is -1.", -1L, newActivity.getId());
            assertNotEquals("new activity's history id is -1.", -1L, newActivityHistory.getId());
            assertNotNull("Activity record is null", space.getRecord(newActivity.getId()));
            assertNotNull("Activity History record is null", space.getRecord(newActivityHistory.getId()));
            
            // Loads sample and compares it
            Root loadedRoot = loadRoot(space.getRecord(root.getId()));
            RootComparator.compare(root, loadedRoot);
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    
    
    /**
     * Tests updating fields and parents by using @IsChanged annotation
     * 
     * 1. Builds sample
     * 2. Persists sample
     * 3. Modifies Activity (changing activity Type and time)
     * 4. Persists sample
     * 5. Loads sample, compares it with one from step 3.
     * 
     */
    @Test
    public void test_Sample_IsChanged_True() {
        final short TIME = 24;
        TrxSpace space = _storage.startModifyTrx();
        
        try {
            // Creates sample
            Root root = buildAndUpdateWithIndexes(space, getSampleMetadata());
                        
            // Changes activity
            Employee employee = SampleBuilder.getJohnSmith(root);
            Project project = SampleBuilder.getProjectACode(root);
            Activity activity = employee.getActivities().iterator().next();
            activity.setProject(project);
            activity.setTime(TIME);
            update(root, space);
            
            // Loads sample and compares it
            Root loadedRoot = loadRoot(space.getRecord(root.getId()));
            RootComparator.compare(root, loadedRoot);
            
            // Check Activity to be sure
            Activity loadedActivity = SampleBuilder.getEmployeeActivityById(SampleBuilder.getJohnSmith(loadedRoot), activity.getId());
            assertEquals("Time has not changed.", TIME, loadedActivity.getTime());
            assertEquals("Project has not changed.", project.getId(), loadedActivity.getProject().getId());
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    /**
     * Updates fields and parents, but doesn't set @IsChanged = true. Nothing should be updated.
     * 
     * 1. Builds sample
     * 2. Persists sample
     * 3. Loads sample
     * 4. Modifies Activity (changing activity Type and time)
     * 5. Sets IsChanged = false
     * 6. Persists sample
     * 7. Loads sample, compares it with one from step 1.
     * 
     */
    @Test
    public void test_Sample_IsChanged_False() {
        final short TIME = 24;
        TrxSpace space = _storage.startModifyTrx();
        
        try {
            // Creates sample
            Root root = buildAndUpdateWithIndexes(space, getSampleMetadata());
            Root loadedRoot = loadRoot(space.getRecord(root.getId()));
            
            // Changes activity
            Employee employee = SampleBuilder.getJohnSmith(loadedRoot);
            Project project = SampleBuilder.getProjectACode(loadedRoot);
            Activity activity = employee.getActivities().iterator().next();
            activity.setProject(project);
            activity.setTime(TIME);
            activity.setChanged(false);
            update(loadedRoot, space);
            
            // Loads sample and compares it
            loadedRoot = loadRoot(space.getRecord(root.getId()));
            RootComparator.compare(root, loadedRoot);
            
            // Check Activity to be sure
            Activity loadedActivity = SampleBuilder.getEmployeeActivityById(SampleBuilder.getJohnSmith(loadedRoot), activity.getId());
            assertNotEquals("Time has changed.", TIME, loadedActivity.getTime());
            assertNotEquals("Project has changed.", project.getId(), loadedActivity.getProject().getId());
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
    /**
     * 
     * 1. Builds sample
     * 2. Persists sample
     * 3. Loads sample
     * 
     */
    @Test
    public void test_Sample_Delete() {
        TrxSpace space = _storage.startModifyTrx();
        
        try {
            // Creates sample
            Root root = buildAndUpdateWithIndexes(space, getSampleMetadata());
            Root loadedRoot = loadRoot(space.getRecord(root.getId()));
            
            // Deletes role
            Role role = SampleBuilder.getRoleTeamLeader(loadedRoot);
            role.delete();
            update(loadedRoot, space);
            
            // Loads sample and checks it
            loadedRoot = loadRoot(space.getRecord(root.getId()));
            assertFalse("Role has not been deleted.", existsRole(loadedRoot, role.getId()));
            assertNull("Role record hasn't been deleted", space.getRecord(role.getId()));
            
            Employee teamLeader = SampleBuilder.getJohnSmith(loadedRoot);
            assertNull("Employee has link to deleted role", teamLeader.getRole());
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }
    
//    @Test
//    public void test_Simple_New() {
//        throw new UnsupportedOperationException();
//    }
//    
//    @Test
//    public void test_Simple_Update() {
//        throw new UnsupportedOperationException();
//    }
    
    private boolean existsRole(Root root, long roleId) {
        for (Role role : root.getRoles()) {
            if (roleId == role.getId()) {
                return true;
            }
        }
        
        return false;
    }
}
