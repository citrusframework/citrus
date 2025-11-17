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

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;

public interface ParameterizedValidationMatcher<P extends ParameterizedValidationMatcher.ControlParameters> extends ValidationMatcher {

    void validate(String fieldName, String value, P controlParameters, TestContext context) throws ValidationException;

    /**
     * Instantiate new function parameters.
     */
    P getParameters();

    @Override
    default void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
        P params = getParameters();
        params.configure(controlParameters, context);
        validate(fieldName, value, params, context);
    }

    @FunctionalInterface
    interface ControlParameters {
        /**
         * Converts list of String parameters to typed parameters that are passed to the validation matcher.
         */
        void configure(List<String> controlParameters, TestContext context);
    }
}
