/*
 * Copyright 2006-2016 the original author or authors.
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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusSpringObjectFactoryTest {

    @BeforeMethod
    public void resetCitrus() {
        // Need to reset Citrus here because other tests with Spring test support might have initialized a
        // Citrus instance before and this would be closed by the SpringObjectFactory leading to errors in those tests
        CitrusInstanceManager.reset();
    }

    @Test
    public void testRunnerInject() {
        CitrusSpringObjectFactory factory = new CitrusSpringObjectFactory();
        factory.addClass(SpringRunnerSteps.class);

        // Scenario 1
        factory.start();
        final SpringRunnerSteps steps = factory.getInstance(SpringRunnerSteps.class);
        Assert.assertNotNull(steps.getTestRunner());
        Assert.assertNotNull(steps.getTestContext());
        factory.stop();
    }
}
