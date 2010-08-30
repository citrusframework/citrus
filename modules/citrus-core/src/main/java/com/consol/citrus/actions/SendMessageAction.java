/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.actions;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.StringUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.GroovyUtils;


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

    /** The message header as a file resource */
    private Resource headerResource;

    /** The message header as inline data */
    private String headerData;
    
    /** The message sender */
    protected MessageSender messageSender;

    /** The message payload as a file resource */
    private Resource messageResource;

    /** The message payload as inline data */
    private String messageData;
    
    /** The message payload as a Groovy MarkupBuilder script file resource */
    private Resource scriptResource;
    
    /** The message payload as inline Groovy MarkupBuilder script */
    private String scriptData;
    
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
            } else if (scriptResource != null){
                messagePayload = GroovyUtils.buildMarkupBuilderScript(context.replaceDynamicContentInString(FileUtils.readToString(scriptResource)));
            } else if (scriptData != null){
                messagePayload = GroovyUtils.buildMarkupBuilderScript(context.replaceDynamicContentInString(scriptData));
            } else {
                throw new CitrusRuntimeException("No message payload defined! Either define message-data, message-resource or Groovy script.");
            }
    
            if(StringUtils.hasText(messagePayload)) {
                /* explicitly overwrite message elements */
                messagePayload = context.replaceMessageValues(messageElements, messagePayload);
            }
    
            /* Set message header */
            Map<String, Object> headerValuesCopy = context.replaceVariablesInMap(headerValues);

            String headerContent = null;
            if (headerResource != null) {
                headerContent = context.replaceDynamicContentInString(FileUtils.readToString(headerResource).trim());
            } else if (headerData != null){
                headerContent = context.replaceDynamicContentInString(headerData.trim());
            }
            
            if(StringUtils.hasText(headerContent)) {
                headerValuesCopy.put(CitrusMessageHeaders.HEADER_CONTENT, headerContent);
            }
            
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
     * Set the message payload as inline Groovy MarkupBuilder script.
     * @param scriptData the scriptData to set
     */
    public void setScriptData(String scriptData) {
        this.scriptData = scriptData;
    }
    
    /**
     * Set the message payload from external Groovy MarkupBuilder script file resource.
     * @param scriptResource the scriptResource to set
     */
    public void setScriptResource(Resource scriptResource) {
        this.scriptResource = scriptResource;
    }
    
    /**
     * Set the message header as inline data.
     * @param headerData the headerData to set
     */
    public void setHeaderData(String headerData) {
        this.headerData = headerData;
    }

    /**
     * Set the header payload from external file resource.
     * @param headerResource the headerResource to set
     */
    public void setHeaderResource(Resource headerResource) {
        this.headerResource = headerResource;
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
     * @return the headerData
     */
    public String getHeaderData() {
        return headerData;
    }

    /**
     * @return the headerResource
     */
    public Resource getHeaderResource() {
        return headerResource;
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