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
import org.citrusframework.actions.LoadPropertiesAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoadPropertiesTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadProperties() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/actions/load-properties-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "LoadPropertiesTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), LoadPropertiesAction.class);

        LoadPropertiesAction action = (LoadPropertiesAction) result.getTestAction(0);
        Assert.assertEquals(action.getFilePath(), "classpath:org/citrusframework/yaml/load.properties");

        Assert.assertEquals(context.getVariable("property.load.test"), "Citrus rocks!");
    }
}
