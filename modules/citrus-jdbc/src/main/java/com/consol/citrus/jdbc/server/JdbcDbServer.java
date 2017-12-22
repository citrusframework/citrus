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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jdbc.driver.JdbcEndpointConfiguration;
import com.consol.citrus.server.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcDbServer extends AbstractServer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JdbcDbServer.class);

    /** Endpoint configuration */
    private final JdbcEndpointConfiguration endpointConfiguration;

    /** Remote interface stub */
    private Remote stub;
    private Remote remoteDriver;
    private Registry registry;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public JdbcDbServer() {
        this(new JdbcEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public JdbcDbServer(JdbcEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public JdbcEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    @Override
    protected void startup() {
        if (endpointConfiguration.isCreateRegistry()) {
            try {
                LocateRegistry.createRegistry(endpointConfiguration.getPort());
            } catch (RemoteException e) {
                throw new CitrusRuntimeException("Failed to create RMI registry", e);
            }
        }

        try {
            remoteDriver = new JdbcRemoteDriver(endpointConfiguration, getEndpointAdapter());
            stub = UnicastRemoteObject.exportObject(remoteDriver, endpointConfiguration.getPort());
            registry = endpointConfiguration.getRegistry();
            String binding = endpointConfiguration.getBinding();
            registry.bind(binding, stub);
        } catch (RemoteException e) {
            throw new CitrusRuntimeException("Failed to create RMI service in registry", e);
        } catch (AlreadyBoundException e) {
            throw new CitrusRuntimeException("Failed to bind service in RMI registry as it is already bound", e);
        }
    }

    @Override
    protected void shutdown() {
        if (registry != null) {
            try {
                registry.unbind(endpointConfiguration.getBinding());
            } catch (Exception e) {
                log.warn("Failed to unbind from registry:" + e.getMessage());
            }
        }

        if (remoteDriver != null) {
            try {
                UnicastRemoteObject.unexportObject(remoteDriver, true);
            } catch (Exception e) {
                log.warn("Failed to unexport from remote object:" + e.getMessage());
            }
        }

        registry = null;
        stub = null;
        remoteDriver = null;
    }
}
