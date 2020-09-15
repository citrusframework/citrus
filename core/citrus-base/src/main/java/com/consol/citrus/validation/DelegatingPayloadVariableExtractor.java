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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.xml.XmlNamespaceAware;
import com.consol.citrus.variable.VariableExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * Generic extractor implementation delegating to JSONPath or XPath variable extractor based on given expression
 * type. Delegate extractor implementations are referenced through resource path lookup.
 *
 * @author Simon Hofmann
 * @since 2.7.3
 */
public class DelegatingPayloadVariableExtractor implements VariableExtractor {

    /** Map defines path expressions and target variable names */
    private Map<String, String> pathExpressions = new HashMap<>();

    private Map<String, String> namespaces = new HashMap<>();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DelegatingPayloadVariableExtractor.class);

    @Override
    public void extractVariables(Message message, TestContext context) {
        if (CollectionUtils.isEmpty(pathExpressions)) {return;}

        if (log.isDebugEnabled()) {
            log.debug("Reading path elements.");
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
            final VariableExtractor.Builder<?, ?> jsonPathExtractor = VariableExtractor.lookup("jsonPath")
                    .orElseThrow(() -> new CitrusRuntimeException("Missing proper Json Path extractor implementation for resource 'jsonPath' - " +
                            "consider adding proper json validation module to the project"));

            jsonPathExtractor
                    .expressions(jsonPathExpressions)
                    .build()
                    .extractVariables(message, context);
        }

        if (!xpathExpressions.isEmpty()) {
            final VariableExtractor.Builder<?, ?> xpathExtractor = VariableExtractor.lookup("xpath")
                    .orElseThrow(() -> new CitrusRuntimeException("Missing proper Xpath extractor implementation for resource 'xpath' - " +
                            "consider adding proper xml validation module to the project"));

            if (!this.namespaces.isEmpty() && xpathExtractor instanceof XmlNamespaceAware) {
                ((XmlNamespaceAware) xpathExtractor).setNamespaces(this.namespaces);
            }

            xpathExtractor
                    .expressions(xpathExpressions)
                    .build()
                    .extractVariables(message, context);
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
