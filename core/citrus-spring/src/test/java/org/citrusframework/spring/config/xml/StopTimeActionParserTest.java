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

import org.citrusframework.actions.StopTimeAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StopTimeActionParserTest extends AbstractActionParserTest<StopTimeAction> {

    @Test
    public void testActionParser() {
        assertActionCount(2);
        assertActionClassAndName(StopTimeAction.class, "stop-time");

        StopTimeAction action = getNextTestActionFromTest();
        Assertions.assertEquals(StopTimeAction.DEFAULT_TIMELINE_ID, action.getId());
        Assertions.assertEquals(StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX, action.getSuffix());

        action = getNextTestActionFromTest();
        Assertions.assertEquals("custom", action.getId());
        Assertions.assertEquals("_time", action.getSuffix());
    }
}
