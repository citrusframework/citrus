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

package org.citrusframework.json.message.processor;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.AbstractMessageProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.processor.json.JsonUnmarshallingMessageProcessorBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Message processor that unmarshals a JSON string payload to a Java object
 * using Jackson ObjectMapper.
 *
 * @since 4.6
 */
public class JsonUnmarshallingMessageProcessor extends AbstractMessageProcessor {

    private final ObjectMapper mapper;
    private final Class<?> resultType;

    public JsonUnmarshallingMessageProcessor(Class<?> resultType) {
        this(resultType, null);
    }

    public JsonUnmarshallingMessageProcessor(Class<?> resultType, ObjectMapper mapper) {
        this.resultType = resultType;
        this.mapper = mapper;
    }

    @Override
    public void processMessage(Message message, TestContext context) {
        ObjectMapper objectMapper = resolveMapper(context);

        try {
            message.setPayload(objectMapper.readValue(message.getPayload(String.class), resultType));
        } catch (JacksonException e) {
            throw new CitrusRuntimeException("Failed to unmarshal JSON message payload", e);
        }
    }

    private ObjectMapper resolveMapper(TestContext context) {
        if (mapper != null) {
            return mapper;
        }

        Map<String, ObjectMapper> mappers = context.getReferenceResolver().resolveAll(ObjectMapper.class);
        if (mappers.size() == 1) {
            return mappers.values().iterator().next();
        } else {
            throw new CitrusRuntimeException(String.format("Unable to auto detect object mapper - " +
                    "found %d matching mapper instances in reference resolver", mappers.size()));
        }
    }

    public static class Builder implements JsonUnmarshallingMessageProcessorBuilder<JsonUnmarshallingMessageProcessor, Builder> {

        private ObjectMapper mapper;
        private Class<?> resultType;

        @Override
        public Builder type(Class<?> resultType) {
            this.resultType = resultType;
            return this;
        }

        @Override
        public Builder mapper(Object mapper) {
            if (mapper instanceof ObjectMapper objectMapper) {
                this.mapper = objectMapper;
            }

            return this;
        }

        public Builder mapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        @Override
        public JsonUnmarshallingMessageProcessor build() {
            if (resultType == null) {
                throw new CitrusRuntimeException("Missing result type for JSON unmarshalling - " +
                        "please set the target type to unmarshal to");
            }
            return new JsonUnmarshallingMessageProcessor(resultType, mapper);
        }
    }
}
