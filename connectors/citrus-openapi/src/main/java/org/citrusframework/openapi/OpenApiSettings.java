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

import com.google.common.annotations.VisibleForTesting;
import org.citrusframework.openapi.validation.OpenApiValidationPolicy;
import org.citrusframework.util.SystemProvider;

import static java.lang.Boolean.parseBoolean;
import static org.citrusframework.openapi.AutoFillType.REQUIRED;
import static org.citrusframework.openapi.validation.OpenApiValidationPolicy.REPORT;
import static org.citrusframework.util.EnvUtils.booleanPropertyOrDefault;
import static org.citrusframework.util.EnvUtils.enumPropertyOrDefault;
import static org.citrusframework.util.EnvUtils.transformPropertyToEnv;
import static org.citrusframework.util.SystemProvider.SYSTEM_PROVIDER;

/**
 * The {@code OpenApiSettings} class provides configuration settings for enabling or disabling
 * OpenAPI request and response validation globally. The settings can be controlled through system
 * properties or environment variables.
 */
public class OpenApiSettings {

    public static final String GENERATE_OPTIONAL_FIELDS_ENABLED_PROPERTY = "citrus.openapi.generate.optional.fields";
    public static final String GENERATE_OPTIONAL_FIELDS_ENABLED_ENV = transformPropertyToEnv(
        GENERATE_OPTIONAL_FIELDS_ENABLED_PROPERTY);

    public static final String REQUEST_VALIDATION_ENABLED_PROPERTY = "citrus.openapi.validation.enabled.request";
    public static final String REQUEST_VALIDATION_ENABLED_ENV = transformPropertyToEnv(
        REQUEST_VALIDATION_ENABLED_PROPERTY);

    public static final String RESPONSE_VALIDATION_ENABLED_PROPERTY = "citrus.openapi.validation.enabled.response";
    public static final String RESPONSE_VALIDATION_ENABLED_ENV = transformPropertyToEnv(
        RESPONSE_VALIDATION_ENABLED_PROPERTY);

    public static final String NEGLECT_OPEN_API_BASE_PATH_ENABLED_PROPERTY = "citrus.openapi.neglect.base.path";
    public static final String NEGLECT_OPEN_API_BASE_PATH_ENABLED_ENV = transformPropertyToEnv(
        NEGLECT_OPEN_API_BASE_PATH_ENABLED_PROPERTY);

    public static final String REQUEST_AUTO_FILL_RANDOM_VALUES_PROPERTY = "citrus.openapi.request.fill.random.values";
    public static final String REQUEST_AUTO_FILL_RANDOM_VALUES_ENV = transformPropertyToEnv(
        REQUEST_AUTO_FILL_RANDOM_VALUES_PROPERTY);

    public static final String RESPONSE_AUTO_FILL_RANDOM_VALUES_PROPERTY = "citrus.openapi.response.fill.random.values";
    public static final String RESPONSE_AUTO_FILL_RANDOM_VALUES_ENV = transformPropertyToEnv(
        RESPONSE_AUTO_FILL_RANDOM_VALUES_PROPERTY);

    public static final String OPEN_API_VALIDATION_POLICY_PROPERTY = "citrus.openapi.validation.policy";
    public static final String OPEN_API_VALIDATION_POLICY_ENV = transformPropertyToEnv(
        OPEN_API_VALIDATION_POLICY_PROPERTY);


    private OpenApiSettings() {
        // static access only
    }

    public static boolean isGenerateOptionalFieldsEnabled() {
        return isGenerateOptionalFieldsEnabled(SYSTEM_PROVIDER);
    }

    @VisibleForTesting
    static boolean isGenerateOptionalFieldsEnabled(SystemProvider systemProvider) {
        return booleanPropertyOrDefault(systemProvider, GENERATE_OPTIONAL_FIELDS_ENABLED_PROPERTY,
            GENERATE_OPTIONAL_FIELDS_ENABLED_ENV, true);
    }

