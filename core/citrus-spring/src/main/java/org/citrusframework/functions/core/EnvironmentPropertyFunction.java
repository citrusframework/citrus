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
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * Function to get environment variable settings.
 */
public class EnvironmentPropertyFunction implements EnvironmentAware,
        ParameterizedFunction<EnvironmentPropertyFunction.Parameters> {

    /** Spring environment */
    private Environment environment;

    @Override
    public String execute(Parameters params, TestContext context) {
        String propertyName = params.getPropertyName();

        Optional<String> value;
        if (environment != null) {
            value = Optional.ofNullable(environment.getProperty(propertyName));
        } else {
            value = Optional.ofNullable(System.getenv(propertyName));
        }

        final Optional<String> defaultValue = Optional.ofNullable(params.getDefaultValue());
        return value.orElseGet(() -> defaultValue.orElseThrow(() -> new CitrusRuntimeException(String.format("Failed to resolve property '%s' in environment", propertyName))));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Environment getEnvironment() {
        return environment;
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

        @SchemaProperty(required = true, description = "The environment property name.")
        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        @SchemaProperty(description = "The default value when environment property is not set.")
        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }
}
