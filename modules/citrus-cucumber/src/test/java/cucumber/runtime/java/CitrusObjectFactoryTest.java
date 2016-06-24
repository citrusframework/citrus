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

package cucumber.runtime.java;

import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusObjectFactoryTest extends AbstractTestNGUnitTest {

    @BeforeClass
    public void initializeCitrus() {
        CitrusBackend.initializeCitrus(applicationContext);
    }

    @AfterClass(alwaysRun = true)
    public void resetCitrus() {
        CitrusBackend.resetCitrus();
    }

    @Test
    public void testDesignerInject() throws Exception {
        CitrusObjectFactory factory = new CitrusObjectFactory();
        factory.addClass(TestDesignerSteps.class);

        // Scenario 1
        factory.start();
        final TestDesignerSteps steps = factory.getInstance(TestDesignerSteps.class);
        Assert.assertNotNull(steps.getTestDesigner());
        factory.stop();
    }

    @Test
    public void testRunnerInject() throws Exception {
        CitrusObjectFactory factory = new CitrusObjectFactory();
        factory.addClass(TestRunnerSteps.class);

        // Scenario 1
        factory.start();
        final TestRunnerSteps steps = factory.getInstance(TestRunnerSteps.class);
        Assert.assertNotNull(steps.getTestRunner());
        factory.stop();
    }
}