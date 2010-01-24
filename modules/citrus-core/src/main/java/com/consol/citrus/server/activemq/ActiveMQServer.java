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

package com.consol.citrus.server.activemq;

import org.apache.activemq.broker.BrokerService;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.server.AbstractServer;

/**
 * Server implementation starting a ActiveMQ JMS broker instance.
 * 
 * @author Christoph Deppisch
 */
public class ActiveMQServer extends AbstractServer {

    /** Broker url */
    private String brokerURL = "tcp://localhost:61616";
    
    /** Is broker persistent */
    private boolean persistent = false;
    
    /** BrokerService instance */
    private BrokerService broker;
    
    /** Working directory */
    private String workingDirectory = "target/activemq-data";

    /**
     * Starting the broker.
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
     * Startup method for server.
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
     * Shutdown method for server.
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
     * Setter for persistent mode.
     * @param persistent the persistent to set
     */
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * Is broker persistent.
     * @return the persistent
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * Get the broker url.
     * @return the brokerURL
     */
    public String getBrokerURL() {
        return brokerURL;
    }

    /**
     * Set the broker url.
     * @param brokerURL the brokerURL to set
     */
    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    /**
     * Set the working directory.
     * @param workingDirectory the workingDirectory to set
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    
}
