package com.consol.citrus.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.consol.citrus.Server;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;

/**
 * Bean to stop any stoppable test action
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class ShutdownBean extends AbstractTestAction {
    private List serverList = new ArrayList();

    private Server server;

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) throws TestSuiteException {
        for (Iterator iter = serverList.iterator(); iter.hasNext();) {
            Server actServer = (Server) iter.next();
            actServer.shutdown();
        }

        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * @param server the server to set
     */
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * @param serverList the servers to set
     */
    public void setServerList(List serverList) {
        this.serverList = serverList;
    }

    /**
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * @return the serverList
     */
    public List getServerList() {
        return serverList;
    }
}
