/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import static org.easymock.EasyMock.*;

import java.util.*;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.consol.citrus.server.Server;
import com.consol.citrus.testng.AbstractBaseTest;

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
