/*
 * Copyright the original author or authors.
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Loads the step template definitions without the TestNG Spring test support,
 * following the JUnit Jupiter migration of the Cucumber backend tests.
 *
 * @since 2.6
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StepTemplateParserTest {

    /** Application context holding bean definitions parsed */
    protected ConfigurableApplicationContext beanDefinitionContext;

    @BeforeAll
    void parseBeanDefinitions() {
        beanDefinitionContext = new ClassPathXmlApplicationContext(
                new String[]{
                        "component-lifecycle-context.xml",
                        StepTemplateParserTest.class.getPackage().getName().replace('.', '/')
                                + "/" + StepTemplateParserTest.class.getSimpleName() + "-context.xml"},
                true);
    }

    @AfterAll
    void closeBeanDefinitions() {
        if (beanDefinitionContext != null) {
            beanDefinitionContext.close();
        }
    }

    @Test
    public void testStepTemplateParser() {
        Map<String, StepTemplate> templates = beanDefinitionContext.getBeansOfType(StepTemplate.class);

        Assertions.assertEquals(4, templates.size());
        Iterator<StepTemplate> it = templates.values().iterator();

        // 1st template
        StepTemplate template = it.next();
        Assertions.assertEquals("^My name is (.*)$", template.getPattern().toString());
        Assertions.assertEquals(1L, template.getParameterNames().size());
        Assertions.assertEquals("username", template.getParameterNames().get(0));
        Assertions.assertEquals(1L, template.getActions().size());
        Assertions.assertEquals("${username}", ((EchoAction)template.getActions().get(0)).getMessage());

        // 2nd template
        template = it.next();
        Assertions.assertEquals("^I say hello.*$", template.getPattern().toString());
        Assertions.assertEquals(0L, template.getParameterNames().size());
        Assertions.assertEquals(1L, template.getActions().size());
        Assertions.assertEquals("Hello, my name is ${username}!", ((EchoAction)template.getActions().get(0)).getMessage());

        // 3rd template
        template = it.next();
        Assertions.assertEquals("^I say goodbye.*$", template.getPattern().toString());
        Assertions.assertEquals(0L, template.getParameterNames().size());
        Assertions.assertEquals(1L, template.getActions().size());
        Assertions.assertEquals("Goodbye from ${username}!", ((EchoAction)template.getActions().get(0)).getMessage());

        // 4th template
        template = it.next();
        Assertions.assertEquals("^the service should return: \"([^\"]*)\"$", template.getPattern().toString());
        Assertions.assertEquals(1L, template.getParameterNames().size());
        Assertions.assertEquals("body", template.getParameterNames().get(0));
        Assertions.assertEquals(1L, template.getActions().size());
        Assertions.assertEquals("You just said: ${body}", ((EchoAction)template.getActions().get(0)).getMessage());
    }
}
