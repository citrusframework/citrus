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

package org.citrusframework.jms.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import jakarta.jms.Message;
import jakarta.jms.Session;
import org.citrusframework.CitrusSettings;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jms.endpoint.JmsEndpointConfiguration;
import org.citrusframework.message.MessageHeaderUtils;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;
import org.citrusframework.xml.StringResult;
import org.citrusframework.xml.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;

/**
 * Special message converter automatically adds SOAP envelope with proper SOAP header and body elements.
 * For incoming messages automatically removes SOAP envelope so message only contains SOAP body as message payload.
 *
 * Converter also takes care on special SOAP message headers such as SOAP action.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SoapJmsMessageConverter extends JmsMessageConverter implements InitializingPhase, ReferenceResolverAware {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SoapJmsMessageConverter.class);

    /** Soap message factory - either set explicitly or autoconfigured through application context */
    private SoapMessageFactory soapMessageFactory;

    /** Reference resolver used for autoconfiguration of soap message factory */
    private ReferenceResolver referenceResolver;

    /** Message transformer */
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    /** Special SOAP action header */
    private static final String SOAP_ACTION_HEADER = MessageHeaders.PREFIX + "soap_action";

    /** The JMS SOAP action header name */
    private String jmsSoapActionHeader = "SOAPJMS_soapAction";

    @Override
    public org.citrusframework.message.Message convertInbound(Message jmsMessage, JmsEndpointConfiguration endpointConfiguration, TestContext context) {
        try {
            org.citrusframework.message.Message message = super.convertInbound(jmsMessage, endpointConfiguration, context);
            ByteArrayInputStream in = new ByteArrayInputStream(message.getPayload(String.class).getBytes(CitrusSettings.CITRUS_FILE_ENCODING));
            SoapMessage soapMessage = soapMessageFactory.createWebServiceMessage(in);

            StringResult payload = new StringResult();
            transformerFactory.newTransformer().transform(soapMessage.getPayloadSource(), payload);

            // Translate SOAP action header if present
            if (message.getHeader(jmsSoapActionHeader) != null) {
                message.setHeader(SOAP_ACTION_HEADER, message.getHeader(jmsSoapActionHeader));
            }

            SoapHeader soapHeader = soapMessage.getSoapHeader();
            if (soapHeader != null) {
                Iterator<?> iter = soapHeader.examineAllHeaderElements();
                while (iter.hasNext()) {
                    SoapHeaderElement headerEntry = (SoapHeaderElement) iter.next();
                    MessageHeaderUtils.setHeader(message, headerEntry.getName().getLocalPart(), headerEntry.getText());
                }

                if (soapHeader.getSource() != null) {
                    StringResult headerData = new StringResult();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.transform(soapHeader.getSource(), headerData);

                    message.addHeaderData(headerData.toString());
                }
            }

            message.setPayload(payload.toString());
            return message;
        } catch(TransformerException e) {
            throw new CitrusRuntimeException("Failed to transform SOAP message body to payload", e);
        } catch (UnsupportedEncodingException e) {
            throw new CitrusRuntimeException("Found unsupported default encoding", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read SOAP message payload", e);
        }
    }

    @Override
    public Message createJmsMessage(org.citrusframework.message.Message message, Session session, JmsEndpointConfiguration endpointConfiguration, TestContext context) {
        String payload = message.getPayload(String.class);

        logger.debug("Creating SOAP message from payload: " + payload);

        try {
            SoapMessage soapMessage = soapMessageFactory.createWebServiceMessage();
            transformerFactory.newTransformer().transform(new StringSource(payload), soapMessage.getPayloadResult());

            // Translate SOAP action header if present
            if (message.getHeader(SOAP_ACTION_HEADER) != null) {
                message.setHeader(jmsSoapActionHeader, message.getHeader(SOAP_ACTION_HEADER));
                message.removeHeader(SOAP_ACTION_HEADER);
            }

            for (String headerData : message.getHeaderData()) {
                try {
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    transformer.transform(new StringSource(headerData),
                            soapMessage.getSoapHeader().getResult());
                } catch (TransformerException e) {
                    throw new CitrusRuntimeException("Failed to write SOAP header content", e);
                }
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            soapMessage.writeTo(bos);

            message.setPayload(new String(bos.toByteArray()));

            return super.createJmsMessage(message, session, endpointConfiguration, context);
        } catch (TransformerException e) {
            throw new CitrusRuntimeException("Failed to transform payload to SOAP body", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to write SOAP message content", e);
        }
    }

    /**
     * Sets the jmsSoapActionHeader property.
     *
     * @param jmsSoapActionHeader
     */
    public void setJmsSoapActionHeader(String jmsSoapActionHeader) {
        this.jmsSoapActionHeader = jmsSoapActionHeader;
    }

    /**
     * Gets the value of the jmsSoapActionHeader property.
     *
     * @return the jmsSoapActionHeader
     */
    public String getJmsSoapActionHeader() {
        return jmsSoapActionHeader;
    }

    @Override
    public void initialize() {
        if (soapMessageFactory == null) {
            ObjectHelper.assertNotNull(referenceResolver, "Missing reference resolver for auto configuration of soap message factory");
            soapMessageFactory = referenceResolver.resolve(SoapMessageFactory.class);
        }
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Gets the soapMessageFactory.
     *
     * @return
     */
    public SoapMessageFactory getSoapMessageFactory() {
        return soapMessageFactory;
    }

    /**
     * Sets the soapMessageFactory.
     *
     * @param soapMessageFactory
     */
    public void setSoapMessageFactory(SoapMessageFactory soapMessageFactory) {
        this.soapMessageFactory = soapMessageFactory;
    }
}
