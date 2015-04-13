/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.config.xml;

import com.consol.citrus.actions.StopServerAction;
import com.consol.citrus.server.Server;
import com.consol.citrus.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public class StopServerActionParserTest extends AbstractActionParserTest<StopServerAction> {

    @Test
    public void testStartServerActionParser() {
        assertActionCount(2);
        assertActionClassAndName(StopServerAction.class, "stop-server");

        StopServerAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getServerList().size(), 0L);
        Assert.assertNotNull(action.getServer());
        Assert.assertEquals(action.getServer(), beanDefinitionContext.getBean("myServer", Server.class));

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getServerList().size(), 2L);
        Assert.assertEquals(action.getServerList().get(0), beanDefinitionContext.getBean("myFooServer", Server.class));
        Assert.assertEquals(action.getServerList().get(1), beanDefinitionContext.getBean("myBarServer", Server.class));
        Assert.assertNull(action.getServer());
    }
}
