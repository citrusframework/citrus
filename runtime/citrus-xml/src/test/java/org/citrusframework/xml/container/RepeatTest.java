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

package org.citrusframework.xml.container;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.api.container.IterationCountConditionExpression;
import org.citrusframework.container.RepeatUntilTrue;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.AbstractXmlActionTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RepeatTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadRepeat() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/container/repeat.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("RepeatTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(4L, result.getActionCount());

        Assertions.assertEquals(RepeatUntilTrue.class, result.getTestAction(0).getClass());

        RepeatUntilTrue action = (RepeatUntilTrue) result.getTestAction(0);
        Assertions.assertEquals("i lt 3", action.getCondition());
        Assertions.assertEquals("i", action.getIndexName());
        Assertions.assertEquals(1, action.getStart());
        Assertions.assertEquals(1, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());

        action = (RepeatUntilTrue) result.getTestAction(1);
        Assertions.assertEquals("index lt= 2", action.getCondition());
        Assertions.assertEquals("index", action.getIndexName());
        Assertions.assertEquals(1, action.getStart());
        Assertions.assertEquals(1, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());

        action = (RepeatUntilTrue) result.getTestAction(2);
        Assertions.assertEquals("i lt= 10", action.getCondition());
        Assertions.assertEquals("i", action.getIndexName());
        Assertions.assertEquals(1, action.getStart());
        Assertions.assertEquals(2, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(1).getClass());

        action = (RepeatUntilTrue) result.getTestAction(3);
        Assertions.assertNull(action.getCondition());
        Assertions.assertTrue(action.getConditionExpression() instanceof IterationCountConditionExpression);
        Assertions.assertEquals(5, ((IterationCountConditionExpression) action.getConditionExpression()).getNumberOfIterations());
        Assertions.assertEquals("i", action.getIndexName());
        Assertions.assertEquals(1, action.getStart());
        Assertions.assertEquals(1, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());
    }
}
