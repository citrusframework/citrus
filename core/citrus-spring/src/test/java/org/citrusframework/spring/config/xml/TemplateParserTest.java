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

import java.util.Map;

import org.citrusframework.container.Template;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemplateParserTest extends AbstractActionParserTest<Template> {

    @Test
    public void testActionParser() {
        Map<String, Template> templates = beanDefinitionContext.getBeansOfType(Template.class);

        Assertions.assertEquals(2, templates.size());

        Template template = templates.get("myTemplate");
        Assertions.assertEquals("template(myTemplate)", template.getName());
        Assertions.assertEquals(0, template.getParameter().size());
        Assertions.assertEquals(1, template.getActions().size());
        Assertions.assertTrue(template.isGlobalContext());

        template = templates.get("my2ndTemplate");
        Assertions.assertEquals("template(my2ndTemplate)", template.getName());
        Assertions.assertEquals(0, template.getParameter().size());
        Assertions.assertEquals(2, template.getActions().size());
        Assertions.assertFalse(template.isGlobalContext());
    }
}
