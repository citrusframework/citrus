/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.util;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class SystemProviderTest {

    private SystemProvider systemProvider;

    @BeforeTest
    public void beforeTestSetup() {
        systemProvider = new SystemProvider();
    }

    @Test
    public void getEnvWithExistingVariable() {
        // Assuming 'PATH' is an existing environment variable
        String envVarName = "PATH";
        Optional<String> result = systemProvider.getEnv(envVarName);

        assertTrue("Expected environment variable PATH to be present", result.isPresent());
    }

    @Test
    public void getEnvWithNonExistingVariable() {
        String envVarName = "NON_EXISTING_ENV_VAR";
        Optional<String> result = systemProvider.getEnv(envVarName);

        assertFalse("Expected environment variable NON_EXISTING_ENV_VAR to not be present", result.isPresent());
    }

    @Test
    public void getPropertyWithExistingProperty() {
        // Assuming 'java.version' is an existing system property
        String propertyName = "java.version";
        Optional<String> result = systemProvider.getProperty(propertyName);

        assertTrue("Expected system property java.version to be present", result.isPresent());
    }

    @Test
    public void getPropertyWithNonExistingProperty() {
        String propertyName = "NON_EXISTING_SYSTEM_PROPERTY";
        Optional<String> result = systemProvider.getProperty(propertyName);

        assertFalse("Expected system property NON_EXISTING_SYSTEM_PROPERTY to not be present", result.isPresent());
    }
}
