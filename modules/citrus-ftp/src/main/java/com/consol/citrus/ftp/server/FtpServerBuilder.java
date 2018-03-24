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

package com.consol.citrus.ftp.server;

import com.consol.citrus.endpoint.AbstractEndpointBuilder;
import com.consol.citrus.endpoint.EndpointAdapter;
import org.apache.ftpserver.ftplet.UserManager;
import org.springframework.core.io.Resource;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class FtpServerBuilder extends AbstractEndpointBuilder<FtpServer> {

    /** Endpoint target */
    private FtpServer endpoint = new FtpServer();

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
     * Sets the autoStart property.
     * @param autoStart
     * @return
     */
    public FtpServerBuilder autoStart(boolean autoStart) {
        endpoint.setAutoStart(autoStart);
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
     * Sets the autoHandleCommands property.
     * @param autoHandleCommands
     * @return
     */
    public FtpServerBuilder autoHandleCommands(String autoHandleCommands) {
        endpoint.getEndpointConfiguration().setAutoHandleCommands(autoHandleCommands);
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
     * Sets the userManager properties.
     * @param userManagerProperties
     * @return
     */
    public FtpServerBuilder userManagerProperties(Resource userManagerProperties) {
        endpoint.setUserManagerProperties(userManagerProperties);
        return this;
    }

    /**
     * Sets the endpoint adapter.
     * @param endpointAdapter
     * @return
     */
    public FtpServerBuilder endpointAdapter(EndpointAdapter endpointAdapter) {
        endpoint.setEndpointAdapter(endpointAdapter);
        return this;
    }

    /**
     * Sets the debug logging enabled flag.
     * @param enabled
     * @return
     */
    public FtpServerBuilder debugLogging(boolean enabled) {
        endpoint.setDebugLogging(enabled);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public FtpServerBuilder timeout(long timeout) {
        endpoint.setDefaultTimeout(timeout);
        return this;
    }

}
