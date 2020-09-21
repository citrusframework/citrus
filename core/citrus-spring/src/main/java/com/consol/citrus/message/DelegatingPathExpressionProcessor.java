/*
 * Copyright 2006-2017 the original author or authors.
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
            final Builder<?, ?> jsonPathProcessor = MessageProcessor.lookup("jsonPath")
                    .orElseThrow(() -> new CitrusRuntimeException("Missing proper Json Path extractor implementation for resource 'jsonPath' - " +
                            "consider adding proper json validation module to the project"));

            if (jsonPathProcessor instanceof WithExpressions) {
                ((WithExpressions<?>) jsonPathProcessor).expressions(jsonPathExpressions);
            }
            jsonPathProcessor.build()
                    .process(message, context);
        }

        if (!xpathExpressions.isEmpty()) {
            final Builder<?, ?> xpathProcessor = MessageProcessor.lookup("xpath")
                    .orElseThrow(() -> new CitrusRuntimeException("Missing proper Xpath extractor implementation for resource 'xpath' - " +
                            "consider adding proper xml validation module to the project"));

            if (xpathProcessor instanceof WithExpressions) {
                ((WithExpressions<?>) xpathProcessor).expressions(xpathExpressions);
            }

            xpathProcessor.build()
                    .process(message, context);
        }
    }

    /**
     * Gets the JSONPath / XPath expressions.
     * @return
     */
    public Map<String, Object> getPathExpressions() {
        return pathExpressions;
    }

}
