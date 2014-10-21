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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ftp.message.FtpMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.*;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpClient extends AbstractEndpoint implements Producer, ReplyConsumer, InitializingBean, DisposableBean {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(FtpClient.class);

    /** Apache ftp client */
    private FTPClient ftpClient;

    /** Apache ftp client configuration */
    private FTPClientConfig config = new FTPClientConfig();

    /** Store of reply messages */
    private Map<String, Message> replyMessages = new HashMap<String, Message>();

    /** Retry logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("com.consol.citrus.MessageRetryLogger");

    /**
     * Default constructor initializing endpoint configuration.
     */
    public FtpClient() {
        super(new FtpEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    protected FtpClient(FtpEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public FtpEndpointConfiguration getEndpointConfiguration() {
        return (FtpEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void send(Message message) {
        FtpMessage ftpMessage;
        if (message instanceof FtpMessage) {
            ftpMessage = (FtpMessage) message;
        } else {
            ftpMessage = new FtpMessage(message);
        }

        log.info(String.format("Sending FTP message to: ftp://'%s:%s'", getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort()));

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:\n" + ftpMessage.getPayload().toString());
        }

        try {
            connectAndLogin();

            int reply = ftpClient.sendCommand(ftpMessage.getCommand(), ftpMessage.getArguments());

            if(!FTPReply.isPositiveCompletion(reply) && !FTPReply.isPositivePreliminary(reply)) {
                throw new CitrusRuntimeException(String.format("Failed to send FTP command - reply is: %s:%s", reply, ftpClient.getReplyString()));
            }

            log.info(String.format("FTP message was successfully sent to: '%s:%s'", getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort()));

            onReplyMessage(ftpMessage, new FtpMessage(ftpMessage.getCommand(), ftpMessage.getArguments())
                                                        .setReplyCode(reply)
                                                        .setReplyString(ftpClient.getReplyString()));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to execute ftp command", e);
        }

    }

    /**
     * Opens a new connection and performs login with user name and password if set.
     * @throws IOException
     */
    protected void connectAndLogin() throws IOException {
        if (!ftpClient.isConnected()) {
            ftpClient.connect(getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort());

            log.info("Connected to FTP server: " + ftpClient.getReplyString());

            int reply = ftpClient.getReplyCode();

            if(!FTPReply.isPositiveCompletion(reply)) {
                throw new CitrusRuntimeException("FTP server refused connection.");
            }

            log.info("Successfully opened connection to FTP server");

            if (getEndpointConfiguration().getUser() != null) {
                log.info(String.format("Login as user: '%s'", getEndpointConfiguration().getUser()));
                boolean login = ftpClient.login(getEndpointConfiguration().getUser(), getEndpointConfiguration().getPassword());

                if (!login) {
                    throw new CitrusRuntimeException(String.format("Failed to login to FTP server using credentials: %s:%s", getEndpointConfiguration().getUser(), getEndpointConfiguration().getPassword()));
                }
            }
        }
    }

    @Override
    public Message receive(TestContext context) {
        return receive("", context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive("", context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        long timeLeft = timeout;
        Message message = findReplyMessage(selector);

        while (message == null && timeLeft > 0) {
            timeLeft -= getEndpointConfiguration().getPollingInterval();

            if (RETRY_LOG.isDebugEnabled()) {
                RETRY_LOG.debug("Reply message did not arrive yet - retrying in " + (timeLeft > 0 ? getEndpointConfiguration().getPollingInterval() : getEndpointConfiguration().getPollingInterval() + timeLeft) + "ms");
            }

            try {
                Thread.sleep(timeLeft > 0 ? getEndpointConfiguration().getPollingInterval() : getEndpointConfiguration().getPollingInterval() + timeLeft);
            } catch (InterruptedException e) {
                RETRY_LOG.warn("Thread interrupted while waiting for retry", e);
            }

            message = findReplyMessage(selector);
        }

        return message;
    }

    /**
     * Saves reply message with correlation key to local store for later processing.
     * @param correlationKey
     * @param replyMessage the reply message.
     */
    public void onReplyMessage(String correlationKey, Message replyMessage) {
        replyMessages.put(correlationKey, replyMessage);
    }

    /**
     * Saves reply message to local store for later processing. Constructs correlation key from initial request.
     * @param requestMessage
     * @param replyMessage
     */
    public void onReplyMessage(Message requestMessage, Message replyMessage) {
        if (getEndpointConfiguration().getCorrelator() != null) {
            onReplyMessage(getEndpointConfiguration().getCorrelator().getCorrelationKey(requestMessage), replyMessage);
        } else {
            onReplyMessage("", replyMessage);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (ftpClient == null) {
            ftpClient = new FTPClient();
        }

        ftpClient.configure(config);

        ftpClient.addProtocolCommandListener(new ProtocolCommandListener() {
            @Override
            public void protocolCommandSent(ProtocolCommandEvent event) {
                log.info("Send FTP command: " + event.getCommand());
            }

            @Override
            public void protocolReplyReceived(ProtocolCommandEvent event) {
                log.info("Received FTP command reply: " + event.getReplyCode());
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        if (ftpClient.isConnected()) {
            ftpClient.logout();

            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                log.warn("Failed to disconnect from FTP server", e);
            }

            log.info("Successfully closed connection to FTP server");
        }
    }

    /**
     * Tries to find reply message for correlation key from local store.
     * @param correlationKey
     * @return
     */
    public Message findReplyMessage(String correlationKey) {
        return replyMessages.remove(correlationKey);
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
     * Sets the apache ftp client.
     * @param ftpClient
     */
    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    /**
     * Gets the apache ftp client.
     * @return
     */
    public FTPClient getFtpClient() {
        return ftpClient;
    }
}
