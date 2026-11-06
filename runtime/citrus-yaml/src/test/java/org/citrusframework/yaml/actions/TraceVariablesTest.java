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
import org.citrusframework.actions.TraceVariablesAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TraceVariablesTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadTraceVariables() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/actions/trace-variables.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("TraceVariablesTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(3L, result.getActionCount());
        Assertions.assertEquals(TraceVariablesAction.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        TraceVariablesAction action = (TraceVariablesAction) result.getTestAction(actionIndex++);

        Assertions.assertEquals(0L, action.getVariableNames().size());

        action = (TraceVariablesAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(1L, action.getVariableNames().size());
        Assertions.assertEquals("foo", action.getVariableNames().get(0));

        action = (TraceVariablesAction) result.getTestAction(actionIndex);
        Assertions.assertEquals(2L, action.getVariableNames().size());
        Assertions.assertEquals("foo", action.getVariableNames().get(0));
        Assertions.assertEquals("bar", action.getVariableNames().get(1));
    }
}
