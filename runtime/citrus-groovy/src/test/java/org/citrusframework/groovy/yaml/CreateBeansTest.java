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

package org.citrusframework.groovy.yaml;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.script.CreateBeansAction;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateBeansTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadGroovyActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/yaml/create-beans-test.yaml");

        Assert.assertFalse(context.getReferenceResolver().isResolvable(DefaultTextEqualsMessageValidator.class));

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CreateBeansTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateBeansAction.class);

        CreateBeansAction action = (CreateBeansAction) result.getTestAction(0);
        Assert.assertNull(action.getScriptResourcePath());
        Assert.assertNotNull(action.getScript().trim());

        Assert.assertTrue(context.getReferenceResolver().isResolvable(DefaultTextEqualsMessageValidator.class));
        Assert.assertTrue(context.getReferenceResolver().isResolvable("foo", FooBean.class));
        Assert.assertTrue(context.getReferenceResolver().isResolvable("bar", FooBean.class));
    }
}
