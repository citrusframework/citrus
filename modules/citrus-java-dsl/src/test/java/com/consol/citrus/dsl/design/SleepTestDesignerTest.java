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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.SleepAction;

/**
 * @author Christoph Deppisch
 */
public class SleepTestDesignerTest extends AbstractTestNGUnitTest {
    
    @Test
    public void testSleepBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext) {
            @Override
            public void configure() {
                sleep(0.5);
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
        Assert.assertEquals(action.getSeconds(), "0.5");
        Assert.assertEquals(action.getMilliseconds(), "5000");

        action = (SleepAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "sleep");
        Assert.assertNull(action.getSeconds());
        Assert.assertEquals(action.getMilliseconds(), "500");
    }
}
