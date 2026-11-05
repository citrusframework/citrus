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
import org.citrusframework.groovy.actions.CreateEndpointsAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateEndpointsTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadGroovyActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/yaml/create-endpoints.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CreateEndpointsTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(CreateEndpointsAction.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        CreateEndpointsAction action = (CreateEndpointsAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(action.getScriptResourcePath());
        Assertions.assertNotNull(action.getScript().trim());

        action = (CreateEndpointsAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getScriptResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/groovy/dsl/endpoints.groovy", action.getScriptResourcePath());
        Assertions.assertNull(action.getScript());
    }
}
