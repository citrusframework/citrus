/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.kubernetes.client;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.kubernetes.command.KubernetesCommand;
import org.citrusframework.kubernetes.endpoint.KubernetesEndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.ReplyConsumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kubernetes client uses Java kubernetes client implementation for executing kubernetes commands.
 *
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesClient extends AbstractEndpoint implements Producer, ReplyConsumer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(KubernetesClient.class);

    /** Store of reply messages */
    private CorrelationManager<KubernetesCommand> correlationManager;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public KubernetesClient() {
        this(new KubernetesEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public KubernetesClient(KubernetesEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public KubernetesEndpointConfiguration getEndpointConfiguration() {
        return (KubernetesEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void send(Message message, TestContext context) {
        String correlationKeyName = getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = getEndpointConfiguration().getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        if (logger.isDebugEnabled()) {
            logger.debug("Sending Kubernetes request to: '" + getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl() + "'");
        }

        KubernetesCommand<?> command = getEndpointConfiguration().getMessageConverter().convertOutbound(message, getEndpointConfiguration(), context);
        command.execute(this, context);

        logger.info("Kubernetes request was sent to endpoint: '" + getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl() + "'");

        correlationManager.store(correlationKey, command);
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
        KubernetesCommand command = correlationManager.find(selector, timeout);

        if (command == null) {
            throw new MessageTimeoutException(timeout, getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl());
        }

        if (command.getResultCallback() != null) {
            command.getResultCallback().validateCommandResult(command.getCommandResult(), context);
        }

        return getEndpointConfiguration().getMessageConverter().convertInbound(command, getEndpointConfiguration(), context);
    }

    @Override
    public Producer createProducer() {
        return this;
    }

    @Override
    public SelectiveConsumer createConsumer() {
        return this;
    }

    /**
     * Gets the Kubernetes client.
     * @return
     */
    public io.fabric8.kubernetes.client.KubernetesClient getClient() {
        return getEndpointConfiguration().getKubernetesClient();
    }
}
