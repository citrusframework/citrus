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

package org.citrusframework.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.server.Server;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class StopServerActionTest extends UnitTestSupport {

    @Test
    public void testEmpty() {
        StopServerAction stopServer = new StopServerAction.Builder().build();
        stopServer.execute(context);
    }

    @Test
    public void testSingleServer() {
        Server server = Mockito.mock(Server.class);

        reset(server);

        when(server.getName()).thenReturn("MyServer");

        StopServerAction stopServer = new StopServerAction.Builder()
                .server(server)
                .build();

        stopServer.execute(context);
        verify(server).stop();
    }

    @Test
    public void testServerListSingleton() {
        Server server = Mockito.mock(Server.class);

        reset(server);

        when(server.getName()).thenReturn("MyServer");

        StopServerAction stopServer = new StopServerAction.Builder()
                .server(Collections.singletonList(server))
                .build();
        stopServer.execute(context);
        verify(server).stop();
    }

    @Test
    public void testServerList() {
        Server server1 = Mockito.mock(Server.class);
        Server server2 = Mockito.mock(Server.class);

        reset(server1, server2);

        when(server1.getName()).thenReturn("MyServer1");
        when(server2.getName()).thenReturn("MyServer2");

        List<Server> serverList = new ArrayList<Server>();
        serverList.add(server1);
        serverList.add(server2);

        StopServerAction stopServer = new StopServerAction.Builder()
                .server(serverList)
                .build();
        stopServer.execute(context);

        verify(server1).stop();
        verify(server2).stop();
    }
}
