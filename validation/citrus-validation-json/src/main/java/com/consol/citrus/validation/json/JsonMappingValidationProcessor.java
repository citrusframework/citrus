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

package com.consol.citrus.validation.json;

import java.io.IOException;
import java.util.Map;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageProcessor;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.validation.AbstractValidationProcessor;
import com.consol.citrus.validation.GenericValidationProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.Assert;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public abstract class JsonMappingValidationProcessor<T> extends AbstractValidationProcessor<T> {

    /** JSON object mapper */
    private ObjectMapper mapper;

    /** The result type */
    private final Class<T> resultType;

    /**
     * Default constructor.
     */
    public JsonMappingValidationProcessor(Class<T> resultType) {
        this.resultType = resultType;
    }

    /**
     * Default constructor with object mapper.
     */
    public JsonMappingValidationProcessor(Class<T> resultType, ObjectMapper mapper) {
        this.resultType = resultType;
        this.mapper = mapper;
    }

    @Override
    public void validate(Message message, TestContext context) {
        validate(readJson(message), message.getHeaders(), context);
    }

    private T readJson(Message message) {
        if (mapper == null) {
            Assert.notNull(referenceResolver, "Json mapping validation callback requires object mapper instance " +
                    "or proper reference resolver with nested bean definition of type marshaller");

            mapper = referenceResolver.resolve(ObjectMapper.class);
        }

        try {
            return mapper.readValue(message.getPayload(String.class), resultType);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to unmarshal message payload", e);
        }
    }

    /**
     * Fluent builder.
     * @param <T>
     */
    public static final class Builder<T> implements MessageProcessor.Builder<JsonMappingValidationProcessor<T>, Builder<T>>, ReferenceResolverAware {

        private final Class<T> resultType;
        private ObjectMapper mapper;
        private GenericValidationProcessor<T> validationProcessor;

        private ReferenceResolver referenceResolver;

        public Builder(Class<T> type) {
            this.resultType = type;
        }

        public static <T> Builder<T> validate(Class<T> type) {
            return new Builder<>(type);
        }

        public Builder<T> validator(GenericValidationProcessor<T> validationProcessor) {
            this.validationProcessor = validationProcessor;
            return this;
        }

        public Builder<T> mapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public Builder<T> withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        @Override
        public JsonMappingValidationProcessor<T> build() {
            if (mapper == null) {
                if (referenceResolver != null) {
                    mapper = referenceResolver.resolve(ObjectMapper.class);
                } else {
                    throw new CitrusRuntimeException("Missing object mapper - " +
                            "please set proper mapper or reference resolver");
                }
            }

            if (validationProcessor == null) {
                throw new CitrusRuntimeException("Missing validation processor - " +
                        "please add proper validation logic");
            }

            return new JsonMappingValidationProcessor<T>(resultType, mapper) {
                @Override
                public void validate(T payload, Map<String, Object> headers, TestContext context) {
                    validationProcessor.validate(payload, headers, context);
                }
            };
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }
    }
}
