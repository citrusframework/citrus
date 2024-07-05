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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

import static org.citrusframework.openapi.OpenApiSettings.GENERATE_OPTIONAL_FIELDS_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.REQUEST_VALIDATION_ENABLED_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.RESPONSE_VALIDATION_ENABLED_PROPERTY;
import static org.citrusframework.openapi.OpenApiSettings.VALIDATE_OPTIONAL_FIELDS_ENV;
import static org.citrusframework.openapi.OpenApiSettings.VALIDATE_OPTIONAL_FIELDS_PROPERTY;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class OpenApiSettingsTest {

    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private static final boolean REQUEST_VALIDATION_ENABLED_GLOBALLY = OpenApiSettings.isRequestValidationEnabledGlobally();

    private static final boolean RESPONSE_VALIDATION_ENABLED_GLOBALLY = OpenApiSettings.isResponseValidationEnabledGlobally();

    private static final boolean VALIDATE_OPTIONAL_FIELDS_ENABLED_GLOBALLY = OpenApiSettings.isValidateOptionalFieldsGlobally();

    private static final boolean GENERATE_OPTIONAL_FIELDS_ENABLED_GLOBALLY = OpenApiSettings.isGenerateOptionalFieldsGlobally();

    @BeforeMethod
    public void beforeMethod() {
        System.clearProperty(GENERATE_OPTIONAL_FIELDS_PROPERTY);
        System.clearProperty(VALIDATE_OPTIONAL_FIELDS_PROPERTY);
        System.clearProperty(REQUEST_VALIDATION_ENABLED_PROPERTY);
        System.clearProperty(RESPONSE_VALIDATION_ENABLED_PROPERTY);
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        environmentVariables.teardown();

        if (!GENERATE_OPTIONAL_FIELDS_ENABLED_GLOBALLY) {
            System.clearProperty(GENERATE_OPTIONAL_FIELDS_PROPERTY);
        } else {
            System.setProperty(GENERATE_OPTIONAL_FIELDS_PROPERTY, "true");
        }

        if (!VALIDATE_OPTIONAL_FIELDS_ENABLED_GLOBALLY) {
            System.clearProperty(VALIDATE_OPTIONAL_FIELDS_PROPERTY);
        } else {
            System.setProperty(VALIDATE_OPTIONAL_FIELDS_PROPERTY, "true");
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
    public void testRequestValidationEnabledByDefault()  {
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
    public void testResponseValidationEnabledByDefault()  {
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
    public void testGenerateOptionalFieldsEnabledByDefault()  {
        assertTrue(OpenApiSettings.isGenerateOptionalFieldsGlobally());
    }

    @Test
    public void testValidateOptionalFieldsEnabledByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(VALIDATE_OPTIONAL_FIELDS_PROPERTY, "true");
        assertTrue(OpenApiSettings.isValidateOptionalFieldsGlobally());
    }

    @Test
    public void testValidateOptionalFieldsDisabledByProperty() throws Exception {
        environmentVariables.setup();
        System.setProperty(VALIDATE_OPTIONAL_FIELDS_PROPERTY, "false");
        assertFalse(OpenApiSettings.isValidateOptionalFieldsGlobally());
    }

    @Test
    public void testValidateOptionalFieldsEnabledByEnvVar() throws Exception {
        environmentVariables.set(VALIDATE_OPTIONAL_FIELDS_ENV, "true");
        environmentVariables.setup();
        assertTrue(OpenApiSettings.isValidateOptionalFieldsGlobally());
    }

    @Test
    public void testValidateOptionalFieldsDisabledByEnvVar() throws Exception {
        environmentVariables.set(VALIDATE_OPTIONAL_FIELDS_ENV, "false");
        environmentVariables.setup();
        assertFalse(OpenApiSettings.isValidateOptionalFieldsGlobally());
    }

    @Test
    public void testValidateOptionalFieldsEnabledByDefault()  {
        assertTrue(OpenApiSettings.isValidateOptionalFieldsGlobally());
    }
}
