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
import com.consol.citrus.container.Sequence;

public class SequenceDefinitionTest {
    @Test
    public void testSequenceBuilder() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                sequential(echo("${var}"), sleep(5.0));
            }
        };
        
        builder.run(null, null);
        
        assertEquals(builder.testCase().getActions().size(), 1);
        assertEquals(builder.testCase().getActions().get(0).getClass(), Sequence.class);
        assertEquals(builder.testCase().getActions().get(0).getName(), Sequence.class.getSimpleName());
        
        Sequence container = (Sequence)builder.testCase().getActions().get(0);
        assertEquals(container.getActions().size(), 2);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
    }
}
