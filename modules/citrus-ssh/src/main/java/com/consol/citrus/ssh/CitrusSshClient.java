/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.ssh;

import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.message.AbstractSyncMessageSender;
import com.consol.citrus.message.ReplyMessageHandler;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.ssh.client.SshClient;
import com.consol.citrus.ssh.message.SshReplyMessageReceiver;
import org.springframework.integration.Message;

/**
 * A SSH client which sends a request specified in a test-cast as SSH EXEC call to a target host
 * and notifies a {@link ReplyMessageHandler} after the SSH call has been returned.
 *
 * @author Roland Huss
 * @since 1.3
 * @deprecated since Citrus 1.4, infavor of {@link com.consol.citrus.ssh.client.SshClient}
 */
@Deprecated
public class CitrusSshClient extends AbstractSyncMessageSender {

    /** New Ssh client */
    private final SshClient sshClient;

    public CitrusSshClient() {
        this(new SshClient());
    }

    public CitrusSshClient(SshClient sshClient) {
        super(sshClient);
        this.sshClient = sshClient;
    }

    @Override
    public void send(Message<?> message) {
        sshClient.createProducer().send(message);
    }

    @Override
    public Consumer createConsumer() {
        return sshClient.createConsumer();
    }

    @Override
    public Producer createProducer() {
        return sshClient.createProducer();
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return sshClient.getEndpointConfiguration();
    }

    @Override
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        super.setReplyMessageHandler(replyMessageHandler);

        if (replyMessageHandler instanceof SshReplyMessageReceiver) {
            ((SshReplyMessageReceiver) replyMessageHandler).setEndpoint(sshClient);
        }
    }

    public void setHost(String pHost) {
        sshClient.getEndpointConfiguration().setHost(pHost);
    }

    public void setPort(int pPort) {
        sshClient.getEndpointConfiguration().setPort(pPort);
    }

    public void setUser(String pUser) {
        sshClient.getEndpointConfiguration().setUser(pUser);
    }

    public void setPassword(String pPassword) {
        sshClient.getEndpointConfiguration().setPassword(pPassword);
    }

    public void setPrivateKeyPath(String pPrivateKeyPath) {
        sshClient.getEndpointConfiguration().setPrivateKeyPath(pPrivateKeyPath);
    }

    public void setPrivateKeyPassword(String pPrivateKeyPassword) {
        sshClient.getEndpointConfiguration().setPrivateKeyPassword(pPrivateKeyPassword);
    }

    public void setStrictHostChecking(boolean pStrictHostChecking) {
        sshClient.getEndpointConfiguration().setStrictHostChecking(pStrictHostChecking);
    }

    public void setKnownHosts(String pKnownHosts) {
        sshClient.getEndpointConfiguration().setKnownHosts(pKnownHosts);
    }

    public void setCommandTimeout(long pCommandTimeout) {
        sshClient.getEndpointConfiguration().setCommandTimeout(pCommandTimeout);
    }

    public void setConnectionTimeout(int pConnectionTimeout) {
        sshClient.getEndpointConfiguration().setConnectionTimeout(pConnectionTimeout);
    }

}
