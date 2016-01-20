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
import com.consol.citrus.jmx.model.*;
import com.consol.citrus.message.Message;
import com.consol.citrus.server.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.management.*;
import javax.management.remote.*;
import javax.xml.transform.Source;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxServer extends AbstractServer implements InvocationHandler {

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
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Received message on JMX server: '" + endpointConfiguration.getServerUrl() + "'");
        }

        Message response = getEndpointAdapter().handleMessage(endpointConfiguration.getMessageConverter()
                .convertInbound(createInvocation(proxy, method, args), endpointConfiguration));

        ManagedBeanResult serviceResult = null;
        if (response != null && response.getPayload() != null) {
            if (response.getPayload() instanceof ManagedBeanResult) {
                serviceResult = (ManagedBeanResult) response.getPayload();
            } else if (response.getPayload() instanceof String) {
                serviceResult = (ManagedBeanResult) endpointConfiguration.getMarshaller().unmarshal(response.getPayload(Source.class));
            }
        }

        if (serviceResult != null) {
            return serviceResult.getResultObject(endpointConfiguration.getApplicationContext());
        } else {
            return null;
        }
    }

    /**
     * Creates managed bean invocation model object. Based on method arguments and method signature
     * invocation represents read or write operation.
     * @param mbean
     * @param method
     * @param args
     * @return
     */
    private ManagedBeanInvocation createInvocation(Object mbean, Method method, Object[] args) {
        ManagedBeanInvocation mbeanInvocation = new ManagedBeanInvocation();

        if (Proxy.isProxyClass(mbean.getClass())) {
            mbeanInvocation.setMbean(method.getDeclaringClass().getPackage().getName() + ":type=" + method.getDeclaringClass().getSimpleName());
        } else {
            mbeanInvocation.setMbean(mbean.getClass().getPackage().getName() + ":type=" + mbean.getClass().getSimpleName());
        }

        if (method.getName().startsWith("set") || method.getName().startsWith("get")) {
            ManagedBeanInvocation.Attribute attribute = new ManagedBeanInvocation.Attribute();

            attribute.setName(method.getName().substring(3));
            if (args != null && args.length > 0) {
                attribute.setValueObject(args[0]);
            }

            mbeanInvocation.setAttribute(attribute);
        } else {
            mbeanInvocation.setOperation(method.getName());

            if (args != null) {
                mbeanInvocation.setParameter(new ManagedBeanInvocation.Parameter());

                for (Object arg : args) {
                    OperationParam operationParam = new OperationParam();

                    operationParam.setValueObject(arg);
                    if (Map.class.isAssignableFrom(arg.getClass())) {
                        operationParam.setType(Map.class.getName());
                    } else if (List.class.isAssignableFrom(arg.getClass())) {
                        operationParam.setType(List.class.getName());
                    } else {
                        operationParam.setType(arg.getClass().getName());
                    }

                    mbeanInvocation.getParameter().getParameter().add(operationParam);
                }
            }
        }

        return mbeanInvocation;
    }

    @Override
    public JmxEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    /**
     * Gets the class loader from remote interfaces.
     * @return
     */
    public ClassLoader getClassLoader() {
        if (!CollectionUtils.isEmpty(mbeanInterfaces)) {
            return mbeanInterfaces.get(0).getClassLoader();
        } else {
            return this.getClassLoader();
        }
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
                Object proxy = Proxy.newProxyInstance(getClassLoader(), new Class[] { mbeanType }, this);

                server.registerMBean(new StandardMBean(proxy, mbeanType), new ObjectName(mbeanType.getPackage().getName(), "type", mbeanType.getSimpleName()));

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
