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

package org.citrusframework.yaml.container;

import org.citrusframework.container.Template;
import org.citrusframework.yaml.YamlTemplateLoader;
import org.citrusframework.yaml.actions.AbstractYamlActionTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemplateTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadTemplate() {
        Template template = new YamlTemplateLoader()
                .withReferenceResolver(context.getReferenceResolver())
                .load("classpath:org/citrusframework/yaml/container/template.citrus.it.yaml");

        Assertions.assertEquals("myTemplate", template.getTemplateName());
        Assertions.assertEquals("template:myTemplate", template.getName());
        Assertions.assertEquals(0, template.getParameter().size());
        Assertions.assertEquals(1, template.getActions().size());
        Assertions.assertTrue(template.isGlobalContext());

        template = new YamlTemplateLoader()
                .withReferenceResolver(context.getReferenceResolver())
                .load("classpath:org/citrusframework/yaml/container/template-parameters.citrus.it.yaml");
        Assertions.assertEquals("myTemplate", template.getTemplateName());
        Assertions.assertEquals("template:myTemplate", template.getName());
        Assertions.assertEquals(3, template.getParameter().size());
        Assertions.assertEquals("", template.getParameter().get("foo"));
        Assertions.assertEquals("barValue", template.getParameter().get("bar"));
        Assertions.assertEquals("foo\nbar\nbaz", template.getParameter().get("baz"));
        Assertions.assertEquals(2, template.getActions().size());
        Assertions.assertFalse(template.isGlobalContext());
    }
}
