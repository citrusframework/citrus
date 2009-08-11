package com.consol.citrus.actions;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageSender;


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

    /** The service with which the message is beeing sent or received */
    private MessageSender messageSender;

    /** The message ressource as a file resource */
    private Resource messageResource;

    /** The message ressource as a inline definition within the spring application context */
    private String messageData;

    /**
     * Following actions will be executed:
     * 1. The message resource is parsed and message elements get overwritten
     * 2. The message header properties are set
     * 3. The message is sent via respective service.
     *
     * @return boolean success flag
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) throws CitrusRuntimeException {
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
                throw new CitrusRuntimeException("Could not find message data. Either message-data or message-resource must be specified");
            }

            /* explicitly overwrite message elements */
            messagePayload = context.replaceMessageValues(messageElements, messagePayload);

            /* Set message header */
            Map headerValuesCopy = context.replaceVariablesInMap(headerValues);

            /* store header values map to context - service will read the map */
            Message sendMessage = MessageBuilder.withPayload(messagePayload).copyHeaders(headerValuesCopy).build();

            /* message is sent */
            messageSender.send(sendMessage);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }
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
     * @param messageSender the messageSender to set
     */
    public void setMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
    }
}