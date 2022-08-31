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

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;

import javax.xml.XMLConstants;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.vyhodb.onm.Mapping;
import com.vyhodb.onm.OnmException;
import com.vyhodb.onm.impl.xml.XmlClass;
import com.vyhodb.onm.impl.xml.XmlMeta;

public final class XmlMapping extends Mapping {

    private static final String EXCEPTION_MSG = "Exception occured during reading xml config file.";
    private static final String SCHEMA_RESOURCE_PATH = "com/vyhodb/onm/OnmSchema.xsd";

    private static Schema getSchema() throws SAXException {
        Source xsdSource = new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_RESOURCE_PATH));
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return factory.newSchema(xsdSource);
    }

    @SuppressWarnings("rawtypes")
    private HashMap<Class, XmlClassDescriptor> _classDescriptors = new HashMap<>();

    public XmlMapping(File file) {
        try {
            XmlMeta meta = (XmlMeta) newUnmarshaller().unmarshal(file);
            initClassDescriptors(meta);
        } catch (ClassNotFoundException | NoSuchFieldException | JAXBException | SAXException ex) {
            throw new OnmException(EXCEPTION_MSG, ex);
        }
    }

    public XmlMapping(InputStream inputStream) {
        try {
            XmlMeta meta = (XmlMeta) newUnmarshaller().unmarshal(inputStream);
            initClassDescriptors(meta);
        } catch (ClassNotFoundException | NoSuchFieldException | JAXBException | SAXException ex) {
            throw new OnmException(EXCEPTION_MSG, ex);
        }
    }

    public XmlMapping(Reader reader) {
        try {
            XmlMeta meta = (XmlMeta) newUnmarshaller().unmarshal(reader);
            initClassDescriptors(meta);
        } catch (ClassNotFoundException | NoSuchFieldException | JAXBException | SAXException ex) {
            throw new OnmException(EXCEPTION_MSG, ex);
        }
    }

    public XmlMapping(URL url) {
        try {
            XmlMeta meta = (XmlMeta) newUnmarshaller().unmarshal(url);
            initClassDescriptors(meta);
        } catch (ClassNotFoundException | NoSuchFieldException | JAXBException | SAXException ex) {
            throw new OnmException(EXCEPTION_MSG, ex);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ClassDescriptor getClassDescriptor(Class requiredClass) {
        XmlClassDescriptor descriptor = _classDescriptors.get(requiredClass);
        if (descriptor == null) {
            throw new OnmException("Required class is absent in metadata. Class:" + requiredClass);
        }

        return descriptor;
    }

    private void initClassDescriptors(XmlMeta meta) throws ClassNotFoundException, NoSuchFieldException {
        XmlClassDescriptor descriptor;
        for (XmlClass xmlClass : meta.classes) {
            descriptor = new XmlClassDescriptor(xmlClass);
            _classDescriptors.put(descriptor.getObjectClass(), descriptor);
        }
    }

    private Unmarshaller newUnmarshaller() throws JAXBException, SAXException {
        JAXBContext context = JAXBContext.newInstance(XmlMeta.class);
        Unmarshaller um = context.createUnmarshaller();
        um.setSchema(getSchema());
        return um;
    }
}
