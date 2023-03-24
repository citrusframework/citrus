/*
 * Copyright 2021 the original author or authors.
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.citrusframework.builder.PathExpressionAdapter;
import org.citrusframework.builder.WithExpressions;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.xml.XpathMessageProcessor;
import org.citrusframework.validation.xml.XpathMessageValidationContext;
import org.citrusframework.validation.xml.XpathPayloadVariableExtractor;
import org.citrusframework.variable.VariableExtractor;

/**
 * @author Christoph Deppisch
 */
public class XpathSupport implements WithExpressions<XpathSupport>, PathExpressionAdapter {

    private final Map<String, Object> expressions = new LinkedHashMap<>();

    /**
     * Static entrance for all Xpath related Java DSL functionalities.
     * @return
     */
    public static XpathSupport xpath() {
        return new XpathSupport();
    }

    @Override
    public MessageProcessor asProcessor() {
        return new XpathMessageProcessor.Builder()
                .expressions(expressions)
                .build();
    }

    @Override
    public VariableExtractor asExtractor() {
        return new XpathPayloadVariableExtractor.Builder()
                .expressions(expressions)
                .build();
    }

    @Override
    public ValidationContext asValidationContext() {
        return new XpathMessageValidationContext.Builder()
                .expressions(expressions)
                .build();
    }

    @Override
    public XpathSupport expressions(Map<String, Object> expressions) {
        this.expressions.putAll(expressions);
        return this;
    }

    @Override
    public XpathSupport expression(String expression, Object value) {
        expressions.put(expression, value);
        return this;
    }
}
