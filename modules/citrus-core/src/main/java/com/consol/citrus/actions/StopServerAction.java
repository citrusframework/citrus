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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Action stopping {@link Server} instances.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class StopServerAction extends AbstractTestAction {
    /** List of servers to stop */
    private List<Server> serverList = new ArrayList<Server>();

    /** Single server instance to stop */
    private Server server;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(StopServerAction.class);

    /**
     * Default constructor.
     */
    public StopServerAction() {
        setName("stop-server");
    }

    @Override
    public void doExecute(TestContext context) {
        for (Server actServer : serverList) {
            actServer.stop();
            log.info("Stopped server: " + actServer.getName());
        }

        if (server != null) {
            server.stop();
            log.info("Stopped server: " + server.getName());
        }
    }

    /**
     * @param server the server to set
     */
    public StopServerAction setServer(Server server) {
        this.server = server;
        return this;
    }

    /**
     * @param serverList the servers to set
     */
    public StopServerAction setServerList(List<Server> serverList) {
        this.serverList = serverList;
        return this;
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
