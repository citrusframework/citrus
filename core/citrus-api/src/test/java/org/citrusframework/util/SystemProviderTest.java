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

package org.citrusframework.util;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.citrusframework.util.SystemProvider.SYSTEM_PROVIDER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemProviderTest {

    @Test
    public void getEnvWithExistingVariable() {
        // Assuming 'PATH' is an existing environment variable
        String envVarName = "PATH";
        Optional<String> result = SYSTEM_PROVIDER.getEnv(envVarName);

        assertTrue(result.isPresent(), "Expected environment variable PATH to be present");
    }

    @Test
    public void getEnvWithNonExistingVariable() {
        String envVarName = "NON_EXISTING_ENV_VAR";
        Optional<String> result = SYSTEM_PROVIDER.getEnv(envVarName);

        assertFalse(result.isPresent(), "Expected environment variable NON_EXISTING_ENV_VAR to not be present");
    }

    @Test
    public void getPropertyWithExistingProperty() {
        // Assuming 'java.version' is an existing system property
        String propertyName = "java.version";
        Optional<String> result = SYSTEM_PROVIDER.getProperty(propertyName);

        assertTrue(result.isPresent(), "Expected system property java.version to be present");
    }

    @Test
    public void getPropertyWithNonExistingProperty() {
        String propertyName = "NON_EXISTING_SYSTEM_PROPERTY";
        Optional<String> result = SYSTEM_PROVIDER.getProperty(propertyName);

        assertFalse(result.isPresent(), "Expected system property NON_EXISTING_SYSTEM_PROPERTY to not be present");
    }
}
