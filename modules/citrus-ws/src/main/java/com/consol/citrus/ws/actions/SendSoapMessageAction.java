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

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.client.WebServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

import java.io.IOException;

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
    
    /** SOAP attachment data as external file resource path */
    private String attachmentResourcePath;
    
    /** SOAP attachment */
    private SoapAttachment attachment = new SoapAttachment();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SendSoapMessageAction.class);

    /**
     * Default constructor.
     */
    public SendSoapMessageAction() {
        setName("send");
    }

    @Override
    public void doExecute(final TestContext context) {
        final Message<?> message = createMessage(context, getMessageType());
        
        // extract variables from before sending message so we can save dynamic message ids
        for (VariableExtractor variableExtractor : getVariableExtractors()) {
            variableExtractor.extractVariables(message, context);
        }

        final Endpoint soapEndpoint = getOrCreateEndpoint(context);
        if (!(soapEndpoint instanceof WebServiceClient)) {
            throw new CitrusRuntimeException(String.format("Sending SOAP messages requires a " +
            		"'%s' but was '%s'", WebServiceClient.class.getName(), soapEndpoint.getClass().getName()));
        }

        final WebServiceClient webServiceClient = (WebServiceClient) soapEndpoint;
        final String attachmentContent;
        try {
            if (StringUtils.hasText(attachmentData)) {
                attachmentContent = context.replaceDynamicContentInString(attachmentData);
            } else if (attachmentResourcePath != null) {
                attachmentContent = context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(attachmentResourcePath, context)));
            } else {
                attachmentContent = null;
            }
        
            if (isForkMode()) {
                log.info("Forking send message action ...");

                SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
                taskExecutor.execute(new Runnable() {
                    public void run() {
                        sendSoapMessage(message, attachmentContent, webServiceClient);
                    }
                });
            } else {
                sendSoapMessage(message, attachmentContent, webServiceClient);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    /**
     * Sends the SOAP message with the {@link WebServiceClient}.
     * 
     * @param message the message to send.
     * @param attachmentContent the optional attachmentContent.
     * @param webServiceClient the actual soap client.
     */
    private void sendSoapMessage(Message<?> message, String attachmentContent, WebServiceClient webServiceClient) {
        if (attachmentContent != null) {
            attachment.setContent(attachmentContent);
            webServiceClient.send(message, attachment);
        } else {
            webServiceClient.send(message);
        }
    }

    /**
     * Set the Attachment data file resource.
     * @param attachment the attachment to set
     */
    public void setAttachmentResourcePath(String attachment) {
        this.attachmentResourcePath = attachment;
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
     * Gets the attachmentData.
     * @return the attachmentData
     */
    public String getAttachmentData() {
        return attachmentData;
    }

    /**
     * Gets the attachmentResource.
     * @return the attachmentResource
     */
    public String getAttachmentResourcePath() {
        return attachmentResourcePath;
    }

    /**
     * Gets the attachment.
     * @return the attachment
     */
    public SoapAttachment getAttachment() {
        return attachment;
    }
}
