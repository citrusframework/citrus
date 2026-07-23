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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.yaml.SchemaProperty;

public class StringParameters implements ParameterizedFunction.FunctionParameters {

    private final List<String> values = new ArrayList<>();

    private boolean allowEmpty = false;

    @Override
    public void configure(List<String> parameterList, TestContext context) {
        if (!allowEmpty && (parameterList == null || parameterList.isEmpty())) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        setValues(parameterList);
    }

    public StringParameters withAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
        return this;
    }

    public List<String> getValues() {
        return values;
    }

    @SchemaProperty(required = true, description = "The list of values to evaluate.")
    public void setValues(List<String> values) {
        this.values.addAll(values);
    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }
}
