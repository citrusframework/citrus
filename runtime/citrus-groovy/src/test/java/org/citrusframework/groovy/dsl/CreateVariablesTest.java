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

package org.citrusframework.groovy.dsl;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.groovy.GroovyTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateVariablesTest extends AbstractGroovyActionDslTest {

    @Test
    public void shouldLoadCreateVariables() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/create-variables.citrus.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CreateVariablesTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(CreateVariablesAction.class, result.getActions().get(0).getClass());
        Assertions.assertEquals(CreateVariablesAction.class, result.getActions().get(1).getClass());
        Assertions.assertEquals(CreateVariablesAction.class, result.getActions().get(2).getClass());

        CreateVariablesAction action = (CreateVariablesAction)result.getActions().get(0);
        Assertions.assertEquals("create-variables", action.getName());
        Assertions.assertEquals("{foo=bar}", action.getVariables().toString());

        action = (CreateVariablesAction)result.getActions().get(1);
        Assertions.assertEquals("create-variables", action.getName());
        Assertions.assertEquals("{text=Hello Citrus!}", action.getVariables().toString());

        action = (CreateVariablesAction)result.getActions().get(2);
        Assertions.assertEquals("create-variables", action.getName());
        Assertions.assertEquals("{foobar=bars}", action.getVariables().toString());
    }
}
