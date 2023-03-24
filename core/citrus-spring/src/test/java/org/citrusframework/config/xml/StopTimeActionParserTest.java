/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.config.xml;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.actions.StopTimeAction;
import org.citrusframework.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class StopTimeActionParserTest extends AbstractActionParserTest<StopTimeAction> {

    @Test
    public void testActionParser() {
        assertActionCount(2);
        assertActionClassAndName(StopTimeAction.class, "stop-time");
        
        StopTimeAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getId(), StopTimeAction.DEFAULT_TIMELINE_ID);
        Assert.assertEquals(action.getSuffix(), StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getId(), "custom");
        Assert.assertEquals(action.getSuffix(), "_time");
    }
}
