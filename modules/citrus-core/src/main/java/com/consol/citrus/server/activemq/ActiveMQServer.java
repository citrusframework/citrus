package com.consol.citrus.server.activemq;

import org.apache.activemq.broker.BrokerService;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.server.AbstractServer;

public class ActiveMQServer extends AbstractServer {

    private String brokerURL = "tcp://localhost:61616";
    
    private boolean persistent = false;
    
    private BrokerService broker;
    
    private String workingDirectory = "target/activemq-data";

    /**
     * @throws CitrusRuntimeException
     */
    public void run() {
        try {
            broker.addConnector(brokerURL);
            broker.start();
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    /**
     * @see com.consol.citrus.server.AbstractServer#startup()
     */
    @Override
    protected void startup() {
        broker = new BrokerService();
        broker.setBrokerName(getName());
        broker.setUseShutdownHook(true);
        broker.setUseJmx(false);
        broker.setPersistent(persistent);
        broker.setDeleteAllMessagesOnStartup(true);
        broker.setDataDirectory(workingDirectory);
    }

    /**
     * @see com.consol.citrus.server.AbstractServer#shutdown()
     */
    @Override
    protected void shutdown() {
        if(broker != null) {
            try {
                broker.stop();
            } catch (Exception e) {
                throw new CitrusRuntimeException(e);
            }
        }
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
     * @param workingDirectory the workingDirectory to set
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    
}
