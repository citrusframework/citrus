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

package com.consol.citrus.dsl.definition;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.SleepAction;

/**
 * @author Christoph Deppisch
 */
public class SleepDefinitionTest {
    
    @Test
    public void testSleepBuilder() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                sleep(0.5);
                sleep(500);
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 2);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SleepAction.class);
        Assert.assertEquals(builder.testCase().getActions().get(1).getClass(), SleepAction.class);
        
        SleepAction action = (SleepAction)builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getName(), SleepAction.class.getSimpleName());       
        Assert.assertEquals(action.getDelay(), "0.5");
        Assert.assertEquals(action.getDelay(), "0.5");
    }
}
