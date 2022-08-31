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

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import com.vyhodb.onm.impl.AnnotationMapping;
import com.vyhodb.onm.impl.ClassDescriptor;
import com.vyhodb.onm.impl.XmlMapping;

/**
 * Mapping data cache.
 * <p>
 * Mapping is an object which represents cache of internal mapping data. Objects
 * of this class are used by ONM Reading, ONM Writing and ONM Cloning.
 * <p>
 * Objects of this class are constructed based on one of the following data
 * sources:
 * <ol>
 * <li>Class's annotations.
 * <li>External xml file (see xml schema <a href="./doc-files/OnmSchema.xsd"
 * type="text/xml" target="_blank">OnmSchema.xsd</a>).
 * </ol>
 * 
 * <p>
 * <b>Thread safety</b>
 * <p>
 * Static factory methods of this class as well as Mapping objects are thread
 * safe.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public abstract class Mapping {

    public static final String DEFAULT_CONTEXT_KEY = "Sys$Mapping";

    /**
     * Creates new annotation-based Mapping object.
     * <p>
     * Mapping data is retrieved from annotations of classes, participated in
     * ONM Reading, ONM Writing and ONM Cloning operations.
     * 
     * @return new annotation-based Mapping object
     */
    public static Mapping newAnnotationMapping() {
        return new AnnotationMapping();
    }

    /**
     * Creates new xml-based Mapping object.
     * <p>
     * Mapping data is retrieved from external xml file.
     * 
     * @param file
     *            mapping xml file
     * @return new xml-based Mapping object
     */
    public static Mapping newXmlMapping(File file) {
        if (file == null) {
            throw new IllegalArgumentException("[file] is null");
        }
        return new XmlMapping(file);
    }

    /**
     * Creates new xml-based Mapping object.
     * <p>
     * Mapping data is retrieved from xml represented by inputStream.
     * 
     * @param inputStream
     *            mapping xml
     * @return new xml-based Mapping object
     */
    public static Mapping newXmlMapping(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("[inputStream] is null");
        }
        return new XmlMapping(inputStream);
    }

    /**
     * Creates new xml-based Mapping object.
     * <p>
     * Mapping data is retrieved from xml represented by reader.
     * 
     * @param reader
     *            mapping xml
     * @return new xml-based Mapping object
     */
    public static Mapping newXmlMapping(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("[reader] is null");
        }
        return new XmlMapping(reader);
    }

    /**
     * Creates new xml-based Mapping object.
     * <p>
     * Mapping data is retrieved from external xml resource, specified by URL.
     * 
     * @param url
     *            mapping xml resource
     * @return new xml-based Mapping object
     */
    public static Mapping newXmlMapping(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("[url] is null");
        }
        return new XmlMapping(url);
    }

    /**
     * For internal use only.
     * 
     * @param requiredClass
     * @return class mapping info
     */
    @Deprecated
    public abstract ClassDescriptor getClassDescriptor(Class<?> requiredClass);
}
