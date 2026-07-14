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
import org.testng.Assert;
import org.testng.annotations.Test;

public class BeansTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadBeansConfiguration() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/actions/beans.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "BeansTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);

        Assert.assertTrue(context.getReferenceResolver().isResolvable("myValidator"));
        Assert.assertTrue(context.getReferenceResolver().isResolvable("myValidator", DefaultTextEqualsMessageValidator.class));

        Assert.assertTrue(context.getReferenceResolver().isResolvable("myBean"));
        Assert.assertTrue(context.getReferenceResolver().isResolvable("myBean", FooBean.class));

        FooBean fooBean = context.getReferenceResolver().resolve("myBean", FooBean.class);
        Assert.assertEquals(fooBean.getMessage(), "Hello Citrus!");
        Assert.assertEquals(fooBean.getCount(), 42);
    }
}
