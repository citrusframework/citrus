/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.functions.core;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.Function;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Function returns given string argument in lower case.
 * 
 * @author Christoph Deppisch
 */
public class EnvironmentPropertyFunction implements Function, EnvironmentAware {

    /** Spring environment */
    private Environment environment;

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (CollectionUtils.isEmpty(parameterList)) {
            throw new InvalidFunctionUsageException("Invalid function parameters - must set environment property name");
        }

        String propertyName = parameterList.get(0);

        final Optional<String> defaultValue;
        if (parameterList.size() > 1) {
            defaultValue = Optional.of(parameterList.get(1));
        } else {
            defaultValue = Optional.empty();
        }

        return Optional.ofNullable(environment.getProperty(propertyName))
                       .orElseGet(() -> defaultValue.orElseThrow(() -> new CitrusRuntimeException(String.format("Failed to resolve property '%s' in environment", propertyName))));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
