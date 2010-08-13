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

package com.consol.citrus.ws.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.transform.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.*;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.client.core.SoapFaultMessageResolver;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.MessageUtils;
/**
 * Message sender connection as client to a WebService endpoint. The sender supports
 * SOAP attachments in contrary to the normal message senders.
 * 
 * @author Christoph Deppisch
 */
public class WebServiceMessageSender extends WebServiceGatewaySupport implements MessageSender, FaultMessageResolver {

    /** Reply message handler */
    private ReplyMessageHandler replyMessageHandler;
    
    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(WebServiceMessageSender.class);
    
    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.core.Message)
     */
    public void send(Message<?> message) {
        send(message, null);
    }

    /**
     * Send message with SOAP attachment.
     * @param message
     * @param attachment
     */
    public void send(final Message<?> message, final Attachment attachment) {
        Assert.notNull(message, "Can not send empty message");
        
        log.info("Sending message to: " + getDefaultUri());

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(message.toString());
        }
        
        if(!(message.getPayload() instanceof String)) {
        	throw new CitrusRuntimeException("Unsupported payload type '" + message.getPayload().getClass() +
    				"' Currently only 'java.lang.String' is supported as payload type.");
        }
        
        WebServiceMessageSenderCallback senderCallback = new WebServiceMessageSenderCallback(message, attachment);
        WebServiceMessageReceiverCallback receiverCallback = new WebServiceMessageReceiverCallback();
        getWebServiceTemplate().setFaultMessageResolver(this);
        
        // send and receive
        getWebServiceTemplate().sendAndReceive(senderCallback, receiverCallback);

        Message<String> responseMessage = receiverCallback.getResponse();
        
        if(replyMessageHandler != null) {
            if(correlator != null) {
                replyMessageHandler.onReplyMessage(responseMessage, correlator.getCorrelationKey(message));
            } else {
                replyMessageHandler.onReplyMessage(responseMessage);
            }
        }
    }

    /**
     * Set the reply message handler.
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }
    
    /**
     * @see org.springframework.ws.client.core.FaultMessageResolver#resolveFault(org.springframework.ws.WebServiceMessage)
     */
	public void resolveFault(WebServiceMessage message) throws IOException {
		if(message instanceof SoapMessage) {
			new SoapFaultMessageResolver().resolveFault(message);
		} else {
			new SimpleFaultMessageResolver().resolveFault(message);
		}
	}

    /**
     * Set reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }
    
	/**
	 * Callback for Webservice-Sending-Actions
	 */
	private class WebServiceMessageSenderCallback implements WebServiceMessageCallback {

		private Message<?> message;
		private Attachment attachment = null;
		
		public WebServiceMessageSenderCallback(Message<?> message, Attachment attachment) {
			this.message = message;
			this.attachment = attachment;
		}

		public void doWithMessage(WebServiceMessage requestMessage) throws IOException, TransformerException {
			
		    SoapMessage soapRequest = ((SoapMessage)requestMessage);
		    
		    // Copy payload into soap-body: 
		    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.transform(new StringSource(message.getPayload().toString()), soapRequest.getSoapBody().getPayloadResult());
		    
	        // Copy headers into soap-header:
		    for (Entry<String, Object> headerEntry : message.getHeaders().entrySet()) {
		        if(MessageUtils.isSpringInternalHeader(headerEntry.getKey())) {
		            continue;
		        }
		        
		        if(headerEntry.getKey().toLowerCase().equals(CitrusSoapMessageHeaders.SOAP_ACTION)) {
		            soapRequest.setSoapAction(headerEntry.getValue().toString());
		        } else if(headerEntry.getKey().toLowerCase().equals(CitrusMessageHeaders.HEADER_CONTENT)) {
		            transformer.transform(new StringSource(headerEntry.getValue().toString()), 
		                    soapRequest.getSoapHeader().getResult());
		        } else {
		            SoapHeaderElement headerElement;
		            if(QNameUtils.validateQName(headerEntry.getKey())) {
		                headerElement = soapRequest.getSoapHeader().addHeaderElement(QNameUtils.parseQNameString(headerEntry.getKey()));
		            } else {
		                headerElement = soapRequest.getSoapHeader().addHeaderElement(QNameUtils.createQName("", headerEntry.getKey(), ""));
		            }
		            
		            headerElement.setText(headerEntry.getValue().toString());
		        }
		    }
		    // Add attachment:
		    if(attachment != null) {
		        if(log.isDebugEnabled()) {
		            log.debug("Adding attachment to SOAP message: '" + attachment.getContentId() + "' ('" + attachment.getContentType() + "')");
		        }
		        
		        soapRequest.addAttachment(attachment.getContentId(), new InputStreamSource() {
		            public InputStream getInputStream() throws IOException {
		                return attachment.getInputStream();
		            }
		        }, attachment.getContentType());
		    }
		}
	}
	
	/**
	 * Callback for Webservice-Receiving-Actions
	 */
	private class WebServiceMessageReceiverCallback implements WebServiceMessageCallback {

		private Message<String> response;
		
		public void doWithMessage(WebServiceMessage responseMessage) throws IOException, TransformerException {
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        
	        StringResult responsePayload = new StringResult();
	        transformer.transform(responseMessage.getPayloadSource(), responsePayload);
	        
			MessageBuilder<String> responseMessageBuilder = MessageBuilder.withPayload(responsePayload.toString());
			
		    if (responseMessage instanceof SoapMessage) {
	            SoapMessage soapMessage = (SoapMessage) responseMessage;
	            SoapHeader soapHeader = soapMessage.getSoapHeader();
	            
	            if (soapHeader != null) {
	                Iterator<?> iter = soapHeader.examineAllHeaderElements();
	                while (iter.hasNext()) {
	                    SoapHeaderElement headerEntry = (SoapHeaderElement) iter.next();
	                    responseMessageBuilder.setHeader(headerEntry.getName().getLocalPart(), headerEntry.getText());
	                }
	            }
	            
	            if(StringUtils.hasText(soapMessage.getSoapAction())) {
	                if(soapMessage.getSoapAction().equals("\"\"")) {
	                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.SOAP_ACTION, "");
	                } else {
	                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.SOAP_ACTION, soapMessage.getSoapAction());
	                }
	            }
	            
	            Iterator<?> attachments = soapMessage.getAttachments();

	            while (attachments.hasNext()) {
	                Attachment attachment = (Attachment)attachments.next();
	                
	                if(StringUtils.hasText(attachment.getContentId())) {
	                    String contentId = attachment.getContentId();
	                    
	                    if(contentId.startsWith("<")) {contentId = contentId.substring(1);}
	                    if(contentId.endsWith(">")) {contentId = contentId.substring(0, contentId.length()-1);}
	                    
	                    if(log.isDebugEnabled()) {
	                    	log.debug("Response contains attachment with contentId '" + contentId + "'");
	                    }
	                    
	                    responseMessageBuilder.setHeader(contentId, attachment);	                    
	                    // TODO CW: is this ok here or do we have to include the SoapAttachmentAwareJmsCallback?
	                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.CONTENT_ID, contentId);
	                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.CONTENT_TYPE, attachment.getContentType());
	                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.CONTENT, FileUtils.readToString(attachment.getInputStream()).trim());
	                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.CHARSET_NAME, "UTF-8");
	                } else {
	                    log.warn("Could not handle response attachment with empty 'contentId'. Attachment is ignored in further processing");
	                }
	            }
	        }
		    
		    // now set response for later access via getResponse():
	        response = responseMessageBuilder.build();
		}
		
		public Message<String> getResponse() {
			return response;
		}
	}
}
