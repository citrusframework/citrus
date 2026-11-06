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

package org.citrusframework.xml.actions;

import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.container.Template;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApplyTemplateTest extends AbstractXmlActionTest implements TestActionSupport {

    @Test
    public void shouldLoadApplyTemplate() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/actions/apply-template.citrus.it.xml");

        context.getReferenceResolver().bind("myTemplate", new Template.Builder()
                .actions(echo().message("Hello from Citrus!"))
                .build());

        Template printTemplate = new Template.Builder()
                .parameter("text", "Should be overwritten")
                .actions(echo().message("${text}"), echo().message("${message}"))
                .build();
        context.getReferenceResolver().bind("print", printTemplate);

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("ApplyTemplateTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(3L, result.getActionCount());
        Assertions.assertEquals(Template.class, result.getTestAction(0).getClass());

        Template action = (Template) result.getTestAction(0);
        Assertions.assertEquals("template:myTemplate", action.getName());
        Assertions.assertEquals("myTemplate", action.getTemplateName());

        action = (Template) result.getTestAction(1);
        Assertions.assertEquals("template:print", action.getName());
        Assertions.assertEquals("print", action.getTemplateName());
        Assertions.assertEquals(2, action.getParameter().size());
        Assertions.assertEquals("Hello from Citrus!", action.getParameter().get("text"));
        Assertions.assertTrue(action.getParameter().get("message").contains("<Text>Hello from Citrus!</Text>"));

        Assertions.assertEquals(1L, printTemplate.getParameter().size());
        Assertions.assertEquals("Should be overwritten", printTemplate.getParameter().get("text"));

        action = (Template) result.getTestAction(2);
        Assertions.assertEquals("template:echo", action.getName());
        Assertions.assertEquals("echo", action.getTemplateName());

        Assertions.assertFalse(context.getReferenceResolver().isResolvable("echo", Template.class));
        Assertions.assertFalse(context.getReferenceResolver().isResolvable("echo", Template.Builder.class));
    }
}
