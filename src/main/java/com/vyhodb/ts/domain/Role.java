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

import static com.vyhodb.ts.Utils.compareParents;

@Record
public class Role implements DomainObject {

    @Id
    private long id = -1;
    
    @IsChanged
    private boolean isChanged = false;
    
    @IsDeleted
    private boolean isDeleted = false;
    
    @Field(fieldName="Name")
    private String name;
    
    @Field(fieldName="HourRate")
    private BigDecimal hourRate;
    
    @Parent(linkName="all_roles")
    private Root root;
    
    @Children(linkName="employee2role")
    private ArrayList<Employee> employees = new ArrayList<>();

    public Role() {}
    
    public Role(Root root, String name, BigDecimal hourRate) {
        setRoot(root);
        
        setName(name);
        setHourRate(hourRate);
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

    public BigDecimal getHourRate() {
        return hourRate;
    }

    public void setHourRate(BigDecimal hourRate) {
        this.hourRate = hourRate;
        setChanged(true);
    }

    public Root getRoot() {
        return root;
    }

    public void setRoot(Root root) {
        if (this.root != null) {
            this.root.getRoles().remove(this);
        }
        
        this.root = root;
        
        if (this.root != null) {
            this.root.getRoles().add(this);
        }
        
        setChanged(true);
    }
    
    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }
    
    public Collection<Employee> getEmployees() {
        return employees;
    }

    @Override
    public String toString() {
        return "Role [id=" + id + ", name=" + name + ", hourRate=" + hourRate + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Role)) {
            return false;
        }
        
        Role other = (Role) obj;
        
        return 
                id == other.id &&
                name.equals(other.name) &&
                hourRate.equals(other.hourRate) &&
                compareParents(root, other.root);
    }
    
    public void delete() {
        isDeleted = true;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
}
