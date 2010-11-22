/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.variable;

import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.core.Message;

import com.consol.citrus.context.TestContext;

/**
 * Variable extractor reading message headers to test variables.
 * 
 * @author Christoph Deppisch
 */
public class MessageHeaderVariableExtractor implements VariableExtractor {

    /** Map holding header names and target variable names */
    private Map<String, String> headerMappings = new HashMap<String, String>();
    
    public void extractVariables(Message<?> message, TestContext context) {
        context.createVariablesFromHeaderValues(headerMappings, message.getHeaders());
    }

    /**
     * Set the header mappings.
     * @param headerMappings the headerMappings to set
     */
    public void setHeaderMappings(Map<String, String> headerMappings) {
        this.headerMappings = headerMappings;
    }
}
