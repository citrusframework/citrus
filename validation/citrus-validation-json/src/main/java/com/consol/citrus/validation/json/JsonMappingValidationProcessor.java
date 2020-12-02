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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.AbstractValidationProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.Assert;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public abstract class JsonMappingValidationProcessor<T> extends AbstractValidationProcessor<T> {

    /** JSON object mapper */
    private ObjectMapper jsonMapper;

    /** The result type */
    private Class<T> resultType;

    /**
     * Default constructor.
     */
    public JsonMappingValidationProcessor(Class<T> resultType) {
        super();
        this.resultType = resultType;
    }

    /**
     * Default constructor with object mapper.
     */
    public JsonMappingValidationProcessor(Class<T> resultType, ObjectMapper jsonMapper) {
        this.resultType = resultType;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void validate(Message message, TestContext context) {
        validate(readJson(message), message.getHeaders(), context);
    }

    @SuppressWarnings("unchecked")
    private T readJson(Message message) {
        if (jsonMapper == null) {
            Assert.notNull(referenceResolver, "Json mapping validation callback requires object mapper instance " +
                    "or proper reference resolver with nested bean definition of type marshaller");

            jsonMapper = referenceResolver.resolve(ObjectMapper.class);
        }

        try {
            return jsonMapper.readValue(message.getPayload(String.class), resultType);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to unmarshal message payload", e);
        }
    }
}
