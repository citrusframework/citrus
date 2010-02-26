/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.samples.common.xml.validation;

import java.io.IOException;

import javax.xml.XMLConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;
import org.springframework.integration.core.*;
import org.springframework.integration.xml.DefaultXmlPayloadConverter;
import org.springframework.integration.xml.XmlPayloadConverter;
import org.springframework.xml.validation.*;
import org.xml.sax.SAXParseException;

import com.consol.citrus.samples.common.exceptions.XmlSchemaValidationException;

/**
 * Channel interceptor validating incoming messages with a given XSD Schema resource. In case
 * of validation errors the interceptor raises a {@link XmlValidationException}. The exception can be handled
 * by an exception resolver for transforming into proper SOAP faults for example. 
 * 
 * @author Christoph Deppisch
 */
public class XmlSchemaValidatingChannelInterceptor extends ChannelInterceptorAdapter {
    /** XML validator */
    private XmlValidator xmlValidator;

    /** Payload converter */
    private XmlPayloadConverter converter = new DefaultXmlPayloadConverter();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(XmlSchemaValidatingChannelInterceptor.class);
    
    /**
     * Default constructor using fields.
     * @param schemaResource
     * @throws IOException
     */
    public XmlSchemaValidatingChannelInterceptor(Resource schemaResource) throws IOException {
        this(schemaResource, "xml-schema");
    }

    /**
     * Constructor.
     * 
     * @param schemaResource
     * @param schemaLanguage
     * @throws IOException
     */
    public XmlSchemaValidatingChannelInterceptor(Resource schemaResource, String schemaLanguage) throws IOException {
        if (schemaLanguage.equals("xml-schema")) {
            this.xmlValidator = XmlValidatorFactory.createValidator(schemaResource, XMLConstants.W3C_XML_SCHEMA_NS_URI);
        } else {
            this.xmlValidator = XmlValidatorFactory.createValidator(schemaResource, XMLConstants.RELAXNG_NS_URI);
        }
    }

    /**
     * @see org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter#preSend(org.springframework.integration.core.Message,
     *      org.springframework.integration.core.MessageChannel)
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        validateSchema(message, channel);
        log.debug("XSD schema validation successful");
        return super.preSend(message, channel);
    }

    /**
     * Validates the payload of the message
     * 
     * @param message
     * @param channel
     */
    public void validateSchema(Message<?> message, MessageChannel channel) {
        try {
            SAXParseException[] exceptions = xmlValidator.validate(converter.convertToSource(message.getPayload()));
            if (exceptions.length > 0) {
                StringBuilder msg = new StringBuilder("Invalid XML message on channel ");
                if (channel != null) {
                    msg.append(channel.getName());
                } else {
                    msg.append("<unknown>");
                }
                msg.append(":\n");
                for (SAXParseException e : exceptions) {
                    msg.append("\t").append(e.getMessage());
                    msg.append(" (line=").append(e.getLineNumber());
                    msg.append(", col=").append(e.getColumnNumber()).append(")\n");
                }
                log.warn("XSD schema validation failed: ", msg.toString());
                throw new XmlSchemaValidationException(message, exceptions[0]);
            }
        } catch (IOException ioE) {
            throw new MessagingException("Exception applying schema validation", ioE);
        }
    }
}
