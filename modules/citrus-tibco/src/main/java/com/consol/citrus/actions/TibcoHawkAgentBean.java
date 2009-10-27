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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import COM.TIBCO.hawk.console.hawkeye.AgentManager;
import COM.TIBCO.hawk.console.hawkeye.TIBHawkConsole;
import COM.TIBCO.hawk.talon.*;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class TibcoHawkAgentBean extends AbstractTestAction implements InitializingBean {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TibcoHawkAgentBean.class);

    private TIBHawkConsole hawkConsole;
    private AgentManager agentManager;

    private String hawkDomain;
    private String rvService;
    private String rvNetwork;
    private String rvDaemon;

    private String methodName;
    private String microAgent;

    private DataElement[] methodParameters;

    /**
     * @see com.consol.citrus.actions.AbstractTestAction#execute(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        if (hawkDomain == null || hawkDomain.trim().length() == 0) {
            log.info("Skip TibcoHawkAgentBean because hawk domain is not set properly");
            return;
        }

        try {
            MicroAgentID[] maids = agentManager.getMicroAgentIDs(microAgent, 1);

            if (maids.length==0) {
                log.info("No such MicroAgentID found on agent " + agentManager + " using microAgent " + microAgent);
                return;
            }

            MicroAgentDescriptor descriptor = agentManager.describe(maids[0]);
            log.info("Using MicroAgent: " + descriptor.getName());

            MethodInvocation methodInvocation = new MethodInvocation(methodName, methodParameters);

            log.info("Invoking method " + methodInvocation.getMethodName() + "(" + methodInvocation.getArguments() + ")");
            MicroAgentData m = agentManager.invoke(maids[0], methodInvocation);

            Object maData = m.getData();
            if (maData != null) {
                log.info("Results of method invocation:");
                printData(maData);
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        } finally {
            log.info("Shutting down tibco agent manager");
            agentManager.shutdown();
        }
    }

    /**
     * Printing result data to console
     * @param madata
     */
    private void printData(Object madata)
    {
        // it could be CompositeData
        if (madata instanceof CompositeData) {
            CompositeData compData = (CompositeData)madata;
            DataElement[] data = compData.getDataElements();

            StringBuffer sb = new StringBuffer("composite{");

            for (int i=0; i<data.length; i++) {
                sb.append(data[i] + ((i==(data.length-1))?"}":", "));
            }

            log.info(sb.toString());
        }
        // it could be TabularData
        else if (madata instanceof TabularData) {
            TabularData tabData = (TabularData)madata;

            String[] columnNames = tabData.getColumnNames();
            String[] indexNames = tabData.getIndexNames();
            // alternatively you can use getAllDataElements() as well
            Object[][] table = tabData.getAllData();

            StringBuffer sb = new StringBuffer();
            sb.append("table{");
            sb.append("columns={");

            for (int i=0; i<columnNames.length; i++) {
                sb.append(columnNames[i]+ ((i==(columnNames.length-1))?"} ":", "));
            }

            sb.append("indexColumns={");

            for (int i=0; i<indexNames.length; i++) {
                sb.append(indexNames[i]+ ((i==(indexNames.length-1))?"} ":", "));
            }

            sb.append("values={");
            if (table==null) {
                sb.append("null");
            } else {
                for (int i=0; i<table.length; i++) {
                    sb.append("row"+i+"={");
                    for (int j=0; j<table[i].length; j++) {
                        sb.append(table[i][j] + ((j==(table[i].length-1))?"} ":", "));
                    }
                }
            }
            sb.append("}");
            sb.append("}");

            log.info(sb.toString());
        } else if (madata instanceof MicroAgentException) {
            MicroAgentException exc = (MicroAgentException)madata;
            if (log.isDebugEnabled()) {
                log.debug("Method Invocation returned exception: " + exc.getStackTrace());
            }
        } else if (madata == null) {
            log.info("Method Invocation returned NO data");
        } else {
            log.info("Method Invocation returned data of UNKNOWN TYPE");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (hawkDomain != null && hawkDomain.trim().length() != 0) {
            log.info("Initializing tibco hawk console: hawkDomain: " + hawkDomain + " rvDaemon: " + rvDaemon);
            
            hawkConsole = new TIBHawkConsole(hawkDomain,rvService,rvNetwork,rvDaemon);
            agentManager = hawkConsole.getAgentManager();

            log.info("Initializing tibco agent manager " + agentManager);
            agentManager.initialize();
        }
    }

    /**
     * @param microAgent the agent to set
     */
    public void setMicroAgent(String microAgent) {
        this.microAgent = microAgent;
    }

    /**
     * @param hawkDomain the hawkDomain to set
     */
    public void setHawkDomain(String hawkDomain) {
        this.hawkDomain = hawkDomain;
    }

    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @param methodParameters the methodParameters to set
     */
    public void setMethodParameters(DataElement[] methodParameters) {
        this.methodParameters = new DataElement[methodParameters.length];
        
        int i = 0;
        for (DataElement dataElement : methodParameters) {
            this.methodParameters[i] = dataElement;
            i++;
        }
    }

    /**
     * @param rvDaemon the rvDaemon to set
     */
    public void setRvDaemon(String rvDaemon) {
        this.rvDaemon = rvDaemon;
    }

    /**
     * @param rvNetwork the rvNetwork to set
     */
    public void setRvNetwork(String rvNetwork) {
        this.rvNetwork = rvNetwork;
    }

    /**
     * @param rvService the rvService to set
     */
    public void setRvService(String rvService) {
        this.rvService = rvService;
    }
}