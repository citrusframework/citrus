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
import com.consol.citrus.container.RepeatUntilTrue;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class RepeatTestDesignerTest extends AbstractTestNGUnitTest {
    @Test
    public void testRepeatUntilTrueBuilderNested() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                repeat(echo("${var}"), sleep(3000), echo("${var}"))
                    .index("i")
                    .startsWith(2)
                    .until("i lt 5");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), RepeatUntilTrue.class);
        assertEquals(test.getActions().get(0).getName(), "repeat");
        
        RepeatUntilTrue container = (RepeatUntilTrue)test.getActions().get(0);
        assertEquals(container.getActionCount(), 3);
        assertEquals(container.getCondition(), "i lt 5");
        assertEquals(container.getStart(), 2);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }

    @Test
    public void testRepeatUntilTrueBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                repeat()
                    .index("i")
                    .startsWith(2)
                    .until("i lt 5")
                    .actions(echo("${var}"), sleep(3000), echo("${var}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), RepeatUntilTrue.class);
        assertEquals(test.getActions().get(0).getName(), "repeat");

        RepeatUntilTrue container = (RepeatUntilTrue)test.getActions().get(0);
        assertEquals(container.getActionCount(), 3);
        assertEquals(container.getCondition(), "i lt 5");
        assertEquals(container.getStart(), 2);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }
}
