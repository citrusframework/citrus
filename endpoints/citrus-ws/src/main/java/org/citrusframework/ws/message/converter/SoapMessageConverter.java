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

package org.citrusframework.ws.message.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.soap.MimeHeader;
import jakarta.xml.soap.MimeHeaders;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaderUtils;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.citrusframework.xml.StringResult;
import org.citrusframework.xml.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;
import org.springframework.xml.namespace.QNameUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Default converter implementation for SOAP messages. By default strips away the SOAP envelope and constructs internal message representation
 * from incoming SOAP request messages. Response messages are created from internal message representation accordingly.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SoapMessageConverter implements WebServiceMessageConverter {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SoapMessageConverter.class);

    /** Default payload source encoding */
    private String charset = CitrusSettings.CITRUS_FILE_ENCODING;

    @Override
    public WebServiceMessage convertOutbound(final Message internalMessage,
                                             final WebServiceEndpointConfiguration endpointConfiguration,
                                             final TestContext context) {
        final WebServiceMessage message = endpointConfiguration.getMessageFactory().createWebServiceMessage();
        convertOutbound(message, internalMessage, endpointConfiguration, context);
        return message;
    }

    @Override
    public void convertOutbound(final WebServiceMessage webServiceMessage,
                                final Message message,
                                final WebServiceEndpointConfiguration endpointConfiguration,
                                final TestContext context) {
        final org.springframework.ws.soap.SoapMessage soapRequest = ((org.springframework.ws.soap.SoapMessage)webServiceMessage);

        final SoapMessage soapMessage = convertMessageToSoapMessage(message);

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        copySoapPayload(soapRequest, soapMessage, transformerFactory);
        copySoapHeaders(endpointConfiguration, soapRequest, soapMessage);
        copySoapHeaderData(soapRequest, soapMessage, transformerFactory);

        if (soapMessage.isMtomEnabled() && soapMessage.getAttachments().size() > 0) {
            logger.debug("Converting SOAP request to XOP package");
            soapRequest.convertToXopPackage();
        }

        copySoapAttachments(context, soapRequest, soapMessage);
    }

    @Override
    public SoapMessage convertInbound(final WebServiceMessage message,
                                      final WebServiceEndpointConfiguration endpointConfiguration,
                                      final TestContext context) {
        return convertInbound(message, null, endpointConfiguration);
    }

    @Override
    public SoapMessage convertInbound(final WebServiceMessage webServiceMessage,
                                      final MessageContext messageContext,
                                      final WebServiceEndpointConfiguration endpointConfiguration) {
        try {
            String payload = "";

            if (endpointConfiguration.isKeepSoapEnvelope()) {
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                webServiceMessage.writeTo(bos);
                payload = bos.toString(charset);
            } else if (webServiceMessage.getPayloadSource() != null) {
                final StringResult payloadResult = new StringResult();

                final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                final Transformer transformer = transformerFactory.newTransformer();
                transformer.transform(webServiceMessage.getPayloadSource(), payloadResult);

                payload = payloadResult.toString();
            }

            final SoapMessage message = new SoapMessage(payload);

            handleInboundMessageProperties(messageContext, message);

            if (webServiceMessage instanceof org.springframework.ws.soap.SoapMessage) {
                handleInboundSoapMessage((org.springframework.ws.soap.SoapMessage) webServiceMessage, message, endpointConfiguration);
            }

            handleInboundHttpHeaders(message, endpointConfiguration);

            return message;
        } catch (final TransformerException e) {
            throw new CitrusRuntimeException("Failed to read web service message payload source", e);
        } catch (final IOException e) {
            throw new CitrusRuntimeException("Failed to read web service message", e);
        }
    }

    /**
     * Method handles SOAP specific message information such as SOAP action headers and SOAP attachments.
     *
     * @param soapMessage
     * @param message
     * @param endpointConfiguration
     */
    protected void handleInboundSoapMessage(final org.springframework.ws.soap.SoapMessage soapMessage,
                                            final SoapMessage message,
                                            final WebServiceEndpointConfiguration endpointConfiguration) {
        handleInboundNamespaces(soapMessage, message);
        handleInboundSoapHeaders(soapMessage, message);
        handleInboundAttachments(soapMessage, message);

        if (endpointConfiguration.isHandleMimeHeaders()) {
            handleInboundMimeHeaders(soapMessage, message);
        }
    }

    private void handleInboundNamespaces(final org.springframework.ws.soap.SoapMessage soapMessage,
                                         final SoapMessage message) {
        final Source envelopeSource = soapMessage.getEnvelope().getSource();
        if (envelopeSource != null && envelopeSource instanceof DOMSource) {
            final Node envelopeNode = ((DOMSource) envelopeSource).getNode();
            final NamedNodeMap attributes = envelopeNode.getAttributes();

            for (int i = 0; i < attributes.getLength(); i++) {
                final Node attribute = attributes.item(i);
                if (StringUtils.hasText(attribute.getNamespaceURI()) && attribute.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")) {
                    if (StringUtils.hasText(attribute.getNodeValue()) && !attribute.getNodeValue().equals(envelopeNode.getNamespaceURI())) {
                        final String messagePayload = message.getPayload(String.class);
                        if (StringUtils.hasText(messagePayload)) {
                            int xmlProcessingInstruction = messagePayload.indexOf("?>");
                            xmlProcessingInstruction = xmlProcessingInstruction > 0 ? (xmlProcessingInstruction + 2) : 0;
                            int rootElementEnd = messagePayload.indexOf('>', xmlProcessingInstruction);

                            if (rootElementEnd > 0) {
                                if (messagePayload.charAt(rootElementEnd - 1) == '/') {
                                    // root element is closed immediately e.g. <root/> need to adjust root element end
                                    rootElementEnd--;
                                }

                                final String namespace = attribute.getNodeName() + "=\"" + attribute.getNodeValue() + "\"";
                                if (!messagePayload.contains(namespace)) {
                                    message.setPayload(messagePayload.substring(0, rootElementEnd) + " " + namespace + messagePayload.substring(rootElementEnd));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Reads information from Http connection and adds them as Http marked headers to internal message representation.
     *
     * @param message
     */
    protected void handleInboundHttpHeaders(final SoapMessage message,
                                            final WebServiceEndpointConfiguration endpointConfiguration) {
        final TransportContext transportContext = TransportContextHolder.getTransportContext();
        if (transportContext == null) {
            logger.warn("Unable to get complete set of http request headers - no transport context available");
            return;
        }

        final WebServiceConnection connection = transportContext.getConnection();
        if (connection instanceof HttpServletConnection) {
            final UrlPathHelper pathHelper = new UrlPathHelper();
            final HttpServletConnection servletConnection = (HttpServletConnection) connection;
            final HttpServletRequest httpServletRequest = servletConnection.getHttpServletRequest();
            message.setHeader(SoapMessageHeaders.HTTP_REQUEST_URI, pathHelper.getRequestUri(httpServletRequest));
            message.setHeader(SoapMessageHeaders.HTTP_CONTEXT_PATH, pathHelper.getContextPath(httpServletRequest));

            final String queryParams = pathHelper.getOriginatingQueryString(httpServletRequest);
            message.setHeader(SoapMessageHeaders.HTTP_QUERY_PARAMS, queryParams != null ? queryParams : "");

            message.setHeader(SoapMessageHeaders.HTTP_REQUEST_METHOD, httpServletRequest.getMethod());

            if (endpointConfiguration.isHandleAttributeHeaders()) {
                final Enumeration<String> attributeNames = httpServletRequest.getAttributeNames();
                while (attributeNames.hasMoreElements()) {
                    final String attributeName = attributeNames.nextElement();
                    final Object attribute = httpServletRequest.getAttribute(attributeName);
                    message.setHeader(attributeName, attribute);
                }
            }
        } else {
            logger.warn("Unable to get complete set of http request headers");

            try {
                message.setHeader(SoapMessageHeaders.HTTP_REQUEST_URI, connection.getUri());
            } catch (final URISyntaxException e) {
                logger.warn("Unable to get http request uri from http connection", e);
            }
        }
    }

    /**
     * Reads all soap headers from web service message and
     * adds them to message builder as normal headers. Also takes care of soap action header.
     *
     * @param soapMessage the web service message.
     * @param message the response message builder.
     */
    protected void handleInboundSoapHeaders(final org.springframework.ws.soap.SoapMessage soapMessage,
                                            final SoapMessage message) {
        try {
            final SoapHeader soapHeader = soapMessage.getSoapHeader();

            if (soapHeader != null) {
                final Iterator<?> iter = soapHeader.examineAllHeaderElements();
                while (iter.hasNext()) {
                    final SoapHeaderElement headerEntry = (SoapHeaderElement) iter.next();
                    MessageHeaderUtils.setHeader(message, headerEntry.getName().getLocalPart(), headerEntry.getText());
                }

                if (soapHeader.getSource() != null) {
                    final StringResult headerData = new StringResult();
                    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    final Transformer transformer = transformerFactory.newTransformer();
                    transformer.transform(soapHeader.getSource(), headerData);

                    message.addHeaderData(headerData.toString());
                }
            }

            if (StringUtils.hasText(soapMessage.getSoapAction())) {
                if (soapMessage.getSoapAction().equals("\"\"")) {
                    message.setHeader(SoapMessageHeaders.SOAP_ACTION, "");
                } else {
                    if (soapMessage.getSoapAction().startsWith("\"") && soapMessage.getSoapAction().endsWith("\"")) {
                        message.setHeader(SoapMessageHeaders.SOAP_ACTION,
                                soapMessage.getSoapAction().substring(1, soapMessage.getSoapAction().length() - 1));
                    } else {
                        message.setHeader(SoapMessageHeaders.SOAP_ACTION, soapMessage.getSoapAction());
                    }
                }
            }
        } catch (final TransformerException e) {
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
    private void handleOutboundMimeMessageHeader(final org.springframework.ws.soap.SoapMessage message,
                                                 final String name,
                                                 final Object value,
                                                 final boolean handleMimeHeaders) {
        if (!handleMimeHeaders) {
            return;
        }

        if (message instanceof SaajSoapMessage) {
            final SaajSoapMessage soapMsg = (SaajSoapMessage) message;
            final MimeHeaders headers = soapMsg.getSaajMessage().getMimeHeaders();
            headers.setHeader(name, value.toString());
        } else {
            logger.warn("Unsupported SOAP message implementation - unable to set mime message header '" + name + "'");
        }
    }

    /**
     * Adds mime headers to constructed response message. This can be HTTP headers in case
     * of HTTP transport. Note: HTTP headers may have multiple values that are represented as
     * comma delimited string value.
     *
     * @param soapMessage the source SOAP message.
     * @param message the message build constructing the result message.
     */
    protected void handleInboundMimeHeaders(final org.springframework.ws.soap.SoapMessage soapMessage,
                                            final SoapMessage message) {
        final Map<String, String> mimeHeaders = new HashMap<String, String>();
        MimeHeaders messageMimeHeaders = null;

        // to get access to mime headers we need to get implementation specific here
        if (soapMessage instanceof SaajSoapMessage) {
            messageMimeHeaders = ((SaajSoapMessage)soapMessage).getSaajMessage().getMimeHeaders();
        } else {
            logger.warn("Unsupported SOAP message implementation - skipping mime headers");
        }

        if (messageMimeHeaders != null) {
            final Iterator<?> mimeHeaderIterator = messageMimeHeaders.getAllHeaders();
            while (mimeHeaderIterator.hasNext()) {
                final MimeHeader mimeHeader = (MimeHeader)mimeHeaderIterator.next();
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

            for (final Entry<String, String> httpHeaderEntry : mimeHeaders.entrySet()) {
                message.setHeader(httpHeaderEntry.getKey(), httpHeaderEntry.getValue());
            }
        }
    }

    /**
     * Adds all message properties from web service message to message builder
     * as normal header entries.
     *
     * @param messageContext the web service request message context.
     * @param message the request message builder.
     */
    protected void handleInboundMessageProperties(final MessageContext messageContext,
                                                  final SoapMessage message) {
        if (messageContext == null) { return; }

        final String[] propertyNames = messageContext.getPropertyNames();
        if (propertyNames != null) {
            for (final String propertyName : propertyNames) {
                message.setHeader(propertyName, messageContext.getProperty(propertyName));
            }
        }
    }

    /**
     * Adds attachments if present in soap web service message.
     *
     * @param soapMessage the web service message.
     * @param message the response message builder.
     */
    protected void handleInboundAttachments(final org.springframework.ws.soap.SoapMessage soapMessage,
                                            final SoapMessage message) {
        final Iterator<Attachment> attachments = soapMessage.getAttachments();

        while (attachments.hasNext()) {
            final Attachment attachment = attachments.next();
            final SoapAttachment soapAttachment = SoapAttachment.from(attachment);

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("SOAP message contains attachment with contentId '%s'", soapAttachment.getContentId()));
            }

            message.addAttachment(soapAttachment);
        }
    }

    private SoapMessage convertMessageToSoapMessage(final Message message) {
        final SoapMessage soapMessage;
        if (message instanceof SoapMessage) {
            soapMessage = (SoapMessage) message;
        } else {
            soapMessage = new SoapMessage(message);
        }
        return soapMessage;
    }

    private void copySoapHeaders(final WebServiceEndpointConfiguration endpointConfiguration,
                                 final org.springframework.ws.soap.SoapMessage soapRequest,
                                 final SoapMessage soapMessage) {
        for (final Entry<String, Object> headerEntry : soapMessage.getHeaders().entrySet()) {
            if (MessageHeaderUtils.isSpringInternalHeader(headerEntry.getKey())) {
                continue;
            }

            if (headerEntry.getKey().equalsIgnoreCase(SoapMessageHeaders.SOAP_ACTION)) {
                soapRequest.setSoapAction(headerEntry.getValue().toString());
            } else if (headerEntry.getKey().toLowerCase().startsWith(SoapMessageHeaders.HTTP_PREFIX)) {
                handleOutboundMimeMessageHeader(soapRequest,
                        headerEntry.getKey().substring(SoapMessageHeaders.HTTP_PREFIX.length()),
                        headerEntry.getValue(),
                        endpointConfiguration.isHandleMimeHeaders());
            } else if (!headerEntry.getKey().startsWith(MessageHeaders.PREFIX)) {
                final SoapHeaderElement headerElement;
                if (QNameUtils.validateQName(headerEntry.getKey())) {
                    headerElement = soapRequest.getSoapHeader().addHeaderElement(QNameUtils.parseQNameString(headerEntry.getKey()));
                } else {
                    headerElement = soapRequest.getSoapHeader().addHeaderElement(new QName("", headerEntry.getKey(), ""));
                }

                headerElement.setText(headerEntry.getValue().toString());
            }
        }
    }

    private void copySoapHeaderData(final org.springframework.ws.soap.SoapMessage soapRequest,
                                    final SoapMessage soapMessage,
                                    final TransformerFactory transformerFactory) {
        for (final String headerData : soapMessage.getHeaderData()) {
            try {
                final Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.transform(new StringSource(headerData),
                        soapRequest.getSoapHeader().getResult());
            } catch (final TransformerException e) {
                throw new CitrusRuntimeException("Failed to write SOAP header content", e);
            }
        }
    }

    private void copySoapPayload(final org.springframework.ws.soap.SoapMessage soapRequest, final SoapMessage soapMessage, final TransformerFactory transformerFactory) {
        final String payload = soapMessage.getPayload(String.class);
        if (StringUtils.hasText(payload)) {
            try {
                final Transformer transformer = transformerFactory.newTransformer();
                transformer.transform(new StringSource(payload), soapRequest.getSoapBody().getPayloadResult());
            } catch (final TransformerException e) {
                throw new CitrusRuntimeException("Failed to write SOAP body payload", e);
            }
        }
    }
    private void copySoapAttachments(final TestContext context,
                                     final org.springframework.ws.soap.SoapMessage soapRequest,
                                     final SoapMessage soapMessage) {
        for (final Attachment attachment : soapMessage.getAttachments()) {
            String contentId = context.replaceDynamicContentInString(attachment.getContentId());
            if (!contentId.startsWith("<")) {
                contentId = "<" + contentId + ">";
            }

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Adding attachment to SOAP message: '%s' ('%s')", contentId, attachment.getContentType()));
            }

            soapRequest.addAttachment(contentId, attachment::getInputStream, attachment.getContentType());
        }
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(final String charset) {
        this.charset = charset;
    }
}
