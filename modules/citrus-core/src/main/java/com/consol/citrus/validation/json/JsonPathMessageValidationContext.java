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

import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.ControlMessageValidationContext;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Specialised validation context adds JSON path expressions for message validation.
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathMessageValidationContext extends ControlMessageValidationContext {

    /** Map holding xpath expressions as key and expected values as values */
    private Map<String, String> jsonPathExpressions = new HashMap<String, String>();

    /**
     * Default constructor using JSON message type.
     */
    public JsonPathMessageValidationContext() {
        super(MessageType.JSON.toString());
    }

    /**
     * Get the control message elements that have to be present in
     * the received message. Message element values are compared as well.
     * @return the jsonPathExpressions
     */
    public Map<String, String> getJsonPathExpressions() {
        return jsonPathExpressions;
    }

    /**
     * Set the control message elements explicitly validated XPath expression validation.
     * @param jsonPathExpressions the jsonPathExpressions to set
     */
    public void setJsonPathExpressions(Map<String, String> jsonPathExpressions) {
        this.jsonPathExpressions = jsonPathExpressions;
    }

    /**
     * Check wheather give path expression is a JSONPath expression.
     * @param pathExpression
     * @return
     */
    public static boolean isJsonPathExpression(String pathExpression) {
        return StringUtils.hasText(pathExpression) && (pathExpression.startsWith("$.") || pathExpression.startsWith("$["));
    }
}
