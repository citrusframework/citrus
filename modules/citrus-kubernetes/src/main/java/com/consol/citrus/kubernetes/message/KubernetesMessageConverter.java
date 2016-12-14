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

package com.consol.citrus.kubernetes.message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.kubernetes.command.*;
import com.consol.citrus.kubernetes.endpoint.KubernetesEndpointConfiguration;
import com.consol.citrus.kubernetes.model.*;
import com.consol.citrus.message.*;

import javax.xml.transform.Source;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesMessageConverter implements MessageConverter<KubernetesCommand<?>, KubernetesEndpointConfiguration> {

    @Override
    public KubernetesCommand<?> convertOutbound(Message message, KubernetesEndpointConfiguration endpointConfiguration, TestContext context) {
        KubernetesCommand<?> command = getCommand(message, endpointConfiguration);
        convertOutbound(command, message, endpointConfiguration, context);

        return command;
    }

    @Override
    public void convertOutbound(KubernetesCommand<?> command, Message message, KubernetesEndpointConfiguration endpointConfiguration, TestContext context) {
    }

    @Override
    public Message convertInbound(KubernetesCommand<?> command, KubernetesEndpointConfiguration endpointConfiguration, TestContext context) {
        Map<String, Object> messageHeaders = createMessageHeaders(command);
        return new DefaultMessage(command.getCommandResult(), messageHeaders);
    }

    /**
     * Creates a new kubernetes command message model object from message headers.
     * @param messageHeaders
     * @return
     */
    protected KubernetesCommand<?> createCommand(Map<String, Object> messageHeaders) {
        if (messageHeaders.containsKey(KubernetesMessageHeaders.COMMAND)) {
            KubernetesCommand<?> command;

            switch (messageHeaders.get(KubernetesMessageHeaders.COMMAND).toString()) {
                case "info":
                    command = new Info();
                    break;
                case "list-events":
                    command = new ListEvents();
                    break;
                case "list-endpoints":
                    command = new ListEndpoints();
                    break;
                case "list-pods":
                    command = new ListPods();
                    break;
                case "watch-pods":
                    command = new WatchPods();
                    break;
                case "list-namespaces":
                    command = new ListNamespaces();
                    break;
                case "watch-namespaces":
                    command = new WatchNamespaces();
                    break;
                case "list-nodes":
                    command = new ListNodes();
                    break;
                case "watch-nodes":
                    command = new WatchNodes();
                    break;
                case "list-replication-controllers":
                    command = new ListReplicationControllers();
                    break;
                case "watch-replication-controllers":
                    command = new WatchReplicationControllers();
                    break;
                case "list-services":
                    command = new ListServices();
                    break;
                case "watch-services":
                    command = new WatchServices();
                    break;
                default:
                    command = new Info();
            }

            return command;
        } else {
            return null;
        }
    }

    /**
     * Reads basic command information and converts to message headers.
     * @param command
     * @return
     */
    protected Map<String,Object> createMessageHeaders(KubernetesCommand<?> command) {
        Map<String, Object> headers = new HashMap<String, Object>();

        headers.put(KubernetesMessageHeaders.COMMAND, command.getName().substring("kubernetes:".length()));

        for (Map.Entry<String, Object> entry : command.getParameters().entrySet()) {
            headers.put(entry.getKey(), entry.getValue());
        }

        return headers;
    }

    /**
     * Reads Citrus internal mail message model object from message payload. Either payload is actually a mail message object or
     * XML payload String is unmarshalled to mail message object.
     *
     * @param message
     * @param endpointConfiguration
     * @return
     */
    private KubernetesCommand<?> getCommand(Message message, KubernetesEndpointConfiguration endpointConfiguration) {
        Object payload = message.getPayload();

        KubernetesCommand<?> command;
        if (payload != null) {
            if (payload instanceof KubernetesCommand) {
                command = (KubernetesCommand) payload;
            } else {
                Object unmarshalled = endpointConfiguration.getKubernetesMarshaller().unmarshal(message.getPayload(Source.class));
                command = createCommandFromModel(unmarshalled);
            }
        } else {
            command = createCommand(message.getHeaders());
        }

        if (command == null) {
            throw new CitrusRuntimeException("Unable to create proper Kubernetes command from payload: " + payload);
        }

        return command;
    }

    private KubernetesCommand<?> createCommandFromModel(Object model) {
        KubernetesCommand<?> command;

        if (model instanceof InfoMessage) {
            command = new Info();
        } else if (model instanceof ListPods) {
            command = new ListPods();
        } else if (model instanceof WatchPods) {
            command = new WatchPods();
        } else if (model instanceof ListNodes) {
            command = new ListNodes();
        } else if (model instanceof WatchNodes) {
            command = new WatchNodes();
        } else if (model instanceof ListNamespaces) {
            command = new ListNamespaces();
        } else if (model instanceof WatchNamespaces) {
            command = new WatchNamespaces();
        } else if (model instanceof ListServices) {
            command = new ListServices();
        } else if (model instanceof WatchServices) {
            command = new WatchServices();
        } else if (model instanceof ListReplicationControllers) {
            command = new ListReplicationControllers();
        } else if (model instanceof WatchReplicationControllers) {
            command = new WatchReplicationControllers();
        } else if (model instanceof ListEvents) {
            command = new ListEvents();
        } else if (model instanceof ListEndpoints) {
            command = new ListEndpoints();
        } else {
            throw new CitrusRuntimeException("Failed to read kubernetes command from message model type: " + model.getClass().getName());
        }

        if (model instanceof Nameable) {
            command.getParameters().put(KubernetesMessageHeaders.NAME, ((Nameable) model).getName());
        }

        if (model instanceof Namespaced) {
            command.getParameters().put(KubernetesMessageHeaders.NAMESPACE, ((Namespaced) model).getNamespace());
        }

        if (model instanceof Labled) {
            command.getParameters().put(KubernetesMessageHeaders.LABEL, ((Labled) model).getLabel());
        }

        return command;
    }
}
