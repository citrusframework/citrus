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

package com.consol.citrus.validation;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
import com.consol.citrus.validation.xml.XpathPayloadVariableExtractor;
import com.consol.citrus.variable.VariableExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Generic extractor implementation delegating to JSONPath or XPath variable extractor based on given expression
 * type.
 *
 * @author Simon Hofmann
 * @since 2.7.3
 */
public class DefaultPayloadVariableExtractor implements VariableExtractor {

    /** Map defines path expressions and target variable names */
    private Map<String, String> pathExpressions = new HashMap<>();

    private Map<String, String> namespaces = new HashMap<>();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultPayloadVariableExtractor.class);

    @Override
    public void extractVariables(Message message, TestContext context) {
        if (CollectionUtils.isEmpty(pathExpressions)) {return;}

        if (log.isDebugEnabled()) {
            log.debug("Reading path elements.");
        }

        JsonPathVariableExtractor jsonPathVariableExtractor = new JsonPathVariableExtractor();
        XpathPayloadVariableExtractor xpathPayloadVariableExtractor = new XpathPayloadVariableExtractor();

        if (!this.namespaces.isEmpty()) {
            xpathPayloadVariableExtractor.setNamespaces(this.namespaces);
        }

        Map<String, String> jsonPathExpressions = new LinkedHashMap<>();
        Map<String, String> xpathExpressions = new LinkedHashMap<>();

        for (Map.Entry<String, String> pathExpression : pathExpressions.entrySet()) {
            final String path = context.replaceDynamicContentInString(pathExpression.getKey());
            final String variable = pathExpression.getValue();

            if (JsonPathMessageValidationContext.isJsonPathExpression(path)) {
                jsonPathExpressions.put(path, variable);
            } else {
                xpathExpressions.put(path, variable);
            }
        }

        if (!jsonPathExpressions.isEmpty()) {
            jsonPathVariableExtractor.setJsonPathExpressions(jsonPathExpressions);
            jsonPathVariableExtractor.extractVariables(message, context);
        }

        if (!xpathExpressions.isEmpty()) {
            xpathPayloadVariableExtractor.setXpathExpressions(xpathExpressions);
            xpathPayloadVariableExtractor.extractVariables(message, context);
        }
    }

    /**
     * Sets the JSONPath / XPath expressions.
     * @param pathExpressions
     */
    public void setPathExpressions(Map<String, String> pathExpressions) {
        this.pathExpressions = pathExpressions;
    }

    /**
     * Gets the JSONPath / XPath expressions.
     * @return
     */
    public Map<String, String> getPathExpressions() {
        return pathExpressions;
    }

    /**
     * Gets the XPath namespaces
     * @return the namespaces
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Sets the namespaces
     * @param namespaces the namespaces
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

}
