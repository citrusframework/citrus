/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

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
import com.consol.citrus.util.FileUtils;


/**
 * This action sends a messages to a specified service destination endpoint. Action uses
 * a {@link MessageSender} so action is independent from message transport.
 *
 * @author Christoph Deppisch 
 * @since 2008
 */
public class SendMessageAction extends AbstractTestAction {
    /** Overwrite message payload elements before sending */
    private Map<String, String> messageElements = new HashMap<String, String>();

    /** Message header entries */
    private Map<String, Object> headerValues = new HashMap<String, Object>();

    /** The message sender */
    protected MessageSender messageSender;

    /** The message payload as a file resource */
    private Resource messageResource;

    /** The message payload as inline data */
    private String messageData;
    
    /** Extract message headers to variables */
    protected Map<String, String> extractHeaderValues = new HashMap<String, String>();

    /**
     * Message is constructed with payload and header entries and sent via
     * {@link MessageSender} instance.
     * 
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        Message<?> message = createMessage(context);
        
        context.createVariablesFromHeaderValues(extractHeaderValues, message.getHeaders());
        
        messageSender.send(message);
    }

    /**
     * Create message to be sent.
     * @param context
     * @return
     */
    protected Message<?> createMessage(TestContext context) {
        try {
            String messagePayload = null;
            
            if (messageResource != null) {
                messagePayload = context.replaceDynamicContentInString(FileUtils.readToString(messageResource));
            } else if (messageData != null){
                messagePayload = context.replaceDynamicContentInString(messageData);
            } else {
                throw new CitrusRuntimeException("Could not find message data. Either message-data or message-resource must be specified");
            }
    
            /* explicitly overwrite message elements */
            messagePayload = context.replaceMessageValues(messageElements, messagePayload);
    
            /* Set message header */
            Map<String, Object> headerValuesCopy = context.replaceVariablesInMap(headerValues);
    
            /* store header values map to context - service will read the map */
            return MessageBuilder.withPayload(messagePayload).copyHeaders(headerValuesCopy).build();
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Set the message payload as inline data.
     * @param messageData the messageData to set
     */
    public void setMessageData(String messageData) {
        this.messageData = messageData;
    }

    /**
     * Set the message payload from external file resource.
     * @param messageResource the messageResource to set
     */
    public void setMessageResource(Resource messageResource) {
        this.messageResource = messageResource;
    }

    /**
     * Set header entries.
     * @param headerValues the headerValues to set
     */
    public void setHeaderValues(Map<String, Object> headerValues) {
        this.headerValues = headerValues;
    }

    /**
     * Set message elements to overwrite before sending.
     * @param messageElements the messageElements to set
     */
    public void setMessageElements(Map<String, String> setMessageElements) {
        this.messageElements = setMessageElements;
    }

    /**
     * Get the header values.
     * @return the headerValues
     */
    public Map<String, Object> getHeaderValues() {
        return headerValues;
    }

    /**
     * @return the messageElements
     */
    public Map<String, String> getMessageElements() {
        return messageElements;
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
     * Set the message sender instance.
     * @param messageSender the messageSender to set
     */
    public void setMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Set values to extract as variables.
     * @param extractHeaderValues the extractHeaderValues to set
     */
    public void setExtractHeaderValues(Map<String, String> extractHeaderValues) {
        this.extractHeaderValues = extractHeaderValues;
    }
}