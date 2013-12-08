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

package com.consol.citrus.mail.message;

import com.consol.citrus.TestActor;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.mail.model.*;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.integration.Message;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;
import org.springframework.xml.transform.StringSource;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * Mail message sender uses Spring's Java mail sender. Construct proper mail mime message from Citrus mail message
 * object. Citrus mail message object is apssed as message payload either as plain object instance or XML message payload.
 * XML message payload is converted to proper mail message object using mail message mapper implementation.
 *
 * User can pass custom host, port, username, password and other Java mail properties to this message sender.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailMessageSender implements MessageSender, BeanNameAware {

    /** Mail sender implementation */
    private JavaMailSenderImpl javaMailSender;

    /** Mail message mapper converts from XML to mail message object */
    private MailMessageMapper mailMessageMapper = new MailMessageMapper();

    @Autowired(required = false)
    private MessageListeners messageListener;

    /** Test actor linked to this message sender */
    private TestActor actor;

    /** This sender's name */
    private String name = getClass().getSimpleName();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MailMessageSender.class);

    /**
     * Default constructor.
     */
    public MailMessageSender() {
        this.javaMailSender = new JavaMailSenderImpl();
    }

    /**
     * Constructor with java mail sender implementation.
     * @param javaMailSender
     */
    public MailMessageSender(JavaMailSenderImpl javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void send(Message<?> message) {
        log.info(String.format("Sending mail message to host: '%s://%s:%s'", javaMailSender.getProtocol(), javaMailSender.getHost(), javaMailSender.getPort()));

        MimeMessage mimeMessage = createMailMessage(message);
        javaMailSender.send(mimeMessage);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String mailMessageContent;
        try {
            mimeMessage.writeTo(bos);
            mailMessageContent = bos.toString(); //TODO use charset
        } catch (IOException e) {
            mailMessageContent = message.toString();
        } catch (MessagingException e) {
            mailMessageContent = message.toString();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                log.warn("Failed to close output stream", e);
            }
        }

        if (messageListener != null) {
            messageListener.onOutboundMessage(mailMessageContent);
        } else {
            log.info("Sent message is:" + System.getProperty("line.separator") + mailMessageContent);
        }

        log.info(String.format("Message was successfully sent to host: '%s://%s:%s'", javaMailSender.getProtocol(), javaMailSender.getHost(), javaMailSender.getPort()));
    }

    /**
     * Create mime mail message from Citrus mail message object payload.
     * @param message
     * @return
     */
    protected MimeMessage createMailMessage(Message<?> message) {
        Object payload = message.getPayload();

        MailMessage mailMessage = null;
        if (payload != null) {
            if (payload instanceof MailMessage) {
                mailMessage = (MailMessage) payload;
            } else if (payload instanceof String) {
                mailMessage = (MailMessage) mailMessageMapper.fromXML(payload.toString());

            }
        }

        if (mailMessage == null) {
            throw new CitrusRuntimeException("Unable to create proper mail message from paylaod: " + payload);
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        try {
            mimeMessageHelper.setFrom(mailMessage.getFrom());
            mimeMessageHelper.setTo(StringUtils.commaDelimitedListToStringArray(mailMessage.getTo()));

            if (StringUtils.hasText(mailMessage.getCc())) {
                mimeMessageHelper.setCc(StringUtils.commaDelimitedListToStringArray(mailMessage.getCc()));
            }

            if (StringUtils.hasText(mailMessage.getBcc())) {
                mimeMessageHelper.setBcc(StringUtils.commaDelimitedListToStringArray(mailMessage.getBcc()));
            }

            mimeMessageHelper.setReplyTo(mailMessage.getReplyTo() != null ? mailMessage.getReplyTo() : mailMessage.getFrom());
            mimeMessageHelper.setSentDate(new Date());
            mimeMessageHelper.setSubject(mailMessage.getSubject());
            mimeMessageHelper.setText(mailMessage.getBody().getContent());

            if (mailMessage.getBody().hasAttachments()) {
                for (AttachmentPart attachmentPart : mailMessage.getBody().getAttachments()) {
                    mimeMessageHelper.addAttachment(attachmentPart.getFileName(),
                            new InputStreamResource(new StringSource(attachmentPart.getContent()).getInputStream()),
                            attachmentPart.getContentType());
                }
            }
        } catch (MessagingException e) {
            throw new CitrusRuntimeException("Failed to create mail mime message", e);
        }

        return mimeMessage;
    }

    /**
     * Set the mail protocol. Default is "smtp".
     * @param protocol
     */
    public void setProtocol(String protocol) {
        javaMailSender.setProtocol(protocol);
    }

    /**
     * Set the mail server host, typically an SMTP host.
     * @param host
     */
    public void setHost(String host) {
        javaMailSender.setHost(host);
    }

    /**
     * Set the mail server port.
     * Default is the Java mail port for SMTP (25).
     * @param port
     */
    public void setPort(int port) {
        javaMailSender.setPort(port);
    }

    /**
     * Set the username for accessing the mail host. Underlying mail seesion
     * has to be configured with the property <code>"mail.smtp.auth"</code> set to
     * <code>true</code>.
     * @param username
     */
    public void setUsername(String username) {
        javaMailSender.setUsername(username);
    }

    /**
     * Set the password for accessing the mail host. Underlying mail seesion
     * has to be configured with the property <code>"mail.smtp.auth"</code> set to
     * <code>true</code>.
     * @param password
     */
    public void setPassword(String password) {
        javaMailSender.setPassword(password);
    }

    /**
     * Set JavaMail properties for the mail session such as <code>"mail.smtp.auth"</code>
     * when using username and password. New session is created when properties are set.
     * @param javaMailProperties
     */
    public void setJavaMailProperties(Properties javaMailProperties) {
        javaMailSender.setJavaMailProperties(javaMailProperties);
    }

    /**
     * Gets the message listeners.
     * @return
     */
    public MessageListeners getMessageListener() {
        return messageListener;
    }

    /**
     * Sets the message listeners.
     * @param messageListener
     */
    public void setMessageListener(MessageListeners messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Gets the mail message mapper implementation.
     * @return
     */
    public MailMessageMapper getMailMessageMapper() {
        return mailMessageMapper;
    }

    /**
     * Sets the mail message mapper implementation.
     * @param mailMessageMapper
     */
    public void setMailMessageMapper(MailMessageMapper mailMessageMapper) {
        this.mailMessageMapper = mailMessageMapper;
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return actor;
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        this.actor = actor;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