    public static boolean isRequestValidationEnabled() {
        return isRequestValidationEnabled(SYSTEM_PROVIDER);
    }

    @VisibleForTesting
    static boolean isRequestValidationEnabled(SystemProvider systemProvider) {
        return booleanPropertyOrDefault(systemProvider, REQUEST_VALIDATION_ENABLED_PROPERTY,
            REQUEST_VALIDATION_ENABLED_ENV, true);
    }

    public static boolean isResponseValidationEnabled() {
        return parseBoolean(System.getProperty(
            RESPONSE_VALIDATION_ENABLED_PROPERTY,
            System.getenv(RESPONSE_VALIDATION_ENABLED_ENV) != null ?
                System.getenv(RESPONSE_VALIDATION_ENABLED_ENV) : "true"));
    }

    @VisibleForTesting
    static boolean isResponseValidationEnabled(SystemProvider systemProvider) {
        return booleanPropertyOrDefault(systemProvider, RESPONSE_VALIDATION_ENABLED_PROPERTY,
            RESPONSE_VALIDATION_ENABLED_ENV, true);
    }

    public static boolean isNeglectBasePathEnabled() {
        return parseBoolean(System.getProperty(
            NEGLECT_OPEN_API_BASE_PATH_ENABLED_PROPERTY, System.getenv(
                NEGLECT_OPEN_API_BASE_PATH_ENABLED_ENV) != null ?
                System.getenv(NEGLECT_OPEN_API_BASE_PATH_ENABLED_ENV) : "false"));
    }

    @VisibleForTesting
    static boolean isNeglectBasePathEnabled(SystemProvider systemProvider) {
        return booleanPropertyOrDefault(systemProvider, NEGLECT_OPEN_API_BASE_PATH_ENABLED_PROPERTY,
            NEGLECT_OPEN_API_BASE_PATH_ENABLED_ENV, false);
    }

    /**
     * The default AutoFillType for request is set to REQUIRED to support backwards compatibility.
     */
    public static AutoFillType getRequestAutoFillRandomValues() {
        return getRequestAutoFillRandomValues(SYSTEM_PROVIDER);
    }

    @VisibleForTesting
    static AutoFillType getRequestAutoFillRandomValues(SystemProvider systemProvider) {
        return enumPropertyOrDefault(systemProvider, AutoFillType.class,
            REQUEST_AUTO_FILL_RANDOM_VALUES_PROPERTY,
            REQUEST_AUTO_FILL_RANDOM_VALUES_ENV, REQUIRED);
    }

    /**
     * The default AutoFillType for response is set to REQUIRED to support backwards compatibility.
     */
    public static AutoFillType getResponseAutoFillRandomValues() {
        return getResponseAutoFillRandomValues(SYSTEM_PROVIDER);
    }

    @VisibleForTesting
    static AutoFillType getResponseAutoFillRandomValues(SystemProvider systemProvider) {
        return enumPropertyOrDefault(systemProvider, AutoFillType.class,
            RESPONSE_AUTO_FILL_RANDOM_VALUES_PROPERTY, RESPONSE_AUTO_FILL_RANDOM_VALUES_ENV,
            REQUIRED);
    }

    /**
     * The default OpenApiValidationPolicy for OpenAPI parsed for validation purposes.
     */
    public static OpenApiValidationPolicy getOpenApiValidationPolicy() {
        return getOpenApiValidationPolicy(SYSTEM_PROVIDER);
    }

    @VisibleForTesting
    static OpenApiValidationPolicy getOpenApiValidationPolicy(
        SystemProvider systemProvider) {
        return enumPropertyOrDefault(systemProvider, OpenApiValidationPolicy.class,
            OPEN_API_VALIDATION_POLICY_PROPERTY, OPEN_API_VALIDATION_POLICY_ENV, REPORT);
    }

}
