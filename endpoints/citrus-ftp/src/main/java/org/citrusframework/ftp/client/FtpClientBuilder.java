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
 * @since 2.5
 */
public class FtpClientBuilder extends AbstractEndpointBuilder<FtpClient> {

    /** Endpoint target */
    private final FtpClient endpoint = new FtpClient();

    private String correlator;

    @Override
    public FtpClient build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }
        }

        return super.build();
    }

    @Override
    protected FtpClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the host property.
     */
    public FtpClientBuilder host(String host) {
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
    public FtpClientBuilder port(int port) {
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
    public FtpClientBuilder autoReadFiles(boolean autoReadFiles) {
        endpoint.getEndpointConfiguration().setAutoReadFiles(autoReadFiles);
        return this;
    }

    @SchemaProperty(description = "When enabled the client automatically reads new files.")
    public void setAutoReadFiles(boolean autoReadFiles) {
        autoReadFiles(autoReadFiles);
    }

    /**
     * Sets the local passive mode property.
     */
    public FtpClientBuilder localPassiveMode(boolean localPassiveMode) {
        endpoint.getEndpointConfiguration().setLocalPassiveMode(localPassiveMode);
        return this;
    }

    @SchemaProperty(description = "SEnables the local passive mode.")
    public void setLocalPassiveMode(boolean localPassiveMode) {
        localPassiveMode(localPassiveMode);
    }

    /**
     * Sets the client username.
     */
    public FtpClientBuilder username(String username) {
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
    public FtpClientBuilder password(String password) {
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
     * Sets the message correlator.
     */
    public FtpClientBuilder correlator(MessageCorrelator correlator) {
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
    public FtpClientBuilder errorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
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
    public FtpClientBuilder pollingInterval(int pollingInterval) {
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
    public FtpClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The endpoint timeout when waiting for messages.")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
