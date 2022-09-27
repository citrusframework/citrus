/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.ftp.message;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringWriter;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ftp.model.Command;
import com.consol.citrus.ftp.model.CommandResult;
import com.consol.citrus.ftp.model.ConnectCommand;
import com.consol.citrus.ftp.model.DeleteCommand;
import com.consol.citrus.ftp.model.DeleteCommandResult;
import com.consol.citrus.ftp.model.GetCommand;
import com.consol.citrus.ftp.model.GetCommandResult;
import com.consol.citrus.ftp.model.ListCommand;
import com.consol.citrus.ftp.model.ListCommandResult;
import com.consol.citrus.ftp.model.PutCommand;
import com.consol.citrus.ftp.model.PutCommandResult;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.xml.Jaxb2Marshaller;
import com.consol.citrus.xml.Marshaller;
import com.consol.citrus.xml.StringResult;
import com.consol.citrus.xml.Unmarshaller;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class FtpMarshaller implements Marshaller, Unmarshaller {

    /** Logger */
    private static final Logger log = LoggerFactory.getLogger(FtpMarshaller.class);

    /** System property defining message format to marshal to */
    private static final String JDBC_MARSHALLER_TYPE_PROPERTY = "citrus.ftp.marshaller.type";

    /** Message type format: XML or JSON */
    private String type;

    private final ObjectMapper mapper;
    private final Jaxb2Marshaller marshaller;

    private final Class<?>[] classesToBeBound = new Class<?>[] {Command.class,
            CommandResult.class,
            ConnectCommand.class,
            GetCommand.class,
            PutCommand.class,
            ListCommand.class,
            DeleteCommand.class,
            GetCommandResult.class,
            PutCommandResult.class,
            ListCommandResult.class,
            DeleteCommandResult.class};

    /**
     * Default constructor
     */
    public FtpMarshaller() {
        this.mapper = new ObjectMapper();
        this.marshaller = new Jaxb2Marshaller(new ClassPathResource("com/consol/citrus/schema/citrus-ftp-message.xsd"), classesToBeBound);

        type = System.getProperty(JDBC_MARSHALLER_TYPE_PROPERTY, MessageType.XML.name());

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
                            log.warn("Failed to read ftp JSON object from source: " + io.getMessage());
                            break;
                        }
                    }
                }

                throw new CitrusRuntimeException("Failed to read ftp XML object from source", e);
            }
        } else if (type.equalsIgnoreCase(MessageType.JSON.name())) {
            for (Class<?> type : classesToBeBound) {
                try {
                    return mapper.readValue(((StreamSource) source).getReader(), type);
                } catch (JsonParseException | JsonMappingException e2) {
                    continue;
                } catch (IOException io) {
                    throw new CitrusRuntimeException("Failed to read ftp JSON object from source", io);
                }
            }

            try {
                return marshaller.unmarshal(source);
            } catch (JAXBException me) {
                log.warn("Failed to read ftp XML object from source: " + me.getMessage());
            }

            throw new CitrusRuntimeException("Failed to read ftp JSON object from source" + source);
        } else {
            throw new CitrusRuntimeException("Unsupported ftp marshaller type: " + type);
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
                    throw new CitrusRuntimeException("Failed to write ftp JSON object graph to result", e);
                }
            }
        } else if (type.equalsIgnoreCase(MessageType.XML.name())) {
            try {
                marshaller.marshal(graph, result);
            } catch (JAXBException e) {
                throw new CitrusRuntimeException("Failed to write ftp XML object to result", e);
            }
        } else {
            throw new CitrusRuntimeException("Unsupported ftp marshaller type: " + type);
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
