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

package org.citrusframework.mail.client;

import java.util.Properties;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.mail.message.MailMessageConverter;
import org.citrusframework.mail.model.MailMarshaller;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class MailClientBuilder extends AbstractEndpointBuilder<MailClient> {

    /** Endpoint target */
    private final MailClient endpoint = new MailClient();

    @Override
    protected MailClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public MailClientBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the port.
     * @param port
     * @return
     */
    public MailClientBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the protocol property.
     * @param protocol
     * @return
     */
    public MailClientBuilder protocol(String protocol) {
        endpoint.getEndpointConfiguration().setProtocol(protocol);
        return this;
    }

    /**
     * Sets the username property.
     * @param username
     * @return
     */
    public MailClientBuilder username(String username) {
        endpoint.getEndpointConfiguration().setUsername(username);
        return this;
    }

    /**
     * Sets the password property.
     * @param password
     * @return
     */
    public MailClientBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    /**
     * Sets the Java mail properties.
     * @param javaMailProperties
     * @return
     */
    public MailClientBuilder javaMailProperties(Properties javaMailProperties) {
        endpoint.getEndpointConfiguration().setJavaMailProperties(javaMailProperties);
        return this;
    }

    /**
     * Sets the mail message marshaller.
     * @param marshaller
     * @return
     */
    public MailClientBuilder marshaller(MailMarshaller marshaller) {
        endpoint.getEndpointConfiguration().setMarshaller(marshaller);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public MailClientBuilder messageConverter(MailMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public MailClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
