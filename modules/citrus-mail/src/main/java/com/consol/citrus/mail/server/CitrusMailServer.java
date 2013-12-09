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

package com.consol.citrus.mail.server;

import com.consol.citrus.adapter.handler.EmptyResponseProducingMessageHandler;
import com.consol.citrus.mail.adapter.MessageHandlerAdapter;
import com.consol.citrus.server.AbstractServer;
import org.springframework.beans.factory.InitializingBean;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

/**
 * Mail server implementation starts new SMTP server instance and listens for incoming mail messages. Mail message adapter is
 * responsible for handling the incoming mail messages by forwarding to some message handler instance (e.g. sending mail content to
 * a message channel).
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class CitrusMailServer extends AbstractServer implements InitializingBean {

    /** Server port */
    private int port = 25;

    /** Message handler called for each delivery */
    private MessageHandlerAdapter messageHandlerAdapter;
    private SMTPServer smtpServer;

    @Override
    protected void startup() {
        smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(messageHandlerAdapter));
        smtpServer.setSoftwareName(getName());
        smtpServer.setPort(port);
        smtpServer.start();
    }

    @Override
    protected void shutdown() {
        smtpServer.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (messageHandlerAdapter == null) {
            messageHandlerAdapter = new MessageHandlerAdapter(new EmptyResponseProducingMessageHandler());
        }

        super.afterPropertiesSet();
    }

    /**
     * Gets the server port.
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the server port.
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the smtp server instance.
     * @return
     */
    public SMTPServer getSmtpServer() {
        return smtpServer;
    }

    /**
     * Sets the smtp server instance.
     * @param smtpServer
     */
    public void setSmtpServer(SMTPServer smtpServer) {
        this.smtpServer = smtpServer;
    }

    /**
     * Gets the message handler adapter.
     * @return
     */
    public MessageHandlerAdapter getMessageHandlerAdapter() {
        return messageHandlerAdapter;
    }

    /**
     * Sets the message handler adapter.
     * @param messageHandlerAdapter
     */
    public void setMessageHandlerAdapter(MessageHandlerAdapter messageHandlerAdapter) {
        this.messageHandlerAdapter = messageHandlerAdapter;
    }
}
