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

/**
 * Function parameters allows a single string argument.
 */
public class StringParameter implements ParameterizedFunction.FunctionParameters {

    private String value;
    boolean allowEmpty = true;
    boolean useRawValue = false;

    @Override
    public void configure(List<String> parameterList, TestContext context) {
        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        if (parameterList.size() != 1) {
            throw new InvalidFunctionUsageException("Too many function parameters");
        }

        if (!allowEmpty && parameterList.get(0).isEmpty()) {
            throw new InvalidFunctionUsageException("The input must be a single non-empty string");
        }

        if (useRawValue) {
            setValue(parameterList.get(0));
        } else {
            setValue(context.resolveDynamicValue(parameterList.get(0)));
        }
    }

    public StringParameter withAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
        return this;
    }

    public StringParameter withUseRawValue(boolean useRawValue) {
        this.useRawValue = useRawValue;
        return this;
    }

    public String getValue() {
        return value;
    }

    @SchemaProperty(required = true, description = "The value to evaluate.")
    public void setValue(String value) {
        this.value = value;
    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    public boolean isUseRawValue() {
        return useRawValue;
    }

    public void setUseRawValue(boolean useRawValue) {
        this.useRawValue = useRawValue;
    }
}
