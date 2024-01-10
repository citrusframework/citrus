/*
 * Copyright 2024 the original author or authors.
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

package org.citrusframework.kubernetes.config.xml;

import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * @author Valentin Soldo
 */
public class KubernetesBadClientParserTest extends AbstractBeanDefinitionParserTest {

    @Override
    @BeforeClass(alwaysRun = true, dependsOnMethods = "springTestContextPrepareTestInstance")
    protected void parseBeanDefinitions() {
        // This method must be overridden, else the test would fail when loading, before it actually starts
    }

    @Test
    public void testBadConfiguredKubernetesClient() {
        BeanDefinitionStoreException exception = expectThrows(BeanDefinitionStoreException.class, () -> createApplicationContext("context"));

        Throwable illegalArg = exception.getRootCause();
        assertTrue(illegalArg instanceof IllegalArgumentException);
        assertEquals(illegalArg.getMessage(), "Parameters not set correctly - check if either an oauthToke or password and username is set");
    }
}


