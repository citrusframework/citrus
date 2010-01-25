/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
public class StartServerActionTest extends AbstractBaseTest {
    
    @Test
    public void testEmpty() {
        StartServerAction startServer = new StartServerAction();
        
        startServer.execute(context);
    }
    
    @Test
    public void testSingleServer() {
        Server server = EasyMock.createMock(Server.class);
        
        reset(server);
        
        server.start();
        expectLastCall().once();
        
        expect(server.getName()).andReturn("MyServer");
        
        replay(server);
        
        StartServerAction startServer = new StartServerAction();
        startServer.setServer(server);
        
        startServer.execute(context);
    }
    
    @Test
    public void testServerListSingleton() {
        Server server = EasyMock.createMock(Server.class);
        
        reset(server);
        
        server.start();
        expectLastCall().once();
        
        expect(server.getName()).andReturn("MyServer");
        
        replay(server);
        
        StartServerAction startServer = new StartServerAction();
        startServer.setServerList(Collections.singletonList(server));
        
        startServer.execute(context);
    }
    
    @Test
    public void testServerList() {
        Server server1 = EasyMock.createMock(Server.class);
        Server server2 = EasyMock.createMock(Server.class);
        
        reset(server1, server2);
        
        server1.start();
        expectLastCall().once();
        
        server2.start();
        expectLastCall().once();
        
        expect(server1.getName()).andReturn("MyServer1");
        expect(server2.getName()).andReturn("MyServer2");
        
        replay(server1, server2);
        
        StartServerAction startServer = new StartServerAction();
        List<Server> serverList = new ArrayList<Server>();
        serverList.add(server1);
        serverList.add(server2);
        
        startServer.setServerList(serverList);
        
        startServer.execute(context);
    }
}