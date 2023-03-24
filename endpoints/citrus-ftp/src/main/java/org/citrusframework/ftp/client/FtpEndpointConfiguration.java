/*
 * Copyright 2006-2014 the original author or authors.
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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.citrusframework.endpoint.AbstractPollableEndpointConfiguration;
import org.citrusframework.ftp.message.FtpMarshaller;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.apache.commons.net.ftp.FTPCmd;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /** Ftp host to connect to */
    private String host = "localhost";

    /** Ftp server port */
    private int port = 22222;

    /** User name used for login */
    private String user;

    /** User password used for login */
    private String password;

    /** Auto accept connection requests */
    private boolean autoConnect = true;

    /** Auto login user requests */
    private boolean autoLogin = true;

    /** Marshaller converts from XML to Jdbc model objects */
    private FtpMarshaller marshaller = new FtpMarshaller();

    /** Should http errors be handled within endpoint consumer or simply throw exception */
    private ErrorHandlingStrategy errorHandlingStrategy = ErrorHandlingStrategy.PROPAGATE;

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    /** Comma delimited list of ftp commands to auto handle on server */
    private String autoHandleCommands = Stream.of(FTPCmd.PORT.getCommand(),
                                                  FTPCmd.TYPE.getCommand()).collect(Collectors.joining(","));

    /** Auto read file content retrieved from server */
    private boolean autoReadFiles = true;

    /** File transfer passive mode */
    private boolean localPassiveMode = true;

    /**
     * Gets the ftp host.
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the ftp host.
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Sets the ftp server port.
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the ftp server port.
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(MessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public MessageCorrelator getCorrelator() {
        return correlator;
    }

    /**
     * Sets the user name for login.
     * @param user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Gets the user name for login.
     * @return
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the user password for login.
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user password for login.
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the autoConnect.
     *
     * @return
     */
    public boolean isAutoConnect() {
        return autoConnect;
    }

    /**
     * Sets the autoConnect.
     *
     * @param autoConnect
     */
    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    /**
     * Gets the autoLogin.
     *
     * @return
     */
    public boolean isAutoLogin() {
        return autoLogin;
    }

    /**
     * Sets the autoLogin.
     *
     * @param autoLogin
     */
    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    /**
     * Gets the marshaller.
     *
     * @return
     */
    public FtpMarshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Sets the marshaller.
     *
     * @param marshaller
     */
    public void setMarshaller(FtpMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Gets the errorHandlingStrategy.
     *
     * @return
     */
    public ErrorHandlingStrategy getErrorHandlingStrategy() {
        return errorHandlingStrategy;
    }

    /**
     * Sets the errorHandlingStrategy.
     *
     * @param errorHandlingStrategy
     */
    public void setErrorHandlingStrategy(ErrorHandlingStrategy errorHandlingStrategy) {
        this.errorHandlingStrategy = errorHandlingStrategy;
    }

    /**
     * Gets the autoHandleCommands.
     *
     * @return
     */
    public String getAutoHandleCommands() {
        return autoHandleCommands;
    }

    /**
     * Sets the autoHandleCommands.
     *
     * @param autoHandleCommands
     */
    public void setAutoHandleCommands(String autoHandleCommands) {
        this.autoHandleCommands = autoHandleCommands;
    }

    /**
     * Gets the autoReadFiles.
     *
     * @return
     */
    public boolean isAutoReadFiles() {
        return autoReadFiles;
    }

    /**
     * Sets the autoReadFiles.
     *
     * @param autoReadFiles
     */
    public void setAutoReadFiles(boolean autoReadFiles) {
        this.autoReadFiles = autoReadFiles;
    }

    /**
     * Gets the localPassiveMode.
     *
     * @return
     */
    public boolean isLocalPassiveMode() {
        return localPassiveMode;
    }

    /**
     * Sets the localPassiveMode.
     *
     * @param localPassiveMode
     */
    public void setLocalPassiveMode(boolean localPassiveMode) {
        this.localPassiveMode = localPassiveMode;
    }
}
