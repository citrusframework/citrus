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
