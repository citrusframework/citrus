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

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.container.Template;
import org.citrusframework.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class CallTemplateParserTest extends AbstractActionParserTest<Template> {

    @Test
    public void testCallTemplateParser() {
        assertActionCount(2);
        
        Template action = getNextTestActionFromTest();
        Assert.assertEquals(action.getName(), "call-template:myTemplate");
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getName(), "call-template:print");
        Assert.assertEquals(action.getParameter().size(), 2);
        Assert.assertEquals(action.getParameter().get("text"), "Hello Template");
        Assert.assertTrue(action.getParameter().get("message").contains("<Text>Hello Template</Text>"));
    }
    
    @Test
    public void testCallTemplateParserUnknownTemplate() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to unknown template");
        } catch (BeanCreationException e) {
            Assert.assertTrue(e.getMessage().contains("Could not resolve parent bean definition 'unknownTemplate'"));
        }
    }
    
    @Test
    public void testCallTemplateParserInvalidParam() {
        try {
            createApplicationContext("invalid-param");
            Assert.fail("Missing bean creation exception due to invalid parameter value");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertTrue(e.getCause().getMessage().contains(
                    "Please provide either value attribute or value element for parameter"));
        }
    }
}
