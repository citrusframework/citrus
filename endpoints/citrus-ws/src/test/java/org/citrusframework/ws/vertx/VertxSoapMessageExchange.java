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

package org.citrusframework.ws.vertx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.vertx.VertxHttpMessageExchange;
import org.citrusframework.http.vertx.VertxPrototypeAdapters;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.citrusframework.ws.message.converter.SoapMessageConverter;
import org.citrusframework.ws.server.WebServiceEndpoint;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** SOAP 1.1/1.2 exchange on the test-only Vert.x listener, without a Servlet or Jetty server. */
public final class VertxSoapMessageExchange implements VertxHttpMessageExchange {
    private static final String SOAP11 = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String SOAP12 = "http://www.w3.org/2003/05/soap-envelope";

    @Override public Message exchange(HttpMessage request, EndpointAdapter adapter) throws Exception {
        if (request.getRequestMethod() != RequestMethod.POST) return empty(405).header("Allow", "POST");
        MediaType media;
        try { media = MediaType.parseMediaType(request.getContentType()); }
        catch (IllegalArgumentException e) { return empty(415); }
        boolean soap12 = media.getType().equals("application") && media.getSubtype().equals("soap+xml");
        if (!soap12 && !(media.getType().equals("text") && media.getSubtype().equals("xml"))) return empty(415);
        // The fixture must retain wire bytes, including an XML encoding declaration or BOM.
        if (!(request.getPayload() instanceof byte[] bytes))
            throw new IllegalArgumentException("Configure text/xml and application/soap+xml as binary media types");
        Document document;
        try {
            document = parse(bytes);
            validateEnvelope(document, soap12 ? SOAP12 : SOAP11);
        } catch (Exception e) { return empty(400); }
        var factory = new SaajSoapMessageFactory();
        factory.setSoapVersion(soap12 ? SoapVersion.SOAP_12 : SoapVersion.SOAP_11);
        factory.afterPropertiesSet();
        SaajSoapMessage wire = factory.createWebServiceMessage();
        wire.getSaajMessage().getSOAPPart().setContent(new DOMSource(document));
        String action = soap12 ? media.getParameter("action") : header(request, "SOAPAction");
        if (action != null) wire.setSoapAction(action);
        if (hasRequiredHeader(document, soap12)) {
            SaajSoapMessage fault = factory.createWebServiceMessage();
            fault.getSaajMessage().getSOAPBody().addFault(new QName(soap12 ? SOAP12 : SOAP11, "MustUnderstand", "env"),
                    "A mandatory SOAP header is not supported");
            return serialize(fault, 500);
        }
        var configuration = new WebServiceEndpointConfiguration();
        configuration.setMessageFactory(factory);
        configuration.setMessageConverter(new SoapMessageConverter() {
            @Override protected void handleInboundHttpHeaders(SoapMessage message, WebServiceEndpointConfiguration ignored) {
                // SOAP header elements cannot supply internal identity or reply-routing metadata.
                // The framework-managed message ID is not attacker-controlled routing state, and the
                // message API forbids removing it, so it is left untouched.
                for (String name : List.copyOf(message.getHeaders().keySet()))
                    if (name.startsWith(MessageHeaders.PREFIX) && !name.equals(MessageHeaders.ID)) message.removeHeader(name);
                request.getHeaders().forEach((name, value) -> {
                    if (name.equals(MessageHeaders.ID)) return;
                    if (name.startsWith(MessageHeaders.PREFIX)) message.setHeader(name, value);
                    else message.setHeader(SoapMessageHeaders.HTTP_PREFIX + name, value);
                });
                String soapAction = wire.getSoapAction();
                if (soapAction != null && soapAction.startsWith("\"") && soapAction.endsWith("\"") && soapAction.length() >= 2)
                    soapAction = soapAction.substring(1, soapAction.length() - 1);
                message.setHeader(SoapMessageHeaders.SOAP_ACTION, soapAction == null ? "" : soapAction);
                message.setHeader(SoapMessageHeaders.HTTP_REQUEST_URI, request.getPath());
                message.setHeader(SoapMessageHeaders.HTTP_REQUEST_METHOD, "POST");
                message.setHeader(SoapMessageHeaders.HTTP_CONTENT_TYPE, request.getContentType());
            }
        });
        var endpoint = new WebServiceEndpoint();
        endpoint.setEndpointConfiguration(configuration);
        var explicitStatus = new AtomicInteger();
        endpoint.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            Message reply = adapter.handleMessage(message);
            if (reply != null && reply.getHeader(SoapMessageHeaders.HTTP_STATUS_CODE) != null) {
                int status = Integer.parseInt(reply.getHeader(SoapMessageHeaders.HTTP_STATUS_CODE).toString());
                if (status < 200 || status > 599) throw new IllegalArgumentException("Expected final HTTP status");
                explicitStatus.set(status); return null;
            }
            if (reply instanceof SoapMessage soap && (!soap.getAttachments().isEmpty() || soap.isMtomEnabled()))
                throw new IllegalArgumentException("SOAP attachments and MTOM are not supported by this fixture");
            return reply;
        }));
        var context = new DefaultMessageContext(wire, factory);
        endpoint.invoke(context);
        if (explicitStatus.get() != 0) return empty(explicitStatus.get());
        if (!context.hasResponse()) return empty(202);
        SaajSoapMessage response = (SaajSoapMessage) context.getResponse();
        return serialize(response, response.hasFault() ? 500 : 200);
    }
    private static String header(HttpMessage request, String name) {
        return request.getHeaders().entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(entry -> entry.getValue().toString()).findFirst().orElse(null);
    }
    private static HttpMessage empty(int status) {
        return new HttpMessage(new byte[0]).status(HttpStatusCode.valueOf(status));
    }
    private static HttpMessage serialize(SaajSoapMessage soap, int status) throws Exception {
        soap.getSaajMessage().saveChanges();
        var output = new ByteArrayOutputStream(); soap.writeTo(output);
        var response = new HttpMessage(output.toByteArray()).status(HttpStatusCode.valueOf(status));
        var headers = soap.getSaajMessage().getMimeHeaders().getAllHeaders();
        while (headers.hasNext()) {
            var header = headers.next();
            if (!header.getName().equalsIgnoreCase("Content-Length") && !header.getName().equalsIgnoreCase("Transfer-Encoding"))
                response.setHeader(header.getName(), header.getValue());
        }
        return response;
    }
    private static Document parse(byte[] bytes) throws Exception {
        var factory = DocumentBuilderFactory.newInstance(); factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        // The Xerces factory on the test classpath does not recognize the JAXP property names,
        // so each hardening attribute is best-effort. DOCTYPE rejection and disabled external
        // entities above already block entity expansion attacks.
        for (String attribute : new String[]{XMLConstants.ACCESS_EXTERNAL_DTD, XMLConstants.ACCESS_EXTERNAL_SCHEMA}) {
            try { factory.setAttribute(attribute, ""); }
            catch (IllegalArgumentException ignored) { }
        }
        factory.setXIncludeAware(false); factory.setExpandEntityReferences(false);
        var builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new DefaultHandler() {
            @Override public void error(SAXParseException e) throws SAXParseException { throw e; }
            @Override public void fatalError(SAXParseException e) throws SAXParseException { throw e; }
        });
        return builder.parse(new ByteArrayInputStream(bytes));
    }
    private static List<Element> children(Element parent) {
        var result = new ArrayList<Element>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element) result.add(element);
            else if (child.getNodeType() == Node.TEXT_NODE && !child.getTextContent().isBlank())
                throw new IllegalArgumentException("Unexpected SOAP structural text");
        }
        return result;
    }
    private static void validateEnvelope(Document document, String namespace) {
        Element root = document.getDocumentElement();
        if (!"Envelope".equals(root.getLocalName()) || !namespace.equals(root.getNamespaceURI()))
            throw new IllegalArgumentException("SOAP version does not match Content-Type");
        List<Element> parts = children(root);
        if (parts.isEmpty() || parts.size() > 2) throw new IllegalArgumentException("Expected SOAP Body and optional Header");
        Element body = parts.get(parts.size() - 1);
        if (!"Body".equals(body.getLocalName()) || !namespace.equals(body.getNamespaceURI()) || children(body).size() > 1)
            throw new IllegalArgumentException("Expected one SOAP body payload");
        if (parts.size() == 2 && (!"Header".equals(parts.get(0).getLocalName()) || !namespace.equals(parts.get(0).getNamespaceURI())))
            throw new IllegalArgumentException("Expected SOAP Header before Body");
    }
    private static boolean hasRequiredHeader(Document document, boolean soap12) {
        String ns = soap12 ? SOAP12 : SOAP11;
        for (Element part : children(document.getDocumentElement())) {
            if (!part.getLocalName().equals("Header")) continue;
            for (Element header : children(part)) {
                String required = header.getAttributeNS(ns, "mustUnderstand");
                String role = header.getAttributeNS(ns, soap12 ? "role" : "actor");
                boolean targeted = role.isEmpty() || role.equals(soap12 ? SOAP12 + "/role/ultimateReceiver" : "")
                        || role.equals(soap12 ? SOAP12 + "/role/next" : "http://schemas.xmlsoap.org/soap/actor/next");
                if (targeted && (required.equals("1") || required.equals("true"))) return true;
            }
        }
        return false;
    }
}
