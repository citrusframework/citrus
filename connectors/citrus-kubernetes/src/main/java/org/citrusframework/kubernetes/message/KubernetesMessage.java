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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.command.*;
import org.citrusframework.kubernetes.model.KubernetesRequest;
import org.citrusframework.kubernetes.model.KubernetesResponse;
import org.citrusframework.message.DefaultMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

import java.io.IOException;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesMessage extends DefaultMessage {

    private ObjectMapper mapper = new ObjectMapper();

    private KubernetesRequest request;
    private KubernetesResponse response;

    /**
     * Prevent traditional instantiation.
     */
    private KubernetesMessage() { super(); }

    /**
     * Constructor using payload and headers.
     * @param payload
     * @param headers
     */
    private KubernetesMessage(Object payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    /**
     * Constructor using response.
     * @param request
     */
    private KubernetesMessage(KubernetesRequest request) {
        this.request = request;
    }

    /**
     * Constructor using response.
     * @param response
     */
    private KubernetesMessage(KubernetesResponse response) {
        this.response = response;
    }

    /**
     * Response generating instantiation.
     * @param response
     * @return
     */
    public static KubernetesMessage response(KubernetesResponse response) {
        return new KubernetesMessage(response);
    }

    /**
     * Response generating instantiation.
     * @param command
     * @param result
     * @return
     */
    public static KubernetesMessage response(String command, KubernetesResource<?> result) {
        KubernetesResponse response = new KubernetesResponse();
        response.setCommand(command);
        response.setResult(result);

        return new KubernetesMessage(response);
    }

    /**
     * Response generating instantiation.
     * @param command
     * @param action
     * @param result
     * @return
     */
    public static KubernetesMessage response(String command, Watcher.Action action, KubernetesResource<?> result) {
        KubernetesResponse response = new KubernetesResponse();
        response.setCommand(command);
        response.setResult(result);
        response.setAction(action.name());

        return new KubernetesMessage(response);
    }

    /**
     * Error generating instantiation.
     * @param error
     * @return
     */
    public static KubernetesMessage response(String command, KubernetesClientException error) {
        return response(command, error.getMessage());
    }

    /**
     * Error generating instantiation.
     * @param error
     * @return
     */
    public static KubernetesMessage response(String command, String error) {
        KubernetesResponse response = new KubernetesResponse();
        response.setCommand(command);
        response.setError(error);

        return new KubernetesMessage(response);
    }

    /**
     * Request generating instantiation.
     * @param request
     * @return
     */
    public static KubernetesMessage request(KubernetesRequest request) {
        return new KubernetesMessage(request);
    }

    /**
     * Request generating instantiation.
     * @param command
     * @return
     */
    public static KubernetesMessage request(KubernetesCommand<?> command) {
        KubernetesRequest request = new KubernetesRequest();
        request.setCommand(command.getName());

        for (Map.Entry<String, Object> entry : command.getParameters().entrySet()) {
            if (entry.getKey().equals(KubernetesMessageHeaders.NAME)) {
                request.setName(entry.getValue().toString());
            }

            if (entry.getKey().equals(KubernetesMessageHeaders.NAMESPACE)) {
                request.setNamespace(entry.getValue().toString());
            }

            if (entry.getKey().equals(KubernetesMessageHeaders.LABEL)) {
                request.setLabel(entry.getValue().toString());
            }
        }

        return new KubernetesMessage(request);
    }

    @Override
    public <T> T getPayload(Class<T> type) {
        try {
            if (KubernetesRequest.class.isAssignableFrom(type)) {
                if (getPayload() instanceof KubernetesRequest) {
                    return (T) getPayload();
                }

                return (T) mapper.readValue(getPayload(String.class), KubernetesRequest.class);
            } else if (KubernetesResponse.class.isAssignableFrom(type)) {
                if (getPayload() instanceof KubernetesResponse) {
                    return (T) getPayload();
                }

                return (T) mapper.readValue(getPayload(String.class), KubernetesRequest.class);
            } else if (String.class.equals(type)) {
                if (request != null) {
                    return (T) mapper.writeValueAsString(request);
                } else if (response != null) {
                    return (T) mapper.writeValueAsString(response);
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to convert payload to required type: " + type, e);
        }

        return super.getPayload(type);
    }
}
