/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.config.xml;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.actions.LoadPropertiesAction;
import org.citrusframework.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class LoadPropertiesActionParserTest extends AbstractActionParserTest<LoadPropertiesAction> {

    @Test
    public void testLoadPropertiesActionParser() {
        assertActionCount(1);
        assertActionClassAndName(LoadPropertiesAction.class, "load");
        
        LoadPropertiesAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getFilePath(), "classpath:org/citrusframework/actions/load.properties");
    }
    
    @Test
    public void testLoadPropertiesActionParserError() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to missing properties file");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("Missing properties file"));
        }
    }
}
