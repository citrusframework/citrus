/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.citrusframework.exceptions.CitrusRuntimeException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.PropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Marshaller uses Jaxb to marshal/unmarshal data.
 */
public class Jaxb2Marshaller implements Marshaller, Unmarshaller {

    /** Logger */
    private static final Logger log = LoggerFactory.getLogger(Jaxb2Marshaller.class);

    private volatile JAXBContext jaxbContext;
    private final Schema schema;
    private final Class<?>[] classesToBeBound;
    private final String contextPath;

    private final Map<String, Object> marshallerProperties = new HashMap<>();

    public Jaxb2Marshaller() {
        this(new Class<?>[]{});
    }

    public Jaxb2Marshaller(Class<?> ... classesToBeBound) {
        this.classesToBeBound = classesToBeBound;
        this.contextPath = null;
        this.schema = null;
    }

    public Jaxb2Marshaller(String ... contextPaths) {
        this.classesToBeBound = null;
        this.contextPath = StringUtils.arrayToDelimitedString(contextPaths, ":");
        this.schema = null;
    }

    public Jaxb2Marshaller(Resource schemaResource, Class<?> ... classesToBeBound) {
        this.classesToBeBound = classesToBeBound;
        this.contextPath = null;
        this.schema = loadSchema(schemaResource);
    }

    public Jaxb2Marshaller(Resource schemaResource, String ... contextPaths) {
        this.classesToBeBound = null;
        this.contextPath = StringUtils.arrayToDelimitedString(contextPaths, ":");
        this.schema = loadSchema(schemaResource);
    }

    public Jaxb2Marshaller(Resource[] schemaResources, Class<?> ... classesToBeBound) {
        this.classesToBeBound = classesToBeBound;
        this.contextPath = null;
        this.schema = loadSchema(schemaResources);
    }

    public Jaxb2Marshaller(Resource[] schemaResources, String ... contextPaths) {
        this.classesToBeBound = null;
        this.contextPath = StringUtils.arrayToDelimitedString(contextPaths, ":");
        this.schema = loadSchema(schemaResources);
    }

    @Override
    public void marshal(Object graph, Result result) throws JAXBException {
        createMarshaller().marshal(graph, result);
    }

    @Override
    public Object unmarshal(Source source) throws JAXBException {
        return createUnmarshaller().unmarshal(source);
    }

    private jakarta.xml.bind.Marshaller createMarshaller() throws JAXBException {
        jakarta.xml.bind.Marshaller marshaller = getOrCreateContext().createMarshaller();

        if (schema != null) {
            marshaller.setSchema(schema);
        }

        marshallerProperties.forEach((k, v) -> {
            try {
                marshaller.setProperty(k, v);
            } catch (PropertyException e) {
                log.warn(String.format("Unable to set marshaller property %s=%s", k, v));
            }
        });

        return marshaller;
    }

    private jakarta.xml.bind.Unmarshaller createUnmarshaller() throws JAXBException {
        jakarta.xml.bind.Unmarshaller unmarshaller = getOrCreateContext().createUnmarshaller();

        if (schema != null) {
            unmarshaller.setSchema(schema);
        }

        return unmarshaller;
    }

    private JAXBContext getOrCreateContext() throws JAXBException {
        if (jaxbContext == null) {
            synchronized (this) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Creating JAXBContext with bound classes %s", Arrays.toString(classesToBeBound)));
                }

                if (classesToBeBound != null) {
                    jaxbContext = JAXBContext.newInstance(classesToBeBound);
                } else if (contextPath != null) {
                    jaxbContext = JAXBContext.newInstance(contextPath);
                } else {
                    jaxbContext = JAXBContext.newInstance();
                }
            }
        }

        return jaxbContext;
    }

    public void setProperty(String key, Object value) {
        this.marshallerProperties.put(key, value);
    }

    private Schema loadSchema(Resource... schemas) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Using marshaller validation schemas '%s'", StringUtils.arrayToCommaDelimitedString(schemas)));
        }

        try {
            List<Source> schemaSources = new ArrayList<>();
            XMLReader xmlReader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
            xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            for (Resource resource : schemas) {
                Assert.isTrue(resource != null && resource.exists(), () -> "Resource does not exist: " + resource);
                InputSource inputSource = new InputSource(resource.getInputStream());
                inputSource.setSystemId(resource.getURI().toString());
                schemaSources.add(new SAXSource(xmlReader, inputSource));
            }
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return schemaFactory.newSchema(schemaSources.toArray(new Source[0]));
        } catch (IOException | SAXException e) {
            throw new CitrusRuntimeException("Failed to load schemas for marshaller", e);
        }
    }
}
