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

package org.citrusframework.mail.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jakarta.mail.Authenticator;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.RawMessage;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMailMessage;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailClient extends AbstractEndpoint implements Producer, InitializingPhase {

    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    private MailSender mailSender = new MailSender();

    /**
     * Default constructor initializing endpoint configuration.
     */
    public MailClient() {
        super(new MailEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     */
    public MailClient(MailEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public MailEndpointConfiguration getEndpointConfiguration() {
        return (MailEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void send(Message message, TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Sending mail message to host: '%s://%s:%s'", getEndpointConfiguration().getProtocol(), getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort()));
        }

        MimeMailMessage mimeMessage = getEndpointConfiguration().getMessageConverter().convertOutbound(message, getEndpointConfiguration(), context);

        try {
            mailSender.send(mimeMessage.getMimeMessage());
        } catch (MessagingException e) {
            throw new CitrusRuntimeException("Failed to send mail message!", e);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Message mailMessage;
        try {
            mimeMessage.getMimeMessage().writeTo(bos);
            mailMessage = new RawMessage(bos.toString()); //TODO use message charset encoding
        } catch (IOException | MessagingException e) {
            mailMessage = message;
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                logger.warn("Failed to close output stream", e);
            }
        }

        context.onOutboundMessage(mailMessage);

        logger.info(String.format("Mail message was sent to host: '%s://%s:%s'", getEndpointConfiguration().getProtocol(), getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort()));
    }

    /**
     * Creates a message producer for this endpoint for sending messages
     * to this endpoint.
     */
    @Override
    public Producer createProducer() {
        return this;
    }

    /**
     * Creates a message consumer for this endpoint. Consumer receives
     * messages on this endpoint.
     */
    @Override
    public Consumer createConsumer() {
        throw new CitrusRuntimeException("Mail client is unable to create message consumer!");
    }

    @Override
    public void initialize() {
        if (!StringUtils.hasText(getEndpointConfiguration().getProtocol())) {
            throw new CitrusRuntimeException("A mailing protocol must be configured!");
        }

        if (StringUtils.hasText(getEndpointConfiguration().getUsername()) ||
                StringUtils.hasText(getEndpointConfiguration().getPassword())) {

            getEndpointConfiguration().getJavaMailProperties().setProperty("mail." + getEndpointConfiguration().getProtocol() + ".auth", "true");
            getEndpointConfiguration().setAuthenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getEndpointConfiguration().getUsername(), getEndpointConfiguration().getPassword());
                }
            });
        }

        if (StringUtils.hasText(getEndpointConfiguration().getHost())) {
            getEndpointConfiguration().getJavaMailProperties().setProperty("mail." + getEndpointConfiguration().getProtocol() + ".host", getEndpointConfiguration().getHost());
        }
        if (getEndpointConfiguration().getPort() > 0) {
            getEndpointConfiguration().getJavaMailProperties().setProperty("mail." + getEndpointConfiguration().getProtocol() + ".port", String.valueOf(getEndpointConfiguration().getPort()));
        }
    }

    void setMailSender(MailSender mailSenderMock) {
        this.mailSender = mailSenderMock;
    }
}
