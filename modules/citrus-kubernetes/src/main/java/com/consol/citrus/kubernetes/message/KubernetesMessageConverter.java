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
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.util.StringUtils;

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
        KubernetesResponse response;
        if (command instanceof Info) {
            response = new InfoResponse();
        } else if (command instanceof ListPods) {
            response = new ListPodsResponse();
        } else if (command instanceof WatchPods) {
            response = new WatchPodsResponse();
        } else if (command instanceof ListNodes) {
            response = new ListNodesResponse();
        } else if (command instanceof WatchNodes) {
            response = new WatchNodesResponse();
        } else if (command instanceof ListNamespaces) {
            response = new ListNamespacesResponse();
        } else if (command instanceof WatchNamespaces) {
            response = new WatchNamespacesResponse();
        } else if (command instanceof ListServices) {
            response = new ListServicesResponse();
        } else if (command instanceof WatchServices) {
            response = new WatchServicesResponse();
        } else if (command instanceof ListReplicationControllers) {
            response = new ListReplicationControllersResponse();
        } else if (command instanceof WatchReplicationControllers) {
            response = new WatchReplicationControllersResponse();
        } else if (command instanceof ListEvents) {
            response = new ListEventsResponse();
        } else if (command instanceof ListEndpoints) {
            response = new ListEndpointsResponse();
        } else {
            throw new CitrusRuntimeException("Failed to create kubernetes response from command type: " + command.getClass().getName());
        }

        KubernetesMessage message = KubernetesMessage.response(response);
        message.setHeader(KubernetesMessageHeaders.COMMAND, command.getName().substring("kubernetes:".length()));
        for (Map.Entry<String, Object> header : createMessageHeaders(command).entrySet()) {
            message.setHeader(header.getKey(), header.getValue());
        }

        CommandResult<?> commandResult = command.getCommandResult();
        if (commandResult != null) {
            if (commandResult.getResult() != null) {
                try {
                    response.setResult(endpointConfiguration.getResultMapper().writeValueAsString(commandResult.getResult()));
                } catch (JsonProcessingException e) {
                    throw new CitrusRuntimeException(e);
                }
            }

            if (commandResult.hasError()) {
                response.setError(commandResult.getError().getMessage());
            }

            if (commandResult instanceof WatchEventResult) {
                if (response instanceof KubernetesWatchResponse) {
                    ((KubernetesWatchResponse) response).setAction(((WatchEventResult) commandResult).getAction().name());
                }
                message.setHeader(KubernetesMessageHeaders.ACTION, ((WatchEventResult) commandResult).getAction().name());
            }
        }

        return message;
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
        if (message instanceof KubernetesMessage) {
            command = createCommandFromRequest(message.getPayload(KubernetesRequest.class));
        } else if (payload == null || !StringUtils.hasText(payload.toString())) {
            command = createCommand(message.getHeaders());
        } else if (payload instanceof KubernetesCommand) {
            command = (KubernetesCommand) payload;
        } else {
            KubernetesRequest request = (KubernetesRequest) endpointConfiguration.getKubernetesMarshaller().unmarshal(message.getPayload(Source.class));
            command = createCommandFromRequest(request);
        }

        if (command == null) {
            throw new CitrusRuntimeException("Unable to create proper Kubernetes command from payload: " + payload);
        }

        return command;
    }

    private KubernetesCommand<?> createCommandFromRequest(KubernetesRequest request) {
        KubernetesCommand<?> command;

        if (request instanceof InfoRequest) {
            command = new Info();
        } else if (request instanceof ListPodsRequest) {
            command = new ListPods();
        } else if (request instanceof WatchPodsRequest) {
            command = new WatchPods();
        } else if (request instanceof ListNodesRequest) {
            command = new ListNodes();
        } else if (request instanceof WatchNodesRequest) {
            command = new WatchNodes();
        } else if (request instanceof ListNamespacesRequest) {
            command = new ListNamespaces();
        } else if (request instanceof WatchNamespacesRequest) {
            command = new WatchNamespaces();
        } else if (request instanceof ListServicesRequest) {
            command = new ListServices();
        } else if (request instanceof WatchServicesRequest) {
            command = new WatchServices();
        } else if (request instanceof ListReplicationControllersRequest) {
            command = new ListReplicationControllers();
        } else if (request instanceof WatchReplicationControllersRequest) {
            command = new WatchReplicationControllers();
        } else if (request instanceof ListEventsRequest) {
            command = new ListEvents();
        } else if (request instanceof ListEndpointsRequest) {
            command = new ListEndpoints();
        } else {
            throw new CitrusRuntimeException("Failed to read kubernetes command from message request type: " + request.getClass().getName());
        }

        if (request instanceof Nameable) {
            command.getParameters().put(KubernetesMessageHeaders.NAME, ((Nameable) request).getName());
        }

        if (request instanceof Namespaced) {
            command.getParameters().put(KubernetesMessageHeaders.NAMESPACE, ((Namespaced) request).getNamespace());
        }

        if (request instanceof Labled) {
            command.getParameters().put(KubernetesMessageHeaders.LABEL, ((Labled) request).getLabel());
        }

        return command;
    }
}
