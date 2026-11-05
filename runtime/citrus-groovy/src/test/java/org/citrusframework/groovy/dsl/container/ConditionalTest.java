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
import org.citrusframework.actions.FailAction;
import org.citrusframework.container.Conditional;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.groovy.dsl.AbstractGroovyActionDslTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConditionalTest extends AbstractGroovyActionDslTest {

    @Test
    public void shouldLoadConditional() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/container/conditional.citrus.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("ConditionalTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(Conditional.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("${shouldRun}", ((Conditional) result.getTestAction(0)).getCondition());
        Assertions.assertEquals(1L, ((Conditional) result.getTestAction(0)).getActionCount());
        Assertions.assertEquals(EchoAction.class, ((Conditional) result.getTestAction(0)).getTestAction(0).getClass());

        Assertions.assertEquals(Conditional.class, result.getTestAction(1).getClass());
        Assertions.assertEquals("${shouldNotRun}", ((Conditional) result.getTestAction(1)).getCondition());
        Assertions.assertEquals(1L, ((Conditional) result.getTestAction(1)).getActionCount());
        Assertions.assertEquals(FailAction.class, ((Conditional) result.getTestAction(1)).getTestAction(0).getClass());
    }
}
