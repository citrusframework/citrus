/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.jms.message;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jms.endpoint.JmsEndpointConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import javax.jms.Message;
import javax.jms.Session;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SoapJmsMessageConverter extends JmsMessageConverter {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SoapJmsMessageConverter.class);

    @Autowired
    private SoapMessageFactory soapMessageFactory;

    /** Message transformer */
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();

    @Override
    public org.springframework.messaging.Message<?> convertInbound(Message jmsMessage, JmsEndpointConfiguration endpointConfiguration) {

        try {
            org.springframework.messaging.Message<?> message = super.convertInbound(jmsMessage, endpointConfiguration);
            ByteArrayInputStream in = new ByteArrayInputStream(message.getPayload().toString().getBytes(getDefaultCharset()));
            SoapMessage soapMessage = soapMessageFactory.createWebServiceMessage(in);

            StringResult payload = new StringResult();
            transformerFactory.newTransformer().transform(soapMessage.getPayloadSource(), payload);

            return MessageBuilder.withPayload(payload.toString()).copyHeaders(message.getHeaders()).build();
        } catch(TransformerException e) {
            throw new CitrusRuntimeException("Failed to transform SOAP message body to payload", e);
        } catch (UnsupportedEncodingException e) {
            throw new CitrusRuntimeException("Found unsupported default encoding", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read SOAP message payload", e);
        }
    }

    @Override
    public Message createJmsMessage(org.springframework.messaging.Message<?> message, Session session, JmsEndpointConfiguration endpointConfiguration) {
        String payload = message.getPayload().toString();

        log.debug("Creating SOAP message from payload: " + payload);

        try {
            SoapMessage soapMessage = soapMessageFactory.createWebServiceMessage();
            transformerFactory.newTransformer().transform(new StringSource(payload), soapMessage.getPayloadResult());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            soapMessage.writeTo(bos);

            return super.createJmsMessage(MessageBuilder.withPayload(new String(bos.toByteArray())).copyHeaders(message.getHeaders()).build(), session, endpointConfiguration);
        } catch (TransformerException e) {
            throw new CitrusRuntimeException("Failed to transform payload to SOAP body", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to write SOAP message content", e);
        }
    }

    /**
     * Either gets Citrus default encoding or system default encoding.
     * @return
     */
    private String getDefaultCharset() {
        return System.getProperty(CitrusConstants.CITRUS_FILE_ENCODING, Charset.defaultCharset().displayName());
    }
}
