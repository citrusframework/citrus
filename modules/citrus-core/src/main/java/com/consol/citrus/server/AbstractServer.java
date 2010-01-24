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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract base class for {@link Server} implementations.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractServer implements Server, InitializingBean, BeanNameAware {
    /** Name of this server (will be injected through Spring) */
    private String name = "";
    
    /** Running flag */
    private boolean running = false;
    
    /** Autostart server after properties are set */
    private boolean autoStart = false;
    
    /** Thread running the server */
    private Thread thread;
    
    /**  Monitor for startup and running lifecycle */
    private Object runningLock = new Object();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    
    /**
     * @see com.consol.citrus.server.Server#start()
     */
    public void start() {
        startup();
        
        synchronized (runningLock) {
            running = true;
        }
        
        thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
        
        //give server some time to startup
        try {
        	Thread.sleep(1000);
        } catch (InterruptedException e) {
			log.error("Failed to wait for server to startup", e);
		}
    }

    /**
     * @see com.consol.citrus.server.Server#stop()
     */
    public void stop() {
        shutdown();
        
        synchronized (runningLock) {
            running = false;
        }
        
        thread = null;
    }

    /**
     * Subclasses must implement this method called on server startup.
     */
    protected abstract void startup();
    
    /**
     * Subclasses must implement this method called on server shutdown.
     */
    protected abstract void shutdown();
    
    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if(autoStart) {
            start();
        }
    }

    /**
     * Join server thread.
     */
    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error("Error occured", e);
        }
    }

    /**
     * @see com.consol.citrus.server.Server#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see com.consol.citrus.server.Server#isRunning()
     */
    public boolean isRunning() {
        synchronized (runningLock) {
            return running;
        }
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name) {
        this.name = name;
    }
    
    /**
     * Enable/disable server auto start
     * @param autoStart the autoStart to set
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

}
