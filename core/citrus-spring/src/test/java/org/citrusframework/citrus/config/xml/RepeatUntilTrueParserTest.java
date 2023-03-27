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

package org.citrusframework.citrus.config.xml;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.citrus.container.RepeatUntilTrue;
import org.citrusframework.citrus.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class RepeatUntilTrueParserTest extends AbstractActionParserTest<RepeatUntilTrue> {

    @Test
    public void testActionParser() {
        assertActionCount(3);
        assertActionClassAndName(RepeatUntilTrue.class, "repeat-until-true");
        
        RepeatUntilTrue action = getNextTestActionFromTest();
        Assert.assertEquals(action.getCondition(), "i lt 3");
        Assert.assertEquals(action.getIndexName(), "i");
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getActionCount(), 1);
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getCondition(), "index lt= 2");
        Assert.assertEquals(action.getIndexName(), "index");
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getActionCount(), 1);
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getCondition(), "i lt= 10");
        Assert.assertEquals(action.getIndexName(), "i");
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getActionCount(), 2);
    }
}
