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
        log.info("Starting server: " + name + " ...");
            
        startup();
        
        synchronized (runningLock) {
            running = true;
        }
        
        thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
        
        log.info("Started server: " + name);
    }

    /**
     * @see com.consol.citrus.server.Server#stop()
     */
    public void stop() {
        log.info("Stopping server: " + name + " ...");
        
        shutdown();
        
        synchronized (runningLock) {
            running = false;
        }
        
        thread = null;
        
        log.info("Stopped server: " + name);
    }
    
    /** 
     * Subclasses may overwrite this method in order to add special execution logic.
     */
    public void run() {}

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
