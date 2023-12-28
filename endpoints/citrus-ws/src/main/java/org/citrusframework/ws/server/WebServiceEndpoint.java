/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.ws.server;

import jakarta.xml.soap.MimeHeaders;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapFault;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.citrusframework.xml.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.MimeMessage;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.HttpServletConnection;
import org.springframework.xml.namespace.QNameUtils;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import static jakarta.xml.soap.SOAPConstants.SOAP_RECEIVER_FAULT;
import static jakarta.xml.soap.SOAPConstants.SOAP_SENDER_FAULT;
import static java.lang.Integer.parseInt;
import static org.citrusframework.message.MessageHeaderUtils.isSpringInternalHeader;
import static org.citrusframework.util.ObjectHelper.assertNotNull;
import static org.citrusframework.util.StringUtils.hasText;
import static org.springframework.http.HttpStatusCode.valueOf;
import static org.springframework.ws.transport.context.TransportContextHolder.getTransportContext;

/**
 * SpringWS {@link MessageEndpoint} implementation. Endpoint will delegate message processing to
 * a {@link EndpointAdapter} implementation.
 *
 * @author Christoph Deppisch
 */
public class WebServiceEndpoint implements MessageEndpoint {

    /**
     * EndpointAdapter handling incoming requests and providing proper responses
     */
    private EndpointAdapter endpointAdapter = new EmptyResponseEndpointAdapter();

    /**
     * Default namespace for all SOAP header entries
     */
    private String defaultNamespaceUri;

    /**
     * Default prefix for all SOAP header entries
     */
    private String defaultPrefix = "";

    /**
     * Endpoint configuration
     */
    private WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(WebServiceEndpoint.class);

    /**
     * JMS headers begin with this prefix
     */
    private static final String DEFAULT_JMS_HEADER_PREFIX = "JMS";

    private static HttpStatusCode getHttpStatusCode(Entry<String, Object> headerEntry) {
        try {
            return valueOf(parseInt(headerEntry.getValue().toString()));
        } catch (IllegalArgumentException e) {
            logger.warn("Error while parsing HTTP response status code!", e);
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * @throws CitrusRuntimeException
     * @see org.springframework.ws.server.endpoint.MessageEndpoint#invoke(org.springframework.ws.context.MessageContext)
     */
    public void invoke(final MessageContext messageContext) throws Exception {
        assertNotNull(messageContext.getRequest(), "Request must not be null - unable to send message");

        Message requestMessage = endpointConfiguration.getMessageConverter().convertInbound(messageContext.getRequest(), messageContext, endpointConfiguration);

        if (logger.isDebugEnabled()) {
            logger.debug("Received SOAP request:\n{}", requestMessage);
        }

        //delegate request processing to endpoint adapter
        Message replyMessage = endpointAdapter.handleMessage(requestMessage);

        if (simulateHttpStatusCode(replyMessage)) {
            return;
        }

        if (replyMessage != null && replyMessage.getPayload() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending SOAP response:\n{}", replyMessage);
            }

            SoapMessage response = (SoapMessage) messageContext.getResponse();

            //add soap fault or normal soap body to response
            if (replyMessage instanceof SoapFault soapFault) {
                addSoapFault(response, soapFault);
            } else {
                addSoapBody(response, replyMessage);
            }

            addSoapAttachments(response, replyMessage);
            addSoapHeaders(response, replyMessage);
            addMimeHeaders(response, replyMessage);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No reply message from endpoint adapter '" + endpointAdapter + "'");
            }
            logger.warn("No SOAP response for calling client");
        }
    }

    private void addSoapAttachments(MimeMessage response, Message replyMessage) {
        if (replyMessage instanceof org.citrusframework.ws.message.SoapMessage soapMessage) {
            List<SoapAttachment> soapAttachments = soapMessage.getAttachments();
            soapAttachments.stream()
                    .filter(soapAttachment -> !soapAttachment.isMtomInline())
                    .forEach(soapAttachment -> {
                        String contentId = soapAttachment.getContentId();

                        if (!contentId.startsWith("<")) {
                            contentId = "<" + contentId + ">";
                        }

                        response.addAttachment(contentId, soapAttachment.getDataHandler());
                    });
        }
    }

