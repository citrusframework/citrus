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

import com.consol.citrus.kubernetes.model.*;
import com.consol.citrus.message.DefaultMessage;
import org.springframework.xml.transform.StringResult;

import javax.xml.transform.Source;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesMessage extends DefaultMessage {

    private KubernetesMarshaller marshaller = new KubernetesMarshaller();

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
     * Request generating instantiation.
     * @param request
     * @return
     */
    public static KubernetesMessage request(KubernetesRequest request) {
        return new KubernetesMessage(request);
    }

    @Override
    public <T> T getPayload(Class<T> type) {
        if (KubernetesRequest.class.isAssignableFrom(type)) {
            if (getPayload() instanceof KubernetesRequest) {
                return (T) getPayload();
            }

            return (T) marshaller.unmarshal(getPayload(Source.class));
        } else if (KubernetesResponse.class.isAssignableFrom(type)) {
            if (getPayload() instanceof KubernetesResponse) {
                return (T) getPayload();
            }

            return (T) marshaller.unmarshal(getPayload(Source.class));
        } else if (String.class.equals(type)) {
            StringResult payloadResult = new StringResult();
            if (request != null) {
                marshaller.marshal(request, payloadResult);
                return (T) payloadResult.toString();
            } else if (response != null) {
                marshaller.marshal(response, payloadResult);
                return (T) payloadResult.toString();
            }
        }

        return super.getPayload(type);
    }
}
