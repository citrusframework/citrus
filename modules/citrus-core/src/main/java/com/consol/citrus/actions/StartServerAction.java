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
 * Action starting a {@link Server} instance.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class StartServerAction extends AbstractTestAction {
    /** List of servers to start */
    private List<Server> serverList = new ArrayList<Server>();

    /** Single server instance to start */
    private Server server;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(StartServerAction.class);

    /**
     * Default constructor.
     */
    public StartServerAction() {
        setName("start-server");
    }

    @Override
    public void doExecute(TestContext context) {
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
    public StartServerAction setServer(Server server) {
        this.server = server;
        return this;
    }

    /**
     * @param serverList the servers to set
     */
    public StartServerAction setServerList(List<Server> serverList) {
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
