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

package com.consol.citrus.actions;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.tibco.workflow.api.WfClient;
import com.tibco.workflow.api.WfJob;
import com.tibco.workflow.api.WfSession;
import com.tibco.workflow.api.util.*;

/**
 * This bean is used to clean up all currently running jobs in TIBCO Business Works Collaborator.
 * The bean uses the icjava API to access the InConcert Server. API is able to manage the jobs and tasks.
 *
 * @author Christoph Deppisch
 * @since 2007
 *
 */
public class CleanIcJobsBean extends AbstractTestAction {
    /** User credentials */
    private String userName;
    private String password;
    
    /** InConcert server name */
    private String serverName;

    /** Collaborator service */
    private String service;
    
    /** Collaborator network */
    private String network;
    
    /** Collaborator deamon */
    private String daemon;
    
    /** Queue count */
    private int queueCount;
    
    /** Time to wait for connection to set up */
    private int serverDiscoveryTimeout;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CleanIcJobsBean.class);

    /**
     * @see com.consol.citrus.actions.AbstractTestAction#execute(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
	@Override
    public void execute(TestContext context) {
        int cntJobsDeleted = 0;

        if (serverName == null || serverName.trim().length() == 0) {
            log.info("Skip CleanIcJobsBean because IcServerName is not set properly");
            return;
        }

        WfClient client;
        WfSession session = null;
        try {
            log.info("Connect to icServer ...");
            client = new WfClient();
            WfTransportConfig wfTransportConfig = new TibrvWfTransportConfig(service, network, daemon, queueCount, serverDiscoveryTimeout);

            client.setDefaultTransportConfig(serverName, wfTransportConfig);

            session = client.createSession(userName, password, serverName);
            log.info("Connected to icServer "  + serverName + " as user " + userName);
            
            if(log.isDebugEnabled()) {
                log.debug("Using transport configuration: " + client.getDefaultTransportConfig(serverName));
            }

            //			WfJobManager manager = session.getWfJobManager();
            WfJobSet jobSet = session.getUser().getJobsOwned();

            for (Iterator<?> iter = jobSet.iterator(); iter.hasNext();) {
                WfJob job = (WfJob) iter.next();

                if (!job.getName().equals("Permanent Job")) {
                    log.info("Deleting collaborator job " + job.getName() + "(active=" + job.isActive() + ") " + " created by " + job.getCreator().getName() + " on " + job.getCreationTime());

                    job.delete();
                    cntJobsDeleted++;
                }
            }

            if (cntJobsDeleted > 0) {
                log.info("Found " + cntJobsDeleted + " jobs to delete");
            } else {
                log.info("No colaborator jobs found to delete");
            }

            session.terminate();
        } catch (Exception e) {
            if (session != null) {
                try {
                    session.terminate();
                } catch (WfException e1) {
                    e1.printStackTrace();
                }
            }

            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Set the server name.
     * @param icServerName the icServerName to set
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Set the user password.
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Set the user name.
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Set the server demaon.
     * @param deamon the deamon to set
     */
    public void setDaemon(String daemon) {
        this.daemon = daemon;
    }

    /**
     * Set the server network.
     * @param network the network to set
     */
    public void setNetwork(String network) {
        this.network = network;
    }

    /**
     * Set the queue count.
     * @param queueCount the queueCount to set
     */
    public void setQueueCount(int queueCount) {
        this.queueCount = queueCount;
    }

    /**
     * Set the server discovery timeout.
     * @param serverDiscoveryTimeout the serverDiscoveryTimeout to set
     */
    public void setServerDiscoveryTimeout(int serverDiscoveryTimeout) {
        this.serverDiscoveryTimeout = serverDiscoveryTimeout;
    }

    /**
     * Set the server service.
     * @param service the service to set
     */
    public void setService(String service) {
        this.service = service;
    }
}
