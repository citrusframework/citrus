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

package org.citrusframework.ssh.server;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.ssh.message.SshMessageConverter;
import org.citrusframework.ssh.model.SshMarshaller;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.SchemaType;

/**
 * @since 2.5
 */
@SchemaType(module = "citrus-ssh")
@XmlType(name = "", propOrder = {})
public class SshServerBuilder extends AbstractServerBuilder<SshServer, SshServerBuilder> {

    /** Endpoint target */
    private final SshServer endpoint = new SshServer();

    private String messageConverter;
    private String marshaller;

    @Override
    public SshServer build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, SshMessageConverter.class));
            }

            if (StringUtils.hasText(marshaller)) {
                marshaller(referenceResolver.resolve(marshaller, SshMarshaller.class));
            }
        }

        return super.build();
    }

    @Override
    protected SshServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     */
    public SshServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    @SchemaProperty(description = "Sets the port.")
    @XmlAttribute
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the user property.
     */
    public SshServerBuilder user(String user) {
        endpoint.setUser(user);
        return this;
    }

    @SchemaProperty(description = "Sets the username.")
    @XmlAttribute
    public void setUser(String user) {
        user(user);
    }

    /**
     * Sets the client password.
     */
    public SshServerBuilder password(String password) {
        endpoint.setPassword(password);
        return this;
    }

    @SchemaProperty(description = "Sets the password.")
    @XmlAttribute
    public void setPassword(String password) {
        password(password);
    }

    /**
     * Sets the hostKeyPath property.
     */
    public SshServerBuilder hostKeyPath(String hostKeyPath) {
        endpoint.setHostKeyPath(hostKeyPath);
        return this;
    }

    @SchemaProperty(description = "Sets the host key path")
    @XmlAttribute(name = "host-key-path")
    public void setHostKeyPath(String hostKeyPath) {
        hostKeyPath(hostKeyPath);
    }

    /**
     * Sets the userHomePath property.
     */
    public SshServerBuilder userHomePath(String userHomePath) {
        endpoint.setUserHomePath(userHomePath);
        return this;
    }

    @SchemaProperty(description = "Sets the user home path")
    @XmlAttribute(name = "user-home-path")
    public void setUserHomePath(String userHomePath) {
        userHomePath(userHomePath);
    }

    /**
     * Sets the allowedKeyPath property.
     */
    public SshServerBuilder allowedKeyPath(String allowedKeyPath) {
        endpoint.setAllowedKeyPath(allowedKeyPath);
        return this;
    }

    @SchemaProperty(description = "Sets the allowed key path.")
    @XmlAttribute(name = "allowed-key-path")
    public void setAllowedKeyPath(String allowedKeyPath) {
        allowedKeyPath(allowedKeyPath);
    }

    /**
     * Sets the message converter.
     */
    public SshServerBuilder messageConverter(SshMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(description = "Sets the message converter.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the marshaller.
     */
    public SshServerBuilder marshaller(SshMarshaller marshaller) {
        endpoint.setMarshaller(marshaller);
        return this;
    }

    @SchemaProperty(description = "Sets the marshaller as a bean reference.")
    @XmlAttribute
    public void setMarshaller(String marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Sets the polling interval.
     */
    public SshServerBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Sets the polling interval.")
    @XmlAttribute(name = "polling-interval")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }
}
