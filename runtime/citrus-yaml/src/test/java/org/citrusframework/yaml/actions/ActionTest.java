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

import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ActionTest extends AbstractYamlActionTest implements TestActionSupport {

    @Test
    public void shouldLoadAction() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/actions/action.citrus.it.yaml");

        EchoAction action = echo().build();

        context.getReferenceResolver().bind("echoBuilder", echo().message("Citrus rocks!"));
        context.getReferenceResolver().bind("echo", action);

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("ActionTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(EchoAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("Citrus rocks!", ((EchoAction) result.getTestAction(0)).getMessage());

        Assertions.assertEquals(EchoAction.class, result.getTestAction(1).getClass());
        Assertions.assertEquals(action, result.getTestAction(1));
    }
}
