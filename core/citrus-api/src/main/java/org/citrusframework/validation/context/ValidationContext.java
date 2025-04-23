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

/**
 * Basic validation context holding validation specific information.
 *
 */
public interface ValidationContext {

    /**
     * Indicates whether this validation context requires a validator.
     * @return true if a validator is required; false otherwise.
     */
    default boolean requiresValidator() {
        return false;
    }

    /**
     * Update the validation status if it is allowed.
     * @param status the new status.
     */
    void updateStatus(ValidationStatus status);

    /**
     * Marks the validation result for this context.
     * By default, all validation context do have the status UNKNOWN marking that the validation has not performed yet.
     * Validators must set proper status after the validation to mark the context as being processed.
     * @return the status indicating the validation result for this context.
     */
    default ValidationStatus getStatus() {
        return ValidationStatus.UNKNOWN;
    }

    /**
     * Fluent builder
     * @param <T> context type
     * @param <B> builder reference to self
     */
    @FunctionalInterface
    interface Builder<T extends ValidationContext, B extends Builder<T, B>> {

        /**
         * Builds new validation context instance.
         * @return the built context.
         */
        T build();
    }
}
