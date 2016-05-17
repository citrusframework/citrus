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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;

import com.consol.citrus.telnet.model.*;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;

import com.consol.citrus.message.*;
import com.consol.citrus.message.correlation.CorrelationManager;
import com.consol.citrus.message.correlation.PollingCorrelationManager;
import com.consol.citrus.messaging.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Wurmbrand
 * @since 2.6
 */
public class TelnetClient extends AbstractEndpoint implements Producer, ReplyConsumer {
    /** Logger */
	private static Logger log = LoggerFactory.getLogger(TelnetClient.class);
	
    /** Apache telnet client */
	private org.apache.commons.net.telnet.TelnetClient telnetClient;
    
    /** Store of reply messages */
	private CorrelationManager<Message> correlationManager;
    
	private final static String CONNECTION_REFUSED = "Connection refused";
	private final String prompt;
	private TelnetSession telnetSession;

    /**
     * Default constructor initializing endpoint configuration.
     */
	public TelnetClient() {
		this(new TelnetEndpointConfiguration());
	}
	
    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */	
	public TelnetClient(TelnetEndpointConfiguration endpointConfiguration) {
		super(endpointConfiguration);
		this.telnetClient  = new org.apache.commons.net.telnet.TelnetClient();
		this.prompt = endpointConfiguration.getPrompt();
        this.correlationManager = new PollingCorrelationManager<Message>(endpointConfiguration, "Reply message did not arrive yet");
        this.telnetClient.setConnectTimeout(getEndpointConfiguration().getConnectionTimeout());
	}

    @Override
    public TelnetEndpointConfiguration getEndpointConfiguration() {
    	return (TelnetEndpointConfiguration) super.getEndpointConfiguration();
    }

	@Override
	public void send(Message message, TestContext context) {

		String correlationKeyName = getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = getEndpointConfiguration().getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);
        
        TelnetRequest request = (TelnetRequest) getEndpointConfiguration().getMessageConverter().convertOutbound(message, getEndpointConfiguration());
        String reply = null;
        TelnetEndpointConfiguration ec = (TelnetEndpointConfiguration) super.getEndpointConfiguration();
        if (log.isDebugEnabled()) {
        	log.debug("CONNECTING telnet://{}:{}@{}:{}", ec.getUser(), ec.getPassword(), ec.getHost(), ec.getPort());
        }
		
        try {
			telnetSession = connect(ec.getHost(),ec.getPort());
			login(ec.getUser(),ec.getPassword());
			write(request.getCommand());
			reply = readUntil(prompt + " ");
		} catch (SocketException e) {
			throw new CitrusRuntimeException("Unexpected socket failure",e);
		} catch (IOException e) {
			throw new CitrusRuntimeException("Unexpected io failure",e);
		} catch (ConnectionRefusedException e) {
			reply = CONNECTION_REFUSED;
		} finally {
			if (telnetSession!=null) {				
				telnetSession.close();
			}
	        log.debug("DISCONNECT");
			disconnect();
		}
        TelnetResponse telnetResponse = new TelnetResponse(reply);
        Message response = getEndpointConfiguration().getMessageConverter().convertInbound(telnetResponse, getEndpointConfiguration())
                .setHeader("user", ec.getUser());
        correlationManager.store(correlationKey, response);
       
	}

	private TelnetSession connect(String host, int port) throws SocketException, IOException {
		telnetClient.connect(host,port);
		// Get input and output stream references 
		return new TelnetSession(telnetClient.getInputStream(), new PrintStream(telnetClient.getOutputStream()));
	}
	
	private void login(String user, String password) throws ConnectionRefusedException, IOException {
		// Log the user on
		String reply = readUntil("login: ");
		if (telnetClient.isConnected()==false || (reply!=null && reply.contains(CONNECTION_REFUSED))) {
			throw new ConnectionRefusedException();			
		}
		write(user);
		readUntil("password: ");
		write(password);
		// Advance to a prompt
		readUntil(prompt + " ");
	}
	
	
	private String readUntil(String pattern) throws IOException {
		StringBuffer sb = new StringBuffer();
		byte[] buf = new byte[1024];
		long timeout = System.currentTimeMillis() + getEndpointConfiguration().getCommandTimeout();
		while (timeout > System.currentTimeMillis()) {
			int len = telnetSession.getIn().available();
			if (len > 0) {
				telnetSession.getIn().read(buf, 0, len);
				String chunk = new String(buf,0,len);
				System.out.print(chunk);
				sb.append(chunk);
				int patternIndex = sb.indexOf(pattern);
				if (patternIndex>=0) {
					return sb.substring(0,patternIndex).trim();
				}
				timeout = System.currentTimeMillis() + getEndpointConfiguration().getCommandTimeout();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ok
			}
		}
		return sb.toString().trim();
	}

	private void write(String value) {
		try {
			telnetSession.getOut().println(value);
			telnetSession.getOut().flush();
			System.out.println(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String sendCommand(String command) {
		try {
			write(command);
			readUntil(prompt + " ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void disconnect() {
		try {
			telnetClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static class TelnetSession {
		private final InputStream in;
		private final PrintStream out;
		
		private TelnetSession(InputStream in, PrintStream out) {
			super();
			this.in = in;
			this.out = out;
		}

		public InputStream getIn() {
			return in;
		}

		public PrintStream getOut() {
			return out;
		}
		
		public void close() {
			try {
				in.close();
			} catch (IOException e) {
				// do nothing
			}
			out.close();
		}
	}

	@Override
	public Message receive(TestContext context) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context);
	}

	@Override
	public Message receive(String selector, TestContext context) {
		return receive(selector, context, getEndpointConfiguration().getTimeout());
	}
	
	@Override
	public Message receive(TestContext context, long timeout) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context, timeout);
	}
 
	@Override
	public Message receive(String selector, TestContext context, long timeout) {
        Message message = correlationManager.find(selector, timeout);

        if (message == null) {
            throw new ActionTimeoutException("Action timeout while receiving synchronous reply message from telnet server");
        }

        return message;
     }
	
    /**
     * Creates a message producer for this endpoint for sending messages
     * to this endpoint.
     */
    @Override
    public Producer createProducer() {
        return this;
    }

    /**
     * Creates a message consumer for this endpoint. Consumer receives
     * messages on this endpoint.
     *
     * @return
     */
    @Override
    public SelectiveConsumer createConsumer() {
        return this;
    }

    /**
     * Sets the apache telnet client.
     * @param telnetClient
     */
    public void setTelnetClient(org.apache.commons.net.telnet.TelnetClient telnetClient) {
        this.telnetClient = telnetClient;
    }

    /**
     * Gets the apache ftp client.
     * @return
     */
    public  org.apache.commons.net.telnet.TelnetClient getTelnetClient() {
        return telnetClient;
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<Message> correlationManager) {
        this.correlationManager = correlationManager;
    }

}
