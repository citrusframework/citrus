/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.mail.server;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.mail.client.MailEndpointConfiguration;
import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.mail.message.MailMessageConverter;
import com.consol.citrus.mail.model.*;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.server.AbstractServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.xml.transform.StringResult;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.xml.transform.Source;
import java.io.InputStream;
import java.util.*;

/**
 * Mail server implementation starts new SMTP server instance and listens for incoming mail messages. Incoming mail messages
 * are converted to XML representation and forwarded to some message endpoint adapter (e.g. forwarding mail content to
 * a message channel).
 *
 * By default incoming messages are accepted automatically. When auto accept is disabled the endpoint adapter is invoked with
 * accept request and test case has to decide accept outcome in response.
 *
 * In case of incoming multipart mail messages the server is able to split the body parts into separate XML messages
 * handled by the endpoint adapter.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailServer extends AbstractServer implements SimpleMessageListener, InitializingBean {

    /** Server port */
    private int port = 25;

    /** XML message mapper */
    private MailMarshaller marshaller = new MailMarshaller();

    /** Mail message converter */
    private MailMessageConverter messageConverter = new MailMessageConverter();

    /** Java mail session */
    private Session mailSession;

    /** Java mail properties */
    private Properties javaMailProperties = new Properties();

    /** Should accept automatically or handled via test case */
    private boolean autoAccept = true;

    /** Should split multipart messages for each mime part */
    private boolean splitMultipart = false;

    /** Smtp server instance */
    private SMTPServer smtpServer;

    @Override
    protected void startup() {
        smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(this));
        smtpServer.setSoftwareName(getName());
        smtpServer.setPort(port);
        smtpServer.start();
    }

    @Override
    protected void shutdown() {
        smtpServer.stop();
    }

    @Override
    public boolean accept(String from, String recipient) {
        if (autoAccept) {
            return true;
        }

        StringResult result = new StringResult();
        marshaller.marshal(createAcceptRequest(from, recipient), result);
        Message response = getEndpointAdapter().handleMessage(
                new DefaultMessage(result.toString()));

        if (response == null || response.getPayload() == null) {
            throw new CitrusRuntimeException("Did not receive accept response. Missing accept response because autoAccept is disabled.");
        }

        AcceptResponse acceptResponse = null;
        if (response.getPayload() instanceof AcceptResponse) {
            acceptResponse = (AcceptResponse) response.getPayload();
        } else if (response.getPayload() instanceof String) {
            acceptResponse = (AcceptResponse) marshaller.unmarshal(response.getPayload(Source.class));
        }

        if (acceptResponse == null) {
            throw new CitrusRuntimeException("Unable to read accept response from paylaod: " + response);
        }

        return acceptResponse.isAccept();
    }

    @Override
    public void deliver(String from, String recipient, InputStream data) {
        try {
            MimeMailMessage mimeMailMessage = new MimeMailMessage(new MimeMessage(getSession(), data));
            Message request = messageConverter.convertInbound(mimeMailMessage, getEndpointConfiguration());
            Message response = invokeEndpointAdapter(request);

            if (response != null && response.getPayload() != null) {
                MailResponse mailResponse = null;
                if (response.getPayload() instanceof MailResponse) {
                    mailResponse = (MailResponse) response.getPayload();
                } else if (response.getPayload() instanceof String) {
                    mailResponse = (MailResponse) marshaller.unmarshal(response.getPayload(Source.class));
                }

                if (mailResponse != null && mailResponse.getCode() != MailResponse.OK_CODE) {
                    throw new RejectException(mailResponse.getCode(), mailResponse.getMessage());
                }
            }
        } catch (MessagingException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Invokes the endpoint adapter with constructed mail message and headers.
     * @param request
     */
    protected Message invokeEndpointAdapter(Message request) {
        MailMessage mailMessage = (MailMessage) request.getPayload();

        if (splitMultipart) {
            return split(mailMessage.getBody(), request.copyHeaders());
        } else {
            StringResult result = new StringResult();
            marshaller.marshal(mailMessage, result);
            return getEndpointAdapter().handleMessage(new DefaultMessage(result.toString(), request.copyHeaders()));
        }
    }

    /**
     * Split mail message into several messages. Each body and each attachment results in separate message
     * invoked on endpoint adapter. Mail message response if any should be sent only once within test case.
     * However latest mail response sent by test case is returned, others are ignored.
     *
     * @param bodyPart
     * @param messageHeaders
     */
    private Message split(BodyPart bodyPart, Map<String, Object> messageHeaders) {
        MailMessage mailMessage = createMailMessage(messageHeaders);
        mailMessage.setBody(new BodyPart(bodyPart.getContent(), bodyPart.getContentType()));

        StringResult result = new StringResult();
        Stack<Message> responseStack = new Stack<Message>();
        if (bodyPart instanceof AttachmentPart) {
            marshaller.marshal(mailMessage, result);
            fillStack(getEndpointAdapter().handleMessage(new DefaultMessage(result.toString(), messageHeaders)
                    .setHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE, bodyPart.getContentType())
                    .setHeader(CitrusMailMessageHeaders.MAIL_FILENAME, ((AttachmentPart) bodyPart).getFileName())), responseStack);
        } else {
            marshaller.marshal(mailMessage, result);
            fillStack(getEndpointAdapter().handleMessage(new DefaultMessage(result.toString(), messageHeaders)
                    .setHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE, bodyPart.getContentType())), responseStack);
        }

        if (bodyPart.hasAttachments()) {
            for (AttachmentPart attachmentPart : bodyPart.getAttachments().getAttachments()) {
                fillStack(split(attachmentPart, messageHeaders), responseStack);
            }
        }

        return responseStack.isEmpty() ? null : responseStack.pop();
    }

    private void fillStack(Message message, Stack<Message> responseStack) {
        if (message != null) {
            responseStack.push(message);
        }
    }

    /**
     * Creates a new mail message model object from message headers.
     * @param messageHeaders
     * @return
     */
    protected MailMessage createMailMessage(Map<String, Object> messageHeaders) {
        MailMessage message = new MailMessage();
        message.setFrom(messageHeaders.get(CitrusMailMessageHeaders.MAIL_FROM).toString());
        message.setTo(messageHeaders.get(CitrusMailMessageHeaders.MAIL_TO).toString());
        message.setCc(messageHeaders.get(CitrusMailMessageHeaders.MAIL_CC).toString());
        message.setBcc(messageHeaders.get(CitrusMailMessageHeaders.MAIL_BCC).toString());
        message.setSubject(messageHeaders.get(CitrusMailMessageHeaders.MAIL_SUBJECT).toString());
        return message;
    }

    private AcceptRequest createAcceptRequest(String from, String recipient) {
        return new AcceptRequest(from, recipient);
    }

    @Override
    public MailEndpointConfiguration getEndpointConfiguration() {
        MailEndpointConfiguration endpointConfiguration = new MailEndpointConfiguration();
        endpointConfiguration.setMessageConverter(messageConverter);
        endpointConfiguration.setMailMarshaller(marshaller);
        endpointConfiguration.setJavaMailProperties(javaMailProperties);

        return endpointConfiguration;
    }

    /**
     * Return new mail session if not already created before.
     * @return
     */
    public synchronized Session getSession() {
        if (this.mailSession == null) {
            this.mailSession = Session.getInstance(this.javaMailProperties);
        }
        return this.mailSession;
    }

    /**
     * Is auto accept enabled.
     * @return
     */
    public boolean isAutoAccept() {
        return autoAccept;
    }

    /**
     * Enable/disable auto accept feature.
     * @param autoAccept
     */
    public void setAutoAccept(boolean autoAccept) {
        this.autoAccept = autoAccept;
    }

    /**
     * Gets the mail message marshaller.
     * @return
     */
    public MailMarshaller getMailMarshaller() {
        return marshaller;
    }

    /**
     * Sets the mail message marshaller.
     * @param marshaller
     */
    public void setMailMarshaller(MailMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Gets the Java mail properties.
     * @return
     */
    public Properties getJavaMailProperties() {
        return javaMailProperties;
    }

    /**
     * Sets the Java mail properties.
     * @param javaMailProperties
     */
    public void setJavaMailProperties(Properties javaMailProperties) {
        this.javaMailProperties = javaMailProperties;
    }

    /**
     * Gets the server port.
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the server port.
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the smtp server instance.
     * @return
     */
    public SMTPServer getSmtpServer() {
        return smtpServer;
    }

    /**
     * Sets the smtp server instance.
     * @param smtpServer
     */
    public void setSmtpServer(SMTPServer smtpServer) {
        this.smtpServer = smtpServer;
    }

    /**
     * Gets the split multipart message.
     * @return
     */
    public boolean isSplitMultipart() {
        return splitMultipart;
    }

    /**
     * Sets the split multipart message.
     * @param splitMultipart
     */
    public void setSplitMultipart(boolean splitMultipart) {
        this.splitMultipart = splitMultipart;
    }

    /**
     * Gets the message converter.
     * @return
     */
    public MailMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     */
    public void setMessageConverter(MailMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
}
