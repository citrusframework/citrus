/*
 * Copyright 2006-2018 the original author or authors.
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

import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.Async;
import org.citrusframework.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class AsyncParserTest extends AbstractActionParserTest<Async> {

    @Test
    public void testActionParser() {
        assertActionCount(2);
        assertActionClassAndName(Async.class, "async");
        
        Async action = getNextTestActionFromTest();
        Assert.assertEquals(action.getActionCount(), 2L);
        Assert.assertEquals(action.getSuccessActions().size(), 0L);
        Assert.assertEquals(action.getErrorActions().size(), 0L);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getActionCount(), 1L);
        Assert.assertEquals(action.getSuccessActions().size(), 1L);
        Assert.assertEquals(((EchoAction)action.getSuccessActions().get(0)).getMessage(), "Success!");
        Assert.assertEquals(action.getErrorActions().size(), 1L);
        Assert.assertEquals(((EchoAction)action.getErrorActions().get(0)).getMessage(), "Failed!");
    }
}
