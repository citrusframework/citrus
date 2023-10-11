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

package org.citrusframework.functions.core;

import java.util.List;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.Function;

/**
 * Function returns given string argument in lower case.
 *
 * @author Christoph Deppisch
 */
public class SystemPropertyFunction implements Function {

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Invalid function parameters - must set system property name");
        }

        String propertyName = parameterList.get(0);

        final Optional<String> defaultValue;
        if (parameterList.size() > 1) {
            defaultValue = Optional.of(parameterList.get(1));
        } else {
            defaultValue = Optional.empty();
        }

        return Optional.ofNullable(System.getProperty(propertyName))
                .orElseGet(() -> defaultValue.orElseThrow(() -> new CitrusRuntimeException(String.format("Failed to resolve system property '%s'", propertyName))));
    }
}
