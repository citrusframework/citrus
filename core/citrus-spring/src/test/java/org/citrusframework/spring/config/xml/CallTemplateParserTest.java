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

package org.citrusframework.spring.config.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;

import org.citrusframework.container.Template;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;

public class CallTemplateParserTest extends AbstractActionParserTest<Template> {

    @Test
    public void testCallTemplateParser() {
        assertActionCount(2);

        Template action = getNextTestActionFromTest();
        Assertions.assertEquals("call-template:myTemplate", action.getName());

        action = getNextTestActionFromTest();
        Assertions.assertEquals("call-template:print", action.getName());
        Assertions.assertEquals(2, action.getParameter().size());
        Assertions.assertEquals("Hello Template", action.getParameter().get("text"));
        Assertions.assertTrue(action.getParameter().get("message").contains("<Text>Hello Template</Text>"));
    }

    @Test
    public void testCallTemplateParserUnknownTemplate() {
        try {
            createApplicationContext("failed");
            Assertions.fail("Missing bean creation exception due to unknown template");
        } catch (BeanCreationException e) {
            Assertions.assertTrue(e.getMessage().contains("Could not resolve parent bean definition 'unknownTemplate'"));
        }
    }

    @Test
    public void testCallTemplateParserInvalidParam() {
        try {
            createApplicationContext("invalid-param");
            Assertions.fail("Missing bean creation exception due to invalid parameter value");
        } catch (BeanDefinitionStoreException e) {
            Assertions.assertTrue(e.getCause().getMessage().contains(
                    "Please provide either value attribute or value element for parameter"));
        }
    }
}
