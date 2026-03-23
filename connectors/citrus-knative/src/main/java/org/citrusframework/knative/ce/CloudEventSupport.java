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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessageType;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

public final class CloudEventSupport {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
            .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
            .build();

    /**
     * Prevent instantiation
     */
    private CloudEventSupport() {
        //utility class
    }

    /**
     * Prepare request message with given event data as body and CloudEvent attributes set as Http headers.
     */
    public static CloudEventMessage createEventMessage(String eventData, Map<String, Object> attributes) {
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
     */
    public static Map<String, Object> attributesFromJson(String json) {
        Map<String, Object> attributes = new HashMap<>();
        try {
            JsonNode event = mapper.reader().readTree(json);
            for (CloudEvent.Attribute attribute : CloudEvent.v1_0().attributes()) {
                Optional.ofNullable(event.findValue(attribute.json()))
                        .ifPresent(e -> attributes.put(attribute.json(), e.stringValue()));
            }

            if (event.findValue("data") != null) {
                attributes.put("data", event.get("data").stringValue());
            }
        } catch (JacksonException e) {
            throw new CitrusRuntimeException("Failed to read cloud event json", e);
        }

        return attributes;
    }
}
