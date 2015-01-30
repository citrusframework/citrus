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

package com.consol.citrus.ftp.client;

import com.consol.citrus.endpoint.AbstractPollableEndpointConfiguration;
import com.consol.citrus.message.DefaultMessageCorrelator;
import com.consol.citrus.message.MessageCorrelator;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /** Ftp host to connect to */
    private String host = "localhost";

    /** Ftp server port */
    private Integer port = 22222;

    /** User name used for login */
    private String user;

    /** User password used for login */
    private String password;

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

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
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Gets the ftp server port.
     * @return
     */
    public Integer getPort() {
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
}
