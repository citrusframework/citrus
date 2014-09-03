/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.ws.message.converter;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.message.MessageHeaderUtils;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.client.WebServiceEndpointConfiguration;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;
import com.consol.citrus.ws.message.callback.SoapResponseMessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.transform.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Default converter implementation for SOAP messages. By default strips away the SOAP envelope and constructs internal message representation
 * from incoming SOAP request messages. Response messages are created from internal message representation accordingly.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SoapMessageConverter implements WebServiceMessageConverter {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SoapResponseMessageCallback.class);
    
    /** Optional SOAP attachment */
    private Attachment attachment;

    /** Should keep soap envelope when creating internal message */
    private boolean keepSoapEnvelope = false;

    @Override
    public WebServiceMessage convertOutbound(Message<?> internalMessage, WebServiceEndpointConfiguration endpointConfiguration) {
        WebServiceMessage message = endpointConfiguration.getMessageFactory().createWebServiceMessage();
        convertOutbound(message, internalMessage, endpointConfiguration);
        return message;
    }

    @Override
    public void convertOutbound(WebServiceMessage webServiceMessage, Message<?> message, WebServiceEndpointConfiguration endpointConfiguration) {
        SoapMessage soapRequest = ((SoapMessage)webServiceMessage);

        // Copy payload into soap-body:
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new StringSource(message.getPayload().toString()), soapRequest.getSoapBody().getPayloadResult());
        } catch (TransformerException e) {
            throw new CitrusRuntimeException("Failed to write SOAP body payload", e);
        }

        // Copy headers into soap-header:
        for (Entry<String, Object> headerEntry : message.getHeaders().entrySet()) {
            if (MessageHeaderUtils.isSpringInternalHeader(headerEntry.getKey())) {
                continue;
            }

            if (headerEntry.getKey().equalsIgnoreCase(CitrusSoapMessageHeaders.SOAP_ACTION)) {
                soapRequest.setSoapAction(headerEntry.getValue().toString());
            } else if (headerEntry.getKey().equalsIgnoreCase(CitrusMessageHeaders.HEADER_CONTENT)) {
                try {
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.transform(new StringSource(headerEntry.getValue().toString()),
                            soapRequest.getSoapHeader().getResult());
                } catch (TransformerException e) {
                    throw new CitrusRuntimeException("Failed to write SOAP header content", e);
                }
            } else if (headerEntry.getKey().toLowerCase().startsWith(CitrusSoapMessageHeaders.HTTP_PREFIX)) {
                handleOutboundMimeMessageHeader(soapRequest,
                        headerEntry.getKey().substring(CitrusSoapMessageHeaders.HTTP_PREFIX.length()),
                        headerEntry.getValue(),
                        endpointConfiguration.isHandleMimeHeaders());
            } else if (!headerEntry.getKey().startsWith(CitrusMessageHeaders.PREFIX)) {
                SoapHeaderElement headerElement;
                if (QNameUtils.validateQName(headerEntry.getKey())) {
                    headerElement = soapRequest.getSoapHeader().addHeaderElement(QNameUtils.parseQNameString(headerEntry.getKey()));
                } else {
                    headerElement = soapRequest.getSoapHeader().addHeaderElement(QNameUtils.createQName("", headerEntry.getKey(), ""));
                }

                headerElement.setText(headerEntry.getValue().toString());
            }
        }

        // Add attachment:
        if (attachment != null) {
            if (log.isDebugEnabled()) {
                log.debug("Adding attachment to SOAP message: '" + attachment.getContentId() + "' ('" + attachment.getContentType() + "')");
            }

            soapRequest.addAttachment(attachment.getContentId(), new InputStreamSource() {
                public InputStream getInputStream() throws IOException {
                    return attachment.getInputStream();
                }
            }, attachment.getContentType());
        }
    }

    @Override
    public Message<?> convertInbound(WebServiceMessage message, WebServiceEndpointConfiguration endpointConfiguration) {
        return convertInbound(message, null, endpointConfiguration);
    }

    @Override
    public Message<?> convertInbound(WebServiceMessage message, MessageContext messageContext, WebServiceEndpointConfiguration endpointConfiguration) {
        try {
            StringResult payloadResult = new StringResult();

            if (keepSoapEnvelope) {
                message.writeTo(payloadResult.getOutputStream());
            } else if (message.getPayloadSource() != null) {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.transform(message.getPayloadSource(), payloadResult);
            }

            MessageBuilder<String> messageBuilder = MessageBuilder.withPayload(payloadResult.toString());

            handleInboundMessageProperties(messageContext, messageBuilder);

            if (message instanceof SoapMessage) {
                handleInboundSoapMessage((SoapMessage) message, messageBuilder, endpointConfiguration);
            }

            handleInboundHttpHeaders(messageBuilder);

            return messageBuilder.build();
        } catch (TransformerException e) {
            throw new CitrusRuntimeException("Failed to read web service message payload source", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read web service message");
        }
    }

    /**
     * Method handles SOAP specific message information such as SOAP action headers and SOAP attachments.
     *
     * @param soapMessage
     * @param messageBuilder
     * @param endpointConfiguration
     */
    protected void handleInboundSoapMessage(SoapMessage soapMessage, MessageBuilder<String> messageBuilder, WebServiceEndpointConfiguration endpointConfiguration) {
        handleInboundSoapHeaders(soapMessage, messageBuilder);
        handleInboundAttachments(soapMessage, messageBuilder);

        if (endpointConfiguration.isHandleMimeHeaders()) {
            handleInboundMimeHeaders(soapMessage, messageBuilder);
        }
    }

    /**
     * Reads information from Http connection and adds them as Http marked headers to internal message representation.
     *
     * @param messageBuilder
     */
    protected void handleInboundHttpHeaders(MessageBuilder<String> messageBuilder) {
        TransportContext transportContext = TransportContextHolder.getTransportContext();
        if (transportContext == null) {
            log.warn("Unable to get complete set of http request headers - no transport context available");
            return;
        }

        WebServiceConnection connection = transportContext.getConnection();
        if (connection instanceof HttpServletConnection) {
            UrlPathHelper pathHelper = new UrlPathHelper();
            HttpServletConnection servletConnection = (HttpServletConnection) connection;
            messageBuilder.setHeader(CitrusSoapMessageHeaders.HTTP_REQUEST_URI, pathHelper.getRequestUri(servletConnection.getHttpServletRequest()));
            messageBuilder.setHeader(CitrusSoapMessageHeaders.HTTP_CONTEXT_PATH, pathHelper.getContextPath(servletConnection.getHttpServletRequest()));

            String queryParams = pathHelper.getOriginatingQueryString(servletConnection.getHttpServletRequest());
            messageBuilder.setHeader(CitrusSoapMessageHeaders.HTTP_QUERY_PARAMS, queryParams != null ? queryParams : "");

            messageBuilder.setHeader(CitrusSoapMessageHeaders.HTTP_REQUEST_METHOD, servletConnection.getHttpServletRequest().getMethod().toString());
        } else {
            log.warn("Unable to get complete set of http request headers");

            try {
                messageBuilder.setHeader(CitrusSoapMessageHeaders.HTTP_REQUEST_URI, connection.getUri());
            } catch (URISyntaxException e) {
                log.warn("Unable to get http request uri from http connection", e);
            }
        }
    }

    /**
     * Reads all soap headers from web service message and 
     * adds them to message builder as normal headers. Also takes care of soap action header.
     * 
     * @param soapMessage the web service message.
     * @param messageBuilder the response message builder.
     */
    protected void handleInboundSoapHeaders(SoapMessage soapMessage, MessageBuilder<?> messageBuilder) {
        try {
            SoapHeader soapHeader = soapMessage.getSoapHeader();

            if (soapHeader != null) {
                Iterator<?> iter = soapHeader.examineAllHeaderElements();
                while (iter.hasNext()) {
                    SoapHeaderElement headerEntry = (SoapHeaderElement) iter.next();
                    MessageHeaderUtils.setHeader(messageBuilder, headerEntry.getName().getLocalPart(), headerEntry.getText());
                }

                if (soapHeader.getSource() != null) {
                    StringResult headerData = new StringResult();
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.transform(soapHeader.getSource(), headerData);

                    messageBuilder.setHeader(CitrusMessageHeaders.HEADER_CONTENT, headerData.toString());
                }
            }

            if (StringUtils.hasText(soapMessage.getSoapAction())) {
                if (soapMessage.getSoapAction().equals("\"\"")) {
                    messageBuilder.setHeader(CitrusSoapMessageHeaders.SOAP_ACTION, "");
                } else {
                    if (soapMessage.getSoapAction().startsWith("\"") && soapMessage.getSoapAction().endsWith("\"")) {
                        messageBuilder.setHeader(CitrusSoapMessageHeaders.SOAP_ACTION,
                                soapMessage.getSoapAction().substring(1, soapMessage.getSoapAction().length()-1));
                    } else {
                        messageBuilder.setHeader(CitrusSoapMessageHeaders.SOAP_ACTION, soapMessage.getSoapAction());
                    }
                }
            }
        } catch (TransformerException e) {
            throw new CitrusRuntimeException("Failed to read SOAP header source", e);
        }
    }

    /**
     * Adds a HTTP message header to the SOAP message.
     *
     * @param message the SOAP request message.
     * @param name the header name.
     * @param value the header value.
     * @param handleMimeHeaders should handle mime headers.
     */
    private void handleOutboundMimeMessageHeader(SoapMessage message, String name, Object value, boolean handleMimeHeaders) {
        if (!handleMimeHeaders) {
            return;
        }

        if (message instanceof SaajSoapMessage) {
            SaajSoapMessage soapMsg = (SaajSoapMessage) message;
            MimeHeaders headers = soapMsg.getSaajMessage().getMimeHeaders();
            headers.setHeader(name, value.toString());
        } else if (message instanceof AxiomSoapMessage) {
            log.warn("Unable to set mime message header '" + name + "' on AxiomSoapMessage - unsupported");
        } else {
            log.warn("Unsupported SOAP message implementation - unable to set mime message header '" + name + "'");
        }
    }
    
    /**
     * Adds mime headers to constructed response message. This can be HTTP headers in case
     * of HTTP transport. Note: HTTP headers may have multiple values that are represented as 
     * comma delimited string value.
     * 
     * @param soapMessage the source SOAP message.
     * @param messageBuilder the message build constructing the result message.
     */
    protected void handleInboundMimeHeaders(SoapMessage soapMessage, MessageBuilder<String> messageBuilder) {
        Map<String, String> mimeHeaders = new HashMap<String, String>();
        MimeHeaders messageMimeHeaders = null;
        
        // to get access to mime headers we need to get implementation specific here
        if (soapMessage instanceof SaajSoapMessage) {
            messageMimeHeaders = ((SaajSoapMessage)soapMessage).getSaajMessage().getMimeHeaders();
        } else if (soapMessage instanceof AxiomSoapMessage) {
            // we do not handle axiom message implementations as it is very difficult to get access to the mime headers there
            log.warn("Skip mime headers for AxiomSoapMessage - unsupported");
        } else {
            log.warn("Unsupported SOAP message implementation - skipping mime headers");
        }
        
        if (messageMimeHeaders != null) {
            Iterator<?> mimeHeaderIterator = messageMimeHeaders.getAllHeaders();
            while (mimeHeaderIterator.hasNext()) {
                MimeHeader mimeHeader = (MimeHeader)mimeHeaderIterator.next();
                // http headers can have multpile values so headers might occur several times in map
                if (mimeHeaders.containsKey(mimeHeader.getName())) {
                    // header is already present, so concat values to a single comma delimited string
                    String value = mimeHeaders.get(mimeHeader.getName());
                    value += ", " + mimeHeader.getValue();
                    mimeHeaders.put(mimeHeader.getName(), value);
                } else {
                    mimeHeaders.put(mimeHeader.getName(), mimeHeader.getValue());
                }
            }
            
            for (Entry<String, String> httpHeaderEntry : mimeHeaders.entrySet()) {
                messageBuilder.setHeader(httpHeaderEntry.getKey(), httpHeaderEntry.getValue());
            }
        }
    }
    
    /**
     * Adds all message properties from web service message to message builder 
     * as normal header entries.
     * 
     * @param messageContext the web service request message context.
     * @param messageBuilder the request message builder.
     */
    protected void handleInboundMessageProperties(MessageContext messageContext, MessageBuilder<String> messageBuilder) {
        if (messageContext == null) { return; }
        
        String[] propertyNames = messageContext.getPropertyNames();
        if (propertyNames != null) {
            for (String propertyName : propertyNames) {
                messageBuilder.setHeader(propertyName, messageContext.getProperty(propertyName));
            }
        }
    }
    
    /**
     * Adds attachments if present in soap web service message.
     * 
     * @param soapMessage the web service message.
     * @param messageBuilder the response message builder.
     * @throws IOException 
     */
    protected void handleInboundAttachments(SoapMessage soapMessage, MessageBuilder<String> messageBuilder) {
        try {
            Iterator<?> attachments = soapMessage.getAttachments();

            while (attachments.hasNext()) {
                Attachment attachment = (Attachment)attachments.next();

                if (StringUtils.hasText(attachment.getContentId())) {
                    String contentId = attachment.getContentId();

                    if (contentId.startsWith("<")) {contentId = contentId.substring(1);}
                    if (contentId.endsWith(">")) {contentId = contentId.substring(0, contentId.length()-1);}

                    if (log.isDebugEnabled()) {
                        log.debug("SOAP message contains attachment with contentId '" + contentId + "'");
                    }

                    messageBuilder.setHeader(contentId, attachment);
                    messageBuilder.setHeader(CitrusSoapMessageHeaders.CONTENT_ID, contentId);
                    messageBuilder.setHeader(CitrusSoapMessageHeaders.CONTENT_TYPE, attachment.getContentType());
                    messageBuilder.setHeader(CitrusSoapMessageHeaders.CONTENT, FileUtils.readToString(attachment.getInputStream()).trim());
                    messageBuilder.setHeader(CitrusSoapMessageHeaders.CHARSET_NAME, "UTF-8"); // TODO map this dynamically
                } else {
                    log.warn("Could not handle SOAP attachment with empty 'contentId'. Attachment is ignored in further processing");
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read SOAP attachment content", e);
        }
    }

    /**
     * Gets the keep soap envelope flag.
     * @return
     */
    public boolean isKeepSoapEnvelope() {
        return keepSoapEnvelope;
    }

    /**
     * Sets the keep soap header flag.
     * @param keepSoapEnvelope
     */
    public void setKeepSoapEnvelope(boolean keepSoapEnvelope) {
        this.keepSoapEnvelope = keepSoapEnvelope;
    }

    @Override
    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

}
