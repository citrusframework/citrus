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

package com.consol.citrus.ssh.client;

import com.consol.citrus.endpoint.AbstractEndpointConfiguration;
import com.consol.citrus.message.ReplyMessageCorrelator;
import com.consol.citrus.ssh.XmlMapper;

/**
 * @author Roland Huss, Christoph Deppisch
 * @since 1.4
 */
public class SshEndpointConfiguration extends AbstractEndpointConfiguration {
    // Host to connect to. Default: localhost
    private String host = "localhost";

    // SSH Port to connect to. Default: 2222
    private int port = 2222;

    // User for doing the SSH communication
    private String user;

    // Password if no private key authentication is used
    private String password;

    // Path to private key of user
    private String privateKeyPath;

    // Password for private key
    private String privateKeyPassword;

    // Whether strict host checking should be performed
    private boolean strictHostChecking = false;

    // If strict host checking is used, path to the 'known_hosts' file
    private String knownHosts;

    // Timeout how long to wait for answering the request
    private long commandTimeout = 1000 * 60 * 5; // 5 minutes

    // Timeout how long to wait for a connection to connect
    private int connectionTimeout = 1000 * 60 * 1; // 1 minute

    // Message parser
    private XmlMapper xmlMapper = new XmlMapper();

    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;

    /** Polling interval when waiting for synchronous reply message to arrive */
    private long pollingInterval = 500;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    public void setPrivateKeyPassword(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
    }

    public boolean isStrictHostChecking() {
        return strictHostChecking;
    }

    public void setStrictHostChecking(boolean strictHostChecking) {
        this.strictHostChecking = strictHostChecking;
    }

    public String getKnownHosts() {
        return knownHosts;
    }

    public void setKnownHosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }

    public long getCommandTimeout() {
        return commandTimeout;
    }

    public void setCommandTimeout(long commandTimeout) {
        this.commandTimeout = commandTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public XmlMapper getXmlMapper() {
        return xmlMapper;
    }

    public void setXmlMapper(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    public ReplyMessageCorrelator getCorrelator() {
        return correlator;
    }

    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}
