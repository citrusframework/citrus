/*
 * Copyright 2022 the original author or authors.
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

package com.consol.citrus.message;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.consol.citrus.builder.WithExpressions;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import org.springframework.util.CollectionUtils;

/**
 * Generic processor implementation delegating to JSONPath or XPath message processor based on given expression
 * type. Delegate processor implementations are referenced through resource path lookup.
 *
 */
public class DelegatingPathExpressionProcessor implements MessageProcessor {

    /** Map defines path expressions */
    private final Map<String, Object> pathExpressions;

    public DelegatingPathExpressionProcessor() {
        this(new HashMap<>());
    }

    public DelegatingPathExpressionProcessor(Map<String, Object> pathExpressions) {
        this.pathExpressions = pathExpressions;
    }

    @Override
    public void process(Message message, TestContext context) {
        if (CollectionUtils.isEmpty(pathExpressions)) {
            return;
        }

        Map<String, Object> jsonPathExpressions = new LinkedHashMap<>();
        Map<String, Object> xpathExpressions = new LinkedHashMap<>();

        for (Map.Entry<String, Object> pathExpression : pathExpressions.entrySet()) {
            final String path = context.replaceDynamicContentInString(pathExpression.getKey());
            final Object variable = pathExpression.getValue();

            if (JsonPathMessageValidationContext.isJsonPathExpression(path)) {
                jsonPathExpressions.put(path, variable);
            } else {
                xpathExpressions.put(path, variable);
            }
        }

        if (!jsonPathExpressions.isEmpty()) {
            final MessageProcessor.Builder<?, ?> jsonPathProcessor = lookupMessageProcessor("jsonPath", context);

            if (jsonPathProcessor instanceof WithExpressions) {
                ((WithExpressions<?>) jsonPathProcessor).expressions(jsonPathExpressions);
            }
            jsonPathProcessor.build()
                    .process(message, context);
        }

        if (!xpathExpressions.isEmpty()) {
            final Builder<?, ?> xpathProcessor = lookupMessageProcessor("xpath", context);

            if (xpathProcessor instanceof WithExpressions) {
                ((WithExpressions<?>) xpathProcessor).expressions(xpathExpressions);
            }

            xpathProcessor.build()
                    .process(message, context);
        }
    }

    private MessageProcessor.Builder<?, ?> lookupMessageProcessor(String type, TestContext context) {
        return MessageProcessor.lookup(type)
                .orElseGet(() -> {
                    if (context.getReferenceResolver().isResolvable(type, MessageProcessor.Builder.class)) {
                        return context.getReferenceResolver().resolve(type, MessageProcessor.Builder.class);
                    }

                    if (context.getReferenceResolver().isResolvable(type + "MessageProcessorBuilder", MessageProcessor.Builder.class)) {
                        return context.getReferenceResolver().resolve(type + "MessageProcessorBuilder", MessageProcessor.Builder.class);
                    }

                    throw new CitrusRuntimeException(String.format("Missing proper message processor implementation of type '%s' - " +
                            "consider adding proper module to the project", type));
                });
    }

    /**
     * Gets the JSONPath / XPath expressions.
     * @return
     */
    public Map<String, Object> getPathExpressions() {
        return pathExpressions;
    }

}
