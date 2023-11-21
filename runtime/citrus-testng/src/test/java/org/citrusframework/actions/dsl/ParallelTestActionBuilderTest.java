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

package org.citrusframework.actions.dsl;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.Parallel;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.container.Parallel.Builder.parallel;
import static org.citrusframework.container.Sequence.Builder.sequential;
import static org.testng.Assert.assertEquals;

public class ParallelTestActionBuilderTest extends UnitTestSupport {
    @Test
    public void testParallelBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("var", "foo");

        builder.$(parallel()
            .actions(
                    echo("${var}"),
                    sleep().milliseconds(200L),
                    echo("Hello World!")
            ));

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Parallel.class);
        assertEquals(test.getActions().get(0).getName(), "parallel");

        Parallel container = (Parallel)test.getActions().get(0);
        assertEquals(container.getActionCount(), 3);
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }

    @Test
    public void testParallelBuilderNestedContainer() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("var", "foo");

        builder.$(parallel()
            .actions(
                echo("${var}"),
                sequential()
                    .actions(
                        echo("1st in sequential"),
                        echo("2nd in sequential"),
                        sleep().milliseconds(200L)
                    ),
                sleep().milliseconds(200),
                echo("Hello World!")
            ));

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Parallel.class);
        assertEquals(test.getActions().get(0).getName(), "parallel");

        Parallel container = (Parallel)test.getActions().get(0);
        assertEquals(container.getActionCount(), 4);
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }
}
