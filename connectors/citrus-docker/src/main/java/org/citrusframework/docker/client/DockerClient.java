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

package org.citrusframework.docker.client;

import java.util.Objects;

import org.citrusframework.context.TestContext;
import org.citrusframework.docker.command.DockerCommand;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.ReplyConsumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Docker client uses Java docker client implementation for executing docker commands.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class DockerClient extends AbstractEndpoint implements Producer, ReplyConsumer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DockerClient.class);

    /** Store of reply messages */
    private CorrelationManager<DockerCommand> correlationManager;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public DockerClient() {
        this(new DockerEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public DockerClient(DockerEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public DockerEndpointConfiguration getEndpointConfiguration() {
        return (DockerEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void send(Message message, TestContext context) {
        String correlationKeyName = getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = getEndpointConfiguration().getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        if (logger.isDebugEnabled()) {
            logger.debug("Sending Docker request to: '" + getEndpointConfiguration().getDockerClientConfig().getDockerHost() + "'");
        }

        DockerCommand command = message.getPayload(DockerCommand.class);
        command.execute(this, context);

        logger.info("Docker request was sent to endpoint: '" + getEndpointConfiguration().getDockerClientConfig().getDockerHost() + "'");

        correlationManager.store(correlationKey, command);

        if (command.getResultCallback() != null) {
            command.getResultCallback().doWithCommandResult(command.getCommandResult(), context);
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
        DockerCommand<?> command = correlationManager.find(selector, timeout);

        if (command == null) {
            throw new MessageTimeoutException(timeout, Objects.toString(getEndpointConfiguration().getDockerClientConfig().getDockerHost()));
        }

        return new DefaultMessage(command.getCommandResult());
    }

    @Override
    public Producer createProducer() {
        return this;
    }

    @Override
    public SelectiveConsumer createConsumer() {
        return this;
    }
}
