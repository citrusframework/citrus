/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.citrus.config.xml;

import org.citrusframework.citrus.actions.StopTimerAction;
import org.citrusframework.citrus.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class StopTimerParserTest extends AbstractActionParserTest<StopTimerAction> {

    @Test
    public void testActionParser() {
        assertActionCount(2);
        assertActionClassAndName(StopTimerAction.class, "stop-timer");

        StopTimerAction action = getNextTestActionFromTest();
        Assert.assertNull(action.getTimerId());

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTimerId(), "timer#1");
    }
}
