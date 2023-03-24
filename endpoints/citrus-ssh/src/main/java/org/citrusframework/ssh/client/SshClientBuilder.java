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

package org.citrusframework.ssh.client;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.ssh.message.SshMessageConverter;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class SshClientBuilder extends AbstractEndpointBuilder<SshClient> {

    /** Endpoint target */
    private SshClient endpoint = new SshClient();

    @Override
    protected SshClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public SshClientBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public SshClientBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the user property.
     * @param user
     * @return
     */
    public SshClientBuilder user(String user) {
        endpoint.getEndpointConfiguration().setUser(user);
        return this;
    }

    /**
     * Sets the client password.
     * @param password
     * @return
     */
    public SshClientBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    /**
     * Sets the privateKeyPath property.
     * @param privateKeyPath
     * @return
     */
    public SshClientBuilder privateKeyPath(String privateKeyPath) {
        endpoint.getEndpointConfiguration().setPrivateKeyPath(privateKeyPath);
        return this;
    }

    /**
     * Sets the privateKeyPassword property.
     * @param privateKeyPassword
     * @return
     */
    public SshClientBuilder privateKeyPassword(String privateKeyPassword) {
        endpoint.getEndpointConfiguration().setPrivateKeyPassword(privateKeyPassword);
        return this;
    }

    /**
     * Sets the strictHostChecking property.
     * @param strictHostChecking
     * @return
     */
    public SshClientBuilder strictHostChecking(boolean strictHostChecking) {
        endpoint.getEndpointConfiguration().setStrictHostChecking(strictHostChecking);
        return this;
    }

    /**
     * Sets the knownHosts property.
     * @param knownHosts
     * @return
     */
    public SshClientBuilder knownHosts(String knownHosts) {
        endpoint.getEndpointConfiguration().setKnownHosts(knownHosts);
        return this;
    }

    /**
     * Sets the commandTimeout property.
     * @param commandTimeout
     * @return
     */
    public SshClientBuilder commandTimeout(long commandTimeout) {
        endpoint.getEndpointConfiguration().setCommandTimeout(commandTimeout);
        return this;
    }

    /**
     * Sets the connectionTimeout property.
     * @param connectionTimeout
     * @return
     */
    public SshClientBuilder connectionTimeout(int connectionTimeout) {
        endpoint.getEndpointConfiguration().setConnectionTimeout(connectionTimeout);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public SshClientBuilder messageConverter(SshMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public SshClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public SshClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public SshClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
