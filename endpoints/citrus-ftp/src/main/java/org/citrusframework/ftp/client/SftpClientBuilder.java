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

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class SftpClientBuilder extends AbstractEndpointBuilder<SftpClient> {

    /** Endpoint target */
    private SftpClient endpoint = new SftpClient();

    @Override
    protected SftpClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public SftpClientBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public SftpClientBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the auto read files property.
     * @param autoReadFiles
     * @return
     */
    public SftpClientBuilder autoReadFiles(boolean autoReadFiles) {
        endpoint.getEndpointConfiguration().setAutoReadFiles(autoReadFiles);
        return this;
    }

    /**
     * Sets the local passive mode property.
     * @param localPassiveMode
     * @return
     */
    public SftpClientBuilder localPassiveMode(boolean localPassiveMode) {
        endpoint.getEndpointConfiguration().setLocalPassiveMode(localPassiveMode);
        return this;
    }

    /**
     * Sets the client username.
     * @param username
     * @return
     */
    public SftpClientBuilder username(String username) {
        endpoint.getEndpointConfiguration().setUser(username);
        return this;
    }

    /**
     * Sets the client password.
     * @param password
     * @return
     */
    public SftpClientBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    /**
     * Sets the privateKeyPath property.
     * @param privateKeyPath
     * @return
     */
    public SftpClientBuilder privateKeyPath(String privateKeyPath) {
        endpoint.getEndpointConfiguration().setPrivateKeyPath(privateKeyPath);
        return this;
    }

    /**
     * Sets the privateKeyPassword property.
     * @param privateKeyPassword
     * @return
     */
    public SftpClientBuilder privateKeyPassword(String privateKeyPassword) {
        endpoint.getEndpointConfiguration().setPrivateKeyPassword(privateKeyPassword);
        return this;
    }

    /**
     * Sets the strictHostChecking property.
     * @param strictHostChecking
     * @return
     */
    public SftpClientBuilder strictHostChecking(boolean strictHostChecking) {
        endpoint.getEndpointConfiguration().setStrictHostChecking(strictHostChecking);
        return this;
    }

    /**
     * Sets the knownHosts property.
     * @param knownHosts
     * @return
     */
    public SftpClientBuilder knownHosts(String knownHosts) {
        endpoint.getEndpointConfiguration().setKnownHosts(knownHosts);
        return this;
    }

    /**
     * Sets the preferred authentications property.
     * @param preferredAuthentications
     * @return
     */
    public SftpClientBuilder preferredAuthentications(String preferredAuthentications) {
        endpoint.getEndpointConfiguration().setPreferredAuthentications(preferredAuthentications);
        return this;
    }

    /**
     * Sets the sessionConfigs property.
     * @param sessionConfigs
     * @return
     */
    public SftpClientBuilder sessionConfigs(Map<String, String> sessionConfigs) {
        endpoint.getEndpointConfiguration().setSessionConfigs(sessionConfigs);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public SftpClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the error handling strategy.
     * @param errorStrategy
     * @return
     */
    public SftpClientBuilder errorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(errorStrategy);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public SftpClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public SftpClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
