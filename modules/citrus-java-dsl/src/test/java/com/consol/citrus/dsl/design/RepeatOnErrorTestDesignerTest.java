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
import com.consol.citrus.container.RepeatOnErrorUntilTrue;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class RepeatOnErrorTestDesignerTest extends AbstractTestNGUnitTest {
    @Test
    public void testRepeatOnErrorUntilTrueBuilderNested() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                repeatOnError(echo("${var}"), sleep(3000), echo("${var}"))
                    .autoSleep(2000)
                    .until("i gt 5");

                repeatOnError(echo("${var}"))
                    .autoSleep(200)
                    .index("k")
                    .startsWith(2)
                    .until("k gt= 5");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 2);
        assertEquals(test.getActions().get(0).getClass(), RepeatOnErrorUntilTrue.class);
        assertEquals(test.getActions().get(0).getName(), "repeat-on-error");
        
        RepeatOnErrorUntilTrue container = (RepeatOnErrorUntilTrue)test.getActions().get(0);
        assertEquals(container.getActionCount(), 3);
        assertEquals(container.getAutoSleep(), Long.valueOf(2000L));
        assertEquals(container.getCondition(), "i gt 5");
        assertEquals(container.getStart(), 1);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);

        container = (RepeatOnErrorUntilTrue)test.getActions().get(1);
        assertEquals(container.getActionCount(), 1);
        assertEquals(container.getAutoSleep(), Long.valueOf(200L));
        assertEquals(container.getCondition(), "k gt= 5");
        assertEquals(container.getStart(), 2);
        assertEquals(container.getIndexName(), "k");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }

    @Test
    public void testRepeatOnErrorUntilTrueBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                repeatOnError()
                        .autoSleep(2000)
                        .until("i gt 5")
                        .actions(echo("${var}"), sleep(3000), echo("${var}"));

                repeatOnError()
                        .autoSleep(200)
                        .index("k")
                        .startsWith(2)
                        .until("k gt= 5")
                        .actions(echo("${var}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 2);
        assertEquals(test.getActions().get(0).getClass(), RepeatOnErrorUntilTrue.class);
        assertEquals(test.getActions().get(0).getName(), "repeat-on-error");

        RepeatOnErrorUntilTrue container = (RepeatOnErrorUntilTrue)test.getActions().get(0);
        assertEquals(container.getActionCount(), 3);
        assertEquals(container.getAutoSleep(), Long.valueOf(2000L));
        assertEquals(container.getCondition(), "i gt 5");
        assertEquals(container.getStart(), 1);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);

        container = (RepeatOnErrorUntilTrue)test.getActions().get(1);
        assertEquals(container.getActionCount(), 1);
        assertEquals(container.getAutoSleep(), Long.valueOf(200L));
        assertEquals(container.getCondition(), "k gt= 5");
        assertEquals(container.getStart(), 2);
        assertEquals(container.getIndexName(), "k");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }
}
