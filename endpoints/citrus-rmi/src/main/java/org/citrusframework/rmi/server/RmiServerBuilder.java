/*
 * Copyright the original author or authors.
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

import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.rmi.endpoint.RmiEndpointUtils;
import org.citrusframework.rmi.message.RmiMessageConverter;
import org.citrusframework.rmi.model.RmiMarshaller;
import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.SchemaType;

/**
 * @since 2.5
 */
@SchemaType(module = "citrus-rmi")
@XmlType(name = "", propOrder = {})
public class RmiServerBuilder  extends AbstractServerBuilder<RmiServer, RmiServerBuilder> {

    /** Endpoint target */
    private final RmiServer endpoint = new RmiServer();

    private String messageConverter;
    private String correlator;
    private String registry;
    private String marshaller;

    @Override
    public RmiServer build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, RmiMessageConverter.class));
            }

            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }

            if (StringUtils.hasText(registry)) {
                registry(referenceResolver.resolve(registry, Registry.class));
            }

            if (StringUtils.hasText(marshaller)) {
                marshaller(referenceResolver.resolve(marshaller, RmiMarshaller.class));
            }
        }

        return super.build();
    }

    @Override
    protected RmiServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the serverUrl property.
     */
    public RmiServerBuilder serverUrl(String serverUrl) {
        endpoint.getEndpointConfiguration().setServerUrl(serverUrl);

        host(RmiEndpointUtils.getHost(serverUrl.substring("rmi://".length())));
        port(RmiEndpointUtils.getPort(serverUrl.substring("rmi://".length()), getEndpoint().getEndpointConfiguration()));
        binding(RmiEndpointUtils.getBinding(serverUrl.substring("rmi://".length())));
        return this;
    }

    @SchemaProperty
    @XmlAttribute
    public void setServerUrl(String serverUrl) {
        serverUrl(serverUrl);
    }

    /**
     * Sets the host property.
     */
    public RmiServerBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    @SchemaProperty
    @XmlAttribute
    public void setHost(String host) {
        host(host);
    }

    /**
     * Sets the port property.
     */
    public RmiServerBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    @SchemaProperty
    @XmlAttribute
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the registry property.
     */
    public RmiServerBuilder registry(Registry registry) {
        endpoint.getEndpointConfiguration().setRegistry(registry);
        return this;
    }

    @SchemaProperty
    @XmlAttribute
    public void setRegistry(String registry) {
        this.registry = registry;
    }

    /**
     * Sets the binding property.
     */
    public RmiServerBuilder binding(String binding) {
        endpoint.getEndpointConfiguration().setBinding(binding);
        return this;
    }

    @SchemaProperty
    @XmlAttribute
    public void setBinding(String binding) {
        binding(binding);
    }

    /**
     * Sets the method property.
     */
    public RmiServerBuilder method(String method) {
        endpoint.getEndpointConfiguration().setMethod(method);
        return this;
    }

    @SchemaProperty
    @XmlAttribute
    public void setMethod(String method) {
        method(method);
    }

    /**
     * Sets the createRegistry property.
     */
    public RmiServerBuilder createRegistry(boolean createRegistry) {
        endpoint.setCreateRegistry(createRegistry);
        return this;
    }

    @SchemaProperty
    @XmlAttribute
    public void setCreateRegistry(boolean createRegistry) {
        createRegistry(createRegistry);
    }

    /**
     * Sets the remote interfaces property.
     */
    @SafeVarargs
    public final RmiServerBuilder remoteInterfaces(Class<? extends Remote> ... remoteInterfaces) {
        endpoint.setRemoteInterfaces(Arrays.asList(remoteInterfaces));
        return this;
    }

    @SchemaProperty(description = "List of remote interfaces as fully qualified class name.")
    @SuppressWarnings("unchecked")
    public void setRemoteInterfaces(List<String> remoteInterfaces) {
        List<Class<? extends Remote>> interfaces = new ArrayList<>();

        for (String interfaceName : remoteInterfaces) {
            try {
                Class<?> maybeRemote = Class.forName(interfaceName, false, ClassLoaderHelper.getClassLoader());
                if (Remote.class.isAssignableFrom(maybeRemote)) {
                    interfaces.add((Class<? extends Remote>) maybeRemote);
                }
            } catch (ClassNotFoundException e) {
                throw new CitrusRuntimeException(String.format("Failed to load remote interface '%s'", interfaceName), e);
            }
        }

        remoteInterfaces(interfaces);
    }

    @XmlAttribute(name = "remote-interfaces")
    public void setRemoteInterfaces(String remoteInterfaces) {
        setRemoteInterfaces(Arrays.asList(remoteInterfaces.split(",")));
    }

    /**
     * Sets the remote interfaces property.
     */
    public RmiServerBuilder remoteInterfaces(List<Class<? extends Remote>> remoteInterfaces) {
        endpoint.setRemoteInterfaces(remoteInterfaces);
        return this;
    }

    /**
     * Sets the marshaller.
     */
    public RmiServerBuilder marshaller(RmiMarshaller marshaller) {
        endpoint.getEndpointConfiguration().setMarshaller(marshaller);
        return this;
    }

    @SchemaProperty
    @XmlAttribute
    public void setMarshaller(String marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Sets the message converter.
     */
    public RmiServerBuilder messageConverter(RmiMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(description = "Sets the message converter.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the message correlator.
     */
    public RmiServerBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(description = "Sets the message correlator.")
    @XmlAttribute(name = "message-correlator")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the polling interval.
     */
    public RmiServerBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Sets the polling interval.")
    @XmlAttribute(name = "polling-interval")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }
}
