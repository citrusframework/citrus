/*
 * Copyright 2006-2015 the original author or authors.
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
package com.consol.citrus.telnet.client;

import com.consol.citrus.telnet.message.TelnetMessageConverter;
import com.consol.citrus.telnet.model.TelnetMarshaller;

import com.consol.citrus.endpoint.AbstractPollableEndpointConfiguration;
import com.consol.citrus.message.DefaultMessageCorrelator;
import com.consol.citrus.message.MessageCorrelator;

/**
 * @author Michael Wurmbrand
 * @since 2.6
 */
public class TelnetEndpointConfiguration extends AbstractPollableEndpointConfiguration {

	/** Host to connect to. Default: localhost */
    private String host = "localhost";

     /** Telnet Port to connect to. Default: 23 */
    private int port = 23;

     /** User for doing the Telnet communication */
    private String user;

     /** Password  */
    private String password;

    private String prompt = "#";

    /** Timeout how long to wait for answering the request */
    private long commandTimeout = 1000 * 60 * 5; // 5 minutes

     /** Timeout how long to wait for a connection to connect */
    private int connectionTimeout = 1000 * 60 * 1; // 1 minute

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();
    
    /** Telnet message marshaller converts from XML to telnet message object */
    private TelnetMarshaller telnetMarshaller = new TelnetMarshaller();

    /** Telnet message converter */
    private TelnetMessageConverter messageConverter = new TelnetMessageConverter();
    
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

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
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

	public TelnetMarshaller getTelnetMarshaller() {
		return telnetMarshaller;
	}

	public void setTelnetMarshaller(TelnetMarshaller telnetMarshaller) {
		this.telnetMarshaller = telnetMarshaller;
	}

	public TelnetMessageConverter getMessageConverter() {
		return messageConverter;
	}

	public void setMessageConverter(TelnetMessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}

}
