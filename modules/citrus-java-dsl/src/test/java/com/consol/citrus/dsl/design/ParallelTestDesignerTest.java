/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.Parallel;
import com.consol.citrus.container.Sequence;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ParallelTestDesignerTest extends AbstractTestNGUnitTest {

    @Test
    public void testParallelBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                parallel()
                    .actions(echo("${var}"),
                        sleep(2000),
                        echo("ASDF"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Parallel.class);
        assertEquals(test.getActions().get(0).getName(), "parallel");

        Parallel container = (Parallel)test.getActions().get(0);
        assertEquals(container.getActionCount(), 3);
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }

    @Test
    public void testParallelBuilderNestedContainers() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                parallel().actions(echo("1.0"),
                        sequential().actions(echo("2.1"), echo("2.2")),
                        echo("3.0"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Parallel.class);
        assertEquals(test.getActions().get(0).getName(), "parallel");

        Parallel container = (Parallel)test.getActions().get(0);
        assertEquals(container.getActionCount(), 3);
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
        assertEquals(((EchoAction) container.getTestAction(0)).getMessage(), "1.0");
        assertEquals(container.getTestAction(1).getClass(), Sequence.class);

        Sequence sequence = (Sequence) container.getTestAction(1);
        assertEquals(sequence.getActionCount(), 2);
        assertEquals(sequence.getTestAction(0).getClass(), EchoAction.class);
        assertEquals(((EchoAction) sequence.getTestAction(0)).getMessage(), "2.1");
        assertEquals(sequence.getTestAction(1).getClass(), EchoAction.class);
        assertEquals(((EchoAction) sequence.getTestAction(1)).getMessage(), "2.2");

        assertEquals(container.getTestAction(2).getClass(), EchoAction.class);
        assertEquals(((EchoAction) container.getTestAction(2)).getMessage(), "3.0");
    }
}
