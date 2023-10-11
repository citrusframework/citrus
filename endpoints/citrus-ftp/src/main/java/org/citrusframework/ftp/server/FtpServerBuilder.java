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

package org.citrusframework.ftp.server;

import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.citrusframework.ftp.message.FtpMarshaller;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.spi.Resource;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class FtpServerBuilder extends AbstractServerBuilder<FtpServer, FtpServerBuilder> {

    /** Endpoint target */
    private final FtpServer endpoint = new FtpServer();

    @Override
    protected FtpServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public FtpServerBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the autoConnect property.
     * @param autoConnect
     * @return
     */
    public FtpServerBuilder autoConnect(boolean autoConnect) {
        endpoint.getEndpointConfiguration().setAutoConnect(autoConnect);
        return this;
    }

    /**
     * Sets the autoLogin property.
     * @param autoLogin
     * @return
     */
    public FtpServerBuilder autoLogin(boolean autoLogin) {
        endpoint.getEndpointConfiguration().setAutoLogin(autoLogin);
        return this;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public FtpServerBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the user property.
     * @param user
     * @return
     */
    public FtpServerBuilder user(String user) {
        endpoint.getEndpointConfiguration().setUser(user);
        return this;
    }

    /**
     * Sets the password property.
     * @param password
     * @return
     */
    public FtpServerBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    /**
     * Sets the autoHandleCommands property.
     * @param autoHandleCommands
     * @return
     */
    public FtpServerBuilder autoHandleCommands(String autoHandleCommands) {
        endpoint.getEndpointConfiguration().setAutoHandleCommands(autoHandleCommands);
        return this;
    }

    /**
     * Sets the autoReadFiles property.
     * @param autoReadFiles
     * @return
     */
    public FtpServerBuilder autoReadFiles(boolean autoReadFiles) {
        endpoint.getEndpointConfiguration().setAutoReadFiles(autoReadFiles);
        return this;
    }

    /**
     * Sets the localPassiveMode property.
     * @param localPassiveMode
     * @return
     */
    public FtpServerBuilder localPassiveMode(boolean localPassiveMode) {
        endpoint.getEndpointConfiguration().setLocalPassiveMode(localPassiveMode);
        return this;
    }

    /**
     * Sets the ftp server.
     * @param server
     * @return
     */
    public FtpServerBuilder server(org.apache.ftpserver.FtpServer server) {
        endpoint.setFtpServer(server);
        return this;
    }

    /**
     * Sets the userManager property.
     * @param userManager
     * @return
     */
    public FtpServerBuilder userManager(UserManager userManager) {
        endpoint.setUserManager(userManager);
        return this;
    }

    /**
     * Sets the listener factory property.
     * @param factory
     * @return
     */
    public FtpServerBuilder listenerFactory(ListenerFactory factory) {
        endpoint.setListenerFactory(factory);
        return this;
    }

    /**
     * Sets the userManager properties.
     * @param userManagerProperties
     * @return
     */
    public FtpServerBuilder userManagerProperties(Resource userManagerProperties) {
        endpoint.setUserManagerProperties(userManagerProperties);
        return this;
    }

    /**
     * Sets the marshaller.
     * @param marshaller
     * @return
     */
    public FtpServerBuilder marshaller(FtpMarshaller marshaller) {
        endpoint.getEndpointConfiguration().setMarshaller(marshaller);
        return this;
    }

    /**
     * Sets the correlator.
     * @param correlator
     * @return
     */
    public FtpServerBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the error handling strategy.
     * @param errorHandlingStrategy
     * @return
     */
    public FtpServerBuilder errorHandlingStrategy(ErrorHandlingStrategy errorHandlingStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(errorHandlingStrategy);
        return this;
    }
}
