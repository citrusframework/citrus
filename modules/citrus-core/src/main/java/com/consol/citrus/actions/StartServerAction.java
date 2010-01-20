/*
 * Copyright 2006-2010 ConSol* Software GmbH.
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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.server.Server;

/**
 * Action starting a server instance during test
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class StartServerAction extends AbstractTestAction {
    /** List of beans to start */
    private List<Server> serverList = new ArrayList<Server>();

    private Server server;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(StartServerAction.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) {
        log.info("Starting up servers");

        for (Server actServer : serverList) {
            actServer.start();
            log.info("Started server: " + actServer.getName());
        }

        if (server != null) {
            server.start();
            log.info("Started server: " + server.getName());
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
