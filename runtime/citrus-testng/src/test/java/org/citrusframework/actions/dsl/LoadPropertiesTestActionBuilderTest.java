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

package org.citrusframework.actions.dsl;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.LoadPropertiesAction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.LoadPropertiesAction.Builder.load;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class LoadPropertiesTestActionBuilderTest extends UnitTestSupport {
    @Test
    public void testLoadBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("checked", true);
        builder.$(load("classpath:org/citrusframework/actions/dsl/build.properties"));

        Assert.assertNotNull(context.getVariable("welcomeText"));
        Assert.assertEquals(context.getVariable("welcomeText"), "Welcome with property file!");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), LoadPropertiesAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), LoadPropertiesAction.class);

        LoadPropertiesAction action = (LoadPropertiesAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "load");
        Assert.assertEquals(action.getFilePath(), "classpath:org/citrusframework/actions/dsl/build.properties");
    }
}
