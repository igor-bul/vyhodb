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

import java.util.Date;

import static com.vyhodb.ts.Utils.compareParents;

@Record
public class ActivityHistory implements DomainObject {

    @Id
    private long id = -1;
    
    @IsChanged
    private boolean isChanged = false;
    
    @Field(fieldName="ActionType")
    private String actionType;
    
    @Field(fieldName="ActionDateTime")
    private Date actionDateTime;
    
    @Parent(linkName="activity_history2activity")
    private Activity activity;

    public ActivityHistory(){}
    
    public ActivityHistory(Activity activity, String actionType) {
        this(activity, actionType, new Date());
    }
    
    public ActivityHistory(Activity activity, String actionType, Date actionDateTime) {
        setActivity(activity);
        
        setActionType(actionType);
        setActionDateTime(actionDateTime);
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
        setChanged();
    }

    public Date getActionDateTime() {
        return actionDateTime;
    }

    public void setActionDateTime(Date actionDateTime) {
        this.actionDateTime = actionDateTime;
        setChanged();
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        if (this.activity != null) {
            this.activity.getActivityHistories().remove(this);
        }
        
        this.activity = activity;
        
        if (this.activity != null) {
            this.activity.getActivityHistories().add(this);
        }
        
        setChanged();
    }
    
    public void setChanged() {
        isChanged = true;
    }

    @Override
    public String toString() {
        Long activityId = activity == null ? null : activity.getId();
        
        return "ActivityHistory [id=" + id + ", activity=" + activityId + ", actionType=" + actionType + ", actionDateTime=" + actionDateTime + "]";
    }
    
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof ActivityHistory)) {
            return false;
        }
        
        ActivityHistory other = (ActivityHistory) obj;
        
        return 
                id == other.id &&
                actionType.equals(other.actionType) &&
                actionDateTime.equals(other.actionDateTime) &&
                
                compareParents(activity, other.activity);
    }
}
