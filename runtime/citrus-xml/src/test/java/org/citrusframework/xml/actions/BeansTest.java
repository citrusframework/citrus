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

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BeansTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadBeansConfiguration() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/actions/beans.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("BeansTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());

        Assertions.assertTrue(context.getReferenceResolver().isResolvable("myValidator"));
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("myValidator", DefaultTextEqualsMessageValidator.class));

        Assertions.assertTrue(context.getReferenceResolver().isResolvable("myBean"));
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("myBean", FooBean.class));

        FooBean fooBean = context.getReferenceResolver().resolve("myBean", FooBean.class);
        Assertions.assertEquals("Hello Citrus!", fooBean.getMessage());
        Assertions.assertEquals(42, fooBean.getCount());
    }
}
