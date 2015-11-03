/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.WaitAction;
import com.consol.citrus.condition.HttpCondition;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class WaitTestDesignerTest extends AbstractTestNGUnitTest {
    @Test
    public void testWaitBuilder() {
        final String seconds = "3";
        final String interval = "1500";
        final String url = "http://some.path/";

        MockTestDesigner builder = new MockTestDesigner(applicationContext) {
            @Override
            public void configure() {
                waitFor().http(url).seconds(seconds).interval(interval);
            }
        };
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), WaitAction.class);

        WaitAction action = (WaitAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getSeconds(), seconds);
        Assert.assertEquals(action.getInterval(), interval);
        Assert.assertEquals(action.getCondition().getClass(), HttpCondition.class);
        HttpCondition condition = (HttpCondition) action.getCondition();
        Assert.assertEquals(condition.getUrl(), url);
        Assert.assertEquals(condition.getHttpResponseCode(), "200");
        Assert.assertEquals(condition.getTimeout(), "1000");
    }

}
