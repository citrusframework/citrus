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

package org.citrusframework.functions.core;

import java.util.List;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.Function;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

/**
 * Function to get environment variable settings.
 *
 * @author Christoph Deppisch
 */
public class EnvironmentPropertyFunction implements Function, EnvironmentAware {

    /** Spring environment */
    private Environment environment;

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Invalid function parameters - must set environment property name");
        }

        String propertyName = parameterList.get(0);

        final Optional<String> defaultValue;
        if (parameterList.size() > 1) {
            defaultValue = Optional.of(parameterList.get(1));
        } else {
            defaultValue = Optional.empty();
        }

        Optional<String> value;
        if (environment != null) {
            value = Optional.ofNullable(environment.getProperty(propertyName));
        } else {
            value = Optional.ofNullable(System.getenv(propertyName));
        }

        return value.orElseGet(() -> defaultValue.orElseThrow(() -> new CitrusRuntimeException(String.format("Failed to resolve property '%s' in environment", propertyName))));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Environment getEnvironment() {
        return environment;
    }
}
