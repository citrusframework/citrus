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

package org.citrusframework.validation.context;

import java.util.List;

import org.citrusframework.validation.HeaderValidator;

public interface HeaderValidationContextBuilder<T extends ValidationContext, B extends HeaderValidationContextBuilder<T, B>>
        extends ValidationContext.Builder<T, B> {

    /**
     * Sets the headerNameIgnoreCase.
     */
    B ignoreCase(boolean headerNameIgnoreCase);

    /**
     * Adds header validator.
     */
    B validator(HeaderValidator validator);

    /**
     * Adds header validator reference.
     */
    B validator(String validatorName);

    /**
     * Sets the validators.
     */
    B validators(List<HeaderValidator> validators);

    /**
     * Sets the validatorNames.
     */
    B validatorNames(List<String> validatorNames);

    interface Factory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        HeaderValidationContextBuilder<?, ?> headers();

    }
}
