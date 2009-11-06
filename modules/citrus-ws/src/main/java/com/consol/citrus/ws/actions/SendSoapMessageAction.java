/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.actions;

import java.io.*;
import java.text.ParseException;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.activemq.util.ByteArrayInputStream;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.util.StringUtils;
import org.springframework.ws.mime.Attachment;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.message.WebServiceMessageSender;

public class SendSoapMessageAction extends SendMessageAction {

    private String attachmentData;
    
    private Resource attachmentResource;
    
    private String contentType = "text/plain";
    
    private String contentId = "SOAPAttachment";
    
    private String charsetName = "UTF-8";
    
    @Override
    public void execute(final TestContext context) {
        Message<?> message = createMessage(context);
        
        context.createVariablesFromHeaderValues(extractHeaderValues, message.getHeaders());
        
        if(messageSender instanceof WebServiceMessageSender == false) {
            throw new CitrusRuntimeException("Sending SOAP messages requires a " +
            		"'com.consol.citrus.ws.message.WebServiceMessageSender' but was '" + message.getClass().getName() + "'");
        }
        
        String content = null;
        try {
            if(StringUtils.hasText(attachmentData)) {
                content = context.replaceDynamicContentInString(attachmentData);
            } else if(attachmentResource != null) {
                content = context.replaceDynamicContentInString(FileUtils.readToString(attachmentResource));
            }
        
            if(content != null) {
                ((WebServiceMessageSender)messageSender).send(message, new SoapAttatchment(content));
            } else {
                ((WebServiceMessageSender)messageSender).send(message);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    private class SoapAttatchment implements Attachment {
        private String content;
        
        public SoapAttatchment(String content) {
            this.content = content;
        }
        
        public String getContentId() {
            return contentId;
        }

        public String getContentType() {
            return contentType;
        }

        public DataHandler getDataHandler() {
            return new DataHandler(new DataSource() {
                public OutputStream getOutputStream() throws IOException {
                    throw new UnsupportedOperationException();
                }
                
                public String getName() {
                    return contentId;
                }
                
                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(content.getBytes(charsetName));
                }
                
                public String getContentType() {
                    return contentType;
                }
            });
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content.getBytes(charsetName));
        }

        public long getSize() {
            try {
                return content.getBytes(charsetName).length;
            } catch (UnsupportedEncodingException e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    /**
     * @param attachment the attachment to set
     */
    public void setAttachmentResource(Resource attachment) {
        this.attachmentResource = attachment;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    /**
     * @param attachmentContent the attachmentContent to set
     */
    public void setAttachmentContent(String attachmentContent) {
        this.attachmentData = attachmentContent;
    }
}
