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
import org.citrusframework.container.Parallel;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.groovy.dsl.AbstractGroovyActionDslTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParallelTest extends AbstractGroovyActionDslTest {

    @Test
    public void shouldLoadParallel() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/container/parallel.citrus.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("ParallelTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());

        Assertions.assertEquals(Parallel.class, result.getTestAction(0).getClass());

        Parallel action = (Parallel) result.getTestAction(0);
        Assertions.assertEquals(2, action.getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getActions().get(0).getClass());
        Assertions.assertEquals(EchoAction.class, action.getActions().get(1).getClass());

        action = (Parallel) result.getTestAction(1);
        Assertions.assertEquals(3, action.getActionCount());
        Assertions.assertEquals(Parallel.class, action.getActions().get(0).getClass());
        Assertions.assertEquals(2, ((Parallel)action.getActions().get(0)).getActionCount());
        Assertions.assertEquals(EchoAction.class, action.getActions().get(1).getClass());
        Assertions.assertEquals(EchoAction.class, action.getActions().get(2).getClass());
    }
}
