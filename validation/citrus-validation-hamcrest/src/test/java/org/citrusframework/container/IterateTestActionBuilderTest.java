/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.container;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.container.HamcrestConditionExpression.assertThat;
import static org.citrusframework.container.Iterate.Builder.iterate;
import static org.hamcrest.Matchers.lessThan;
import static org.testng.Assert.assertEquals;

public class IterateTestActionBuilderTest extends UnitTestSupport {
    @Test
    public void testIterateBuilderWithHamcrestConditionExpression() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(iterate().startsWith(0)
                    .step(1)
                    .condition(assertThat(lessThan(5)))
            .actions(createVariable("index", "${i}")));

        Assert.assertNotNull(context.getVariable("i"));
        Assert.assertEquals(context.getVariable("i"), "4");

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Iterate.class);
        assertEquals(test.getActions().get(0).getName(), "iterate");

        Iterate container = (Iterate)test.getActions().get(0);
        assertEquals(container.getActionCount(), 1);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getStep(), 1);
        assertEquals(container.getStart(), 0);
    }
}
