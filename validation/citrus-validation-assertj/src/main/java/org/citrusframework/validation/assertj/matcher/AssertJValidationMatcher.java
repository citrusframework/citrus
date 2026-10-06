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

package org.citrusframework.validation.assertj.matcher;

import java.util.List;
import java.util.Optional;

import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.ControlExpressionParser;
import org.citrusframework.validation.matcher.ParameterizedValidationMatcher;

/**
 * Validation matcher {@code @assertj(...)@} that applies a chain of AssertJ assertions to the received value,
 * e.g. {@code @assertj(asInt().isBetween(1, 10))@}. The form {@code @assertj('value', 'chain')@} asserts on
 * the given value instead.
 */
@SchemaType(module = "citrus-validation-assertj")
public class AssertJValidationMatcher implements ParameterizedValidationMatcher<AssertJValidationMatcher.Parameters>, ControlExpressionParser {

    @Override
    public void validate(String fieldName, String value, Parameters controlParameters, TestContext context) throws ValidationException {
        String expression = controlParameters.getExpression().trim();
        String actual = Optional.ofNullable(controlParameters.getValue()).orElse(value);

        List<AssertionCall> calls = AssertionChainParser.parse(expression);
        try {
            AssertionChainInvoker.invoke(actual, calls);
        } catch (AssertionError e) {
            throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName
                    + "'. Received value is '" + actual
                    + "' and did not satisfy '" + expression + "'.", e);
        }
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    /**
     * Splits {@code 'value', chain} into the value and the chain; anything else is a chain on its own.
     * Unlike the default parser, the chain may contain quoted arguments, and the value may contain {@code \'}.
     */
    @Override
    public List<String> extractControlValues(String controlExpression, Character delimiter) {
        String expression = controlExpression.trim();
        if (!expression.startsWith("'")) {
            return List.of(expression);
        }

        StringBuilder value = new StringBuilder();
        int position = 1;
        while (position < expression.length()) {
            char current = expression.charAt(position++);
            if (current == '\\' && position < expression.length() && expression.charAt(position) == '\'') {
                value.append(expression.charAt(position++));
            } else if (current == '\'') {
                String rest = expression.substring(position).trim();
                if (rest.startsWith(",")) {
                    return List.of(value.toString(), unquote(rest.substring(1).trim()));
                }
                break;
            } else {
                value.append(current);
            }
        }

        return List.of(expression);
    }

    private static String unquote(String chain) {
        if (chain.length() > 1 && chain.startsWith("'") && chain.endsWith("'")) {
            return chain.substring(1, chain.length() - 1);
        }
        return chain;
    }

    public static class Parameters implements ParameterizedValidationMatcher.ControlParameters {
        private String expression;
        private String value;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList.size() > 1) {
                setValue(context.replaceDynamicContentInString(parameterList.get(0)));
                setExpression(parameterList.get(1));
            } else {
                setExpression(parameterList.get(0));
            }
        }

        public String getExpression() {
            return expression;
        }

        @SchemaProperty(required = true, description = "The AssertJ assertion chain to apply, e.g. asInt().isBetween(1, 10).")
        public void setExpression(String expression) {
            this.expression = expression;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(description = "The value to verify instead of the received value.")
        public void setValue(String value) {
            this.value = value;
        }
    }
}
