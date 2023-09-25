/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.jmx.server;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jmx.endpoint.JmxEndpointConfiguration;
import org.citrusframework.jmx.model.ManagedBeanDefinition;
import org.citrusframework.server.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxServer extends AbstractServer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JmxServer.class);

    /** Endpoint configuration */
    private final JmxEndpointConfiguration endpointConfiguration;

    /** MBean definitions this server should expose */
    private List<ManagedBeanDefinition> mbeans;

    /** MBean server instance */
    private MBeanServer server;
    private JMXConnectorServer jmxConnectorServer;

    /** Should server automatically create service registry */
    private boolean createRegistry = false;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public JmxServer() {
        this(new JmxEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public JmxServer(JmxEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public JmxEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    @Override
    protected void startup() {
        if (createRegistry) {
            try {
                LocateRegistry.createRegistry(endpointConfiguration.getPort());
            } catch (RemoteException e) {
                throw new CitrusRuntimeException("Failed to create RMI registry", e);
            }
        }

        try {
            if (getEndpointConfiguration().getServerUrl().equals("platform")) {
                server = ManagementFactory.getPlatformMBeanServer();
            } else {
                server = MBeanServerFactory.createMBeanServer();
                jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(new JMXServiceURL(endpointConfiguration.getServerUrl()), endpointConfiguration.getEnvironmentProperties(), server);
                jmxConnectorServer.start();
            }

            for (ManagedBeanDefinition mbean : mbeans) {
                server.registerMBean(new JmxEndpointMBean(mbean, endpointConfiguration, getEndpointAdapter()), mbean.createObjectName());
            }
        } catch (IOException | NotCompliantMBeanException | InstanceAlreadyExistsException | MBeanRegistrationException e) {
            throw new CitrusRuntimeException("Failed to create JMX managed bean on mbean server", e);
        }
    }

    @Override
    protected void shutdown() {
        if (server != null) {
            try {
                for (ManagedBeanDefinition mbean : mbeans) {
                    server.unregisterMBean(mbean.createObjectName());
                }
            } catch (Exception e) {
                logger.warn("Failed to unregister mBean:" + e.getMessage());
            }
        }

        if (jmxConnectorServer != null) {
            try {
                jmxConnectorServer.stop();
            } catch (IOException e) {
                logger.warn("Error during jmx connector shutdown: " + e.getMessage());
            }
        }

        server = null;
        jmxConnectorServer = null;
    }

    /**
     * Gets the value of the mbeans property.
     *
     * @return the mbeans
     */
    public List<ManagedBeanDefinition> getMbeans() {
        return mbeans;
    }

    /**
     * Sets the mbeans property.
     *
     * @param mbeans
     */
    public void setMbeans(List<ManagedBeanDefinition> mbeans) {
        this.mbeans = mbeans;
    }

    /**
     * Gets the value of the createRegistry property.
     *
     * @return the createRegistry
     */
    public boolean isCreateRegistry() {
        return createRegistry;
    }

    /**
     * Sets the createRegistry property.
     *
     * @param createRegistry
     */
    public void setCreateRegistry(boolean createRegistry) {
        this.createRegistry = createRegistry;
    }
}
