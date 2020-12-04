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

package com.consol.citrus.dsl;

import com.consol.citrus.validation.json.JsonMappingValidationProcessor;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageProcessor;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;

/**
 * @author Christoph Deppisch
 */
public class JsonSupport {

    /**
     * Static entrance for all Json related Java DSL functionalities.
     * @return
     */
    public static JsonSupport json() {
        return new JsonSupport();
    }

    public JsonPathSupport jsonPath() {
        return new JsonPathSupport();
    }

    public JsonPathVariableExtractor.Builder extract() {
        return new JsonPathVariableExtractor.Builder();
    }

    public JsonMessageValidationContext.Builder validate() {
        return new JsonMessageValidationContext.Builder();
    }

    public <T> JsonMappingValidationProcessor.Builder<T> validate(Class<T> type) {
        return JsonMappingValidationProcessor.Builder.validate(type);
    }

    public static final class JsonPathSupport {
        /**
         * Static entrance for all JsonPath related Java DSL functionalities.
         * @return
         */
        public static JsonPathSupport jsonPath() {
            return new JsonPathSupport();
        }

        public JsonPathMessageProcessor.Builder process() {
            return new JsonPathMessageProcessor.Builder();
        }

        public JsonPathVariableExtractor.Builder extract() {
            return new JsonPathVariableExtractor.Builder();
        }

        public JsonPathMessageValidationContext.Builder validate() {
            return new JsonPathMessageValidationContext.Builder();
        }
    }
}
