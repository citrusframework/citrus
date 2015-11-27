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

package com.consol.citrus.server;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing shutdown hook thread for server implementations to stop on JVM shutdown.
 * 
 * @author Christoph Deppisch
 * @since 2007
 */
public class ServerShutdownThread extends Thread
{
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ServerShutdownThread.class);

    /** List of servers to be shut down */
    private List<Server> servers = new ArrayList<Server>();

    /** Marks if thread was already executed before */
    private boolean done = false;
    
    @Override
    public void run()
    {
        log.debug("ShutdownThread running ...");

        try {
            for (Server server: servers) {
                server.stop();
                
                try { // avoid JVM crash
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    log.error("Thread was interrupted", e);
                }
            }
        } catch (CitrusRuntimeException e) {
            log.error("Error while shutting down server", e);
        }
    }
    
    /**
     * Adds ShutdownHook to JVM
     */
    private void create() {
        if (!done) {
            Runtime.getRuntime().addShutdownHook(this);
            done = true;
        }
    }

    /**
     * Default Constructor using fields
     * @param server
     */
    public ServerShutdownThread(Server server) {
        create();

        servers.add(server);
    }

    /**
     * Default Constructor using fields
     * @param servers
     */
    public ServerShutdownThread(List<Server> servers) {
        create();
        
        this.servers.addAll(servers);
    }
    
    /**
     * Adds server to maintained server list
     * @param server
     */
    public void add(Server server) {
        create();
        
        servers.add(server);
    }
}