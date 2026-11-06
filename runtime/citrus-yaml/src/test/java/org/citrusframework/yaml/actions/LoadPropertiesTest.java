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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoadPropertiesTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadProperties() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/actions/load-properties.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("LoadPropertiesTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(LoadPropertiesAction.class, result.getTestAction(0).getClass());

        LoadPropertiesAction action = (LoadPropertiesAction) result.getTestAction(0);
        Assertions.assertEquals("classpath:org/citrusframework/yaml/load.properties", action.getFilePath());

        Assertions.assertEquals("Citrus rocks!", context.getVariable("property.load.test"));
    }
}
