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

package com.consol.citrus.ws.echo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

/**
 * A client for the Echo Web Service that uses SAAJ.
 *
 * @author Ben Ethridge
 * @author Arjen Poutsma
 */
public class EchoClient {

    public static final String NAMESPACE_URI = "http://www.springframework.org/spring-ws/samples/echo";

    public static final String PREFIX = "tns";

    private SOAPConnectionFactory connectionFactory;

    private MessageFactory messageFactory;

    private URL url;

    public EchoClient(String url) throws SOAPException, MalformedURLException {
        connectionFactory = SOAPConnectionFactory.newInstance();
        messageFactory = MessageFactory.newInstance();
        this.url = new URL(url);
    }

    private SOAPMessage createEchoRequest() throws SOAPException {
        SOAPMessage message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Name echoRequestName = envelope.createName("echoRequest", PREFIX, NAMESPACE_URI);
        SOAPBodyElement echoRequestElement = message.getSOAPBody()
        .addBodyElement(echoRequestName);
        echoRequestElement.setValue("Hello");
        return message;
    }

    public void callWebService() throws SOAPException, IOException {
        SOAPMessage request = createEchoRequest();
        SOAPConnection connection = connectionFactory.createConnection();
        SOAPMessage response = connection.call(request, url);
        if (!response.getSOAPBody().hasFault()) {
            writeEchoResponse(response);
        }
        else {
            SOAPFault fault = response.getSOAPBody().getFault();
            System.err.println("Received SOAP Fault");
            System.err.println("SOAP Fault Code :" + fault.getFaultCode());
            System.err.println("SOAP Fault String :" + fault.getFaultString());
        }
    }

    private void writeEchoResponse(SOAPMessage message) throws SOAPException {
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Name echoResponseName = envelope.createName("echoResponse", PREFIX, NAMESPACE_URI);
        SOAPBodyElement echoResponseElement = (SOAPBodyElement) message
        .getSOAPBody().getChildElements(echoResponseName).next();
        String echoValue = echoResponseElement.getTextContent();
        System.out.println("Echo Response [" + echoValue + "]");
    }

    public static void main(String[] args) throws Exception {
        String url = "http://localhost:8081/ws-stub/echo/services";
        if (args.length > 0) {
            url = args[0];
        }
        EchoClient echoClient = new EchoClient(url);
        echoClient.callWebService();
    }
}