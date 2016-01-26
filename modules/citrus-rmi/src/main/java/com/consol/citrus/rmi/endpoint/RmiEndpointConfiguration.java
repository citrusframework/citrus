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

package com.consol.citrus.rmi.endpoint;

import com.consol.citrus.endpoint.AbstractPollableEndpointConfiguration;
import com.consol.citrus.message.DefaultMessageCorrelator;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.rmi.message.RmiMessageConverter;
import com.consol.citrus.rmi.model.RmiMarshaller;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiEndpointConfiguration extends AbstractPollableEndpointConfiguration implements ApplicationContextAware {

    /** Rmi server url */
    private String serverUrl;

    /** Rmi connection parameters */
    private String host;
    private int port = Registry.REGISTRY_PORT;
    private String binding;

    private String method;

    /** RMI registry */
    private Registry registry;

    /** Message converter */
    private RmiMessageConverter messageConverter = new RmiMessageConverter();

    /** Marshaller converts from XML to RMI model objects */
    private RmiMarshaller marshaller = new RmiMarshaller();

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    /** Spring application context used for method arg object reference evaluation */
    private ApplicationContext applicationContext;

    /**
     * Gets the RMI registry based on host and port settings in this configuration.
     * @return
     * @throws RemoteException
     */
    public Registry getRegistry() throws RemoteException {
        if (registry == null) {
            if (StringUtils.hasText(host)) {
                registry = LocateRegistry.getRegistry(host, port);
            } else {
                registry = LocateRegistry.getRegistry(port);
            }
        }

        return registry;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;

        this.host = RmiEndpointUtils.getHost(serverUrl.substring("rmi://".length()));
        this.port = RmiEndpointUtils.getPort(serverUrl.substring("rmi://".length()), this);
        this.binding = RmiEndpointUtils.getBinding(serverUrl.substring("rmi://".length()));
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public RmiMessageConverter getMessageConverter() {
        return messageConverter;
    }

    public void setMessageConverter(RmiMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public MessageCorrelator getCorrelator() {
        return correlator;
    }

    public void setCorrelator(MessageCorrelator correlator) {
        this.correlator = correlator;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public RmiMarshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(RmiMarshaller marshaller) {
        this.marshaller = marshaller;
    }
}
