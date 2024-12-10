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

import static org.citrusframework.openapi.OpenApiSettings.GENERATE_OPTIONAL_FIELDS_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.NEGLECT_OPEN_API_BASE_PATH_ENV;
import static org.citrusframework.openapi.OpenApiSettings.NEGLECT_OPEN_API_BASE_PATH_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.REQUEST_AUTO_FILL_RANDOM_VALUES;
import static org.citrusframework.openapi.OpenApiSettings.REQUEST_AUTO_FILL_RANDOM_VALUES_ENV;
import static org.citrusframework.openapi.OpenApiSettings.REQUEST_VALIDATION_ENABLED_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.RESPONSE_AUTO_FILL_RANDOM_VALUES;
import static org.citrusframework.openapi.OpenApiSettings.RESPONSE_AUTO_FILL_RANDOM_VALUES_ENV;
import static org.citrusframework.openapi.OpenApiSettings.RESPONSE_VALIDATION_ENABLED_PROPERTY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

public class OpenApiSettingsTest {

    private static final boolean REQUEST_VALIDATION_ENABLED_GLOBALLY = OpenApiSettings.isRequestValidationEnabledGlobally();
    private static final boolean RESPONSE_VALIDATION_ENABLED_GLOBALLY = OpenApiSettings.isResponseValidationEnabledGlobally();
    private static final boolean GENERATE_OPTIONAL_FIELDS_ENABLED_GLOBALLY = OpenApiSettings.isGenerateOptionalFieldsGlobally();
    private static final AutoFillType REQUEST_AUTO_FILL_RANDOM_VALUES_ENABLED_GLOBALLY = OpenApiSettings.getRequestAutoFillRandomValues();
    private static final AutoFillType RESPONSE_AUTO_FILL_RANDOM_VALUES_ENABLED_GLOBALLY = OpenApiSettings.getResponseAutoFillRandomValues();
    private static final boolean NEGLECT_BASE_PATH_GLOBALLY = OpenApiSettings.isNeglectBasePathGlobally();

    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeMethod
    public void beforeMethod() {
        System.clearProperty(GENERATE_OPTIONAL_FIELDS_PROPERTY);
        System.clearProperty(REQUEST_VALIDATION_ENABLED_PROPERTY);
        System.clearProperty(RESPONSE_VALIDATION_ENABLED_PROPERTY);
        System.clearProperty(NEGLECT_OPEN_API_BASE_PATH_PROPERTY);
        System.clearProperty(REQUEST_AUTO_FILL_RANDOM_VALUES);
        System.clearProperty(RESPONSE_AUTO_FILL_RANDOM_VALUES);
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        environmentVariables.teardown();

        if (!GENERATE_OPTIONAL_FIELDS_ENABLED_GLOBALLY) {
            System.clearProperty(GENERATE_OPTIONAL_FIELDS_PROPERTY);
        } else {
            System.setProperty(GENERATE_OPTIONAL_FIELDS_PROPERTY, "true");
        }

        if (!REQUEST_VALIDATION_ENABLED_GLOBALLY) {
            System.clearProperty(REQUEST_VALIDATION_ENABLED_PROPERTY);
        } else {
            System.setProperty(REQUEST_VALIDATION_ENABLED_PROPERTY, "true");
        }

        if (!RESPONSE_VALIDATION_ENABLED_GLOBALLY) {
            System.clearProperty(RESPONSE_VALIDATION_ENABLED_PROPERTY);
        } else {
            System.setProperty(RESPONSE_VALIDATION_ENABLED_PROPERTY, "true");
        }

        if (!NEGLECT_BASE_PATH_GLOBALLY) {
            System.clearProperty(NEGLECT_OPEN_API_BASE_PATH_PROPERTY);
        } else {
            System.setProperty(NEGLECT_OPEN_API_BASE_PATH_PROPERTY, "true");
        }

        System.setProperty(RESPONSE_AUTO_FILL_RANDOM_VALUES,
            RESPONSE_AUTO_FILL_RANDOM_VALUES_ENABLED_GLOBALLY.toString());
        System.setProperty(REQUEST_AUTO_FILL_RANDOM_VALUES,
            REQUEST_AUTO_FILL_RANDOM_VALUES_ENABLED_GLOBALLY.toString());
    }

