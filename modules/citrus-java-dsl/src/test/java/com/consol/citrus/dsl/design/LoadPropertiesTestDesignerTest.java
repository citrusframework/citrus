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

import com.consol.citrus.actions.LoadPropertiesAction;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class LoadPropertiesTestDesignerTest extends AbstractTestNGUnitTest {
    @Test
    public void testLoadBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext) {
            @Override
            public void configure() {
                load("classpath:test.properties");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), LoadPropertiesAction.class);
        
        LoadPropertiesAction action = (LoadPropertiesAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "load");
        Assert.assertEquals(action.getFilePath(), "classpath:test.properties");
    }
}
