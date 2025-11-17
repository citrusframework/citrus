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

package org.citrusframework.validation.matcher.core;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.ParameterizedValidationMatcher;
import org.citrusframework.validation.matcher.parameter.OptionalStringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates new variables from given field. Either uses field name or control value as variable name.
 * @since 2.0
 */
public class CreateVariableValidationMatcher implements ParameterizedValidationMatcher<OptionalStringParameter> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CreateVariableValidationMatcher.class);

    @Override
    public void validate(String fieldName, String value, OptionalStringParameter controlParameter, TestContext context) throws ValidationException {
        final String name;
        if (controlParameter.isPresent()) {
            name = controlParameter.getValue();
        } else {
            name = fieldName;
        }

        logger.debug("Setting variable: {} to value: {}", name, value);

        context.setVariable(name, value);
    }

    @Override
    public OptionalStringParameter getParameters() {
        return new OptionalStringParameter();
    }
}
