/*
 * Copyright 2006-2009 ConSol* Software GmbH.
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
 * This bean is used to clean up all currently run jobs in Business Works Collaborator.
 * The bean uses the icjava API to access the InConcert Server in order to manage the jobs and tasks.
 *
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 16.01.2007
 *
 */
public class CleanIcJobsBean extends AbstractTestAction {
    private String userName;
    private String password;
    private String serverName;

    private String service;
    private String network;
    private String daemon;
    private int queueCount;
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

            for (Iterator iter = jobSet.iterator(); iter.hasNext();) {
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
     * @param icServerName the icServerName to set
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @param deamon the deamon to set
     */
    public void setDaemon(String daemon) {
        this.daemon = daemon;
    }

    /**
     * @param network the network to set
     */
    public void setNetwork(String network) {
        this.network = network;
    }

    /**
     * @param queueCount the queueCount to set
     */
    public void setQueueCount(int queueCount) {
        this.queueCount = queueCount;
    }

    /**
     * @param serverDiscoveryTimeout the serverDiscoveryTimeout to set
     */
    public void setServerDiscoveryTimeout(int serverDiscoveryTimeout) {
        this.serverDiscoveryTimeout = serverDiscoveryTimeout;
    }

    /**
     * @param service the service to set
     */
    public void setService(String service) {
        this.service = service;
    }
}
