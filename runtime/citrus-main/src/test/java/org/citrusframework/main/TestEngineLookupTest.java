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

package org.citrusframework.main;

import org.citrusframework.api.main.TestEngine;
import org.citrusframework.api.main.TestRunConfiguration;
import org.citrusframework.cucumber.CucumberTestEngine;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.junit.JUnit4TestEngine;
import org.citrusframework.junit.jupiter.JUnitJupiterEngine;
import org.citrusframework.testng.TestNGEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestEngineLookupTest {

    @Test
    public void shouldResolveJUnitEngine() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setEngine("junit");
        Assertions.assertEquals(JUnitJupiterEngine.class, TestEngine.lookup(configuration).getClass());
    }

    @Test
    public void shouldResolveJUnitJupiterEngine() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setEngine("junit-jupiter");
        Assertions.assertEquals(JUnitJupiterEngine.class, TestEngine.lookup(configuration).getClass());
    }

    @Test
    public void shouldResolveJUnit4Engine() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setEngine("junit4");
        Assertions.assertEquals(JUnit4TestEngine.class, TestEngine.lookup(configuration).getClass());
    }

    @Test
    public void shouldResolveTestNGEngine() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setEngine("testng");
        Assertions.assertEquals(TestNGEngine.class, TestEngine.lookup(configuration).getClass());
    }

    @Test
    public void shouldResolveCucumberEngine() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setEngine("cucumber");
        Assertions.assertEquals(CucumberTestEngine.class, TestEngine.lookup(configuration).getClass());
    }

    @Test
    public void shouldHandleUnknownEngine() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setEngine("unknown");
        CitrusRuntimeException e = Assertions.assertThrows(CitrusRuntimeException.class, () -> TestEngine.lookup(configuration));
        Assertions.assertTrue(e.getMessage().matches("Failed to resolve Citrus engine from resource.*"));
    }
}