    /**
     * If Http status code is set on reply message headers simulate Http error with status code.
     * No SOAP response is sent back in this case.
     *
     * @param replyMessage
     * @return
     */
    private boolean simulateHttpStatusCode(Message replyMessage) throws IOException {
        if (replyMessage == null || replyMessage.getHeaders() == null || replyMessage.getHeaders().isEmpty()) {
            return false;
        }

        for (Entry<String, Object> headerEntry : replyMessage.getHeaders().entrySet()) {
            if (headerEntry.getKey().equalsIgnoreCase(SoapMessageHeaders.HTTP_STATUS_CODE)) {
                WebServiceConnection connection = getTransportContext().getConnection();

                if (connection instanceof HttpServletConnection httpServletConnection) {
                    HttpStatusCode statusCode = getHttpStatusCode(headerEntry);

                    if (statusCode.is4xxClientError()) {
                        httpServletConnection.setFaultCode(SOAP_SENDER_FAULT);
                    } else if (statusCode.is5xxServerError()) {
                        httpServletConnection.setFaultCode(SOAP_RECEIVER_FAULT);
                    } else {
                        httpServletConnection.setFaultCode(null);
                    }

                    httpServletConnection.getHttpServletResponse().setStatus(statusCode.value());

                    return true;
                } else {
                    logger.warn("Unable to set custom HTTP status code on connection other than HttpServletConnection (" + connection.getClass().getName() + ")");
                }
            }
        }

        return false;
    }

    /**
     * Adds mime headers outside of SOAP envelope. Header entries that go to this header section
     * must have internal http header prefix defined in {@link org.citrusframework.ws.message.SoapMessageHeaders}.
     *
     * @param response     the soap response message.
     * @param replyMessage the internal reply message.
     */
    private void addMimeHeaders(SoapMessage response, Message replyMessage) {
        for (Entry<String, Object> headerEntry : replyMessage.getHeaders().entrySet()) {
            if (headerEntry.getKey().toLowerCase().startsWith(SoapMessageHeaders.HTTP_PREFIX)) {
                String headerName = headerEntry.getKey().substring(SoapMessageHeaders.HTTP_PREFIX.length());

                if (response instanceof SaajSoapMessage saajSoapMessage) {
                    MimeHeaders headers = saajSoapMessage.getSaajMessage().getMimeHeaders();
                    headers.setHeader(headerName, headerEntry.getValue().toString());
                } else {
                    logger.warn("Unsupported SOAP message implementation - unable to set mime message header '" + headerName + "'");
                }
            }
        }
    }

    /**
     * Add message payload as SOAP body element to the SOAP response.
     *
     * @param response
     * @param replyMessage
     */
    private void addSoapBody(SoapMessage response, Message replyMessage) throws TransformerException {
        if (!(replyMessage.getPayload() instanceof String) || hasText(replyMessage.getPayload(String.class))) {
            Source responseSource = getPayloadAsSource(replyMessage.getPayload());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.transform(responseSource, response.getPayloadResult());
        }
    }

