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

import org.citrusframework.container.RepeatOnErrorUntilTrue;
import org.citrusframework.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class RepeatOnErrorUntilTrueParserTest extends AbstractActionParserTest<RepeatOnErrorUntilTrue> {

    @Test
    public void testRepeatOnErrorParser() {
        assertActionCount(4);
        assertActionClassAndName(RepeatOnErrorUntilTrue.class, "repeat-onerror-until-true");
        
        RepeatOnErrorUntilTrue action = getNextTestActionFromTest();
        Assert.assertEquals(action.getCondition(), "i gt 3");
        Assert.assertEquals(action.getIndexName(), "i");
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getAutoSleep(), Long.valueOf(1000L));
        Assert.assertEquals(action.getActionCount(), 1);
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getCondition(), "index gt= 2");
        Assert.assertEquals(action.getIndexName(), "index");
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getAutoSleep(), Long.valueOf(1000L));
        Assert.assertEquals(action.getActionCount(), 1);
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getCondition(), "i gt= 10");
        Assert.assertEquals(action.getIndexName(), "i");
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getAutoSleep(), Long.valueOf(500L));
        Assert.assertEquals(action.getActionCount(), 2);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getCondition(), "i gt= 5");
        Assert.assertEquals(action.getIndexName(), "i");
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getAutoSleep(), Long.valueOf(250L));
        Assert.assertEquals(action.getActionCount(), 1);
    }
}
