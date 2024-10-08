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
import org.testng.Assert;
import org.testng.annotations.Test;

public class TemplateTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadTemplate() {
        Template template = new YamlTemplateLoader()
                .withReferenceResolver(context.getReferenceResolver())
                .load("classpath:org/citrusframework/yaml/container/template-test.yaml");

        Assert.assertEquals(template.getTemplateName(), "myTemplate");
        Assert.assertEquals(template.getName(), "template:myTemplate");
        Assert.assertEquals(template.getParameter().size(), 0);
        Assert.assertEquals(template.getActions().size(), 1);
        Assert.assertTrue(template.isGlobalContext());

        template = new YamlTemplateLoader()
                .withReferenceResolver(context.getReferenceResolver())
                .load("classpath:org/citrusframework/yaml/container/template-parameters-test.yaml");
        Assert.assertEquals(template.getTemplateName(), "myTemplate");
        Assert.assertEquals(template.getName(), "template:myTemplate");
        Assert.assertEquals(template.getParameter().size(), 3);
        Assert.assertEquals(template.getParameter().get("foo"), "");
        Assert.assertEquals(template.getParameter().get("bar"), "barValue");
        Assert.assertEquals(template.getParameter().get("baz"), "foo\nbar\nbaz");
        Assert.assertEquals(template.getActions().size(), 2);
        Assert.assertFalse(template.isGlobalContext());
    }
}
