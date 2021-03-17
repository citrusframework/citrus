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

package com.consol.citrus.dsl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.consol.citrus.builder.WithExpressions;
import com.consol.citrus.validation.DelegatingPayloadVariableExtractor;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.VariableExtractorAdapter;

/**
 * @author Christoph Deppisch
 */
public class PathExpressionSupport implements WithExpressions<PathExpressionSupport>, VariableExtractorAdapter {

    private final Map<String, Object> expressions = new LinkedHashMap<>();

    /**
     * Static entrance for all generic path expression related Java DSL functionalities.
     * @return
     */
    public static PathExpressionSupport path() {
        return new PathExpressionSupport();
    }

    @Override
    public VariableExtractor asExtractor() {
        return new DelegatingPayloadVariableExtractor.Builder()
                .expressions(expressions)
                .build();
    }

    @Override
    public PathExpressionSupport expressions(Map<String, Object> expressions) {
        this.expressions.putAll(expressions);
        return this;
    }

    @Override
    public PathExpressionSupport expression(String expression, Object value) {
        expressions.put(expression, value);
        return this;
    }
}
