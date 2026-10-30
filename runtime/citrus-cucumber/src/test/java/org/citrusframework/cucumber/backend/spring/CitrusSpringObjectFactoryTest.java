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

package org.citrusframework.cucumber.backend.spring;

import org.citrusframework.CitrusInstanceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @since 2.6
 */
public class CitrusSpringObjectFactoryTest {

    @BeforeEach
    public void resetCitrus() {
        // Drop the Citrus instance without closing it: other tests with Spring test support share the
        // cached Spring application context, and closing it here would poison the Spring test context
        // cache (the next context load fails with "LifecycleProcessor not initialized"). Detaching is
        // enough, otherwise the Spring object factory would shut down the shared instance and context.
        ReflectionTestUtils.setField(CitrusInstanceManager.class, "citrus", null);
    }

    @Test
    public void testRunnerInject() {
        CitrusSpringObjectFactory factory = new CitrusSpringObjectFactory();
        factory.addClass(SpringRunnerSteps.class);

        // Scenario 1
        factory.start();
        final SpringRunnerSteps steps = factory.getInstance(SpringRunnerSteps.class);
        Assertions.assertNotNull(steps.getTestRunner());
        Assertions.assertNotNull(steps.getTestContext());
        factory.stop();
    }
}
