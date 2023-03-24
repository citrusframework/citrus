/*
 * Copyright 2006-2013 the original author or authors.
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

import org.citrusframework.endpoint.AbstractPollableEndpointConfiguration;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.ssh.message.SshMessageConverter;
import org.citrusframework.ssh.model.SshMarshaller;

/**
 * @author Roland Huss, Christoph Deppisch
 * @since 1.4
 */
public class SshEndpointConfiguration extends AbstractPollableEndpointConfiguration {
    /** Host to connect to. Default: localhost */
    private String host = "localhost";

     /** SSH Port to connect to. Default: 2222 */
    private int port = 2222;

     /** User for doing the SSH communication */
    private String user;

     /** Password if no private key authentication is used */
    private String password;

     /** Path to private key of user */
    private String privateKeyPath;

     /** Password for private key */
    private String privateKeyPassword;

     /** Whether strict host checking should be performed */
    private boolean strictHostChecking = false;

     /** If strict host checking is used, path to the 'known_hosts' file */
    private String knownHosts;

     /** Timeout how long to wait for answering the request */
    private long commandTimeout = 1000 * 60 * 5; // 5 minutes

     /** Timeout how long to wait for a connection to connect */
    private int connectionTimeout = 1000 * 60 * 1; // 1 minute

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    /** Ssh message marshaller converts from XML to ssh message object */
    private SshMarshaller sshMarshaller = new SshMarshaller();

    /** Ssh message converter */
    private SshMessageConverter messageConverter = new SshMessageConverter();

    /**
     * Gets the ssh server host.
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the ssh server host.
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the ssh server port.
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the ssh server port.
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the ssh user.
     * @return
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the ssh user.
     * @param user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Gets the ssh user password.
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the ssh user password.
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the private key store path.
     * @return
     */
    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    /**
     * Sets the private key store path.
     * @param privateKeyPath
     */
    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    /**
     * Gets the private keystore password.
     * @return
     */
    public String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    /**
     * Sets the private keystore password.
     * @param privateKeyPassword
     */
    public void setPrivateKeyPassword(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
    }

    /**
     * Is strict host checking enabled.
     * @return
     */
    public boolean isStrictHostChecking() {
        return strictHostChecking;
    }

    /**
     * Enables/disables strict host checking.
     * @param strictHostChecking
     */
    public void setStrictHostChecking(boolean strictHostChecking) {
        this.strictHostChecking = strictHostChecking;
    }

    /**
     * Gets known hosts.
     * @return
     */
    public String getKnownHosts() {
        return knownHosts;
    }

    /**
     * Sets known hosts.
     * @param knownHosts
     */
    public void setKnownHosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }

    /**
     * Gets the command timeout.
     * @return
     */
    public long getCommandTimeout() {
        return commandTimeout;
    }

    /**
     * Sets the command timeout.
     * @param commandTimeout
     */
    public void setCommandTimeout(long commandTimeout) {
        this.commandTimeout = commandTimeout;
    }

    /**
     * Gets the connection timeout.
     * @return
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the connection timeout.
     * @param connectionTimeout
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Gets the message correlator.
     * @return
     */
    public MessageCorrelator getCorrelator() {
        return correlator;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     */
    public void setCorrelator(MessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Gets the message converter.
     * @return
     */
    public SshMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     */
    public void setMessageConverter(SshMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Gets the ssh oxm marshaller.
     * @return
     */
    public SshMarshaller getSshMarshaller() {
        return sshMarshaller;
    }

    /**
     * Sets the ssh oxm marshaller.
     * @param sshMarshaller
     */
    public void setSshMarshaller(SshMarshaller sshMarshaller) {
        this.sshMarshaller = sshMarshaller;
    }
}
