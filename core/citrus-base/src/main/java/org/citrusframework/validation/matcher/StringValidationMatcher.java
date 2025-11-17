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

package org.citrusframework.validation.matcher;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.parameter.StringParameter;

/**
 * Validation matcher supports a single String typed parameters as a control value.
 */
public interface StringValidationMatcher extends ParameterizedValidationMatcher<StringParameter> {

    /**
     * Validate with given control value.
     * @throws ValidationException when validation fails.
     */
    void validate(String fieldName, String value, String control, TestContext context) throws ValidationException;

    @Override
    default void validate(String fieldName, String value, StringParameter controlParameter, TestContext context) throws ValidationException {
        validate(fieldName, value, controlParameter.getValue(), context);
    }

    @Override
    default StringParameter getParameters() {
        return new StringParameter();
    }
}