    @Test
    public void testRequestValidationEnabledByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(REQUEST_VALIDATION_ENABLED_PROPERTY, "true");
        assertTrue(OpenApiSettings.isRequestValidationEnabledGlobally());
    }

    @Test
    public void testRequestValidationDisabledByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(REQUEST_VALIDATION_ENABLED_PROPERTY, "false");
        assertFalse(OpenApiSettings.isRequestValidationEnabledGlobally());
    }

    @Test
    public void testRequestValidationEnabledByEnvVar() throws Exception {
        environmentVariables.set(OpenApiSettings.REQUEST_VALIDATION_ENABLED_ENV, "true");
        environmentVariables.setup();
        assertTrue(OpenApiSettings.isRequestValidationEnabledGlobally());
    }

    @Test
    public void testRequestValidationDisabledByEnvVar() throws Exception {
        environmentVariables.set(OpenApiSettings.REQUEST_VALIDATION_ENABLED_ENV, "false");
        environmentVariables.setup();
        assertFalse(OpenApiSettings.isRequestValidationEnabledGlobally());
    }

    @Test
    public void testRequestValidationEnabledByDefault() {
        assertTrue(OpenApiSettings.isRequestValidationEnabledGlobally());
    }

    @Test
    public void testResponseValidationEnabledByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(RESPONSE_VALIDATION_ENABLED_PROPERTY, "true");
        assertTrue(OpenApiSettings.isResponseValidationEnabledGlobally());
    }

    @Test
    public void testResponseValidationDisabledByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(RESPONSE_VALIDATION_ENABLED_PROPERTY, "false");
        assertFalse(OpenApiSettings.isResponseValidationEnabledGlobally());
    }

    @Test
    public void testResponseValidationEnabledByEnvVar() throws Exception {
        environmentVariables.set(OpenApiSettings.RESPONSE_VALIDATION_ENABLED_ENV, "true");
        environmentVariables.setup();
        assertTrue(OpenApiSettings.isResponseValidationEnabledGlobally());
    }

    @Test
    public void testResponseValidationDisabledByEnvVar() throws Exception {
        environmentVariables.set(OpenApiSettings.RESPONSE_VALIDATION_ENABLED_ENV, "false");
        environmentVariables.setup();
        assertFalse(OpenApiSettings.isResponseValidationEnabledGlobally());
    }

    @Test
    public void testResponseValidationEnabledByDefault() {
        assertTrue(OpenApiSettings.isResponseValidationEnabledGlobally());
    }

    @Test
    public void testGenerateOptionalFieldsEnabledByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(GENERATE_OPTIONAL_FIELDS_PROPERTY, "true");
        assertTrue(OpenApiSettings.isGenerateOptionalFieldsGlobally());
    }

    @Test
    public void testGenerateOptionalFieldsDisabledByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(GENERATE_OPTIONAL_FIELDS_PROPERTY, "false");
        assertFalse(OpenApiSettings.isGenerateOptionalFieldsGlobally());
    }

    @Test
    public void testGenerateOptionalFieldsEnabledByEnvVar() throws Exception {
        environmentVariables.set(OpenApiSettings.GENERATE_OPTIONAL_FIELDS_ENV, "true");
        environmentVariables.setup();
        assertTrue(OpenApiSettings.isGenerateOptionalFieldsGlobally());
    }

    @Test
    public void testGenerateOptionalFieldsDisabledByEnvVar() throws Exception {
        environmentVariables.set(OpenApiSettings.GENERATE_OPTIONAL_FIELDS_ENV, "false");
        environmentVariables.setup();
        assertFalse(OpenApiSettings.isGenerateOptionalFieldsGlobally());
    }

    @Test
    public void testGenerateOptionalFieldsEnabledByDefault() {
        assertTrue(OpenApiSettings.isGenerateOptionalFieldsGlobally());
    }

    @Test
    public void testNeglectBasePathEnabledByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(NEGLECT_OPEN_API_BASE_PATH_PROPERTY, "true");
        assertTrue(OpenApiSettings.isNeglectBasePathGlobally());
    }

    @Test
    public void testNeglectBasePathDisabledByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(NEGLECT_OPEN_API_BASE_PATH_PROPERTY, "false");
        assertFalse(OpenApiSettings.isNeglectBasePathGlobally());
    }

    @Test
    public void testNeglectBasePathDisabledByEnvVar() throws Exception {
        environmentVariables.set(NEGLECT_OPEN_API_BASE_PATH_ENV, "false");
        environmentVariables.setup();
        assertFalse(OpenApiSettings.isNeglectBasePathGlobally());
    }

    @Test
    public void testNeglectBasePathEnabledByEnvVar() throws Exception {
        environmentVariables.set(NEGLECT_OPEN_API_BASE_PATH_ENV, "true");
        environmentVariables.setup();
        assertTrue(OpenApiSettings.isNeglectBasePathGlobally());
    }

    @Test
    public void testNeglectBasePathDisabledByDefault() {
        assertFalse(OpenApiSettings.isNeglectBasePathGlobally());
    }

    @Test
    public void testRequestAutoFillRandomValuesAllByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(REQUEST_AUTO_FILL_RANDOM_VALUES, "ALL");
        assertEquals(OpenApiSettings.getRequestAutoFillRandomValues(), AutoFillType.ALL);
    }

    @Test
    public void testRequestAutoFillRandomValuesNoneByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(REQUEST_AUTO_FILL_RANDOM_VALUES, "NONE");
        assertEquals(OpenApiSettings.getRequestAutoFillRandomValues(), AutoFillType.NONE);
    }

    @Test
    public void testRequestAutoFillRandomValuesAllByEnvVar() throws Exception {
        environmentVariables.set(REQUEST_AUTO_FILL_RANDOM_VALUES_ENV, "ALL");
        environmentVariables.setup();
        assertEquals(OpenApiSettings.getRequestAutoFillRandomValues(), AutoFillType.ALL);
    }

    @Test
    public void testRequestAutoFillRandomValuesNoneByEnvVar() throws Exception {
        environmentVariables.set(REQUEST_AUTO_FILL_RANDOM_VALUES_ENV, "NONE");
        environmentVariables.setup();
        assertEquals(OpenApiSettings.getRequestAutoFillRandomValues(), AutoFillType.NONE);
    }

    @Test
    public void testResponseAutoFillRandomValuesAllByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(RESPONSE_AUTO_FILL_RANDOM_VALUES, "ALL");
        assertEquals(OpenApiSettings.getResponseAutoFillRandomValues(), AutoFillType.ALL);
    }

    @Test
    public void testResponseAutoFillRandomValuesNoneByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(RESPONSE_AUTO_FILL_RANDOM_VALUES, "NONE");
        assertEquals(OpenApiSettings.getResponseAutoFillRandomValues(), AutoFillType.NONE);
    }

    @Test
    public void testResponseAutoFillRandomValuesAllByEnvVar() throws Exception {
        environmentVariables.set(RESPONSE_AUTO_FILL_RANDOM_VALUES_ENV, "ALL");
        environmentVariables.setup();
        assertEquals(OpenApiSettings.getResponseAutoFillRandomValues(), AutoFillType.ALL);
    }

    @Test
    public void testResponseAutoFillRandomValuesNoneByEnvVar() throws Exception {
        environmentVariables.set(RESPONSE_AUTO_FILL_RANDOM_VALUES_ENV, "NONE");
        environmentVariables.setup();
        assertEquals(OpenApiSettings.getResponseAutoFillRandomValues(), AutoFillType.NONE);
    }

}
