/*
 * Copyright 2006-2013 the original author or authors.
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
package com.consol.citrus.admin.jaxb;

import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringResult;

import javax.xml.bind.*;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * Utility class for creating {@link JAXBContext} as well as for marshaling and un-marshaling xml.
 *
 * @author Martin.Maher@consol.de
 * @since 2013.04.20
 */
@Component
public class JAXBHelper {

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(JAXBHelper.class);

    /** The namespace prefix mapper */
    private NamespacePrefixMapper namespacePrefixMapper = new CitrusNamespacePrefixMapper();

    /**
     * Creates a new JAXB context based on the supplied <code>paths</code>
     *
     * @param paths the paths or packages to use when (un-)marshaling
     * @return the JAXB Context
     */
    public JAXBContext createJAXBContextByPath(String... paths) {
        String contextPath = buildContextPath(paths);
        try {
            return JAXBContext.newInstance(buildContextPath(contextPath));
        } catch (JAXBException e) {
            throw new CitrusAdminRuntimeException(String.format("Error creating the JAXB context using path '%s'", contextPath), e);
        }
    }

    /**
     * Marshal JAXB element
     *
     * @param element the element to marshal
     * @return the string representation
     */
    public <T> String marshal(JAXBContext context, T element) {
        StringResult result = new StringResult();
        marshal(context, element, result);

        return result.toString();
    }

    /**
     * Marshal JAXB Element
     *
     * @param context the JAXBContext
     * @param element the element to marshal
     * @param result the result of the marshaling
     */
    public <T> void marshal(JAXBContext context, T element, Result result) {
        try {
            Marshaller marshaller = getMarshaller(context);
            marshaller.marshal(element, result);
        } catch (JAXBException e) {
            throw new CitrusAdminRuntimeException("Could not marshall element", e);
        }
    }

    /**
     * @param context
     * @param element
     * @param file
     * @param <T>
     */
    public <T> void marshal(JAXBContext context, T element, File file) {
        try {
            Marshaller marshaller = getMarshaller(context);
            marshaller.marshal(element, file);
        } catch (JAXBException e) {
            throw new CitrusAdminRuntimeException("Could not marshall element", e);
        }
    }

    /**
     * Un-marshal XML to JAXB representation.
     *
     * @param clazz the JAXB element to unmarshal
     * @param raw the raw xml
     * @return the JAXB element
     */
    public <T> T unmarshal(JAXBContext context, Class<T> clazz, String raw) {
        try {
            Unmarshaller u = context.createUnmarshaller();
            JAXBElement<T> response = u.unmarshal(new StreamSource(new StringReader(raw)), clazz);
            return response.getValue();
        } catch (JAXBException e) {
            throw new CitrusAdminRuntimeException(String.format("Could not unmarshall %s ", clazz), e);
        }
    }

    /**
     * @param context
     * @param clazz
     * @param file
     * @param <T>
     * @return
     */
    public <T> T unmarshal(JAXBContext context, Class<T> clazz, File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            InputStreamReader in = new InputStreamReader(fis, "UTF-8");
            Unmarshaller u = context.createUnmarshaller();
            JAXBElement<T> response = u.unmarshal(new StreamSource(in), clazz);
            return response.getValue();
        } catch (JAXBException e) {
            throw new CitrusAdminRuntimeException(String.format("Could not unmarshall %s ", clazz), e);
        } catch (FileNotFoundException e) {
            throw new CitrusAdminRuntimeException("Exception thrown during unmarshal", e);
        } catch (UnsupportedEncodingException e) {
            throw new CitrusAdminRuntimeException("Exception thrown during unmarshal", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.error("Error closing file input stream", e);
                }
            }
        }
    }

    private Marshaller getMarshaller(JAXBContext context) throws JAXBException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", namespacePrefixMapper);

        return marshaller;
    }

    protected String buildContextPath(String... contextPaths) {
        StringBuilder pathBuilder = new StringBuilder();

        for (String contextPath : contextPaths) {
            pathBuilder.append(contextPath).append(":");
        }

        if (pathBuilder.length() > 0) {
            pathBuilder.deleteCharAt(pathBuilder.length() - 1);
        }

        return pathBuilder.toString();
    }

    /**
     * Sets the namespace prefix mapper instance.
     * @param namespacePrefixMapper
     */
    public void setNamespacePrefixMapper(NamespacePrefixMapper namespacePrefixMapper) {
        this.namespacePrefixMapper = namespacePrefixMapper;
    }
}
