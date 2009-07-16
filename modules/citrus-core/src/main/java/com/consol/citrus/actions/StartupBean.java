package com.consol.citrus.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.Server;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;

/**
 * Bean to start any startable test action
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class StartupBean extends AbstractTestAction {
    /** List of beans to start */
    private List serverList = new ArrayList();

    private Server server;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(StartupBean.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) throws TestSuiteException {
        log.info("Starting up servers");

        for (Iterator iter = serverList.iterator(); iter.hasNext();) {
            Server actServer = (Server) iter.next();
            actServer.startup();
            log.info("Started server: " + actServer.getName());
        }

        if (server != null) {
            server.startup();
            log.info("Started server: " + server.getName());
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
