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

package com.consol.citrus.server;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.exceptions.CitrusRuntimeException;

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
    private static final Logger log = LoggerFactory.getLogger(ServerShutdownThread.class);

    /** List of servers to be shut down */
    private List<Server> servers = new ArrayList<Server>();

    /** Marks if thread was already executed before */
    private boolean done = false;
    
    @Override
    public void run()
    {
        log.info("ShutdownThread running ...");

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
        if(!done) {
            Runtime.getRuntime().addShutdownHook(this);
            done = true;
        }
    }

    /**
     * Default Constructor using fields
     * @param port
     */
    public ServerShutdownThread(Server server) {
        create();

        servers.add(server);
    }

    /**
     * Default Constructor using fields
     * @param port
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