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

package org.citrusframework.xml.container;

import org.citrusframework.container.Template;
import org.citrusframework.xml.XmlTemplateLoader;
import org.citrusframework.xml.actions.AbstractXmlActionTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemplateTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadTemplate() {
        Template template = new XmlTemplateLoader()
                .withReferenceResolver(context.getReferenceResolver())
                .load("classpath:org/citrusframework/xml/container/template.citrus.it.xml");

        Assertions.assertEquals("myTemplate", template.getTemplateName());
        Assertions.assertEquals("template:myTemplate", template.getName());
        Assertions.assertEquals(0, template.getParameter().size());
        Assertions.assertEquals(1, template.getActions().size());
        Assertions.assertTrue(template.isGlobalContext());

        template = new XmlTemplateLoader()
                .withReferenceResolver(context.getReferenceResolver())
                .load("classpath:org/citrusframework/xml/container/template-parameters.citrus.it.xml");
        Assertions.assertEquals("myTemplate", template.getTemplateName());
        Assertions.assertEquals("template:myTemplate", template.getName());
        Assertions.assertEquals(3, template.getParameter().size());
        Assertions.assertEquals("", template.getParameter().get("foo"));
        Assertions.assertEquals("barValue", template.getParameter().get("bar"));
        Assertions.assertEquals("""

                        foo
                        bar
                        baz
                      \
                """, template.getParameter().get("baz"));
        Assertions.assertEquals(2, template.getActions().size());
        Assertions.assertFalse(template.isGlobalContext());
    }
}
