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

import static com.vyhodb.onm.OnmFactory.*;

import org.junit.Test;

import com.vyhodb.f.F;
import com.vyhodb.omn.comparators.RootComparator;
import com.vyhodb.onm.Mapping;
import com.vyhodb.onm.Writer;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Space;
import com.vyhodb.ts.SampleBuilder;
import com.vyhodb.ts.domain.Activity;
import com.vyhodb.ts.domain.ActivityHistory;
import com.vyhodb.ts.domain.ActivityType;
import com.vyhodb.ts.domain.Employee;
import com.vyhodb.ts.domain.Project;
import com.vyhodb.ts.domain.Root;

public class CloneUpdaterTest extends AbstractUpdaterTests {

    private static final Mapping METADATA_ANNOTATION = Mapping.newAnnotationMapping();
    
    @Override
    protected Mapping getSampleMetadata() {
        return METADATA_ANNOTATION;
    }

    @Override
    protected Mapping getSimpleMetadata() {
        return null;
    }
    
    private F getCloneFunction() {
        return
                startClone(METADATA_ANNOTATION,
                        objectChildren("top_projects", 
                                objectHierarchy("parent_project", 
                                        objectParent("all_projects"),
                                        objectChildren("assignment2project"),
                                        objectChildren("activity2project"),
                                        objectChildren("unapproved_activity2project")
                                )
                        ),
                        
                        objectChildren("all_employees",
                                objectChildren("assignment2employee"),
                                objectChildren("activity2employee",
                                        objectParent("activity2activity_type"),
                                        objectChildren("activity_history2activity")
                                ),
                                objectParent("employee2role")
                        ),
                        
                        // Tests adding into collections, checking contains()
                        objectChildren("all_employees",
                                objectChildren("activity2employee")
                        ),
                        
                        objectChildren("all_roles"),
                        objectChildren("all_activity_types")
                );
    }

    @Override
    protected Root buildAndUpdateWithIndexes(Space space, Mapping metadata) {
        Root root = SampleBuilder.build();

        F cloneF = getCloneFunction();
        Root clonedRoot = (Root) cloneF.eval(root);
        
        Writer.write(metadata, clonedRoot, space);
        SampleBuilder.createIndexes(space.getRecord(clonedRoot.getId()));
        return clonedRoot;
    }

    @Override
    protected void update(Root root, Space space) {
        F cloneF = getCloneFunction();
        Root clonedRoot = (Root) cloneF.eval(root);
        super.update(clonedRoot, space);
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
            
            F cloneF = getCloneFunction();
            Root clonedRoot = (Root) cloneF.eval(root);
            super.update(clonedRoot, space);
            
            // We can't check added objects, because their clones are actually added in this test
            // Checks id
//            assertNotEquals("new activity's id is -1.", -1L, newActivity.getId());
//            assertNotEquals("new activity's history id is -1.", -1L, newActivityHistory.getId());
//            assertNotNull("Activity record is null", space.getRecord(newActivity.getId()));
//            assertNotNull("Activity History record is null", space.getRecord(newActivityHistory.getId()));
            
            // Loads sample and compares it
            Root loadedRoot = loadRoot(space.getRecord(clonedRoot.getId()));
            RootComparator.compare(clonedRoot, loadedRoot);
        }
        finally {
            if (space != null) {
                space.rollback();
            }
        }
    }

}
