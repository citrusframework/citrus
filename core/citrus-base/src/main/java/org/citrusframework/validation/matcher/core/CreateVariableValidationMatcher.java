/*
 * Copyright 2006-2014 the original author or authors.
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

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates new variables from given field. Either uses field name or control value as variable name.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class CreateVariableValidationMatcher implements ValidationMatcher {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CreateVariableValidationMatcher.class);

    @Override
    public void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
        String name = fieldName;

        if (controlParameters != null && controlParameters.size() > 0) {
            name = controlParameters.get(0);
        }

        logger.info("Setting variable: " + name + " to value: " + value);

        context.setVariable(name, value);
    }
}
