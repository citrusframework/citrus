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

package org.citrusframework.ftp.server;

import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.citrusframework.ftp.message.FtpMarshaller;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.5
 */
public class FtpServerBuilder extends AbstractServerBuilder<FtpServer, FtpServerBuilder> {

    /** Endpoint target */
    private final FtpServer endpoint = new FtpServer();

    private String correlator;
    private String ftpServer;
    private String userManager;
    private String listenerFactory;
    private String marshaller;

    @Override
    public FtpServer build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }

            if (StringUtils.hasText(ftpServer)) {
                server(referenceResolver.resolve(ftpServer, org.apache.ftpserver.FtpServer.class));
            }

            if (StringUtils.hasText(userManager)) {
                userManager(referenceResolver.resolve(userManager, UserManager.class));
            }

            if (StringUtils.hasText(listenerFactory)) {
                listenerFactory(referenceResolver.resolve(listenerFactory, ListenerFactory.class));
            }

            if (StringUtils.hasText(marshaller)) {
                marshaller(referenceResolver.resolve(marshaller, FtpMarshaller.class));
            }
        }

        return super.build();
    }

    @Override
    protected FtpServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     */
    public FtpServerBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    @SchemaProperty(description = "The Ftp server port.")
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the autoConnect property.
     */
    public FtpServerBuilder autoConnect(boolean autoConnect) {
        endpoint.getEndpointConfiguration().setAutoConnect(autoConnect);
        return this;
    }

    @SchemaProperty(description = "When enabled the server uses automatic connect mode.")
    public void setAutoConnect(boolean autoConnect) {
        autoConnect(autoConnect);
    }

    /**
     * Sets the autoLogin property.
     */
    public FtpServerBuilder autoLogin(boolean autoLogin) {
        endpoint.getEndpointConfiguration().setAutoLogin(autoLogin);
        return this;
    }

    @SchemaProperty(description = "When enabled the server uses automatic login mode.")
    public void setAutoLogin(boolean autoLogin) {
        autoLogin(autoLogin);
    }

    /**
     * Sets the host property.
     */
    public FtpServerBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    @SchemaProperty(description = "The Ftp server host.")
    public void setHost(String host) {
        host(host);
    }

    /**
     * Sets the user property.
     */
    public FtpServerBuilder user(String user) {
        endpoint.getEndpointConfiguration().setUser(user);
        return this;
    }

    @SchemaProperty(description = "Sets the allowed user name.")
    public void setUser(String user) {
        user(user);
    }

    /**
     * Sets the password property.
     */
    public FtpServerBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the allowed user password."
    )
    public void setPassword(String password) {
        password(password);
    }

    /**
     * Sets the autoHandleCommands property.
     */
    public FtpServerBuilder autoHandleCommands(String autoHandleCommands) {
        endpoint.getEndpointConfiguration().setAutoHandleCommands(autoHandleCommands);
        return this;
    }

    @SchemaProperty(description = "Enables the auto handle commands mode.")
    public void setAutoHandleCommands(String autoHandleCommands) {
        autoHandleCommands(autoHandleCommands);
    }

    /**
     * Sets the autoReadFiles property.
     */
    public FtpServerBuilder autoReadFiles(boolean autoReadFiles) {
        endpoint.getEndpointConfiguration().setAutoReadFiles(autoReadFiles);
        return this;
    }

    @SchemaProperty(description = "When enabled the client automatically reads new files.")
    public void setAutoReadFiles(boolean autoReadFiles) {
        autoReadFiles(autoReadFiles);
    }

    /**
     * Sets the localPassiveMode property.
     */
    public FtpServerBuilder localPassiveMode(boolean localPassiveMode) {
        endpoint.getEndpointConfiguration().setLocalPassiveMode(localPassiveMode);
        return this;
    }

    @SchemaProperty(description = "Enables the local passive mode.")
    public void setLocalPassiveMode(boolean localPassiveMode) {
        localPassiveMode(localPassiveMode);
    }

    /**
     * Sets the ftp server.
     */
    public FtpServerBuilder server(org.apache.ftpserver.FtpServer server) {
        endpoint.setFtpServer(server);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the Ftp server implementation.")
    public void setServer(String server) {
        this.ftpServer = server;
    }

    /**
     * Sets the userManager property.
     */
    public FtpServerBuilder userManager(UserManager userManager) {
        endpoint.setUserManager(userManager);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom user manager implementation.")
    public void setUserManager(String userManager) {
        this.userManager = userManager;
    }

    /**
     * Sets the listener factory property.
     */
    public FtpServerBuilder listenerFactory(ListenerFactory factory) {
        endpoint.setListenerFactory(factory);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom listener factory implementation.")
    public void setListenerFactory(String listenerFactory) {
        this.listenerFactory = listenerFactory;
    }

    /**
     * Sets the userManager properties.
     */
    public FtpServerBuilder userManagerProperties(Resource userManagerProperties) {
        endpoint.setUserManagerProperties(userManagerProperties);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Loads user manage properties from a file resource.")
    public void setUserManagerProperties(String userManagerProperties) {
        userManagerProperties(FileUtils.getFileResource(userManagerProperties));
    }

    /**
     * Sets the marshaller.
     */
    public FtpServerBuilder marshaller(FtpMarshaller marshaller) {
        endpoint.getEndpointConfiguration().setMarshaller(marshaller);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom Ftp message marshaller.")
    public void setMarshaller(String marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Sets the correlator.
     */
    public FtpServerBuilder correlator(MessageCorrelator correlator) {
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
    public FtpServerBuilder errorHandlingStrategy(ErrorHandlingStrategy errorHandlingStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(errorHandlingStrategy);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:errorHandler") },
            description = "Sets the error handling strategy."
    )
    public void setErrorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
        errorHandlingStrategy(errorStrategy);
    }
}
