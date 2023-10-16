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

package org.citrusframework.rmi.server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import javax.xml.transform.Source;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.rmi.endpoint.RmiEndpointConfiguration;
import org.citrusframework.rmi.model.RmiServiceInvocation;
import org.citrusframework.rmi.model.RmiServiceResult;
import org.citrusframework.server.AbstractServer;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiServer extends AbstractServer implements InvocationHandler {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(RmiServer.class);

    /** Endpoint configuration */
    private final RmiEndpointConfiguration endpointConfiguration;

    /** Should server automatically create service registry */
    private boolean createRegistry = false;

    /** Remote interfaces this server should bind */
    private List<Class<? extends Remote>> remoteInterfaces;

    /** Remote interface stub */
    private Remote stub;
    private Remote proxy;
    private Registry registry;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public RmiServer() {
        this(new RmiEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public RmiServer(RmiEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Received message on RMI server: '" + endpointConfiguration.getBinding() + "'");
        }

        Message response = getEndpointAdapter().handleMessage(endpointConfiguration.getMessageConverter()
                .convertInbound(RmiServiceInvocation.create(proxy, method, args), endpointConfiguration, null));

        RmiServiceResult serviceResult = null;
        if (response != null && response.getPayload() != null) {
            if (response.getPayload() instanceof RmiServiceResult) {
                serviceResult = (RmiServiceResult) response.getPayload();
            } else if (response.getPayload() instanceof String) {
                serviceResult = (RmiServiceResult) endpointConfiguration.getMarshaller().unmarshal(response.getPayload(Source.class));
            }

            if (serviceResult != null && StringUtils.hasText(serviceResult.getException())) {
                throw new RemoteException(serviceResult.getException());
            }
        }

        if (serviceResult != null) {
            return serviceResult.getResultObject(endpointConfiguration.getReferenceResolver());
        } else {
            return null;
        }
    }

    @Override
    public RmiEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    /**
     * Gets the class loader from remote interfaces.
     * @return
     */
    public ClassLoader getClassLoader() {
        if (remoteInterfaces != null && !remoteInterfaces.isEmpty()) {
            return remoteInterfaces.get(0).getClassLoader();
        } else {
            return this.getClassLoader();
        }
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
            Class<?>[] interfaces = new Class[remoteInterfaces.size()];
            remoteInterfaces.toArray(interfaces);
            proxy = (Remote) Proxy.newProxyInstance(getClassLoader(), interfaces, this);
            stub = UnicastRemoteObject.exportObject(proxy, endpointConfiguration.getPort());

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
                logger.warn("Failed to unbind from registry:" + e.getMessage());
            }
        }

        if (proxy != null) {
            try {
                UnicastRemoteObject.unexportObject(proxy, true);
            } catch (Exception e) {
                logger.warn("Failed to unexport from remote object:" + e.getMessage());
            }
        }

        registry = null;
        proxy = null;
        stub = null;
    }

    public List<Class<? extends Remote>> getRemoteInterfaces() {
        return remoteInterfaces;
    }

    public void setRemoteInterfaces(List<Class<? extends Remote>> remoteInterfaces) {
        this.remoteInterfaces = remoteInterfaces;
    }

    public boolean isCreateRegistry() {
        return createRegistry;
    }

    public void setCreateRegistry(boolean createRegistry) {
        this.createRegistry = createRegistry;
    }
}
