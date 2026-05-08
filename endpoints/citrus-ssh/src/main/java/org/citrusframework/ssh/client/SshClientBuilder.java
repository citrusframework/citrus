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

package org.citrusframework.ssh.client;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.ssh.message.SshMessageConverter;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.SchemaType;

/**
 * @since 2.5
 */
@SchemaType(module = "citrus-ssh")
@XmlType(name = "", propOrder = {})
public class SshClientBuilder extends AbstractEndpointBuilder<SshClient> {

    /** Endpoint target */
    private final SshClient endpoint = new SshClient();

    private String messageConverter;
    private String correlator;

    @Override
    public SshClient build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, SshMessageConverter.class));
            }

            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }
        }

        return super.build();
    }

    @Override
    protected SshClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the host property.
     */
    public SshClientBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    @SchemaProperty(description = "Sets the host.")
    @XmlAttribute
    public void setHost(String host) {
        host(host);
    }

    /**
     * Sets the port property.
     */
    public SshClientBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
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
    public SshClientBuilder user(String user) {
        endpoint.getEndpointConfiguration().setUser(user);
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
    public SshClientBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    @SchemaProperty(description = "Sets the password.")
    @XmlAttribute
    public void setPassword(String password) {
        password(password);
    }

    /**
     * Sets the privateKeyPath property.
     */
    public SshClientBuilder privateKeyPath(String privateKeyPath) {
        endpoint.getEndpointConfiguration().setPrivateKeyPath(privateKeyPath);
        return this;
    }

    @SchemaProperty(description = "Sets the private key path.")
    @XmlAttribute(name = "privet-key-path")
    public void setPrivateKeyPath(String privateKeyPath) {
        privateKeyPath(privateKeyPath);
    }

    /**
     * Sets the privateKeyPassword property.
     */
    public SshClientBuilder privateKeyPassword(String privateKeyPassword) {
        endpoint.getEndpointConfiguration().setPrivateKeyPassword(privateKeyPassword);
        return this;
    }

    @SchemaProperty(description = "Sets the private key password.")
    @XmlAttribute(name = "private-kay-password")
    public void setPrivateKeyPassword(String privateKeyPassword) {
        privateKeyPassword(privateKeyPassword);
    }

    /**
     * Sets the strictHostChecking property.
     */
    public SshClientBuilder strictHostChecking(boolean strictHostChecking) {
        endpoint.getEndpointConfiguration().setStrictHostChecking(strictHostChecking);
        return this;
    }

    @SchemaProperty(description = "Sets the strict host checking.")
    @XmlAttribute(name = "strict-host-checking")
    public void setStrictHostChecking(boolean strictHostChecking) {
        strictHostChecking(strictHostChecking);
    }

    /**
     * Sets the knownHosts property.
     */
    public SshClientBuilder knownHosts(String knownHosts) {
        endpoint.getEndpointConfiguration().setKnownHosts(knownHosts);
        return this;
    }

    @SchemaProperty(description = "Sets the known hosts.")
    @XmlAttribute(name = "known-hosts")
    public void setKnownHosts(String knownHosts) {
        knownHosts(knownHosts);
    }

    /**
     * Sets the commandTimeout property.
     */
    public SshClientBuilder commandTimeout(long commandTimeout) {
        endpoint.getEndpointConfiguration().setCommandTimeout(commandTimeout);
        return this;
    }

    @SchemaProperty(description = "Sets the command timeout.")
    @XmlAttribute(name = "command-timeout")
    public void setCommandTimeout(long commandTimeout) {
        commandTimeout(commandTimeout);
    }

    /**
     * Sets the connectionTimeout property.
     */
    public SshClientBuilder connectionTimeout(int connectionTimeout) {
        endpoint.getEndpointConfiguration().setConnectionTimeout(connectionTimeout);
        return this;
    }

    @SchemaProperty(description = "Sets the connection timeout.")
    @XmlAttribute(name = "connection-timeout")
    public void setConnectionTimeout(int connectionTimeout) {
        connectionTimeout(connectionTimeout);
    }

    /**
     * Sets the message converter.
     */
    public SshClientBuilder messageConverter(SshMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(description = "Sets the message converter.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the message correlator.
     */
    public SshClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(description = "Sets the message correlator.")
    @XmlAttribute(name = "message-correlator")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the polling interval.
     */
    public SshClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Sets the polling interval.")
    @XmlAttribute(name = "polling-interval")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the default timeout.
     */
    public SshClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "Sets the default timeout.")
    @XmlAttribute
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
