package com.consol.citrus.activemq;

import org.apache.activemq.broker.BrokerService;
import org.springframework.beans.factory.InitializingBean;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.server.Server;

public class ActiveMQServer implements Server, InitializingBean {

    private String name = "activeMQBroker";

    private boolean running = false;
    
    private String brokerURL = "tcp://localhost:61616";
    
    private boolean persistent = false;
    
    private boolean autoStart = false;
    
    private BrokerService broker;

    /**
     * @throws CitrusRuntimeException
     */
    public void run() {
        synchronized (this) {
            running = true;
        }
        
        try {
            broker.addConnector(brokerURL);
            broker.start();
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    public void start() {
        broker = new BrokerService();
        broker.setBrokerName(name);
        broker.setUseShutdownHook(true);
        broker.setUseJmx(false);
        broker.setPersistent(persistent);
        broker.setDeleteAllMessagesOnStartup(true);
        broker.setDataDirectory("target/activemq-data");
        
        run();
    }
    
    /**
     * @throws CitrusRuntimeException
     */
    public void stop() {
        if(broker != null) {
            try {
                broker.stop();
            } catch (Exception e) {
                throw new CitrusRuntimeException(e);
            }
        }
        
        synchronized (this) {
            running = false;
        }
    }
    
    public void afterPropertiesSet() throws Exception {
        if(autoStart) {
            start();
        }
    }

    public void setBeanName(String name) {
        if(this.name == null) {
            this.name = name;
        }
    }

    public boolean isRunning() {
        return running;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param persistent the persistent to set
     */
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * @return the persistent
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * @return the brokerURL
     */
    public String getBrokerURL() {
        return brokerURL;
    }

    /**
     * @param brokerURL the brokerURL to set
     */
    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }
    
    /**
     * @param autoStart the autoStart to set
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
}
