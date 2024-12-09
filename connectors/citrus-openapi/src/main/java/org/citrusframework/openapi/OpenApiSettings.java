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

    public static final String REQUEST_VALIDATION_ENABLED_PROPERTY = "citrus.openapi.validation.enabled.request";
    public static final String REQUEST_VALIDATION_ENABLED_ENV = "CITRUS_OPENAPI_VALIDATION_DISABLE_REQUEST";

    public static final String RESPONSE_VALIDATION_ENABLED_PROPERTY = "citrus.openapi.validation.enabled.response";
    public static final String RESPONSE_VALIDATION_ENABLED_ENV = "CITRUS_OPENAPI_VALIDATION_DISABLE_RESPONSE";

    public static final String NEGLECT_OPEN_API_BASE_PATH_PROPERTY  = "citrus.openapi.neglect.base.path";
    public static final String NEGLECT_OPEN_API_BASE_PATH_ENV = "CITRUS_OPENAPI_NEGLECT_BASE_PATH";

    public static final String REQUEST_AUTO_FILL_RANDOM_VALUES = "citrus.openapi.request.fill.random.values";
    public static final String REQUEST_AUTO_FILL_RANDOM_VALUES_ENV = "CITRUS_OPENAPI_REQUEST_FILL_RANDOM_VALUES";

    public static final String RESPONSE_AUTO_FILL_RANDOM_VALUES = "citrus.openapi.response.fill.random.values";
    public static final String RESPONSE_AUTO_FILL_RANDOM_VALUES_ENV = "CITRUS_OPENAPI_RESPONSE_FILL_RANDOM_VALUES";

    private OpenApiSettings() {
        // static access only
    }

    public static boolean isGenerateOptionalFieldsGlobally() {
        return parseBoolean(System.getProperty(GENERATE_OPTIONAL_FIELDS_PROPERTY, System.getenv(GENERATE_OPTIONAL_FIELDS_ENV) != null ?
                System.getenv(GENERATE_OPTIONAL_FIELDS_ENV) : "true"));
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

    public static boolean isNeglectBasePathGlobally() {
        return parseBoolean(System.getProperty(
            NEGLECT_OPEN_API_BASE_PATH_PROPERTY, System.getenv(NEGLECT_OPEN_API_BASE_PATH_ENV) != null ?
                System.getenv(NEGLECT_OPEN_API_BASE_PATH_ENV) : "false"));
    }

    /**
     * The default AutoFillType for request is set to REQUIRED to support backwards compatibility.
     */
    public static AutoFillType getRequestAutoFillRandomValues() {
        return AutoFillType.valueOf(System.getProperty(
            REQUEST_AUTO_FILL_RANDOM_VALUES, System.getenv(REQUEST_AUTO_FILL_RANDOM_VALUES_ENV) != null ?
                System.getenv(REQUEST_AUTO_FILL_RANDOM_VALUES_ENV) : AutoFillType.REQUIRED.name()));
    }

    /**
     * The default AutoFillType for response is set to REQUIRED to support backwards compatibility.
     */
    public static AutoFillType getResponseAutoFillRandomValues() {
        return AutoFillType.valueOf(System.getProperty(
            RESPONSE_AUTO_FILL_RANDOM_VALUES, System.getenv(RESPONSE_AUTO_FILL_RANDOM_VALUES_ENV) != null ?
                System.getenv(RESPONSE_AUTO_FILL_RANDOM_VALUES_ENV) : AutoFillType.REQUIRED.name()));
    }
}
