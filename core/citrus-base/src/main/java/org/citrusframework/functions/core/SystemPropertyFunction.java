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
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.yaml.SchemaProperty;

/**
 * Function returns given string argument in lower case.
 */
public class SystemPropertyFunction implements ParameterizedFunction<SystemPropertyFunction.Parameters> {

    @Override
    public String execute(Parameters params, TestContext context) {
        String propertyName = params.getPropertyName();

        final Optional<String> defaultValue = Optional.ofNullable(params.getDefaultValue());
        return Optional.ofNullable(System.getProperty(propertyName))
                .orElseGet(() -> defaultValue.orElseThrow(() ->
                        new CitrusRuntimeException(String.format("Failed to resolve system property '%s'", propertyName))));
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements FunctionParameters {
        private String propertyName;
        private String defaultValue;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.isEmpty()) {
                throw new InvalidFunctionUsageException("Function parameters must not be empty");
            }

            setPropertyName(parameterList.get(0));

            if (parameterList.size() > 1) {
                defaultValue = context.replaceDynamicContentInString(parameterList.get(1));
            }
        }

        public String getPropertyName() {
            return propertyName;
        }

        @SchemaProperty(required = true, description = "The system property name.")
        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        @SchemaProperty(description = "The default value when system property is not set.")
        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }
}
