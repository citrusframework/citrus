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

package org.citrusframework.citrus.dsl.design;

import java.util.concurrent.TimeUnit;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.EchoAction;
import org.citrusframework.citrus.actions.SleepAction;
import org.citrusframework.citrus.container.Catch;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CatchTestDesignerTest extends UnitTestSupport {

    @Test
    public void testCatchBuilder() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                catchException()
                        .exception(CitrusRuntimeException.class.getName())
                .when(echo("${var}"));

                catchException()
                        .exception(CitrusRuntimeException.class)
                .when(echo("${var}"), sleep(100L));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 2);
        assertEquals(test.getActions().get(0).getClass(), Catch.class);
        assertEquals(test.getActions().get(0).getName(), "catch");
        assertEquals(test.getActions().get(1).getClass(), Catch.class);
        assertEquals(test.getActions().get(1).getName(), "catch");

        Catch container = (Catch)test.getActions().get(0);
        assertEquals(container.getActionCount(), 1);
        assertEquals(container.getException(), CitrusRuntimeException.class.getName());
        assertEquals(((EchoAction)(container.getActions().get(0))).getMessage(), "${var}");

        container = (Catch)test.getActions().get(1);
        assertEquals(container.getActionCount(), 2);
        assertEquals(container.getException(), CitrusRuntimeException.class.getName());
        assertEquals(((EchoAction)(container.getActions().get(0))).getMessage(), "${var}");
        assertEquals(((SleepAction)(container.getActions().get(1))).getTime(), "100");
        assertEquals(((SleepAction)(container.getActions().get(1))).getTimeUnit(), TimeUnit.MILLISECONDS);
    }
}
