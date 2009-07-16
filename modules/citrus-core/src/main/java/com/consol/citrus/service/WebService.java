package com.consol.citrus.service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.XMLMessage;

public class WebService extends WebServiceGatewaySupport implements Service {

    private static String response;

    public WebService(WebServiceMessageFactory messageFactory) {
        super(messageFactory);
    }

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.service.Service#sendMessage(com.consol.citrus.message.Message)
     */
    public void sendMessage(Message message) throws TestSuiteException {
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        StreamSource source = new StreamSource(new StringReader(message.getMessagePayload()));
        getWebServiceTemplate().sendSourceAndReceiveToResult(source, result);

        writer.flush();
        WebService.storeResponse(writer.toString());

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.service.Service#receiveMessage()
     */
    public Message receiveMessage() throws TestSuiteException {
        Message message = new XMLMessage();

        message.setMessagePayload(WebService.response);
        return message;
    }

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.service.Service#getServiceDestination()
     */
    public String getServiceDestination() {
        return getWebServiceTemplate().getDefaultUri();
    }

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.service.Service#changeServiceDestination(java.lang.String)
     */
    public void changeServiceDestination(String destination) throws TestSuiteException {
        getWebServiceTemplate().setDefaultUri(destination);
    }
    
    public static void storeResponse(String response) {
        WebService.response = response;
    }
}
