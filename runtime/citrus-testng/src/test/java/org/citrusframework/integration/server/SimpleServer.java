/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.integration.server;

import org.citrusframework.context.TestContextFactoryBean;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.message.RawMessage;
import org.citrusframework.server.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Simple server implementation used in start/stop server integration test.
 * @author Christoph Deppisch
 * @since 2.2
 */
public class SimpleServer extends AbstractServer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SimpleServer.class);

    /** Server publishes start stop events to this channel **/
    private DirectEndpoint statusEndpoint;

    /** Test context factory */
    @Autowired
    private TestContextFactoryBean testContextFactory;

    @Override
    protected void startup() {
        logger.info("Simple server was started successfully!");
        statusEndpoint.createProducer().send(new RawMessage("SERVER STARTED"), testContextFactory.getObject());
    }

    @Override
    protected void shutdown() {
        logger.info("Simple server was stopped successfully!");
        statusEndpoint.createProducer().send(new RawMessage("SERVER STOPPED"), testContextFactory.getObject());
    }

    /**
     * Gets the status channel endpoint.
     * @return
     */
    public DirectEndpoint getStatusEndpoint() {
        return statusEndpoint;
    }

    /**
     * Sets the status channel endpoint.
     * @param statusEndpoint
     */
    public void setStatusEndpoint(DirectEndpoint statusEndpoint) {
        this.statusEndpoint = statusEndpoint;
    }
}
