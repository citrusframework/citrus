/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.dsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.message.builder.ObjectMappingPayloadBuilder;
import org.citrusframework.validation.json.JsonMappingValidationProcessor;
import org.citrusframework.validation.json.JsonMessageValidationContext;

/**
 * @author Christoph Deppisch
 */
public class JsonSupport {

    /**
     * Static entrance for all Json related Java DSL functionalities.
     * @return
     */
    public static JsonMessageValidationContext.Builder json() {
        return new JsonMessageValidationContext.Builder();
    }

    /**
     * Static entrance for Json mapping validation that uses object mapper to perform Json object validation.
     * @param type
     * @return
     * @param <T>
     */
    public static <T> JsonMappingValidationProcessor.Builder<T> validate(Class<T> type) {
        return JsonMappingValidationProcessor.Builder.validate(type);
    }

    /**
     * Static builder method constructing a mapping payload builder.
     * @param payload
     * @return
     */
    public static ObjectMappingPayloadBuilder marshal(Object payload) {
        return new ObjectMappingPayloadBuilder(payload);
    }


    /**
     * Static builder method constructing a mapping payload builder.
     * @param payload
     * @param mapper
     * @return
     */
    public static ObjectMappingPayloadBuilder marshal(Object payload, ObjectMapper mapper) {
        return new ObjectMappingPayloadBuilder(payload, mapper);
    }
}
