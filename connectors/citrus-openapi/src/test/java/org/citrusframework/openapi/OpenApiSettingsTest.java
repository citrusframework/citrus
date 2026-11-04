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

import java.util.Optional;

import org.citrusframework.util.SystemProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.citrusframework.api.openapi.AutoFillType.ALL;
import static org.citrusframework.api.openapi.AutoFillType.NONE;
import static org.citrusframework.api.openapi.AutoFillType.REQUIRED;
import static org.citrusframework.openapi.OpenApiSettings.GENERATE_OPTIONAL_FIELDS_ENABLED_ENV;
import static org.citrusframework.openapi.OpenApiSettings.GENERATE_OPTIONAL_FIELDS_ENABLED_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.NEGLECT_OPEN_API_BASE_PATH_ENABLED_ENV;
import static org.citrusframework.openapi.OpenApiSettings.NEGLECT_OPEN_API_BASE_PATH_ENABLED_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.OPEN_API_VALIDATION_POLICY_ENV;
import static org.citrusframework.openapi.OpenApiSettings.OPEN_API_VALIDATION_POLICY_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.REQUEST_AUTO_FILL_RANDOM_VALUES_ENV;
import static org.citrusframework.openapi.OpenApiSettings.REQUEST_AUTO_FILL_RANDOM_VALUES_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.REQUEST_VALIDATION_ENABLED_ENV;
import static org.citrusframework.openapi.OpenApiSettings.REQUEST_VALIDATION_ENABLED_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.RESPONSE_AUTO_FILL_RANDOM_VALUES_ENV;
import static org.citrusframework.openapi.OpenApiSettings.RESPONSE_AUTO_FILL_RANDOM_VALUES_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.RESPONSE_VALIDATION_ENABLED_ENV;
import static org.citrusframework.openapi.OpenApiSettings.RESPONSE_VALIDATION_ENABLED_PROPERTY;
import static org.citrusframework.openapi.validation.OpenApiValidationPolicy.IGNORE;
import static org.citrusframework.openapi.validation.OpenApiValidationPolicy.REPORT;
import static org.citrusframework.openapi.validation.OpenApiValidationPolicy.STRICT;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenApiSettingsTest {

    private SystemProvider systemProvider;

    @BeforeEach
    public void beforeMethod() {
        systemProvider = mock();
    }

    @Test
    public void testRequestValidationEnabledByProperty()  {
        doReturn(Optional.of("true")).when(systemProvider).getProperty(REQUEST_VALIDATION_ENABLED_PROPERTY);
        assertTrue(OpenApiSettings.isRequestValidationEnabled(systemProvider));
    }

    @Test
    public void testRequestValidationDisabledByProperty()  {
        doReturn(Optional.of("false")).when(systemProvider).getProperty(REQUEST_VALIDATION_ENABLED_PROPERTY);
        assertFalse(OpenApiSettings.isRequestValidationEnabled(systemProvider));
    }

    @Test
    public void testRequestValidationEnabledByEnvVar()  {
        systemProvider = mock();
        doReturn(Optional.of("true")).when(systemProvider).getEnv(REQUEST_VALIDATION_ENABLED_ENV);
        assertTrue(OpenApiSettings.isRequestValidationEnabled(systemProvider));
    }

    @Test
    public void testRequestValidationDisabledByEnvVar()  {
        doReturn(Optional.of("false")).when(systemProvider).getEnv(REQUEST_VALIDATION_ENABLED_ENV);
        assertFalse(OpenApiSettings.isRequestValidationEnabled(systemProvider));
    }

    @Test
    public void testRequestValidationEnabledByDefault() {
        assertTrue(OpenApiSettings.isRequestValidationEnabled());
    }

    @Test
    public void testResponseValidationEnabledByProperty()  {
        doReturn(Optional.of("true")).when(systemProvider).getProperty(RESPONSE_VALIDATION_ENABLED_PROPERTY);
        assertTrue(OpenApiSettings.isResponseValidationEnabled(systemProvider));
    }

    @Test
    public void testResponseValidationDisabledByProperty()  {
        doReturn(Optional.of("false")).when(systemProvider).getProperty(RESPONSE_VALIDATION_ENABLED_PROPERTY);
        assertFalse(OpenApiSettings.isResponseValidationEnabled(systemProvider));
    }

    @Test
    public void testResponseValidationEnabledByEnvVar()  {
        systemProvider = mock();
        doReturn(Optional.of("true")).when(systemProvider).getEnv(RESPONSE_VALIDATION_ENABLED_ENV);
        assertTrue(OpenApiSettings.isResponseValidationEnabled(systemProvider));
    }

    @Test
    public void testResponseValidationDisabledByEnvVar()  {
        doReturn(Optional.of("false")).when(systemProvider).getEnv(RESPONSE_VALIDATION_ENABLED_ENV);
        assertFalse(OpenApiSettings.isResponseValidationEnabled(systemProvider));
    }

    @Test
    public void testResponseValidationEnabledByDefault() {
        assertTrue(OpenApiSettings.isResponseValidationEnabled());
    }

    @Test
    public void testGenerateOptionalFieldsEnabledByProperty()  {
        doReturn(Optional.of("true")).when(systemProvider).getProperty(GENERATE_OPTIONAL_FIELDS_ENABLED_PROPERTY);
        assertTrue(OpenApiSettings.isGenerateOptionalFieldsEnabled(systemProvider));
    }

    @Test
    public void testGenerateOptionalFieldsDisabledByProperty()  {
        doReturn(Optional.of("false")).when(systemProvider).getProperty(GENERATE_OPTIONAL_FIELDS_ENABLED_PROPERTY);
        assertFalse(OpenApiSettings.isGenerateOptionalFieldsEnabled(systemProvider));
    }

    @Test
    public void testGenerateOptionalFieldsEnabledByEnvVar()  {
        systemProvider = mock();
        doReturn(Optional.of("true")).when(systemProvider).getEnv(GENERATE_OPTIONAL_FIELDS_ENABLED_ENV);
        assertTrue(OpenApiSettings.isGenerateOptionalFieldsEnabled(systemProvider));
    }

    @Test
    public void testGenerateOptionalFieldsDisabledByEnvVar()  {
        doReturn(Optional.of("false")).when(systemProvider).getEnv(GENERATE_OPTIONAL_FIELDS_ENABLED_ENV);
        assertFalse(OpenApiSettings.isGenerateOptionalFieldsEnabled(systemProvider));
    }

    @Test
    public void testGenerateOptionalFieldsEnabledByDefault() {
        assertTrue(OpenApiSettings.isGenerateOptionalFieldsEnabled());
    }

    @Test
    public void testNeglectBasePathEnabledByProperty()  {
        doReturn(Optional.of("true")).when(systemProvider).getProperty(NEGLECT_OPEN_API_BASE_PATH_ENABLED_PROPERTY);
        assertTrue(OpenApiSettings.isNeglectBasePathEnabled(systemProvider));
    }

    @Test
    public void testNeglectBasePathDisabledByProperty()  {
        doReturn(Optional.of("false")).when(systemProvider).getProperty(NEGLECT_OPEN_API_BASE_PATH_ENABLED_PROPERTY);
        assertFalse(OpenApiSettings.isNeglectBasePathEnabled(systemProvider));
    }

    @Test
    public void testNeglectBasePathEnabledByEnvVar()  {
        systemProvider = mock();
        doReturn(Optional.of("true")).when(systemProvider).getEnv(NEGLECT_OPEN_API_BASE_PATH_ENABLED_ENV);
        assertTrue(OpenApiSettings.isNeglectBasePathEnabled(systemProvider));
    }

    @Test
    public void testNeglectBasePathDisabledByEnvVar()  {
        doReturn(Optional.of("false")).when(systemProvider).getEnv(NEGLECT_OPEN_API_BASE_PATH_ENABLED_ENV);
        assertFalse(OpenApiSettings.isNeglectBasePathEnabled(systemProvider));
    }

    @Test
    public void testNeglectBasePathEnabledByDefault() {
        assertFalse(OpenApiSettings.isNeglectBasePathEnabled());
    }

    @Test
    public void testRequestAutoFillRandomValuesAllByProperty()  {
        doReturn(Optional.of("ALL")).when(systemProvider).getProperty(REQUEST_AUTO_FILL_RANDOM_VALUES_PROPERTY);
        assertEquals(ALL, OpenApiSettings.getRequestAutoFillRandomValues(systemProvider));
    }

    @Test
    public void testRequestAutoFillRandomValuesNoneByProperty()  {
        doReturn(Optional.of("NONE")).when(systemProvider).getProperty(REQUEST_AUTO_FILL_RANDOM_VALUES_PROPERTY);
        assertEquals(NONE, OpenApiSettings.getRequestAutoFillRandomValues(systemProvider));
    }

    @Test
    public void testRequestAutoFillRandomValuesAllEnvVar()  {
        doReturn(Optional.of("ALL")).when(systemProvider).getEnv(REQUEST_AUTO_FILL_RANDOM_VALUES_ENV);
        assertEquals(ALL, OpenApiSettings.getRequestAutoFillRandomValues(systemProvider));
    }

    @Test
    public void testRequestAutoFillRandomValuesNoneEnvVar()  {
        doReturn(Optional.of("NONE")).when(systemProvider).getEnv(REQUEST_AUTO_FILL_RANDOM_VALUES_ENV);
        assertEquals(NONE, OpenApiSettings.getRequestAutoFillRandomValues(systemProvider));
    }

    @Test
    public void testRequestAutoFillRandomValuesRequiredByDefault()  {
        assertEquals(REQUIRED, OpenApiSettings.getRequestAutoFillRandomValues(systemProvider));
    }

    @Test
    public void testResponseAutoFillRandomValuesAllByProperty()  {
        doReturn(Optional.of("ALL")).when(systemProvider).getProperty(RESPONSE_AUTO_FILL_RANDOM_VALUES_PROPERTY);
        assertEquals(ALL, OpenApiSettings.getResponseAutoFillRandomValues(systemProvider));
    }

    @Test
    public void testResponseAutoFillRandomValuesNoneByProperty()  {
        doReturn(Optional.of("NONE")).when(systemProvider).getProperty(RESPONSE_AUTO_FILL_RANDOM_VALUES_PROPERTY);
        assertEquals(NONE, OpenApiSettings.getResponseAutoFillRandomValues(systemProvider));
    }

    @Test
    public void testResponseAutoFillRandomValuesAllEnvVar()  {
        doReturn(Optional.of("ALL")).when(systemProvider).getEnv(RESPONSE_AUTO_FILL_RANDOM_VALUES_ENV);
        assertEquals(ALL, OpenApiSettings.getResponseAutoFillRandomValues(systemProvider));
    }

    @Test
    public void testResponseAutoFillRandomValuesNoneEnvVar()  {
        doReturn(Optional.of("NONE")).when(systemProvider).getEnv(RESPONSE_AUTO_FILL_RANDOM_VALUES_ENV);
        assertEquals(NONE, OpenApiSettings.getResponseAutoFillRandomValues(systemProvider));
    }

    @Test
    public void testResponseAutoFillRandomValuesRequiredByDefault()  {
        assertEquals(REQUIRED, OpenApiSettings.getResponseAutoFillRandomValues(systemProvider));
    }

    @Test
    public void testOpenApiValidationPolicyIgnoreByProperty()  {
        doReturn(Optional.of("IGNORE")).when(systemProvider).getProperty(OPEN_API_VALIDATION_POLICY_PROPERTY);
        assertEquals(IGNORE,
            OpenApiSettings.getOpenApiValidationPolicy(systemProvider));
    }

    @Test
    public void testOpenApiValidationPolicyStrictByProperty()  {
        doReturn(Optional.of("STRICT")).when(systemProvider).getProperty(OPEN_API_VALIDATION_POLICY_PROPERTY);
        assertEquals(STRICT, OpenApiSettings.getOpenApiValidationPolicy(systemProvider));
    }

    @Test
    public void testOpenApiValidationPolicyIgnoreByEnvVar()  {
        doReturn(Optional.of("IGNORE")).when(systemProvider).getEnv(OPEN_API_VALIDATION_POLICY_ENV);
        assertEquals(IGNORE, OpenApiSettings.getOpenApiValidationPolicy(systemProvider));
    }

    @Test
    public void testOpenApiValidationPolicyStrictByEnvVar()  {
        doReturn(Optional.of("STRICT")).when(systemProvider).getEnv(OPEN_API_VALIDATION_POLICY_ENV);
        assertEquals(STRICT, OpenApiSettings.getOpenApiValidationPolicy(systemProvider));
    }

    @Test
    public void testOpenApiValidationPolicyStrictByDefault()  {
        assertEquals(REPORT, OpenApiSettings.getOpenApiValidationPolicy(systemProvider));
    }

}
