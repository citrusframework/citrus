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

package org.citrusframework.groovy.dsl.container;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.RepeatOnErrorUntilTrue;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.groovy.dsl.AbstractGroovyActionDslTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RepeatOnErrorTest extends AbstractGroovyActionDslTest {

    @Test
    @SuppressWarnings("removal")
    public void shouldLoadRepeatOnError() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/container/repeat-on-error.citrus.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("RepeatOnErrorTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(4L, result.getActionCount());

        Assertions.assertEquals(RepeatOnErrorUntilTrue.class, result.getTestAction(0).getClass());

        RepeatOnErrorUntilTrue action = (RepeatOnErrorUntilTrue) result.getTestAction(0);
        Assertions.assertEquals("i > 3", action.getCondition());
        Assertions.assertEquals("i", action.getIndexName());
        Assertions.assertEquals(1, action.getStart());
        Assertions.assertEquals(Long.valueOf(1000L), action.getAutoSleep());
        Assertions.assertEquals(1, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());

        action = (RepeatOnErrorUntilTrue) result.getTestAction(1);
        Assertions.assertEquals("index >= 2", action.getCondition());
        Assertions.assertEquals("index", action.getIndexName());
        Assertions.assertEquals(1, action.getStart());
        Assertions.assertEquals(Long.valueOf(1000L), action.getAutoSleep());
        Assertions.assertEquals(1, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());

        action = (RepeatOnErrorUntilTrue) result.getTestAction(2);
        Assertions.assertEquals("i >= 10", action.getCondition());
        Assertions.assertEquals("i", action.getIndexName());
        Assertions.assertEquals(1, action.getStart());
        Assertions.assertEquals(Long.valueOf(500L), action.getAutoSleep());
        Assertions.assertEquals(2, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(1).getClass());

        action = (RepeatOnErrorUntilTrue) result.getTestAction(3);
        Assertions.assertEquals("i >= 5", action.getCondition());
        Assertions.assertEquals("i", action.getIndexName());
        Assertions.assertEquals(1, action.getStart());
        Assertions.assertEquals(Long.valueOf(250L), action.getAutoSleep());
        Assertions.assertEquals(1, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getTestAction(0).getClass());
    }
}
