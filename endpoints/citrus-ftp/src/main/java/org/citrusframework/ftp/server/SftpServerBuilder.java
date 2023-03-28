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

package org.citrusframework.ftp.server;

import java.util.Map;

import org.citrusframework.ftp.client.SftpEndpointConfiguration;
import org.citrusframework.server.AbstractServerBuilder;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class SftpServerBuilder extends AbstractServerBuilder<SftpServer, SftpServerBuilder> {

    /** Endpoint target */
    private final SftpServer endpoint = new SftpServer();

    @Override
    protected SftpServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public SftpServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    /**
     * Sets the autoConnect property.
     * @param autoConnect
     * @return
     */
    public SftpServerBuilder autoConnect(boolean autoConnect) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setAutoConnect(autoConnect);
        return this;
    }

    /**
     * Sets the autoLogin property.
     * @param autoLogin
     * @return
     */
    public SftpServerBuilder autoLogin(boolean autoLogin) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setAutoLogin(autoLogin);
        return this;
    }

    /**
     * Sets the user property.
     * @param user
     * @return
     */
    public SftpServerBuilder user(String user) {
        endpoint.setUser(user);
        return this;
    }

    /**
     * Sets the client password.
     * @param password
     * @return
     */
    public SftpServerBuilder password(String password) {
        endpoint.setPassword(password);
        return this;
    }

    /**
     * Sets the hostKeyPath property.
     * @param hostKeyPath
     * @return
     */
    public SftpServerBuilder hostKeyPath(String hostKeyPath) {
        endpoint.setHostKeyPath(hostKeyPath);
        return this;
    }

    /**
     * Sets the userHomePath property.
     * @param userHomePath
     * @return
     */
    public SftpServerBuilder userHomePath(String userHomePath) {
        endpoint.setUserHomePath(userHomePath);
        return this;
    }

    /**
     * Sets the allowedKeyPath property.
     * @param allowedKeyPath
     * @return
     */
    public SftpServerBuilder allowedKeyPath(String allowedKeyPath) {
        endpoint.setAllowedKeyPath(allowedKeyPath);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public SftpServerBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the strictHostChecking property.
     * @param strictHostChecking
     * @return
     */
    public SftpServerBuilder strictHostChecking(boolean strictHostChecking) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setStrictHostChecking(strictHostChecking);
        return this;
    }

    /**
     * Sets the knownHosts property.
     * @param knownHosts
     * @return
     */
    public SftpServerBuilder knownHosts(String knownHosts) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setKnownHosts(knownHosts);
        return this;
    }

    /**
     * Sets the privateKeyPath property.
     * @param privateKeyPath
     * @return
     */
    public SftpServerBuilder privateKeyPath(String privateKeyPath) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setPrivateKeyPath(privateKeyPath);
        return this;
    }

    /**
     * Sets the privateKeyPassword property.
     * @param privateKeyPassword
     * @return
     */
    public SftpServerBuilder privateKeyPassword(String privateKeyPassword) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setPrivateKeyPassword(privateKeyPassword);
        return this;
    }

    /**
     * Sets the preferredAuthentications property.
     * @param preferredAuthentications
     * @return
     */
    public SftpServerBuilder preferredAuthentications(String preferredAuthentications) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setPreferredAuthentications(preferredAuthentications);
        return this;
    }

    /**
     * Sets the sessionConfigs property.
     * @param sessionConfigs
     * @return
     */
    public SftpServerBuilder sessionConfigs(Map<String, String> sessionConfigs) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setSessionConfigs(sessionConfigs);
        return this;
    }
}
