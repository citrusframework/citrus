/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.cucumber.config.xml;

import java.util.Iterator;
import java.util.Map;

import org.citrusframework.actions.EchoAction;
import org.citrusframework.cucumber.container.StepTemplate;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class StepTemplateParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testStepTemplateParser() {
        Map<String, StepTemplate> templates = beanDefinitionContext.getBeansOfType(StepTemplate.class);

        Assert.assertEquals(templates.size(), 4);
        Iterator<StepTemplate> it = templates.values().iterator();

        // 1st template
        StepTemplate template = it.next();
        Assert.assertEquals(template.getPattern().toString(), "^My name is (.*)$");
        Assert.assertEquals(template.getParameterNames().size(), 1L);
        Assert.assertEquals(template.getParameterNames().get(0), "username");
        Assert.assertEquals(template.getActions().size(), 1L);
        Assert.assertEquals(((EchoAction)template.getActions().get(0)).getMessage(), "${username}");

        // 2nd template
        template = it.next();
        Assert.assertEquals(template.getPattern().toString(), "^I say hello.*$");
        Assert.assertEquals(template.getParameterNames().size(), 0L);
        Assert.assertEquals(template.getActions().size(), 1L);
        Assert.assertEquals(((EchoAction)template.getActions().get(0)).getMessage(), "Hello, my name is ${username}!");

        // 3rd template
        template = it.next();
        Assert.assertEquals(template.getPattern().toString(), "^I say goodbye.*$");
        Assert.assertEquals(template.getParameterNames().size(), 0L);
        Assert.assertEquals(template.getActions().size(), 1L);
        Assert.assertEquals(((EchoAction)template.getActions().get(0)).getMessage(), "Goodbye from ${username}!");

        // 4th template
        template = it.next();
        Assert.assertEquals(template.getPattern().toString(), "^the service should return: \"([^\"]*)\"$");
        Assert.assertEquals(template.getParameterNames().size(), 1L);
        Assert.assertEquals(template.getParameterNames().get(0), "body");
        Assert.assertEquals(template.getActions().size(), 1L);
        Assert.assertEquals(((EchoAction)template.getActions().get(0)).getMessage(), "You just said: ${body}");
    }

}
