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

package com.consol.citrus.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.SleepAction;
import com.consol.citrus.container.Catch;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class CatchBuilderTest {
    @Test
    public void testCatchBuilder() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                catchException("com.consol.citrus.exceptions.CitrusRuntimeException", echo("${var}"));
                
                catchException(CitrusRuntimeException.class, echo("${var}"), sleep(1.0D));
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 2);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), Catch.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), Catch.class.getSimpleName());
        assertEquals(builder.getTestCase().getActions().get(1).getClass(), Catch.class);
        assertEquals(builder.getTestCase().getActions().get(1).getName(), Catch.class.getSimpleName());
        
        Catch container = (Catch)builder.getTestCase().getActions().get(0);
        assertEquals(container.getActions().size(), 1);
        assertEquals(container.getException(), "com.consol.citrus.exceptions.CitrusRuntimeException");
        assertEquals(((EchoAction)(container.getActions().get(0))).getMessage(), "${var}");
        
        container = (Catch)builder.getTestCase().getActions().get(1);
        assertEquals(container.getActions().size(), 2);
        assertEquals(container.getException(), "com.consol.citrus.exceptions.CitrusRuntimeException");
        assertEquals(((EchoAction)(container.getActions().get(0))).getMessage(), "${var}");
        assertEquals(((SleepAction)(container.getActions().get(1))).getDelay(), "1.0");
    }
}
