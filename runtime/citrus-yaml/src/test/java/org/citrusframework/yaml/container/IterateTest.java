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

package org.citrusframework.yaml.container;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.api.container.IterationCountConditionExpression;
import org.citrusframework.container.Iterate;
import org.citrusframework.yaml.YamlTestLoader;
import org.citrusframework.yaml.actions.AbstractYamlActionTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IterateTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadIterate() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/container/iterate.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("IterateTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(4L, result.getActionCount());

        Assertions.assertEquals(Iterate.class, result.getTestAction(0).getClass());

        Iterate action = (Iterate) result.getTestAction(0);
        Assertions.assertEquals("i < 3", action.getCondition());
        Assertions.assertEquals("i", action.getIndexName());
        Assertions.assertEquals(1, action.getStep());
        Assertions.assertEquals(1, action.getStart());
        Assertions.assertEquals(1L, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());

        action = (Iterate) result.getTestAction(1);
        Assertions.assertEquals(Iterate.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("index <= 2", action.getCondition());
        Assertions.assertEquals("index", action.getIndexName());
        Assertions.assertEquals(1, action.getStep());
        Assertions.assertEquals(1, action.getStart());
        Assertions.assertEquals(1L, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());

        action = (Iterate) result.getTestAction(2);
        Assertions.assertEquals("i <= 10", action.getCondition());
        Assertions.assertEquals("i", action.getIndexName());
        Assertions.assertEquals(5, action.getStep());
        Assertions.assertEquals(0, action.getStart());
        Assertions.assertEquals(2L, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(1).getClass());

        action = (Iterate) result.getTestAction(3);
        Assertions.assertNull(action.getCondition());
        Assertions.assertTrue(action.getConditionExpression() instanceof IterationCountConditionExpression);
        Assertions.assertEquals(5, ((IterationCountConditionExpression) action.getConditionExpression()).getNumberOfIterations());
        Assertions.assertEquals("i", action.getIndexName());
        Assertions.assertEquals(1, action.getStep());
        Assertions.assertEquals(1, action.getStart());
        Assertions.assertEquals(1L, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());
    }
}
