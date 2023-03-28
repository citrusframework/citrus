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

import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.Parallel;
import org.citrusframework.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class ParallelParserTest extends AbstractActionParserTest<Parallel> {

    @Test
    public void testActionParser() {
        assertActionCount(2);
        assertActionClassAndName(Parallel.class, "parallel");
        
        Parallel action = getNextTestActionFromTest();
        Assert.assertEquals(action.getActionCount(), 2);
        Assert.assertEquals(action.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(action.getActions().get(1).getClass(), EchoAction.class);
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getActionCount(), 3);
        Assert.assertEquals(action.getActions().get(0).getClass(), Parallel.class);
        Assert.assertEquals(((Parallel)action.getActions().get(0)).getActionCount(), 2);
        Assert.assertEquals(action.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(action.getActions().get(2).getClass(), EchoAction.class);
    }
}
