/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.ws.interceptor;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.message.RawMessage;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract logging support class offers basic logger methods for SOAP messages.
 *
 * @author Christoph Deppisch
 */
public abstract class LoggingInterceptorSupport extends TransformerObjectSupport {

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MessageListeners messageListener;

    private final TestContextFactory testContextFactory = TestContextFactory.newInstance();

    /**
     * Prevent instantiation.
     */
    protected LoggingInterceptorSupport() {
    }

    /**
     * Logs request message from message context. SOAP messages get logged with envelope transformation
     * other messages with serialization.
     *
     * @param logMessage
     * @param messageContext
     * @param incoming
     * @throws TransformerException
     */
    protected void logRequest(String logMessage, MessageContext messageContext, boolean incoming) throws TransformerException {
        if (messageContext.getRequest() instanceof SoapMessage) {
            logSoapMessage(logMessage, (SoapMessage) messageContext.getRequest(), incoming);
        } else {
            logWebServiceMessage(logMessage, messageContext.getRequest(), incoming);
        }
    }

    /**
     * Logs response message from message context if any. SOAP messages get logged with envelope transformation
     * other messages with serialization.
     *
     * @param logMessage
     * @param messageContext
     * @param incoming
     * @throws TransformerException
     */
    protected void logResponse(String logMessage, MessageContext messageContext, boolean incoming) throws TransformerException {
        if (messageContext.hasResponse()) {
            if (messageContext.getResponse() instanceof SoapMessage) {
                logSoapMessage(logMessage, (SoapMessage) messageContext.getResponse(), incoming);
            } else {
                logWebServiceMessage(logMessage, messageContext.getResponse(), incoming);
            }
        }
    }

    /**
     * Log SOAP message with transformer instance.
     *
     * @param logMessage the customized logger message.
     * @param soapMessage the message content as SOAP envelope source.
     * @param incoming
     * @throws TransformerException
     */
    protected void logSoapMessage(String logMessage, SoapMessage soapMessage, boolean incoming) throws TransformerException {
        Transformer transformer = createIndentingTransformer();
        StringWriter writer = new StringWriter();

        transformer.transform(soapMessage.getEnvelope().getSource(), new StreamResult(writer));
        logMessage(logMessage, XMLUtils.prettyPrint(writer.toString()), incoming);
    }

    /**
     * Log WebService message (other than SOAP) with in memory
     * {@link ByteArrayOutputStream}
     *
     * @param logMessage the customized logger message.
     * @param message the message to logger.
     * @param incoming
     */
    protected void logWebServiceMessage(String logMessage, WebServiceMessage message, boolean incoming) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            message.writeTo(os);
            logMessage(logMessage, os.toString(), incoming);
        } catch (IOException e) {
            logger.warn("Unable to logger WebService message", e);
        }
    }

    /**
     * Performs the final logger call with dynamic message.
     *
     * @param logMessage a custom logger message entry.
     * @param message the message content.
     * @param incoming
     */
    protected void logMessage(String logMessage, String message, boolean incoming) {
        if (hasMessageListeners()) {
            logger.debug(logMessage);

            if (incoming) {
                messageListener.onInboundMessage(new RawMessage(message), testContextFactory.getObject());
            } else {
                messageListener.onOutboundMessage(new RawMessage(message), testContextFactory.getObject());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(logMessage + ":" + System.getProperty("line.separator") + message);
            }
        }
    }

    /**
     * Checks if message listeners are present on this interceptor.
     * @return
     */
    public boolean hasMessageListeners() {
        return messageListener != null && !messageListener.isEmpty();
    }

    /**
     * Get transformer implementation with output properties set.
     *
     * @return the transformer instance.
     * @throws TransformerConfigurationException
     */
    private Transformer createIndentingTransformer() throws TransformerConfigurationException {
        Transformer transformer = createTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        return transformer;
    }

    /**
     * Sets the message listener.
     * @param messageListener
     */
    public void setMessageListener(MessageListeners messageListener) {
        this.messageListener = messageListener;
    }
}
