/*
 * Copyright the original author or authors.
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

package org.citrusframework.ftp.message;

import java.io.IOException;
import java.io.StringWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.ftp.FtpSettings;
import org.citrusframework.ftp.model.Command;
import org.citrusframework.ftp.model.CommandResult;
import org.citrusframework.ftp.model.ConnectCommand;
import org.citrusframework.ftp.model.DeleteCommand;
import org.citrusframework.ftp.model.DeleteCommandResult;
import org.citrusframework.ftp.model.GetCommand;
import org.citrusframework.ftp.model.GetCommandResult;
import org.citrusframework.ftp.model.ListCommand;
import org.citrusframework.ftp.model.ListCommandResult;
import org.citrusframework.ftp.model.PutCommand;
import org.citrusframework.ftp.model.PutCommandResult;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resources;
import org.citrusframework.xml.Jaxb2Marshaller;
import org.citrusframework.xml.Marshaller;
import org.citrusframework.xml.StringResult;
import org.citrusframework.xml.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 2.7.5
 */
public class FtpMarshaller implements Marshaller, Unmarshaller {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(FtpMarshaller.class);

    /** Message type format: XML or JSON */
    private String type = FtpSettings.getMarshallerType();

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
        this.marshaller = new Jaxb2Marshaller(Resources.fromClasspath("org/citrusframework/schema/citrus-ftp-message.xsd"), classesToBeBound);
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
                            logger.warn("Failed to read ftp JSON object from source: " + io.getMessage());
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
                    // do nothing
                } catch (IOException io) {
                    throw new CitrusRuntimeException("Failed to read ftp JSON object from source", io);
                }
            }

            try {
                return marshaller.unmarshal(source);
            } catch (JAXBException me) {
                logger.warn("Failed to read ftp XML object from source: " + me.getMessage());
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
