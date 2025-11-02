/*
 * Copyright the original author or authors.
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

import java.util.Map;
import java.util.Properties;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.mail.message.MailMessageConverter;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.5
 */
public class MailClientBuilder extends AbstractEndpointBuilder<MailClient> {

    /** Endpoint target */
    private final MailClient endpoint = new MailClient();

    private String marshaller;
    private String messageConverter;

    @Override
    public MailClient build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(marshaller)) {
                marshaller(referenceResolver.resolve(marshaller, MailMarshaller.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, MailMessageConverter.class));
            }
        }
        return super.build();
    }

    @Override
    protected MailClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the host property.
     */
    public MailClientBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    @SchemaProperty(description = "The mail server host to connect to.")
    public void setHost(String host) {
        host(host);
    }

    /**
     * Sets the port.
     */
    public MailClientBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    @SchemaProperty(description = "The mail server port.")
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the protocol property.
     */
    public MailClientBuilder protocol(String protocol) {
        endpoint.getEndpointConfiguration().setProtocol(protocol);
        return this;
    }

    @SchemaProperty(description = "The mail protocol.")
    public void setProtocol(String protocol) {
        protocol(protocol);
    }

    /**
     * Sets the username property.
     */
    public MailClientBuilder username(String username) {
        endpoint.getEndpointConfiguration().setUsername(username);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "The client user name."
    )
    public void setUsername(String username) {
        username(username);
    }

    /**
     * Sets the password property.
     */
    public MailClientBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "The client user password."
    )
    public void setPassword(String password) {
        password(password);
    }

    /**
     * Sets the Java mail properties.
     */
    public MailClientBuilder javaMailProperties(Properties javaMailProperties) {
        endpoint.getEndpointConfiguration().setJavaMailProperties(javaMailProperties);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Custom properties passed to the java mail implementation.")
    public void setJavaMailProperties(Map<String, Object> javaMailProperties) {
        Properties props = new Properties();
        props.putAll(javaMailProperties);
        javaMailProperties(props);
    }

    /**
     * Sets the mail message marshaller.
     */
    public MailClientBuilder marshaller(MailMarshaller marshaller) {
        endpoint.getEndpointConfiguration().setMarshaller(marshaller);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom mail message marshaller.")
    public void setMarshaller(String marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Sets the message converter.
     */
    public MailClientBuilder messageConverter(MailMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets custom message converter.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the default timeout.
     */
    public MailClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "Mail client timeout when waiting for a response.")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
