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

import static java.lang.Boolean.parseBoolean;

/**
 * The {@code OpenApiSettings} class provides configuration settings for enabling or disabling
 * OpenAPI request and response validation globally. The settings can be controlled through
 * system properties or environment variables.
 */
public class OpenApiSettings {

    public static final String GENERATE_OPTIONAL_FIELDS_PROPERTY = "citrus.openapi.generate.optional.fields";
    public static final String GENERATE_OPTIONAL_FIELDS_ENV = "CITRUS_OPENAPI_GENERATE_OPTIONAL_FIELDS";

    public static final String VALIDATE_OPTIONAL_FIELDS_PROPERTY = "citrus.openapi.validate.optional.fields";
    public static final String VALIDATE_OPTIONAL_FIELDS_ENV = "CITRUS_OPENAPI_VALIDATE_OPTIONAL_FIELDS";

    public static final String REQUEST_VALIDATION_ENABLED_PROPERTY = "citrus.openapi.validation.enabled.request";
    public static final String REQUEST_VALIDATION_ENABLED_ENV = "CITRUS_OPENAPI_VALIDATION_DISABLE_REQUEST";

    public static final String RESPONSE_VALIDATION_ENABLED_PROPERTY = "citrus.openapi.validation.enabled.response";
    public static final String RESPONSE_VALIDATION_ENABLED_ENV = "CITRUS_OPENAPI_VALIDATION_DISABLE_RESPONSE";

    private OpenApiSettings() {
        // static access only
    }

    public static boolean isGenerateOptionalFieldsGlobally() {
        return parseBoolean(System.getProperty(GENERATE_OPTIONAL_FIELDS_PROPERTY, System.getenv(GENERATE_OPTIONAL_FIELDS_ENV) != null ?
                System.getenv(GENERATE_OPTIONAL_FIELDS_ENV) : "true"));
    }

    public static boolean isValidateOptionalFieldsGlobally() {
        return parseBoolean(System.getProperty(VALIDATE_OPTIONAL_FIELDS_PROPERTY, System.getenv(VALIDATE_OPTIONAL_FIELDS_ENV) != null ?
                System.getenv(VALIDATE_OPTIONAL_FIELDS_ENV) : "true"));
    }

    public static boolean isRequestValidationEnabledGlobally() {
        return parseBoolean(System.getProperty(
                REQUEST_VALIDATION_ENABLED_PROPERTY, System.getenv(REQUEST_VALIDATION_ENABLED_ENV) != null ?
                        System.getenv(REQUEST_VALIDATION_ENABLED_ENV) : "true"));
    }

    public static boolean isResponseValidationEnabledGlobally() {
        return parseBoolean(System.getProperty(
                RESPONSE_VALIDATION_ENABLED_PROPERTY, System.getenv(RESPONSE_VALIDATION_ENABLED_ENV) != null ?
                        System.getenv(RESPONSE_VALIDATION_ENABLED_ENV) : "true"));
    }
}
