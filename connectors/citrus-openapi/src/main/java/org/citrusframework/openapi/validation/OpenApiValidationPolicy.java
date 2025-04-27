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

package org.citrusframework.openapi.validation;

/**
 * Enum that defines the policy for handling OpenAPI validation errors.
 * This enum controls the behavior of the system when validation errors are encountered
 * during OpenAPI validation, allowing for different levels of strictness.
 */
public enum OpenApiValidationPolicy {

    /**
     * No validation will be performed.
     * Any validation errors will be ignored, and the system will continue
     * without reporting or failing due to OpenAPI validation issues.
     */
    IGNORE,

    /**
     * Perform validation and report any errors.
     * Validation errors will be reported, but the system will continue running.
     * This option allows for debugging or monitoring purposes without halting the application.
     */
    REPORT,

    /**
     * Perform validation and fail if any errors are encountered.
     * Validation errors will cause the application to fail startup,
     * ensuring that only valid OpenAPI specifications are allowed to proceed.
     */
    STRICT,
}

