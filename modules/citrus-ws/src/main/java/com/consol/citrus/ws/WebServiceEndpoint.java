package com.consol.citrus.ws;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.xerces.parsers.DOMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.namespace.QNameUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.util.XMLUtils;

public class WebServiceEndpoint implements MessageEndpoint {

    private JmsTemplate jmsTemplate;

    private String sendDestination;
    private String receiveDestination;

    private Resource responseMessageResource;
    private String responseMessagePayload;

    private String prefix = "ns0";
    private String targetNamespace = "http://www.consol.de/namespace/default/";

    private Map responseHeaderElements = new HashMap();

    private static final int MODE_USE_TESTSUITE = 0;
    private static final int MODE_STANDALONE = 1;

    private int mode = MODE_USE_TESTSUITE;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(WebServiceEndpoint.class);

    /**
     * (non-Javadoc)
     * @see org.springframework.ws.server.endpoint.MessageEndpoint#invoke(org.springframework.ws.context.MessageContext)
     */
    public void invoke(final MessageContext msgContext) throws Exception {
        if (mode == MODE_USE_TESTSUITE) {
            jmsTemplate.send(sendDestination, new MessageCreator() {
                public javax.jms.Message createMessage(Session session) throws JMSException {
                    javax.jms.TextMessage msg = null;
                    try {
                        SaajSoapMessage soapRequest = (SaajSoapMessage)msgContext.getRequest();

                        msg = session.createTextMessage(XMLUtils.serialize(soapRequest.getSaajMessage().getSOAPBody().getOwnerDocument()));

                        for (Iterator iterator = soapRequest.getSaajMessage().getSOAPHeader().examineAllHeaderElements(); iterator.hasNext();) {
                            SOAPHeaderElement headerElement = (SOAPHeaderElement) iterator.next();
                            msg.setStringProperty(headerElement.getLocalName(), headerElement.getTextContent());
                        }

                        if (soapRequest.getSoapAction() != null) {
                            log.info("Setting SOAP action: " + soapRequest.getSoapAction());
                            msg.setStringProperty("SOAPAction", soapRequest.getSoapAction());
                        }
                    } catch (Exception e) {
                        log.error("Error during request processing:", e);
                        throw new TestSuiteException(e);
                    }

                    return msg;
                }
            });

            try {
                TextMessage response = (TextMessage)jmsTemplate.receive(receiveDestination);

                if (response != null) {
                    final Reader reader = new StringReader(response.getText());
                    final DOMParser parser = new DOMParser();
                    parser.setFeature("http://xml.org/sax/features/validation", false);

                    parser.parse(new InputSource(reader));

                    SaajSoapMessage soapResponse = (SaajSoapMessage)msgContext.getResponse();
                    soapResponse.getSaajMessage().getSOAPBody().addDocument(parser.getDocument());
                } else {
                    createResponse(msgContext);
                }
            } catch (Exception e) {
                log.error("Error during response processing:", e);
                throw new TestSuiteException(e);
            }
        } else if (mode == MODE_STANDALONE){
            createResponse(msgContext);
        }
    }

    private SaajSoapMessage createResponse(final MessageContext msgContext) throws SAXException, IOException, SOAPException {
        SaajSoapMessage soapResponse = (SaajSoapMessage)msgContext.getResponse();

        log.info("Creating default SOAP response ...");

        final Reader reader;
        final DOMParser parser = new DOMParser();

        parser.setFeature("http://xml.org/sax/features/validation", false);

        if (responseMessagePayload != null) {
            reader = new StringReader(responseMessagePayload);
            parser.parse(new InputSource(reader));
        } else if (responseMessageResource != null) {
            reader = new InputStreamReader(responseMessageResource.getInputStream());
            parser.parse(new InputSource(reader));
        } else {
            throw new TestSuiteException("Could not create default response message as no default message defined");
        }

        soapResponse.getSaajMessage().getSOAPBody().addDocument(parser.getDocument());

        for (Iterator iterator = responseHeaderElements.keySet().iterator(); iterator.hasNext();) {
            String headerName = (String) iterator.next();
            String headerValue = (String)responseHeaderElements.get(headerName);

            if (headerName.equals("SOAPAction")) {
                soapResponse.setSoapAction(headerValue);
            } else {
                SoapHeaderElement headerElement = soapResponse.getSoapHeader().addHeaderElement(QNameUtils.createQName(targetNamespace, headerName, prefix));
                headerElement.setText(headerValue);
            }
        }

        return soapResponse;
    }

    /**
     * @param jmsTemplate the jmsTemplate to set
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * @param sendDestination the sendDestination to set
     */
    public void setSendDestination(String sendDestination) {
        this.sendDestination = sendDestination;
    }

    /**
     * @param receiveDestination the receiveDestination to set
     */
    public void setReceiveDestination(String receiveDestination) {
        this.receiveDestination = receiveDestination;
    }

    /**
     * @param responseMessageResource the responseMessageResource to set
     */
    public void setResponseMessageResource(Resource responseMessageResource) {
        this.responseMessageResource = responseMessageResource;
    }

    /**
     * @param responseMessage the responseMessage to set
     */
    public void setResponseMessagePayload(String responseMessage) {
        this.responseMessagePayload = responseMessage;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * @param responseHeaderElements the responseHeaderElements to set
     */
    public void setResponseHeaderElements(Map responseHeaderElements) {
        this.responseHeaderElements = responseHeaderElements;
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