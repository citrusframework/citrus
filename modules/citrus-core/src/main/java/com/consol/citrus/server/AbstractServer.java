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

package com.consol.citrus.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractServer implements Server, InitializingBean, BeanNameAware {
    /** Name of this server (will be injected through Spring) */
    private String name = "";
    
    /** Running flag */
    private boolean running = false;
    
    /** Autostart server after properties are set */
    private boolean autoStart = false;
    
    /** Thread running the server */
    private Thread thread;
    
    private Object runningLock = new Object();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    
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
    
    public void stop() {
        shutdown();
        
        synchronized (runningLock) {
            running = false;
        }
        
        thread = null;
    }
    
    protected abstract void startup();
    
    protected abstract void shutdown();
    
    public void afterPropertiesSet() throws Exception {
        if(autoStart) {
            start();
        }
    }
    
    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error("Error occured", e);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isRunning() {
        synchronized (runningLock) {
            return running;
        }
    }
    
    public void setBeanName(String name) {
        this.name = name;
    }
    
    /**
     * @param autoStart the autoStart to set
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

}
