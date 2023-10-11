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

package org.citrusframework.kubernetes.message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.command.*;
import org.citrusframework.kubernetes.endpoint.KubernetesEndpointConfiguration;
import org.citrusframework.kubernetes.model.KubernetesRequest;
import org.citrusframework.kubernetes.model.KubernetesResponse;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesMessageConverter implements MessageConverter<KubernetesCommand<?>, KubernetesCommand<?>, KubernetesEndpointConfiguration> {

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
        KubernetesResponse response = new KubernetesResponse();
        KubernetesMessage message = KubernetesMessage.response(response);

        response.setCommand(command.getName());

        message.setHeader(KubernetesMessageHeaders.COMMAND, response.getCommand());
        for (Map.Entry<String, Object> header : createMessageHeaders(command).entrySet()) {
            message.setHeader(header.getKey(), header.getValue());
        }

        CommandResult<?> commandResult = command.getCommandResult();
        if (commandResult != null) {
            if (commandResult.getResult() != null) {
                response.setResult(commandResult.getResult());
            }

            if (commandResult.hasError()) {
                response.setError(commandResult.getError().getMessage());
            }

            if (commandResult instanceof WatchEventResult) {
                response.setAction(((WatchEventResult) commandResult).getAction().name());
                message.setHeader(KubernetesMessageHeaders.ACTION, ((WatchEventResult) commandResult).getAction().name());
            }
        }

        return message;
    }

    /**
     * Creates a new kubernetes command message model object from message headers.
     * @param commandName
     * @return
     */
    private KubernetesCommand<?> getCommandByName(String commandName) {
        if (!StringUtils.hasText(commandName)) {
            throw new CitrusRuntimeException("Missing command name property");
        }

        switch (commandName) {
            case "info":
                return new Info();
            case "list-events":
                return new ListEvents();
            case "list-endpoints":
                return new ListEndpoints();
            case "create-pod":
                return new CreatePod();
            case "get-pod":
                return new GetPod();
            case "delete-pod":
                return new DeletePod();
            case "list-pods":
                return new ListPods();
            case "watch-pods":
                return new WatchPods();
            case "list-namespaces":
                return new ListNamespaces();
            case "watch-namespaces":
                return new WatchNamespaces();
            case "list-nodes":
                return new ListNodes();
            case "watch-nodes":
                return new WatchNodes();
            case "list-replication-controllers":
                return new ListReplicationControllers();
            case "watch-replication-controllers":
                return new WatchReplicationControllers();
            case "create-service":
                return new CreateService();
            case "get-service":
                return new GetService();
            case "delete-service":
                return new DeleteService();
            case "list-services":
                return new ListServices();
            case "watch-services":
                return new WatchServices();
            default:
                throw new CitrusRuntimeException("Unknown kubernetes command: " + commandName);
        }
    }

    /**
     * Reads basic command information and converts to message headers.
     * @param command
     * @return
     */
    private Map<String,Object> createMessageHeaders(KubernetesCommand<?> command) {
        Map<String, Object> headers = new HashMap<String, Object>();

        headers.put(KubernetesMessageHeaders.COMMAND, command.getName());

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
        } else if (message.getHeaders().containsKey(KubernetesMessageHeaders.COMMAND) &&
                (payload == null || !StringUtils.hasText(payload.toString()))) {
            command = getCommandByName(message.getHeader(KubernetesMessageHeaders.COMMAND).toString());
        } else if (payload instanceof KubernetesCommand) {
            command = (KubernetesCommand) payload;
        } else {
            try {
                KubernetesRequest request = endpointConfiguration.getObjectMapper()
                        .readValue(message.getPayload(String.class), KubernetesRequest.class);
                command = createCommandFromRequest(request);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read kubernetes request from payload", e);
            }
        }

        if (command == null) {
            throw new CitrusRuntimeException("Unable to create proper Kubernetes command from payload: " + payload);
        }

        return command;
    }

    private KubernetesCommand<?> createCommandFromRequest(KubernetesRequest request) {
        KubernetesCommand<?> command = getCommandByName(request.getCommand());

        if (StringUtils.hasText(request.getName())) {
            command.getParameters().put(KubernetesMessageHeaders.NAME, request.getName());
        }

        if (StringUtils.hasText(request.getNamespace())) {
            command.getParameters().put(KubernetesMessageHeaders.NAMESPACE, request.getNamespace());
        }

        if (StringUtils.hasText(request.getLabel())) {
            command.getParameters().put(KubernetesMessageHeaders.LABEL, request.getLabel());
        }

        return command;
    }
}