    /**
     * Translates message headers to SOAP headers in response.
     *
     * @param response
     * @param replyMessage
     */
    private void addSoapHeaders(SoapMessage response, Message replyMessage) throws TransformerException {
        for (Entry<String, Object> headerEntry : replyMessage.getHeaders().entrySet()) {
            if (isSpringInternalHeader(headerEntry.getKey()) || headerEntry.getKey().startsWith(DEFAULT_JMS_HEADER_PREFIX)) {
                continue;
            }

            if (headerEntry.getKey().equalsIgnoreCase(SoapMessageHeaders.SOAP_ACTION)) {
                response.setSoapAction(headerEntry.getValue().toString());
            } else if (!headerEntry.getKey().startsWith(MessageHeaders.PREFIX)) {
                SoapHeaderElement headerElement;
                if (QNameUtils.validateQName(headerEntry.getKey())) {
                    QName qname = QNameUtils.parseQNameString(headerEntry.getKey());

                    if (hasText(qname.getNamespaceURI())) {
                        headerElement = response.getSoapHeader().addHeaderElement(qname);
                    } else {
                        headerElement = response.getSoapHeader().addHeaderElement(getDefaultQName(headerEntry.getKey()));
                    }
                } else {
                    throw new SoapHeaderException("Failed to add SOAP header '" + headerEntry.getKey() + "', " + "because of invalid QName");
                }

                headerElement.setText(headerEntry.getValue().toString());
            }
        }

        for (String headerData : replyMessage.getHeaderData()) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.transform(new StringSource(headerData), response.getSoapHeader().getResult());
        }
    }

    /**
     * Adds a SOAP fault to the SOAP response body. The SOAP fault is declared
     * as QName string in the response message's header (see {@link org.citrusframework.ws.message.SoapMessageHeaders})
     *
     * @param response
     * @param replyMessage
     */
    private void addSoapFault(SoapMessage response, SoapFault replyMessage) throws TransformerException {
        SoapBody soapBody = response.getSoapBody();
        org.springframework.ws.soap.SoapFault soapFault;

        if (SoapFaultDefinition.SERVER.equals(replyMessage.getFaultCodeQName()) ||
                SoapFaultDefinition.RECEIVER.equals(replyMessage.getFaultCodeQName())) {
            soapFault = soapBody.addServerOrReceiverFault(replyMessage.getFaultString(),
                    replyMessage.getLocale());
        } else if (SoapFaultDefinition.CLIENT.equals(replyMessage.getFaultCodeQName()) ||
                SoapFaultDefinition.SENDER.equals(replyMessage.getFaultCodeQName())) {
            soapFault = soapBody.addClientOrSenderFault(replyMessage.getFaultString(),
                    replyMessage.getLocale());
        } else if (soapBody instanceof Soap11Body soap11Body) {
            soapFault = soap11Body.addFault(
                    replyMessage.getFaultCodeQName(),
                    replyMessage.getFaultString(),
                    replyMessage.getLocale()
            );
        } else if (soapBody instanceof Soap12Body soap12Body) {
            Soap12Fault soap12Fault = soap12Body.addServerOrReceiverFault(replyMessage.getFaultString(), replyMessage.getLocale());
            soap12Fault.addFaultSubcode(replyMessage.getFaultCodeQName());

            soapFault = soap12Fault;
        } else {
            throw new CitrusRuntimeException("Found unsupported SOAP implementation. Use SOAP 1.1 or SOAP 1.2.");
        }

        if (replyMessage.getFaultActor() != null) {
            soapFault.setFaultActorOrRole(replyMessage.getFaultActor());
        }

        List<String> soapFaultDetails = replyMessage.getFaultDetails();
        if (!soapFaultDetails.isEmpty()) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            SoapFaultDetail faultDetail = soapFault.addFaultDetail();
            for (String soapFaultDetail : soapFaultDetails) {
                transformer.transform(new StringSource(soapFaultDetail), faultDetail.getResult());
            }
        }
    }

    /**
     * Get the message payload object as {@link Source}, supported payload types are
     * {@link Source}, {@link Document} and {@link String}.
     *
     * @param replyPayload payload object
     * @return {@link Source} representation of the payload
     */
    private Source getPayloadAsSource(Object replyPayload) {
        if (replyPayload instanceof Source) {
            return (Source) replyPayload;
        } else if (replyPayload instanceof Document) {
            return new DOMSource((Document) replyPayload);
        } else if (replyPayload instanceof String) {
            return new StringSource((String) replyPayload);
        } else {
            throw new CitrusRuntimeException("Unknown type for reply message payload (" + replyPayload.getClass().getName() + ") " +
                    "Supported types are " + "'" + Source.class.getName() + "', " + "'" + Document.class.getName() + "'" +
                    ", or '" + String.class.getName() + "'");
        }
    }

    /**
     * Get the default QName from local part.
     *
     * @param localPart
     * @return
     */
    private QName getDefaultQName(String localPart) {
        if (hasText(defaultNamespaceUri)) {
            return new QName(defaultNamespaceUri, localPart, defaultPrefix);
        } else {
            throw new SoapHeaderException("Failed to add SOAP header '" + localPart + "', " +
                    "because neither valid QName nor default namespace-uri is set!");
        }
    }

    /**
     * Gets the endpoint adapter.
     *
     * @return
     */
    public EndpointAdapter getEndpointAdapter() {
        return endpointAdapter;
    }

    /**
     * Set the endpoint adapter.
     *
     * @param endpointAdapter the endpointAdapter to set
     */
    public void setEndpointAdapter(EndpointAdapter endpointAdapter) {
        this.endpointAdapter = endpointAdapter;
    }

    /**
     * Gets the default header namespace uri.
     *
     * @return
     */
    public String getDefaultNamespaceUri() {
        return defaultNamespaceUri;
    }

    /**
     * Set the default namespace used in SOAP response headers.
     *
     * @param defaultNamespaceUri the defaultNamespaceUri to set
     */
    public void setDefaultNamespaceUri(String defaultNamespaceUri) {
        this.defaultNamespaceUri = defaultNamespaceUri;
    }

    /**
     * Gets the default header prefix.
     *
     * @return
     */
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    /**
     * Set the default namespace prefix used in SOAP response headers.
     *
     * @param defaultPrefix the defaultPrefix to set
     */
    public void setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }

    /**
     * Gets the endpoint configuration.
     *
     * @return
     */
    public WebServiceEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    /**
     * Sets the endpoint configuration.
     *
     * @param endpointConfiguration
     */
    public void setEndpointConfiguration(WebServiceEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }
}
