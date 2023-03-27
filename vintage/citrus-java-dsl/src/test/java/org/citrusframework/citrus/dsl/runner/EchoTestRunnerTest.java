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

package org.citrusframework.citrus.dsl.runner;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.EchoAction;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class EchoTestRunnerTest extends UnitTestSupport {

    @Test
    public void testEchoBuilder() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                echo("Hello Citrus!");
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), EchoAction.class);

        EchoAction action = (EchoAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "echo");
        Assert.assertEquals(action.getMessage(), "Hello Citrus!");
    }
}
