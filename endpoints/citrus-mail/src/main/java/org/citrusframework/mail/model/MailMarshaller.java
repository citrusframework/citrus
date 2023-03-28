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

package org.citrusframework.mail.model;

import jakarta.xml.bind.JAXBException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringWriter;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessageType;
import org.citrusframework.xml.Jaxb2Marshaller;
import org.citrusframework.xml.Marshaller;
import org.citrusframework.xml.StringResult;
import org.citrusframework.xml.Unmarshaller;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Christoph Deppisch
 * @since 2.1
 */
public class MailMarshaller implements Marshaller, Unmarshaller {

    /** Logger */
    private static final Logger log = LoggerFactory.getLogger(MailMarshaller.class);

    /** System property defining message format to marshal to */
    private static final String MAIL_MARSHALLER_TYPE_PROPERTY = "citrus.mail.marshaller.type";

    /** Message type format: XML or JSON */
    private String type;

    private final ObjectMapper mapper;
    private final Jaxb2Marshaller marshaller;

    private final Class<?>[] classesToBeBound = new Class[] {AcceptRequest.class, AcceptResponse.class, MailRequest.class, MailResponse.class};

    /**
     * Default constructor
     */
    public MailMarshaller() {
        this.mapper = new ObjectMapper();
        this.marshaller = new Jaxb2Marshaller(new ClassPathResource("org/citrusframework/schema/citrus-mail-message.xsd"), classesToBeBound);

        type = System.getProperty(MAIL_MARSHALLER_TYPE_PROPERTY, MessageType.XML.name());
    }

    public Object unmarshal(Source source) {
        if (type.equalsIgnoreCase(MessageType.XML.name())) {
            try {
                return marshaller.unmarshal(source);
            } catch (JAXBException e) {
                if (source instanceof StreamSource) {
                    for (Class<?> type : classesToBeBound) {
                        try {
                            return mapper.readValue(((StreamSource) source).getReader(), type);
                        } catch (JsonParseException | JsonMappingException e2) {
                            continue;
                        } catch (IOException io) {
                            log.warn("Unable to read mail JSON object from source", io);
                            throw new CitrusRuntimeException("Failed to unmarshal source", io);
                        }
                    }
                }

                throw new CitrusRuntimeException("Failed to unmarshal source", e);
            }
        } else if (type.equalsIgnoreCase(MessageType.JSON.name())) {
            for (Class<?> type : classesToBeBound) {
                try {
                    return mapper.readValue(((StreamSource) source).getReader(), type);
                } catch (JsonParseException | JsonMappingException e2) {
                    continue;
                } catch (IOException io) {
                    throw new CitrusRuntimeException("Unable to read mail JSON object from source", io);
                }
            }
            throw new CitrusRuntimeException("Failed to read mail JSON object from source:" + source);
        } else {
            throw new CitrusRuntimeException("Unsupported mail marshaller type: " + type);
        }
    }

    public void marshal(Object graph, Result result) {
        if (type.equalsIgnoreCase(MessageType.JSON.name())) {
            if (result instanceof StringResult) {
                StringWriter writer = new StringWriter();
                ((StringResult) result).setWriter(writer);
                try {
                    mapper.writer().writeValue(writer, graph);
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to write mail object graph to result", e);
                }
            }
        } else if (type.equalsIgnoreCase(MessageType.XML.name())) {
            try {
                marshaller.marshal(graph, result);
            } catch (JAXBException e) {
                throw new CitrusRuntimeException("Failed to marshal object graph", e);
            }
        } else {
            throw new CitrusRuntimeException("Unsupported mail marshaller type: " + type);
        }
    }

    /**
     * Gets the type.
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }
}
