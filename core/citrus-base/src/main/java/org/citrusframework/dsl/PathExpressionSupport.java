/*
 * Copyright the original author or authors.
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

package org.citrusframework.dsl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.citrusframework.builder.PathExpressionAdapter;
import org.citrusframework.builder.WithExpressions;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.PathExpressionValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.variable.VariableExtractor;

public class PathExpressionSupport implements WithExpressions<PathExpressionSupport>, PathExpressionAdapter {

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
    public ValidationContext asValidationContext() {
        return new PathExpressionValidationContext.Builder()
                .expressions(expressions)
                .build();
    }

    @Override
    public MessageProcessor asProcessor() {
        return new DelegatingPathExpressionProcessor.Builder()
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
