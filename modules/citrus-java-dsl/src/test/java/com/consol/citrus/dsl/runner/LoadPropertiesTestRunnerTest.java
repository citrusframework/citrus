/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.LoadPropertiesAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class LoadPropertiesTestRunnerTest extends AbstractTestNGUnitTest {
    @Test
    public void testLoadBuilder() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                variable("checked", true);
                load("classpath:com/consol/citrus/dsl/runner/build.properties");
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("welcomeText"));
        Assert.assertEquals(context.getVariable("welcomeText"), "Welcome with property file!");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), LoadPropertiesAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), LoadPropertiesAction.class);

        LoadPropertiesAction action = (LoadPropertiesAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "load");
        Assert.assertEquals(action.getFilePath(), "classpath:com/consol/citrus/dsl/runner/build.properties");
    }
}
