/*
 * Copyright the original author or authors.
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

package org.citrusframework.spring.config.xml;

import org.citrusframework.container.Iterate;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IterateParserTest extends AbstractActionParserTest<Iterate> {

    @Test
    public void testActionParser() {
        assertActionCount(3);
        assertActionClassAndName(Iterate.class, "iterate");

        Iterate action = getNextTestActionFromTest();
        assertEquals("i lt 3", action.getCondition());
        assertEquals("i", action.getIndexName());
        assertEquals(1, action.getStart());
        assertEquals(1, action.getStep());
        assertEquals(1, action.getActionCount());
        assertNull(action.getTimeout());

        action = getNextTestActionFromTest();
        assertEquals("index lt= 2", action.getCondition());
        assertEquals("index", action.getIndexName());
        assertEquals(1, action.getStart());
        assertEquals(1, action.getStep());
        assertEquals(1, action.getActionCount());
        assertNull(action.getTimeout());

        action = getNextTestActionFromTest();
        assertEquals("i lt= 10", action.getCondition());
        assertEquals("i", action.getIndexName());
        assertEquals(0, action.getStart());
        assertEquals(5, action.getStep());
        assertEquals(2, action.getActionCount());
        assertEquals(Duration.ofSeconds(3), action.getTimeout());
    }
}
