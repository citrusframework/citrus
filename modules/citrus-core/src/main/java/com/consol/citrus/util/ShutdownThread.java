package com.consol.citrus.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.Server;
import com.consol.citrus.exceptions.TestSuiteException;

/**
 * Class representing shutdown hook thread
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 22.02.2007
 */
public class ShutdownThread extends Thread
{
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ShutdownThread.class);

    /** List of servers to be shut down */
    private List<Server> servers = new ArrayList<Server>();

    private boolean done = false;
    
    @Override
    public void run()
    {
        log.info("ShutdownThread running ...");

        try {
            for (Server server: servers) {
                server.shutdown();
                
                try { // avoid JVM crash
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    log.error("Thread was interrupted", e);
                }
            }
        } catch (TestSuiteException e) {
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
    public ShutdownThread(Server server) {
        create();

        servers.add(server);
    }

    /**
     * Default Constructor using fields
     * @param port
     */
    public ShutdownThread(List<Server> servers) {
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