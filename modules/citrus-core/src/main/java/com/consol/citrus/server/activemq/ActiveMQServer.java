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
        
        try {
            broker.addConnector(brokerURL);
            broker.start();
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
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
