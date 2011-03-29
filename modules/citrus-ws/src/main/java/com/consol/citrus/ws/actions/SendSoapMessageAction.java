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

package com.consol.citrus.ws.actions;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.Message;
import org.springframework.util.StringUtils;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.message.WebServiceMessageSender;

/**
 * Message sender implementation sending SOAP messages.
 * 
 *  This special implementation adds SOAP attachment support to normal
 *  message sender.
 *  
 * @author Christoph Deppisch
 */
public class SendSoapMessageAction extends SendMessageAction {

    /** SOAP attachment data */
    private String attachmentData;
    
    /** SOAP attachment data as external file resource */
    private Resource attachmentResource;
    
    /** SOAP attachment */
    private SoapAttachment attachment = new SoapAttachment();
    
    /** Forks the message sending action so other actions can take place while this
     * message sender is waiting for the synchronous response */
    private boolean forkMode = false;
    
    @Override
    public void doExecute(final TestContext context) {
        final Message<?> message = createMessage(context);
        
        // extract variables from before sending message so we can save dynamic message ids
        for (VariableExtractor variableExtractor : getVariableExtractors()) {
            variableExtractor.extractVariables(message, context);
        }
        
        if(!(messageSender instanceof WebServiceMessageSender)) {
            throw new CitrusRuntimeException("Sending SOAP messages requires a " +
            		"'com.consol.citrus.ws.message.WebServiceMessageSender' but was '" + messageSender.getClass().getName() + "'");
        }
        
        final String attachmentContent;
        try {
            if(StringUtils.hasText(attachmentData)) {
                attachmentContent = context.replaceDynamicContentInString(attachmentData);
            } else if(attachmentResource != null) {
                attachmentContent = context.replaceDynamicContentInString(FileUtils.readToString(attachmentResource));
            } else {
                attachmentContent = null;
            }
        
            if (forkMode) {
                SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
                taskExecutor.execute(new Runnable() {
                    public void run() {
                        sendSoapMessage(message, attachmentContent);
                    }
                });
            } else {
                sendSoapMessage(message, attachmentContent);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    /**
     * Sends the SOAP message with the {@link WebServiceMessageSender}.
     * 
     * @param message the message to send.
     * @param attachmentContent the optional attachmentContent.
     */
    private void sendSoapMessage(Message<?> message, String attachmentContent) {
        WebServiceMessageSender webServiceMessageSender = (WebServiceMessageSender) messageSender;
        if(attachmentContent != null) {
            attachment.setContent(attachmentContent);
            
            webServiceMessageSender.send(message, attachment);
        } else {
            webServiceMessageSender.send(message);
        }
    }

    /**
     * Set the Attachment data file resource.
     * @param attachment the attachment to set
     */
    public void setAttachmentResource(Resource attachment) {
        this.attachmentResource = attachment;
    }

    /**
     * Set the content type, delegates to soap attachment.
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        attachment.setContentType(contentType);
    }

    /**
     * Set the content id, delegates to soap attachment.
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        attachment.setContentId(contentId);
    }
    
    /**
     * Set the charset name, delegates to soap attachment.
     * @param charsetName the charsetName to set
     */
    public void setCharsetName(String charsetName) {
        attachment.setCharsetName(charsetName);
    }

    /**
     * Set the attachment data as string value.
     * @param attachmentData the attachmentData to set
     */
    public void setAttachmentData(String attachmentData) {
        this.attachmentData = attachmentData;
    }

    /**
     * Enables fork mode for this message sender.
     * @param fork the fork to set.
     */
    public void setForkMode(boolean fork) {
        this.forkMode = fork;
    }
}
