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

package org.citrusframework.functions.parameter;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.yaml.SchemaProperty;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.getInteger;

public class NumberParameter implements ParameterizedFunction.FunctionParameters {

    private String value;

    @Override
    public void configure(List<String> parameterList, TestContext context) {
        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        setValue(context.replaceDynamicContentInString(parameterList.get(0)));
    }

    public int asInteger() {
        return getInteger(getValue());
    }

    public double asDouble() {
        return parseDouble(getValue());
    }

    public String getValue() {
        return value;
    }

    @SchemaProperty(required = true, description = "The numeric value to evaluate.")
    public void setValue(double value) {
        this.value = String.valueOf(value);
    }

    public void setValue(String value) {
        this.value = value;
    }
}
