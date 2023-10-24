/*
 * Copyright 2006-2018 the original author or authors.
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

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class ScpClientBuilder extends AbstractEndpointBuilder<ScpClient> {

    /** Endpoint target */
    private ScpClient endpoint = new ScpClient();

    @Override
    protected ScpClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port option property.
     * @param option
     * @return
     */
    public ScpClientBuilder portOption(String option) {
        endpoint.getEndpointConfiguration().setPortOption(option);
        return this;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public ScpClientBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public ScpClientBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the auto read files property.
     *
     * @param autoReadFiles
     * @return
     */
    public ScpClientBuilder autoReadFiles(boolean autoReadFiles) {
        endpoint.getEndpointConfiguration().setAutoReadFiles(autoReadFiles);
        return this;
    }

    /**
     * Sets the client username.
     * @param username
     * @return
     */
    public ScpClientBuilder username(String username) {
        endpoint.getEndpointConfiguration().setUser(username);
        return this;
    }

    /**
     * Sets the client password.
     * @param password
     * @return
     */
    public ScpClientBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    /**
     * Sets the privateKeyPath property.
     * @param privateKeyPath
     * @return
     */
    public ScpClientBuilder privateKeyPath(String privateKeyPath) {
        endpoint.getEndpointConfiguration().setPrivateKeyPath(privateKeyPath);
        return this;
    }

    /**
     * Sets the privateKeyPassword property.
     * @param privateKeyPassword
     * @return
     */
    public ScpClientBuilder privateKeyPassword(String privateKeyPassword) {
        endpoint.getEndpointConfiguration().setPrivateKeyPassword(privateKeyPassword);
        return this;
    }

    /**
     * Sets the strictHostChecking property.
     * @param strictHostChecking
     * @return
     */
    public ScpClientBuilder strictHostChecking(boolean strictHostChecking) {
        endpoint.getEndpointConfiguration().setStrictHostChecking(strictHostChecking);
        return this;
    }

    /**
     * Sets the knownHosts property.
     * @param knownHosts
     * @return
     */
    public ScpClientBuilder knownHosts(String knownHosts) {
        endpoint.getEndpointConfiguration().setKnownHosts(knownHosts);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public ScpClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the error handling strategy.
     * @param errorStrategy
     * @return
     */
    public ScpClientBuilder errorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(errorStrategy);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public ScpClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public ScpClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
