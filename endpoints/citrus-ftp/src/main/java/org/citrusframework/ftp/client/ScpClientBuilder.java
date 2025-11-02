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

package org.citrusframework.ftp.client;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.7.6
 */
public class ScpClientBuilder extends AbstractEndpointBuilder<ScpClient> {

    /** Endpoint target */
    private final ScpClient endpoint = new ScpClient();

    private String correlator;

    @Override
    public ScpClient build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }
        }

        return super.build();
    }

    @Override
    protected ScpClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port option property.
     */
    public ScpClientBuilder portOption(String option) {
        endpoint.getEndpointConfiguration().setPortOption(option);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the port option.")
    public void setPortOption(String option) {
        portOption(option);
    }

    /**
     * Sets the host property.
     */
    public ScpClientBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    @SchemaProperty(description = "The Ftp server host.")
    public void setHost(String host) {
        host(host);
    }

    /**
     * Sets the port property.
     */
    public ScpClientBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    @SchemaProperty(description = "The Ftp server port.")
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the auto read files property.
     */
    public ScpClientBuilder autoReadFiles(boolean autoReadFiles) {
        endpoint.getEndpointConfiguration().setAutoReadFiles(autoReadFiles);
        return this;
    }

    @SchemaProperty(description = "When enabled the client automatically reads new files.")
    public void setAutoReadFiles(boolean autoReadFiles) {
        autoReadFiles(autoReadFiles);
    }

    /**
     * Sets the client username.
     */
    public ScpClientBuilder username(String username) {
        endpoint.getEndpointConfiguration().setUser(username);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the user name."
    )
    public void setUsername(String username) {
        username(username);
    }

    /**
     * Sets the client password.
     */
    public ScpClientBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the user password."
    )
    public void setPassword(String password) {
        password(password);
    }

    /**
     * Sets the privateKeyPath property.
     */
    public ScpClientBuilder privateKeyPath(String privateKeyPath) {
        endpoint.getEndpointConfiguration().setPrivateKeyPath(privateKeyPath);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the private key path."
    )
    public void setPrivateKeyPath(String privateKeyPath) {
        privateKeyPath(privateKeyPath);
    }

    /**
     * Sets the privateKeyPassword property.
     */
    public ScpClientBuilder privateKeyPassword(String privateKeyPassword) {
        endpoint.getEndpointConfiguration().setPrivateKeyPassword(privateKeyPassword);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the private key password."
    )
    public void setPrivateKeyPassword(String privateKeyPassword) {
        privateKeyPassword(privateKeyPassword);
    }

    /**
     * Sets the strictHostChecking property.
     */
    public ScpClientBuilder strictHostChecking(boolean strictHostChecking) {
        endpoint.getEndpointConfiguration().setStrictHostChecking(strictHostChecking);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Enable strict host checking."
    )
    public void setStrictHostChecking(boolean strictHostChecking) {
        strictHostChecking(strictHostChecking);
    }

    /**
     * Sets the knownHosts property.
     */
    public ScpClientBuilder knownHosts(String knownHosts) {
        endpoint.getEndpointConfiguration().setKnownHosts(knownHosts);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "List of known hosts."
    )
    public void setKnownHosts(String knownHosts) {
        knownHosts(knownHosts);
    }

    /**
     * Sets the message correlator.
     */
    public ScpClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message correlator.")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the error handling strategy.
     */
    public ScpClientBuilder errorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(errorStrategy);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:errorHandler") },
            description = "Sets the error handling strategy."
    )
    public void setErrorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
        errorHandlingStrategy(errorStrategy);
    }

    /**
     * Sets the polling interval.
     */
    public ScpClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Sets the polling interval when consuming messages.")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the default timeout.
     */
    public ScpClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The endpoint timeout when waiting for messages.")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
