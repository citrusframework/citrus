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
