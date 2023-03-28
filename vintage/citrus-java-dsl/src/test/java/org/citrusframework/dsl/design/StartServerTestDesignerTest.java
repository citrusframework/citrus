/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.dsl.design;

import org.citrusframework.TestCase;
import org.citrusframework.actions.StartServerAction;
import org.citrusframework.server.Server;
import org.citrusframework.dsl.UnitTestSupport;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class StartServerTestDesignerTest extends UnitTestSupport {
    private Server testServer = Mockito.mock(Server.class);

    private Server server1 = Mockito.mock(Server.class);
    private Server server2 = Mockito.mock(Server.class);
    private Server server3 = Mockito.mock(Server.class);

    @Test
    public void testStartServerBuilder() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                start(testServer);
                start(server1, server2, server3);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), StartServerAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), StartServerAction.class);

        StartServerAction action = (StartServerAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "start-server");
        Assert.assertEquals(action.getServers().size(), 1);
        Assert.assertEquals(action.getServers().get(0), testServer);

        action = (StartServerAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "start-server");
        Assert.assertEquals(action.getServers().size(), 3);
        Assert.assertEquals(action.getServers().toString(), "[" + server1.toString() + ", " + server2.toString() + ", " + server3.toString() + "]");
    }
}
