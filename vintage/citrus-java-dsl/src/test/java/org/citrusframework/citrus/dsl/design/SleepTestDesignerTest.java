/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.citrus.dsl.design;

import java.util.concurrent.TimeUnit;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.citrus.actions.SleepAction;

/**
 * @author Christoph Deppisch
 */
public class SleepTestDesignerTest extends UnitTestSupport {

    @Test
    public void testSleepBuilder() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                sleep(0.25);
                sleep(500);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SleepAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SleepAction.class);

        SleepAction action = (SleepAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "sleep");
        Assert.assertEquals(action.getTime(), "250");
        Assert.assertEquals(action.getTimeUnit(), TimeUnit.MILLISECONDS);

        action = (SleepAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "sleep");
        Assert.assertEquals(action.getTime(), "500");
        Assert.assertEquals(action.getTimeUnit(), TimeUnit.MILLISECONDS);
    }
}
