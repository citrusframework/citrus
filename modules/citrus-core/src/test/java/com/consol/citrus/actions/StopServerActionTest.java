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

package com.consol.citrus.actions;

import static org.easymock.EasyMock.*;

import java.util.*;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.consol.citrus.server.Server;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class StopServerActionTest extends AbstractBaseTest {
    
    @Test
    public void testEmpty() {
        StopServerAction stopServer = new StopServerAction();
        
        stopServer.execute(context);
    }
    
    @Test
    public void testSingleServer() {
        Server server = EasyMock.createMock(Server.class);
        
        reset(server);
        
        server.stop();
        expectLastCall().once();
        
        expect(server.getName()).andReturn("MyServer");
        
        replay(server);
        
        StopServerAction stopServer = new StopServerAction();
        stopServer.setServer(server);
        
        stopServer.execute(context);
    }
    
    @Test
    public void testServerListSingleton() {
        Server server = EasyMock.createMock(Server.class);
        
        reset(server);
        
        server.stop();
        expectLastCall().once();
        
        expect(server.getName()).andReturn("MyServer");
        
        replay(server);
        
        StopServerAction stopServer = new StopServerAction();
        stopServer.setServerList(Collections.singletonList(server));
        
        stopServer.execute(context);
    }
    
    @Test
    public void testServerList() {
        Server server1 = EasyMock.createMock(Server.class);
        Server server2 = EasyMock.createMock(Server.class);
        
        reset(server1, server2);
        
        server1.stop();
        expectLastCall().once();
        
        server2.stop();
        expectLastCall().once();
        
        expect(server1.getName()).andReturn("MyServer1");
        expect(server2.getName()).andReturn("MyServer2");
        
        replay(server1, server2);
        
        StopServerAction stopServer = new StopServerAction();
        List<Server> serverList = new ArrayList<Server>();
        serverList.add(server1);
        serverList.add(server2);
        
        stopServer.setServerList(serverList);
        
        stopServer.execute(context);
    }
}
