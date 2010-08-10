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

import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.server.Server;

/**
 * Action stopping {@link Server} instances.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class StopServerAction extends AbstractTestAction {
    /** List of servers to stop */
    private List<Server> serverList = new ArrayList<Server>();

    /** Single server isntance to stop */
    private Server server;

    /**
     * @see TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) {
        for (Server actServer : serverList) {
            actServer.stop();
        }

        if (server != null) {
            server.stop();
        }
    }

    /**
     * @param server the server to set
     */
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * @param serverList the servers to set
     */
    public void setServerList(List<Server> serverList) {
        this.serverList = serverList;
    }

    /**
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * @return the serverList
     */
    public List<Server> getServerList() {
        return serverList;
    }
}
