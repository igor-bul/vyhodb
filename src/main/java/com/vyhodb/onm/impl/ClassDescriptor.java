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

package com.vyhodb.onm.impl;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.vyhodb.onm.OnmException;
import com.vyhodb.space.Record;

public abstract class ClassDescriptor {

    public class LinkField {
        public final Field field;
        public final Class<?> linkClass;

        public LinkField(Field field, Class<?> linkClass) {
            this.field = field;
            this.linkClass = linkClass;
        }
    }
    protected HashMap<String, LinkField> _children = new HashMap<>();
    protected HashMap<String, Field> _fieldNames = new HashMap<>();
    protected Field _idField;
    protected Field _isChanged;
    protected Field _isDeleted;
    protected Class<?> _objectClass;

    protected HashMap<String, LinkField> _parents = new HashMap<>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addChild(String linkName, Object child, Object parent, boolean mandatory) {
        try {
            LinkField lm = _children.get(linkName);
            if (lm == null) {
                if (mandatory) {
                    throw new OnmException("Children link [" + linkName + "] was not found in class " + _objectClass);
                }
            } else {
                Collection children = (Collection) lm.field.get(parent);
                if (!children.contains(child)) {
                    children.add(child);
                }
            }

        } catch (IllegalAccessException ex) {
            throw new OnmException(ex);
        }
    }

    public Object cloneObject(Object object) {
        try {
            Object cloned = object.getClass().newInstance();

            for (Field field : _fieldNames.values()) {
                field.set(cloned, field.get(object));
            }

            if (_idField != null) {
                _idField.set(cloned, _idField.get(object));
            }

            if (_isChanged != null) {
                _isChanged.set(cloned, _isChanged.get(object));
            }

            if (_isDeleted != null) {
                _isDeleted.set(cloned, _isDeleted.get(object));
            }

            return cloned;
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException ex) {
            throw new OnmException(ex);
        }
    }

    /**
     * If collection is already initialized in POJO class (for instance by
     * default constructor) then new collection isn't created
     * 
     * @param newObject
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private void createChildrenCollections(Object newObject) {
        try {
            Field field;
            for (Entry<String, LinkField> entry : _children.entrySet()) {
                field = entry.getValue().field;
                if (field.get(newObject) == null) {
                    field.set(newObject, new HashSet<>());
                }
            }

        } catch (IllegalAccessException ex) {
            throw new OnmException(ex);
        }
    }

    public Class<?> getChildClass(String childLinkName) {
        LinkField lm = _children.get(childLinkName);
        if (lm == null)
            throw new OnmException("No children is configured for linkname " + childLinkName + " in class " + _objectClass);

        return lm.linkClass;
    }

    @SuppressWarnings("unchecked")
    public Collection<Object> getChildren(String childLinkName, Object object) {
        try {
            LinkField linkField = _children.get(childLinkName);
            if (linkField == null) {
                throw new OnmException("No children is configured for linkname [" + childLinkName + "] in class " + _objectClass);
            }

            return (Collection<Object>) linkField.field.get(object);
        } catch (IllegalAccessException ex) {
            throw new OnmException(ex);
        }
    }

    public Set<Entry<String, LinkField>> getChildrenFields() {
        return _children.entrySet();
    }

    public Long getId(Object object) {
        try {
            return (Long) _idField.get(object);
        } catch (IllegalAccessException ex) {
            throw new OnmException(ex);
        }
    }

    public Object getParent(String parentLinkName, Object object) {
        try {
            LinkField linkField = _parents.get(parentLinkName);
            if (linkField == null) {
                throw new OnmException("No parent is configured for linkname [" + parentLinkName + "] in class " + _objectClass);
            }

            return linkField.field.get(object);
        } catch (IllegalAccessException ex) {
            throw new OnmException(ex);
        }
    }

    public Class<?> getParentClass(String parentLinkName) {
        LinkField lm = _parents.get(parentLinkName);
        if (lm == null)
            throw new OnmException("No parent is configured for linkname " + parentLinkName + " in class " + _objectClass);

        return lm.linkClass;
    }

    public Set<Entry<String, LinkField>> getParentFields() {
        return _parents.entrySet();
    }

    public boolean isChanged(Object object) {
        // If no @IsChanged annotation
        if (_isChanged == null)
            return true;

        // If new - return true
        if (getId(object) < 0)
            return true;

        // Returns isChanged field value
        try {
            return (Boolean) _isChanged.get(object);
        } catch (IllegalAccessException ex) {
            throw new OnmException(ex);
        }
    }

    public boolean isDeleted(Object object) {
        if (_isDeleted == null)
            return false;

        try {
            return (Boolean) _isDeleted.get(object);
        } catch (IllegalAccessException ex) {
            throw new OnmException(ex);
        }
    }

    /**
     * Does: 1. Creates new object 2. Sets it's id 3. Sets it's fields 4.
     * Creates empty children collections
     * 
     * @param record
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public Object newObject(Record record) {
        try {
            Object newObject = _objectClass.newInstance();

            // Sets id
            _idField.set(newObject, record.getId());
            setFields(record, newObject);
            createChildrenCollections(newObject);

            return newObject;

        } catch (IllegalAccessException | InstantiationException ex) {
            throw new OnmException(ex);

        }
    }

    private void setFields(Record record, Object newObject) {
        try {
            Field field;
            for (Entry<String, Field> entry : _fieldNames.entrySet()) {
                field = entry.getValue();
                field.set(newObject, record.getField(entry.getKey()));
            }

        } catch (IllegalAccessException ex) {
            throw new OnmException(ex);
        }
    }

    public void setId(Long id, Object object) {
        try {
            _idField.set(object, id);
        } catch (IllegalAccessException ex) {
            throw new OnmException(ex);
        }
    }

    public void setParent(String linkName, Object child, Object parent, boolean mandatory) {
        try {
            LinkField lm = _parents.get(linkName);
            if (lm == null) {
                if (mandatory) {
                    throw new OnmException("Parent link [" + linkName + "] was not found in class " + _objectClass);
                }
            } else {
                lm.field.set(child, parent);
            }

        } catch (IllegalAccessException ex) {
            throw new OnmException(ex);
        }
    }

    public void updateFields(Object object, Record record) {
        try {
            for (Entry<String, Field> entry : _fieldNames.entrySet()) {
                record.setField(entry.getKey(), entry.getValue().get(object));
            }
        } catch (IllegalAccessException ex) {
            throw new OnmException(ex);
        }
    }
}
