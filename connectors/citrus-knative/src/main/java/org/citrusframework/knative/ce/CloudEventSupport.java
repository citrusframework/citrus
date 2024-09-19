/*
 * Copyright the original author or authors.
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

package org.citrusframework.knative.ce;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessageType;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

public final class CloudEventSupport {

    /**
     * Prevent instantiation
     */
    private CloudEventSupport() {
        //utility class
    }

    /**
     * Prepare request message with given event data as body and CloudEvent attributes set as Http headers.
     * @param eventData
     * @param attributes
     * @return
     */
    public static CloudEventMessage createEventMessage(String eventData, Map<String, String> attributes) {
        CloudEventMessage request = CloudEventMessage.fromEvent(CloudEvent.v1_0());
        request.setType(MessageType.JSON);
        request.method(HttpMethod.POST);

        if (attributes.containsKey("data")) {
            request.setPayload(attributes.get("data"));
        } else if (StringUtils.hasText(eventData)) {
            request.setPayload(eventData);
        }

        attributes.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("data"))
                .forEach(entry -> {
                    Optional<CloudEvent.Attribute> attribute = CloudEvent.v1_0().attributes()
                            .stream()
                            .filter(a -> a.http().equalsIgnoreCase(entry.getKey()) || a.json().equals(entry.getKey()))
                            .findFirst();

                    if (attribute.isPresent()) {
                        request.setAttribute(attribute.get(), entry.getValue());
                    } else {
                        request.header(entry.getKey(), entry.getValue());
                    }
                });

        return request;
    }

    /**
     * Reads given json string and extracts CloudEvent attributes.
     * @param json
     * @return
     */
    public static Map<String, String> attributesFromJson(String json) {
        Map<String, String> attributes = new HashMap<>();
        try {
            JsonNode event = new ObjectMapper().reader().readTree(json);
            for (CloudEvent.Attribute attribute : CloudEvent.v1_0().attributes()) {
                Optional.ofNullable(event.findValue(attribute.json()))
                        .ifPresent(e -> attributes.put(attribute.json(), e.textValue()));
            }

            if (event.findValue("data") != null) {
                attributes.put("data", event.get("data").textValue());
            }
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException("Failed to read cloud event json", e);
        }

        return attributes;
    }
}
