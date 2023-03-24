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

package org.citrusframework.ssh.server;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.ssh.message.SshMessageConverter;
import org.citrusframework.ssh.model.SshMarshaller;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class SshServerBuilder extends AbstractServerBuilder<SshServer, SshServerBuilder> {

    /** Endpoint target */
    private final SshServer endpoint = new SshServer();

    @Override
    protected SshServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public SshServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    /**
     * Sets the user property.
     * @param user
     * @return
     */
    public SshServerBuilder user(String user) {
        endpoint.setUser(user);
        return this;
    }

    /**
     * Sets the client password.
     * @param password
     * @return
     */
    public SshServerBuilder password(String password) {
        endpoint.setPassword(password);
        return this;
    }

    /**
     * Sets the hostKeyPath property.
     * @param hostKeyPath
     * @return
     */
    public SshServerBuilder hostKeyPath(String hostKeyPath) {
        endpoint.setHostKeyPath(hostKeyPath);
        return this;
    }

    /**
     * Sets the userHomePath property.
     * @param userHomePath
     * @return
     */
    public SshServerBuilder userHomePath(String userHomePath) {
        endpoint.setUserHomePath(userHomePath);
        return this;
    }

    /**
     * Sets the allowedKeyPath property.
     * @param allowedKeyPath
     * @return
     */
    public SshServerBuilder allowedKeyPath(String allowedKeyPath) {
        endpoint.setAllowedKeyPath(allowedKeyPath);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public SshServerBuilder messageConverter(SshMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the marshaller.
     * @param marshaller
     * @return
     */
    public SshServerBuilder marshaller(SshMarshaller marshaller) {
        endpoint.setMarshaller(marshaller);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public SshServerBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the endpoint adapter.
     * @param endpointAdapter
     * @return
     */
    public SshServerBuilder endpointAdapter(EndpointAdapter endpointAdapter) {
        endpoint.setEndpointAdapter(endpointAdapter);
        return this;
    }

    /**
     * Sets the debug logging enabled flag.
     * @param enabled
     * @return
     */
    public SshServerBuilder debugLogging(boolean enabled) {
        endpoint.setDebugLogging(enabled);
        return this;
    }

    /**
     * Sets the autoStart property.
     * @param autoStart
     * @return
     */
    public SshServerBuilder autoStart(boolean autoStart) {
        endpoint.setAutoStart(autoStart);
        return this;
    }
}
