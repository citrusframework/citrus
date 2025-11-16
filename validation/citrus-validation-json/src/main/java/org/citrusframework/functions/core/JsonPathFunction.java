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

package org.citrusframework.functions.core;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.json.JsonPathUtils;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.6.2
 */
public class JsonPathFunction implements ParameterizedFunction<JsonPathFunction.Parameters> {

    @Override
    public String execute(Parameters params, TestContext context) {
        return JsonPathUtils.evaluateAsString(
                context.replaceDynamicContentInString(params.getSource()), params.getExpression());
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements FunctionParameters {
        private String source;
        private String expression;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.isEmpty()) {
                throw new InvalidFunctionUsageException("Function parameters must not be empty");
            }

            if (parameterList.size() < 2) {
                throw new InvalidFunctionUsageException("Missing parameter for function - usage jsonPath('jsonSource', 'expression')");
            }

            if (parameterList.size() == 2) {
                setSource(parameterList.get(0));
                setExpression(context.replaceDynamicContentInString(parameterList.get(1)));
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(parameterList.get(0));
                for (int i = 1; i < parameterList.size() -1; i++) {
                    sb.append(", ").append(parameterList.get(i));
                }

                setSource(sb.toString());
                setExpression(context.replaceDynamicContentInString(parameterList.get(parameterList.size() - 1)));
            }
        }

        public String getSource() {
            return source;
        }

        @SchemaProperty(required = true, description = "The Json source.")
        public void setSource(String source) {
            this.source = source;
        }

        public String getExpression() {
            return expression;
        }

        @SchemaProperty(description = "The JsonPath expression to evaluate.")
        public void setExpression(String expression) {
            this.expression = expression;
        }
    }
}
