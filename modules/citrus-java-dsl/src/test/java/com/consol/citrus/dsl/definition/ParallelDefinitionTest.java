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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.Parallel;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;

public class ParallelDefinitionTest {
    @Test
    public void testParallelBuilder() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                parallel(echo("${var}"), 
                        sleep(2.0), 
                        echo("ASDF"));
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), Parallel.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), Parallel.class.getSimpleName());
        
        Parallel container = (Parallel)builder.getTestCase().getActions().get(0); 
        assertEquals(container.getActions().size(), 3);
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }
}
