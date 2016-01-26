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
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.container.Iterate;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class IterateTestDesignerTest extends AbstractTestNGUnitTest {
    @Test
    public void testIterateBuilderNested() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                iterate(createVariable("index", "${i}"))
                    .index("i")
                    .startsWith(0)
                    .step(1)
                    .condition("i lt 5");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Iterate.class);
        assertEquals(test.getActions().get(0).getName(), "iterate");
        
        Iterate container = (Iterate)test.getActions().get(0);
        assertEquals(container.getActionCount(), 1);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getCondition(), "i lt 5");
        assertEquals(container.getStep(), 1);
        assertEquals(container.getStart(), 0);
    }

    @Test
    public void testIterateBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                iterate(createVariable("index", "${i}"))
                        .index("i")
                        .startsWith(0)
                        .step(1)
                        .condition("i lt 5");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Iterate.class);
        assertEquals(test.getActions().get(0).getName(), "iterate");

        Iterate container = (Iterate)test.getActions().get(0);
        assertEquals(container.getActionCount(), 1);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getCondition(), "i lt 5");
        assertEquals(container.getStep(), 1);
        assertEquals(container.getStart(), 0);
    }

    @Test
    public void testIterateBuilderWithAnonymousAction() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            /** Logger */
            private Logger log = LoggerFactory.getLogger(IterateTestDesignerTest.class);

            @Override
            public void configure() {
                AbstractTestAction anonymous = new AbstractTestAction() {
                    @Override
                    public void doExecute(TestContext context) {
                        log.info(context.getVariable("index"));
                    }
                };

                iterate(createVariable("index", "${i}"), anonymous)
                        .index("i")
                        .startsWith(0)
                        .step(1)
                        .condition("i lt 5");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Iterate.class);
        assertEquals(test.getActions().get(0).getName(), "iterate");

        Iterate container = (Iterate)test.getActions().get(0);
        assertEquals(container.getActionCount(), 2);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getCondition(), "i lt 5");
        assertEquals(container.getStep(), 1);
        assertEquals(container.getStart(), 0);
    }
}
