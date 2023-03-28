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

package org.citrusframework.ftp.client;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class FtpClientBuilder extends AbstractEndpointBuilder<FtpClient> {

    /** Endpoint target */
    private final FtpClient endpoint = new FtpClient();

    @Override
    protected FtpClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public FtpClientBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public FtpClientBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the auto read files property.
     * @param autoReadFiles
     * @return
     */
    public FtpClientBuilder autoReadFiles(boolean autoReadFiles) {
        endpoint.getEndpointConfiguration().setAutoReadFiles(autoReadFiles);
        return this;
    }

    /**
     * Sets the local passive mode property.
     * @param localPassiveMode
     * @return
     */
    public FtpClientBuilder localPassiveMode(boolean localPassiveMode) {
        endpoint.getEndpointConfiguration().setLocalPassiveMode(localPassiveMode);
        return this;
    }

    /**
     * Sets the client username.
     * @param username
     * @return
     */
    public FtpClientBuilder username(String username) {
        endpoint.getEndpointConfiguration().setUser(username);
        return this;
    }

    /**
     * Sets the client password.
     * @param password
     * @return
     */
    public FtpClientBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public FtpClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the error handling strategy.
     * @param errorStrategy
     * @return
     */
    public FtpClientBuilder errorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(errorStrategy);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public FtpClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public FtpClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
