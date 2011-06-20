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

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.container.Template;
import com.consol.citrus.testng.AbstractBeanDefinitionParserBaseTest;

/**
 * @author Christoph Deppisch
 */
public class CallTemplateParserTest extends AbstractBeanDefinitionParserBaseTest {

    @Test
    public void testCallTemplateParser() {
        Assert.assertEquals(getTestCase().getActions().size(), 2);
        
        Assert.assertEquals(getTestCase().getActions().get(0).getClass(), Template.class);
        Assert.assertEquals(((Template)getTestCase().getActions().get(0)).getName(), "call-template:myTemplate");
        
        Assert.assertEquals(getTestCase().getActions().get(1).getClass(), Template.class);
        Assert.assertEquals(((Template)getTestCase().getActions().get(1)).getName(), "call-template:print");
        
        Assert.assertEquals(((Template)getTestCase().getActions().get(1)).getParameter().size(), 2);
        Assert.assertEquals(((Template)getTestCase().getActions().get(1)).getParameter().get("text"), "Hello Template");
        Assert.assertTrue(((Template)getTestCase().getActions().get(1)).getParameter().get("message").contains("<Text>Hello Template</Text>"));
    }
    
    @Test
    public void testCalTemplateParserUnknownTemplate() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to unknown template");
        } catch (BeanCreationException e) {
            Assert.assertTrue(e.getMessage().contains("NoSuchBeanDefinitionException: No bean named 'unknownTemplate' is defined"));
        }
    }
    
    @Test
    public void testCalTemplateParserInvalidParam() {
        try {
            createApplicationContext("invalid-param");
            Assert.fail("Missing bean creation exception due to invalid parameter value");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertTrue(e.getCause().getMessage().contains(
                    "Please provide either value attribute or value element for parameter"));
        }
    }
}
