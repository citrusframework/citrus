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

package com.consol.citrus.ftp.client;

import com.consol.citrus.endpoint.AbstractEndpointBuilder;
import com.consol.citrus.message.MessageCorrelator;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class FtpClientBuilder extends AbstractEndpointBuilder<FtpClient> {

    /** Endpoint target */
    private FtpClient endpoint = new FtpClient();

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
