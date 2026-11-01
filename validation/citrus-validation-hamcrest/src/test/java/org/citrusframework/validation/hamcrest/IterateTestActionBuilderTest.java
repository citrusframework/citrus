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

package org.citrusframework.validation.hamcrest;

import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.container.Iterate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.validation.hamcrest.HamcrestConditionExpression.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IterateTestActionBuilderTest extends UnitTestSupport implements TestActionSupport {
    @Test
    public void testIterateBuilderWithHamcrestConditionExpression() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(iterate().startsWith(0)
                    .step(1)
                    .condition(assertThat(lessThan(5)))
            .actions(createVariable("index", "${i}")));

        Assertions.assertNotNull(context.getVariable("i"));
        Assertions.assertEquals("4", context.getVariable("i"));

        TestCase test = builder.getTestCase();
        assertEquals(1, test.getActionCount());
        assertEquals(Iterate.class, test.getActions().get(0).getClass());
        assertEquals("iterate", test.getActions().get(0).getName());

        Iterate container = (Iterate)test.getActions().get(0);
        assertEquals(1, container.getActionCount());
        assertEquals("i", container.getIndexName());
        assertEquals(1, container.getStep());
        assertEquals(0, container.getStart());
    }
}
