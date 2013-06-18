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

import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.LoadPropertiesAction;

public class LoadPropertiesDefinitionTest extends AbstractTestNGUnitTest {
    @Test
    public void TestLoadBuilder() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                load("classpath:test.properties");
            }
        };
            
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), LoadPropertiesAction.class);
        
        LoadPropertiesAction action = (LoadPropertiesAction)builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getName(), LoadPropertiesAction.class.getSimpleName());
        Assert.assertEquals(action.getFilePath(), "classpath:test.properties");
    }
}
