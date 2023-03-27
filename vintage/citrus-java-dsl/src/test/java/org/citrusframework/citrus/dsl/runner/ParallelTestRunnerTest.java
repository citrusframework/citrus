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

package org.citrusframework.citrus.dsl.runner;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.EchoAction;
import org.citrusframework.citrus.container.Parallel;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ParallelTestRunnerTest extends UnitTestSupport {
    @Test
    public void testParallelBuilder() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                variable("var", "foo");

                parallel()
                    .actions(
                            echo("${var}"),
                            sleep(200),
                            echo("Hello World!")
                    );
            }
        };

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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                variable("var", "foo");

                parallel()
                    .actions(
                        echo("${var}"),
                        sequential()
                            .actions(
                                echo("1st in sequential"),
                                echo("2nd in sequential")
                            ),
                        sleep(200),
                        echo("Hello World!")
                    );
            }
        };

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Parallel.class);
        assertEquals(test.getActions().get(0).getName(), "parallel");

        Parallel container = (Parallel)test.getActions().get(0);
        assertEquals(container.getActionCount(), 4);
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }
}
