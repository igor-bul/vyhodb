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

package com.vyhodb.onm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.vyhodb.onm.impl.ClassDescriptor;
import com.vyhodb.onm.impl.ClassDescriptor.LinkField;
import com.vyhodb.space.Space;
import com.vyhodb.space.Record;

/**
 * Object graph writer, used for ONM Writing.
 * <p>
 * This class realizes <b>ONM Writing</b> functionality. There is only one
 * static method in this class which is used for updating vyhodb space:
 * {@linkplain #write(Mapping, Object, Space)}.
 * <p>
 * This method traverses over java object graph and creates, modifies or deletes
 * records which correspond to traversing objects. Particular java object and
 * vyhodb record relate to each other by {@linkplain Id @Id} annotated field.
 * 
 * <h5>ONM Writing rules</h5>
 * <ul>
 * 
 * <li><b>New.</b> If object's {@linkplain Id @Id} field value is < 0 then
 * object is considered as new and appropriate record is created in specified
 * space.
 * 
 * <li><b>Delete.</b> If object has {@linkplain IsDeleted @IsDeleted} field and
 * it's value is true, then corresponding record is deleted.
 * 
 * <li><b>Changing.</b> By default each traversed java object is considered as
 * changed and corresponding record (fields and parent records) is updated. To
 * explicitly specify which object is changed and which isn't, application
 * developer can specify {@linkplain IsChanged @IsChanged} field by annotation
 * or by xml mapping. If <b>IsChanged</b> field exist and is <b>true</b> then
 * object considered as changed and corresponding to it record is updated. If
 * such field is <b>false</b> - record isn't updated.
 * 
 * <li><b>Links updating.</b> Only parent links are updated. Children
 * collections are used only for traversing over object graph. See
 * {@linkplain Parent @Parent}, {@linkplain Children @Children} for parent and
 * children fields.
 * 
 * </ul>
 * 
 * <h5>Object graph traversing rules</h5>
 * <ul>
 * <li>Traversing is performing using fields annotated by <b>@Children</b> and
 * <b>@Parent</b>.</li>
 * <li>Objects, which are considered as deleted (<b>@IsDeleted</b> == true), are
 * traversed as well as non-deleted ones.</li>
 * </ul>
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 * 
 * @see com.vyhodb.onm.Record
 */
public final class Writer {

    private static class Updater {
        private final HashSet<Long> _deletedRecords = new HashSet<>();
        private final Mapping _mapping;
        private final HashMap<Long, Record> _processedRecords = new HashMap<>();
        private final Space _space;

        Updater(Mapping mapping, Space space) {
            _mapping = mapping;
            _space = space;
        }

        @SuppressWarnings("deprecation")
        private Record process(Object object) throws IllegalArgumentException, IllegalAccessException {

            final ClassDescriptor classDescriptor = _mapping.getClassDescriptor(object.getClass());
            Long id = classDescriptor.getId(object);

            // Specified object has been deleted
            if (_deletedRecords.contains(id))
                return null;

            Record record = _processedRecords.get(id);

            // Specified object has been processed
            if (record != null)
                return record;

            final boolean isChanged = classDescriptor.isChanged(object);
            final boolean isDeleted = classDescriptor.isDeleted(object);

            // Reads/creates record
            if (id < 0) {
                record = _space.newRecord();
                id = record.getId();
                classDescriptor.setId(id, object);
            } else {
                record = _space.getRecord(id);
                if (record == null)
                    throw new OnmException("Record with specified id does not exist or was deleted. Id:" + id + " , object:" + object);
            }

            // Updates fields
            if (isChanged)
                classDescriptor.updateFields(object, record);

            // Adds into appropriate collection
            if (isDeleted)
                _deletedRecords.add(id);
            else
                _processedRecords.put(id, record);

            // Processes parents
            processParent(object, classDescriptor, record, isChanged);

            // Processes children
            processChildren(object, classDescriptor);

            // Deletes record or returns result
            if (isDeleted) {
                record.delete();
                return null;
            } else {
                return record;
            }
        }

        @SuppressWarnings("rawtypes")
        private void processChildren(Object object, ClassDescriptor classDescriptor) throws IllegalArgumentException, IllegalAccessException {
            Collection children = null;
            for (Map.Entry<String, LinkField> entry : classDescriptor.getChildrenFields()) {
                children = (Collection) entry.getValue().field.get(object);
                if (children != null) {
                    for (Object child : children) {
                        process(child);
                    }
                }
            }
        }

        private void processParent(Object object, ClassDescriptor classDescriptor, Record record, boolean isChanged) throws IllegalArgumentException, IllegalAccessException {
            String linkName;
            Object parentObject;
            Record parentRecord = null;

            for (Map.Entry<String, LinkField> entry : classDescriptor.getParentFields()) {
                linkName = entry.getKey();
                parentObject = entry.getValue().field.get(object);

                if (parentObject != null) {
                    // Line below can return null for deleted objects
                    parentRecord = process(parentObject);
                }

                if (isChanged) {
                    if (parentObject != null) {
                        record.setParent(linkName, parentRecord);
                    } else {
                        record.setParent(linkName, null);
                    }
                }
            }
        }

        Record update(Object root) {
            try {
                return process(root);
            } catch (IllegalAccessException rex) {
                throw new OnmException(rex);
            }
        }
    }

    /**
     * Updates vyhodb space by java object graph.
     * 
     * @param mapping
     *            mapping cache
     * @param rootObject
     *            root object of java object graph
     * @param space
     *            space which records are updated
     */
    public static void write(Mapping mapping, Object rootObject, Space space) {
        if (mapping == null) {
            throw new IllegalArgumentException("[mapping] is null");
        }

        if (rootObject == null) {
            throw new IllegalArgumentException("[rootObject] is null");
        }

        if (space == null) {
            throw new IllegalArgumentException("[space] is null");
        }

        Updater updater = new Updater(mapping, space);
        updater.update(rootObject);
    }
}
