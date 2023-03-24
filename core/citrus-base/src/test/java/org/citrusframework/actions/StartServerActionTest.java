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

import java.util.Collections;

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
public class StartServerActionTest extends UnitTestSupport {

    @Test
    public void testEmpty() {
        StartServerAction startServer = new StartServerAction.Builder()
                .build();
        startServer.execute(context);
    }

    @Test
    public void testSingleServer() {
        Server server = Mockito.mock(Server.class);

        reset(server);

        when(server.getName()).thenReturn("MyServer");

        StartServerAction startServer = new StartServerAction.Builder()
                .server(server)
                .build();
        startServer.execute(context);
        verify(server).start();
    }

    @Test
    public void testServerListSingleton() {
        Server server = Mockito.mock(Server.class);

        reset(server);

        when(server.getName()).thenReturn("MyServer");

        StartServerAction startServer = new StartServerAction.Builder()
                .server(Collections.singletonList(server))
                .build();
        startServer.execute(context);
        verify(server).start();
    }

    @Test
    public void testServerList() {
        Server server1 = Mockito.mock(Server.class);
        Server server2 = Mockito.mock(Server.class);

        reset(server1, server2);

        when(server1.getName()).thenReturn("MyServer1");
        when(server2.getName()).thenReturn("MyServer2");

        StartServerAction startServer = new StartServerAction.Builder()
                .server(server1, server2)
                .build();
        startServer.execute(context);

        verify(server1).start();
        verify(server2).start();
    }
}
