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
import java.util.Collection;

import static com.vyhodb.ts.Utils.compareParents;

@Record
public class ActivityType implements DomainObject {

    @Id
    private long id = -1;
    
    @IsChanged
    private boolean isChanged = false;
    
    @Field(fieldName="Name")
    private String name;
    
    @Field(fieldName="Description")
    private String description;
    
    @Parent(linkName="all_activity_types")
    private Root root;
    
    @Children(linkName="activity2activity_type")
    private ArrayList<Activity> activities = new ArrayList<>();
    
    public ActivityType(){}
    
    public ActivityType(String name, String description) {
        this(null, name, description);
    }
    
    public ActivityType(Root root, String name, String description) {
        setRoot(root);
        
        setName(name);
        setDescription(description);
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
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
    
    public Root getRoot() {
        return root;
    }
    
    public void setRoot(Root root) {
        if (this.root != null) {
            this.root.getActivityTypes().remove(this);
        }
        
        this.root = root;
        
        if (this.root != null) {
            this.root.getActivityTypes().add(this);
        }
        
        setChanged(true);
    }
    
    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }
    
    public Collection<Activity> getActivities() {
        return activities;
    }

    @Override
    public String toString() {
        return "ActivityType [id=" + id + ", name=" + name + ", description=" + description + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof ActivityType)) {
            return false;
        }
        
        ActivityType other = (ActivityType) obj;
        
        return 
                id == other.id &&
                name.equals(other.name) &&
                description.equals(other.description) &&
               
                compareParents(root, other.root);
    }
}
