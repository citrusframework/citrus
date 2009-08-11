package com.consol.citrus.activemq;

import org.apache.activemq.broker.BrokerService;

import com.consol.citrus.Server;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class ActiveMQServer implements Server {

    private String name = "activeMQBroker";

    private boolean running = false;
    
    private String host = "localhost";
    private int port = 61616;
    
    private boolean persistent = false;
    
    private BrokerService broker;

    public void run() {
        synchronized (this) {
            running = true;
        }
        
        try {
            broker.addConnector("tcp://" + host + ":" + port);
            broker.start();
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    public void start() throws CitrusRuntimeException {
        broker = new BrokerService();
        broker.setBrokerName(name);
        broker.setUseShutdownHook(true);
        broker.setUseJmx(false);
        broker.setPersistent(persistent);
        broker.setDeleteAllMessagesOnStartup(true);
        broker.setDataDirectory("target/activemq-data");
        
        run();
    }
    
    public void stop() throws CitrusRuntimeException {
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
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
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
}
