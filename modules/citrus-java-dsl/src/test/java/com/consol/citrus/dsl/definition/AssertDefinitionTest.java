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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.Assert;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AssertDefinitionTest extends AbstractTestNGUnitTest {
    @Test
    public void testAssertBuilder() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                assertException(echo("${foo}"))
                    .exception(CitrusRuntimeException.class)
                    .message("Unknown variable 'foo'");
            }
        };

        builder.execute();

        TestCase test = builder.build();
        assertEquals(test.getActions().size(), 1);
        assertEquals(test.getActions().get(0).getClass(), Assert.class);
        assertEquals(test.getActions().get(0).getName(), "assert");
        
        Assert container = (Assert)(test.getTestAction(0));
        
        assertEquals(container.getActions().size(), 1);
        assertEquals(container.getAction().getClass(), EchoAction.class);
        assertEquals(container.getException(), CitrusRuntimeException.class);
        assertEquals(container.getMessage(), "Unknown variable 'foo'");
        assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
    }

    @Test
    public void testAssertBuilderWithAnonymousAction() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                assertException(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                context.getVariable("foo");
                            }
                        })
                    .exception(CitrusRuntimeException.class)
                    .message("Unknown variable 'foo'");
            }
        };

        builder.execute();

        TestCase test = builder.build();
        assertEquals(test.getActions().size(), 1);
        assertEquals(test.getActions().get(0).getClass(), Assert.class);
        assertEquals(test.getActions().get(0).getName(), "assert");

        Assert container = (Assert)(test.getTestAction(0));

        assertEquals(container.getActions().size(), 1);
        assertTrue(container.getAction().getClass().isAnonymousClass());
        assertEquals(container.getException(), CitrusRuntimeException.class);
        assertEquals(container.getMessage(), "Unknown variable 'foo'");
    }

}
