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

package com.consol.citrus.jmx.server;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jmx.endpoint.JmxEndpointConfiguration;
import com.consol.citrus.server.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxServer extends AbstractServer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmxServer.class);

    /** Endpoint configuration */
    private final JmxEndpointConfiguration endpointConfiguration;

    /** MBean interfaces this server should bind */
    private List<Class<?>> mbeanInterfaces;

    /** MBean server instance */
    private MBeanServer server;
    private JMXConnectorServer jmxConnectorServer;

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
        try {
            if (getEndpointConfiguration().getServerUrl().equals("platform")) {
                server = ManagementFactory.getPlatformMBeanServer();
            } else {
                server = MBeanServerFactory.createMBeanServer();
                jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(new JMXServiceURL(endpointConfiguration.getServerUrl()), endpointConfiguration.getEnvironmentProperties(), server);
                jmxConnectorServer.start();
            }

            for (Class mbeanType : mbeanInterfaces) {
                server.registerMBean(new JmxEndpointMBean(mbeanType, endpointConfiguration, getEndpointAdapter()), new ObjectName(mbeanType.getPackage().getName(), "type", mbeanType.getSimpleName()));

                try {
                    server.getObjectInstance(new ObjectName(mbeanType.getPackage().getName(), "type", mbeanType.getSimpleName()));
                } catch (InstanceNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException |MalformedObjectNameException | NotCompliantMBeanException | InstanceAlreadyExistsException | MBeanRegistrationException e) {
            throw new CitrusRuntimeException("Failed to create JMX managed bean on mbean server", e);
        }
    }

    @Override
    protected void shutdown() {
        if (server != null) {
            try {
                for (Class mbeanType : mbeanInterfaces) {
                    server.unregisterMBean(new ObjectName(mbeanType.getPackage().getName(), "type", mbeanType.getSimpleName()));
                }
            } catch (Throwable t) {}
        }

        if (jmxConnectorServer != null) {
            try {
                jmxConnectorServer.stop();
            } catch (IOException e) {
                log.warn("Error during jmx connector shutdown: " + e.getMessage());
            }
        }

        server = null;
        jmxConnectorServer = null;
    }

    public List<Class<?>> getMbeanInterfaces() {
        return mbeanInterfaces;
    }

    public void setMbeanInterfaces(List<Class<?>> mbeanInterfaces) {
        this.mbeanInterfaces = mbeanInterfaces;
    }
}
