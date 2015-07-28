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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.UnknownElementException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.interceptor.AbstractMessageConstructionInterceptor;
import com.jayway.jsonpath.*;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathMessageConstructionInterceptor extends AbstractMessageConstructionInterceptor {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JsonPathMessageConstructionInterceptor.class);

    /** Overwrites message elements before validating (via JSONPath expressions) */
    private Map<String, String> jsonPathExpressions = new HashMap<>();

    /**
     * Default constructor.
     */
    public JsonPathMessageConstructionInterceptor() {
        super();
    }

    /**
     * Default constructor using fields.
     * @param jsonPathExpressions
     */
    public JsonPathMessageConstructionInterceptor(Map<String, String> jsonPathExpressions) {
        super();
        this.jsonPathExpressions = jsonPathExpressions;
    }

    /**
     * Intercept the message payload construction and replace elements identified
     * via XPath expressions.
     *
     * Method parses the message payload to DOM document representation, therefore message payload
     * needs to be XML here.
     */
    @Override
    public Message interceptMessage(Message message, String messageType, TestContext context) {
        if (message.getPayload() == null || !StringUtils.hasText(message.getPayload(String.class))) {
            return message;
        }

        String jsonPathExpression = null;
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            Object jsonData = parser.parse(message.getPayload(String.class));
            DocumentContext documentContext = JsonPath.parse(jsonData);

            for (Map.Entry<String, String> entry : jsonPathExpressions.entrySet()) {
                jsonPathExpression = entry.getKey();
                String valueExpression = context.replaceDynamicContentInString(entry.getValue());

                documentContext.set(jsonPathExpression, valueExpression);

                if (log.isDebugEnabled()) {
                    log.debug("Element " + jsonPathExpression + " was set to value: " + valueExpression);
                }
            }

            message.setPayload(jsonData.toString());
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        } catch (PathNotFoundException e) {
            throw new UnknownElementException(String.format("Could not find element for expression: %s", jsonPathExpression), e);
        }

        return message;
    }

    @Override
    public boolean supportsMessageType(String messageType) {
        return MessageType.JSON.toString().equalsIgnoreCase(messageType);
    }

    public void setJsonPathExpressions(Map<String, String> jsonPathExpressions) {
        this.jsonPathExpressions = jsonPathExpressions;
    }

    public Map<String, String> getJsonPathExpressions() {
        return jsonPathExpressions;
    }
}
