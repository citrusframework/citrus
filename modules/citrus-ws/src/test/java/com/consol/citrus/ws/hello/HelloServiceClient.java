/*
 * Copyright 2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.ws.hello;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.apache.xerces.parsers.DOMParser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.consol.citrus.util.XMLUtils;

/**
 * A client for the Echo Web Service that uses SAAJ.
 *
 * @author Ben Ethridge
 * @author Arjen Poutsma
 */
public class HelloServiceClient {

    private SOAPConnectionFactory connectionFactory;

    private MessageFactory messageFactory;

    private URL url;

    private String prefix = "ns0";

    private String targetNamespace = "http://www.consol.de/namespace/default/";

    private Resource requestResource;

    private Map headerElements = new HashMap();

    public HelloServiceClient() throws SOAPException, MalformedURLException {
        connectionFactory = SOAPConnectionFactory.newInstance();
        messageFactory = MessageFactory.newInstance();
    }

    private SOAPMessage createRequest() throws SOAPException, SAXException, IOException {
        SOAPMessage message = messageFactory.createMessage();

        System.out.println("Creating SOAP request ...");

        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false);
        parser.parse(new InputSource(requestResource.getInputStream()));

        Document payload = parser.getDocument();
        System.out.println("SOAP request is: " + XMLUtils.serialize(payload));

        message.getSOAPBody().addDocument(payload);

        for (Iterator iterator = headerElements.keySet().iterator(); iterator.hasNext();) {
            String headerName = (String) iterator.next();
            String headerValue = (String)headerElements.get(headerName);

            if (headerName.equals("SOAPAction")) {
                MimeHeaders mimeHeaders = message.getMimeHeaders();
                mimeHeaders.addHeader("SOAPAction", headerValue);
            } else {
                SOAPHeaderElement headerElement = message.getSOAPHeader().addHeaderElement(message.getSOAPPart().getEnvelope().createName(headerName, prefix, targetNamespace));
                headerElement.setValue(headerValue);
            }
        }

        return message;
    }

    public void callWebService() throws SOAPException, IOException, SAXException {
        SOAPMessage request = createRequest();
        SOAPConnection connection = connectionFactory.createConnection();

        System.out.println("Sending SOAP request ...");
        SOAPMessage response = connection.call(request, url);

        if (!response.getSOAPBody().hasFault()) {
            System.out.println("Received SOAP response");

            for (Iterator iterator = response.getSOAPHeader().examineAllHeaderElements(); iterator.hasNext();) {
                SOAPHeaderElement headerElement = (SOAPHeaderElement) iterator.next();
                System.out.println("Found SOAP header element: " + headerElement.getLocalName() + " = " + headerElement.getTextContent());
            }

            System.out.println(XMLUtils.serialize(response.getSOAPBody().getOwnerDocument()));
        }
        else {
            SOAPFault fault = response.getSOAPBody().getFault();
            System.err.println("Received SOAP fault");
            System.err.println("SOAP Fault Code :" + fault.getFaultCode());
            System.err.println("SOAP Fault String :" + fault.getFaultString());
        }
    }

    public static void main(String[] args) throws IOException {
        ApplicationContext applicationContext =
            new ClassPathXmlApplicationContext("applicationContext.xml", HelloServiceClient.class);
        HelloServiceClient client = (HelloServiceClient) applicationContext.getBean("helloServiceClient");
        try {
            client.callWebService();
        } catch (SOAPException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(SOAPConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * @param messageFactory the messageFactory to set
     */
    public void setMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * @param request the request to set
     */
    public void setRequestResource(Resource request) {
        this.requestResource = request;
    }

    /**
     * @param headerElements the headerElements to set
     */
    public void setHeaderElements(Map headerElements) {
        this.headerElements = headerElements;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @param targetNamespace the targetNamespace to set
     */
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }
}