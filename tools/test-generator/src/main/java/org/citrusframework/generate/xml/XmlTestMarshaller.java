/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.generate.xml;

import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.xml.namespace.CitrusNamespacePrefixMapper;
import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class XmlTestMarshaller {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XmlTestMarshaller.class);

    private volatile JAXBContext jaxbContext;
    private final Schema schema;
    private String contextPath;

    /** Namespace prefix mapper */
    private NamespacePrefixMapper namespacePrefixMapper = new CitrusNamespacePrefixMapper();

    public XmlTestMarshaller() {
        this.schema = loadSchema(Resources.fromClasspath("org/citrusframework/schema/citrus-testcase.xsd"));
    }

    public void marshal(Object graph, Result result) {
        try {
            createMarshaller().marshal(graph, result);
        } catch (JAXBException e) {
            throw new CitrusRuntimeException("Failed to marshal object graph", e);
        }
    }

    public Object unmarshal(Source source) {
        try {
            return createUnmarshaller().unmarshal(source);
        } catch (JAXBException e) {
            throw new CitrusRuntimeException("Failed to unmarshal source", e);
        }
    }

    private Marshaller createMarshaller() throws JAXBException {
        Marshaller marshaller = getOrCreateContext().createMarshaller();

        if (schema != null) {
            marshaller.setSchema(schema);
        }

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", namespacePrefixMapper);
        marshaller.setProperty("jakarta.xml.bind.namespacePrefixMapper", namespacePrefixMapper);

        return marshaller;
    }

    private Unmarshaller createUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = getOrCreateContext().createUnmarshaller();

        if (schema != null) {
            unmarshaller.setSchema(schema);
        }

        return unmarshaller;
    }

    private JAXBContext getOrCreateContext() throws JAXBException {
        if (jaxbContext == null) {
            synchronized (this) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Creating JAXBContext with context path %s", contextPath));
                }

                jaxbContext = JAXBContext.newInstance(contextPath);
            }
        }
        return jaxbContext;
    }

    private Schema loadSchema(Resource resource) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Using marshaller validation schema '%s'", resource.getLocation()));
        }

        try {
            XMLReader xmlReader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
            xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

            if (resource == null || !resource.exists()) {
                throw new ValidationException("Resource does not exist: " + resource);
            }
            InputSource inputSource = new InputSource(resource.getInputStream());
            inputSource.setSystemId(resource.getURI().toString());
            Source schemaSource = new SAXSource(xmlReader, inputSource);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return schemaFactory.newSchema(schemaSource);
        } catch (SAXException e) {
            throw new CitrusRuntimeException("Failed to load schema for marshaller", e);
        }
    }

    public void setContextPaths(List<String> contextPaths) {
        this.contextPath = String.join(":", contextPaths.toArray(new String[0]));
    }

    public NamespacePrefixMapper getNamespacePrefixMapper() {
        return namespacePrefixMapper;
    }

    public void setNamespacePrefixMapper(NamespacePrefixMapper namespacePrefixMapper) {
        this.namespacePrefixMapper = namespacePrefixMapper;
    }
}
