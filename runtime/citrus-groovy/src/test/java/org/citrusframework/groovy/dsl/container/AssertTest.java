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
import org.citrusframework.actions.FailAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.groovy.dsl.AbstractGroovyActionDslTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssertTest extends AbstractGroovyActionDslTest {

    @Test
    public void shouldLoadAssert() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/container/assert.citrus.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("AssertTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(org.citrusframework.container.Assert.class, result.getTestAction(0).getClass());
        Assertions.assertEquals(CitrusRuntimeException.class, ((org.citrusframework.container.Assert) result.getTestAction(0)).getException());
        Assertions.assertEquals("Something went wrong", ((org.citrusframework.container.Assert) result.getTestAction(0)).getMessage());
        Assertions.assertEquals(1L, ((org.citrusframework.container.Assert) result.getTestAction(0)).getActionCount());
        Assertions.assertEquals(FailAction.class, ((org.citrusframework.container.Assert) result.getTestAction(0)).getAction().getClass());
    }
}
