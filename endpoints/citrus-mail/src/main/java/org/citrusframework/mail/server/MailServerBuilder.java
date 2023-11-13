/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.mail.server;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.icegreen.greenmail.util.GreenMail;
import org.citrusframework.mail.message.MailMessageConverter;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.server.AbstractServerBuilder;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class MailServerBuilder extends AbstractServerBuilder<MailServer, MailServerBuilder> {

    /** Endpoint target */
    private final MailServer endpoint = new MailServer();

    @Override
    protected MailServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public MailServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    /**
     * Sets the mail marshaller.
     * @param marshaller
     * @return
     */
    public MailServerBuilder marshaller(MailMarshaller marshaller) {
        endpoint.setMarshaller(marshaller);
        return this;
    }

    /**
     * Sets the Java mail properties.
     * @param javaMailProperties
     * @return
     */
    public MailServerBuilder javaMailProperties(Properties javaMailProperties) {
        endpoint.setJavaMailProperties(javaMailProperties);
        return this;
    }

    /**
     * Enables/disables user authentication.
     * @param authRequired
     * @return
     */
    public MailServerBuilder authRequired(boolean authRequired) {
        endpoint.setAuthRequired(authRequired);
        return this;
    }

    /**
     * Enables/disables auto accept.
     * @param autoAccept
     * @return
     */
    public MailServerBuilder autoAccept(boolean autoAccept) {
        endpoint.setAutoAccept(autoAccept);
        return this;
    }

    /**
     * Enables/disables split multipart.
     * @param splitMultipart
     * @return
     */
    public MailServerBuilder splitMultipart(boolean splitMultipart) {
        endpoint.setSplitMultipart(splitMultipart);
        return this;
    }

    /**
     * Sets the smtpServer property.
     * @param smtpServer
     * @return
     */
    public MailServerBuilder smtp(GreenMail smtpServer) {
        endpoint.setSmtpServer(smtpServer);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public MailServerBuilder messageConverter(MailMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the known users.
     * @param users
     * @return
     */
    public MailServerBuilder knownUsers(List<String> users) {
        endpoint.setKnownUsers(users);
        return this;
    }

    /**
     * Sets the known users.
     * @param users
     * @return
     */
    public MailServerBuilder knownUsers(String... users) {
        endpoint.setKnownUsers(Arrays.asList(users));
        return this;
    }
}
