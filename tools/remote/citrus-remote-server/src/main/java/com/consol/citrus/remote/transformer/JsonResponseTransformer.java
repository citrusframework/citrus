/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.remote.transformer;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import spark.ResponseTransformer;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JsonResponseTransformer implements ResponseTransformer {

    private final ObjectMapper mapper;

    /**
     * Default constructor initializing object mapper.
     */
    public JsonResponseTransformer() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String render(Object model) {
        try {
            return mapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException("Failed to write json test results", e);
        }
    }
}
