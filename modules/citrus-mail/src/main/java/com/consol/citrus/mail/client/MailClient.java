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

package com.consol.citrus.mail.client;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.RawMessage;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailClient extends AbstractEndpoint implements Producer, InitializingBean {
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
        log.info(String.format("Sending mail message to host: '%s://%s:%s'", getEndpointConfiguration().getProtocol(), getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort()));

        MimeMailMessage mimeMessage = getEndpointConfiguration().getMessageConverter().convertOutbound(message, getEndpointConfiguration());
        getEndpointConfiguration().getJavaMailSender().send(mimeMessage.getMimeMessage());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Message mailMessage;
        try {
            mimeMessage.getMimeMessage().writeTo(bos);
            mailMessage = new RawMessage(bos.toString()); //TODO use message charset encoding
        } catch (IOException e) {
            mailMessage = message;
        } catch (MessagingException e) {
            mailMessage = message;
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                log.warn("Failed to close output stream", e);
            }
        }

        context.onOutboundMessage(mailMessage);

        log.info(String.format("Message was successfully sent to host: '%s://%s:%s'", getEndpointConfiguration().getProtocol(), getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort()));
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
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.hasText(getEndpointConfiguration().getJavaMailSender().getUsername()) ||
                StringUtils.hasText(getEndpointConfiguration().getJavaMailSender().getPassword())) {

            Properties javaMailProperties = getEndpointConfiguration().getJavaMailSender().getJavaMailProperties();
            if (javaMailProperties == null) {
                javaMailProperties = new Properties();
            }

            javaMailProperties.setProperty("mail.smtp.auth", "true");
            getEndpointConfiguration().getJavaMailSender().setJavaMailProperties(javaMailProperties);
        }

        if (!StringUtils.hasText(getEndpointConfiguration().getJavaMailSender().getProtocol())) {
            getEndpointConfiguration().getJavaMailSender().setProtocol("smtp");
        }
    }

}
