/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.jdbc.server;

import com.consol.citrus.jdbc.driver.JdbcServerConfiguration;
import com.consol.citrus.server.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.xml.transform.StringResult;
import spark.*;

import static spark.Spark.*;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcServer extends AbstractServer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JdbcServer.class);

    /** Endpoint configuration */
    private final JdbcServerConfiguration endpointConfiguration;

    /** Controller handling requests */
    private JdbcEndpointAdapterController controller;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public JdbcServer() {
        this(new JdbcServerConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public JdbcServer(JdbcServerConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public JdbcServerConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    @Override
    protected void startup() {
        controller = new JdbcEndpointAdapterController(getEndpointConfiguration(), getEndpointAdapter());

        port(endpointConfiguration.getPort());

        before((Filter) (request, response) -> log.info(request.requestMethod() + " " + request.url()));

        get("/connection", (req, res) -> {
            controller.getConnection(req.params());
            return "";
        });

        delete("/connection", (req, res) -> {
            controller.closeConnection();
            return "";
        });

        get("/statement", (req, res) -> {
            controller.createStatement();
            return "";
        });

        delete("/statement", (req, res) -> {
            controller.closeStatement();
            return "";
        });

        post("/statement", (req, res) -> {
            controller.createPreparedStatement(req.body());
            return "";
        });

        post("/query", (req, res) -> controller.executeQuery(req.body()), model -> {
            StringResult result = new StringResult();
            endpointConfiguration.getMarshaller().marshal(model, result);
            return result.toString();
        });

        post("/execute", (req, res) -> {
            controller.execute(req.body());
            return "";
        });

        post("/update", (req, res) -> controller.executeUpdate(req.body()));

        exception(JdbcServerException.class, (exception, request, response) -> {
            response.status(500);
            response.body(exception.getMessage());
        });
    }

    @Override
    protected void shutdown() {
        Spark.stop();
    }
}
