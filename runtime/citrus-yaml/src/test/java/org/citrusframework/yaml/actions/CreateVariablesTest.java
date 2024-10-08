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

package org.citrusframework.yaml.actions;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateVariablesTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadCreateVariables() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/actions/create-variables-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CreateVariablesTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(((CreateVariablesAction) result.getTestAction(0)).getVariables().size(), 4L);
        Assert.assertEquals(((CreateVariablesAction) result.getTestAction(0)).getVariables().get("var1"), "test1");
        Assert.assertEquals(((CreateVariablesAction) result.getTestAction(0)).getVariables().get("var2"), "test2");
        Assert.assertEquals(((CreateVariablesAction) result.getTestAction(0)).getVariables().get("var3"), "test3");
        Assert.assertEquals(((CreateVariablesAction) result.getTestAction(0)).getVariables().get("var4"), "script:<groovy>return \"test4\"");
    }
}
