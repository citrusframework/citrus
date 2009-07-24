package com.consol.citrus.actions;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.service.Service;
import com.consol.citrus.variable.VariableUtils;


/**
 * This bean sends messages to a specified service destination.
 *
 * @author deppisch Christoph Deppisch Consol*GmbH 2008
 */
public class SendMessageBean extends AbstractTestAction {
    /** Map holding elements that will overwrite message body elements before message gets sent.
     * Keys in the map specify the element paths inside the message body. Value set will contain
     * static values or variables
     * */
    private Map messageElements = new HashMap();

    /** Map containing header values to be set in message header before sending.
     * Key set describes the header names. Value set will hold static values or variables
     */
    private Map headerValues = new HashMap();

    /** Send destination for explicit overwrite of service destination */
    private String destination;

    /** The service with which the message is beeing sent or received */
    private Service service;

    /** The message ressource as a file resource */
    private Resource messageResource;

    /** The message ressource as a inline definition within the spring application context */
    private String messageData;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SendMessageBean.class);

    /**
     * Following actions will be executed:
     * 1. The message resource is parsed and message elements get overwritten
     * 2. The message header properties are set
     * 3. The message is sent via respective service.
     *
     * @return boolean success flag
     * @throws TestSuiteException
     */
    @Override
    public void execute(TestContext context) throws TestSuiteException {
        if (destination != null) {
            String newDestination = null;

            if (VariableUtils.isVariableName(destination)) {
                newDestination = context.getVariable(destination);
            } else if(context.getFunctionRegistry().isFunction(destination)) {
                newDestination = FunctionUtils.resolveFunction(destination, context);
            } else {
                newDestination = destination;
            }

            if (newDestination != null) {
                if(log.isDebugEnabled()) {
                    log.debug("Setting service destination to custom value " + newDestination);
                }
                service.changeServiceDestination(newDestination);
            } else if(log.isDebugEnabled()) {
                log.debug("Setting service destination to custom value failed. Maybe variable is not set properly: " + destination);
            }
        }

        try {
            String messagePayload = null;
            
            if (messageResource != null) {
                BufferedInputStream reader = new BufferedInputStream(messageResource.getInputStream());
                StringBuffer contentBuffer = new StringBuffer();
                
                byte[] contents = new byte[1024];
                int bytesRead=0;
                while( (bytesRead = reader.read(contents)) != -1){
                    contentBuffer.append(new String(contents, 0, bytesRead));
                }
                
                messagePayload = contentBuffer.toString();
            } else if (messageData != null){
                messagePayload = context.replaceDynamicContentInString(messageData);
            } else {
                throw new TestSuiteException("Could not find message data. Either message-data or message-resource must be specified");
            }

            /* explicitly overwrite message elements */
            messagePayload = context.replaceMessageValues(messageElements, messagePayload);

            /* Set message header */
            Map headerValuesCopy = context.replaceVariablesInMap(headerValues);

            /* store header values map to context - service will read the map */
            Message sendMessage = MessageBuilder.withPayload(messagePayload).copyHeaders(headerValuesCopy).build();

            /* message is sent */
            service.sendMessage(sendMessage);
        } catch (IOException e) {
            throw new TestSuiteException(e);
        } catch (ParseException e) {
            throw new TestSuiteException(e);
        }
    }

    /**
     * Setter for destination
     * @param destination
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @param service the service to set
     */
    public void setService(Service service) {
        this.service = service;
    }

    /**
     * @param messageData the messageData to set
     */
    public void setMessageData(String messageData) {
        this.messageData = messageData;
    }

    /**
     * @param messageResource the messageResource to set
     */
    public void setMessageResource(Resource messageResource) {
        this.messageResource = messageResource;
    }

    /**
     * @param headerValues the headerValues to set
     */
    public void setHeaderValues(HashMap headerValues) {
        this.headerValues = headerValues;
    }

    /**
     * @param messageElements the messageElements to set
     */
    public void setMessageElements(HashMap setMessageElements) {
        this.messageElements = setMessageElements;
    }

    /**
     * @return the headerValues
     */
    public Map getHeaderValues() {
        return headerValues;
    }

    /**
     * @param headerValues the headerValues to set
     */
    public void setHeaderValues(Map headerValues) {
        this.headerValues = headerValues;
    }

    /**
     * @return the messageElements
     */
    public Map getMessageElements() {
        return messageElements;
    }

    /**
     * @param messageElements the messageElements to set
     */
    public void setMessageElements(Map messageElements) {
        this.messageElements = messageElements;
    }

    /**
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @return the messageData
     */
    public String getMessageData() {
        return messageData;
    }

    /**
     * @return the messageResource
     */
    public Resource getMessageResource() {
        return messageResource;
    }

    /**
     * @return the service
     */
    public Service getService() {
        return service;
    }
}