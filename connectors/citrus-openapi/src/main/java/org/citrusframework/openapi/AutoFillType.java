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

package org.citrusframework.openapi;

/**
 * Enum representing different types of autofill behavior for OpenAPI parameters/body.
 * This enum defines how missing or required parameters/body should be autofilled.
 */
public enum AutoFillType {
    /**
     * No autofill will be performed for any parameters/body.
     */
    NONE,

    /**
     * Autofill will be applied only to required parameters/body that are missing.
     */
    REQUIRED,

    /**
     * Autofill will be applied to all parameters/body, whether they are required or not.
     */
    ALL;

    /**
     *  Determines whether a value should be autofilled based on its required status
     *  and the given autofill strategy.
     *
     * @param required {@code true} if the parameter is required, {@code false} or {@code null} otherwise
     * @return {@code true} if the parameter should be autofilled based on the current autofill type
     */
    public boolean shouldFill(Boolean required) {
        return this == ALL || (Boolean.TRUE == required && this == REQUIRED);
    }
}
