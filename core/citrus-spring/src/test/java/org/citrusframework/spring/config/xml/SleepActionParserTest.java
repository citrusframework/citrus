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

import java.util.concurrent.TimeUnit;

import org.citrusframework.actions.SleepAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SleepActionParserTest extends AbstractActionParserTest<SleepAction> {

    @Test
    public void testSleepActionParser() {
        assertActionCount(4);
        assertActionClassAndName(SleepAction.class, "sleep");

        SleepAction action = getNextTestActionFromTest();
        Assertions.assertEquals("5000", action.getTime());
        Assertions.assertEquals(TimeUnit.MILLISECONDS, action.getTimeUnit());

        action = getNextTestActionFromTest();
        Assertions.assertEquals("1.5", action.getTime());
        Assertions.assertEquals(TimeUnit.SECONDS, action.getTimeUnit());

        action = getNextTestActionFromTest();
        Assertions.assertEquals("1.5", action.getTime());
        Assertions.assertEquals(TimeUnit.SECONDS, action.getTimeUnit());

        action = getNextTestActionFromTest();
        Assertions.assertEquals("1500", action.getTime());
        Assertions.assertEquals(TimeUnit.MILLISECONDS, action.getTimeUnit());
    }
}
