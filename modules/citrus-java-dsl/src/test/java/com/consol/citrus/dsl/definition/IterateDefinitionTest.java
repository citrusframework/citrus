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

import com.consol.citrus.container.Iterate;

public class IterateDefinitionTest {
    @Test
    public void testIterateBuilder() {      
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                iterate(variables().add("index", "${i}"))
                    .index("i")
                    .startsWith(0)
                    .step(1)
                    .condition("i lt 5");
            }
        };
        
        builder.run(null, null);
        
        assertEquals(builder.testCase().getActions().size(), 1);
        assertEquals(builder.testCase().getActions().get(0).getClass(), Iterate.class);
        assertEquals(builder.testCase().getActions().get(0).getName(), Iterate.class.getSimpleName());
        
        Iterate container = (Iterate)builder.testCase().getActions().get(0);   
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getCondition(), "i lt 5");
        assertEquals(container.getStep(), 1);
        assertEquals(container.getIndex(), 0);
    }
}
