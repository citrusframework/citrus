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

package org.citrusframework.cucumber.backend;

import io.cucumber.core.exception.CucumberException;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @since 2.6
 */
public class CitrusObjectFactoryTest {

    @Test
    public void testRunnerInject() {
        CitrusObjectFactory factory = new CitrusObjectFactory();
        factory.addClass(TestRunnerSteps.class);

        // Scenario 1
        factory.start();
        final TestRunnerSteps steps = factory.getInstance(TestRunnerSteps.class);
        Assert.assertNotNull(steps.getTestRunner());
        Assert.assertNotNull(steps.getTestContext());
        factory.stop();
    }

    @Test
    public void testPackagePrivateStepsInstantiation() {
        CitrusObjectFactory factory = new CitrusObjectFactory();
        factory.addClass(PackagePrivateSteps.class);

        factory.start();
        final PackagePrivateSteps steps = factory.getInstance(PackagePrivateSteps.class);
        Assert.assertNotNull(steps);
        Assert.assertNotNull(steps.runner);
        factory.stop();
    }

    @Test
    public void testPrivateConstructorFails() {
        CitrusObjectFactory factory = new CitrusObjectFactory();
        factory.addClass(PrivateConstructorSteps.class);

        factory.start();
        try {
            CucumberException e = Assert.expectThrows(CucumberException.class,
                    () -> factory.getInstance(PrivateConstructorSteps.class));
            Assert.assertTrue(e.getMessage().contains(PrivateConstructorSteps.class.getName()), e.getMessage());
        } finally {
            factory.stop();
        }
    }

    static class PackagePrivateSteps {

        @CitrusResource
        TestCaseRunner runner;

        PackagePrivateSteps() {
        }
    }

    static class PrivateConstructorSteps {

        private PrivateConstructorSteps() {
        }
    }
}
