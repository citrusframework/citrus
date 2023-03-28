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
import java.util.Properties;

import org.citrusframework.common.InitializingPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.RawMessage;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailClient extends AbstractEndpoint implements Producer, InitializingPhase {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MailClient.class);

    /**
     * Default constructor initializing endpoint configuration.
     */
    public MailClient() {
        super(new MailEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
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
        if (log.isDebugEnabled()) {
            log.debug(String.format("Sending mail message to host: '%s://%s:%s'", getEndpointConfiguration().getProtocol(), getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort()));
        }

        MimeMailMessage mimeMessage = getEndpointConfiguration().getMessageConverter().convertOutbound(message, getEndpointConfiguration(), context);
        getEndpointConfiguration().getJavaMailSender().send(mimeMessage.getMimeMessage());

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
                log.warn("Failed to close output stream", e);
            }
        }

        context.onOutboundMessage(mailMessage);

        log.info(String.format("Mail message was sent to host: '%s://%s:%s'", getEndpointConfiguration().getProtocol(), getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort()));
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
     *
     * @return
     */
    @Override
    public Consumer createConsumer() {
        throw new CitrusRuntimeException("Mail client is unable to create message consumer!");
    }

    @Override
    public void initialize() {
        if (StringUtils.hasText(getEndpointConfiguration().getJavaMailSender().getUsername()) ||
                StringUtils.hasText(getEndpointConfiguration().getJavaMailSender().getPassword())) {

            Properties javaMailProperties = getEndpointConfiguration().getJavaMailSender().getJavaMailProperties();

            javaMailProperties.setProperty("mail.smtp.auth", "true");
            getEndpointConfiguration().getJavaMailSender().setJavaMailProperties(javaMailProperties);
        }

        if (!StringUtils.hasText(getEndpointConfiguration().getJavaMailSender().getProtocol())) {
            getEndpointConfiguration().getJavaMailSender().setProtocol("smtp");
        }
    }

}
