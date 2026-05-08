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

package org.citrusframework.xml.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.CreateEndpointAction;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.w3c.dom.Node;

@XmlRootElement(name = "create-endpoint")
public class CreateEndpoint implements TestActionBuilder<CreateEndpointAction> {

    private final CreateEndpointAction.Builder builder = new CreateEndpointAction.Builder();

    private String type;
    private String uri;
    private String name;
    private boolean autoClose = CitrusSettings.isAutoCloseDynamicEndpoints();
    private boolean autoRemove = CitrusSettings.isAutoRemoveDynamicEndpoints();
    private Properties properties;

    private Object anyEndpoint;

    @XmlAttribute
    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    @XmlAttribute
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(String value) {
        this.name = value;
    }

    @XmlAttribute(name = "auto-close")
    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    @XmlAttribute(name = "auto-remove")
    public void setAutoRemove(boolean autoRemove) {
        this.autoRemove = autoRemove;
    }

    public boolean isAutoRemove() {
        return autoRemove;
    }

    public Properties getProperties() {
        return properties;
    }

    @XmlElement
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Object getAnyEndpoint() {
        return anyEndpoint;
    }

    @XmlAnyElement(lax = true)
    public void setAnyEndpoint(Object anyEndpoint) {
        this.anyEndpoint = anyEndpoint;
    }

    @Override
    public CreateEndpointAction build() {
        builder.type(type);

        if (uri != null) {
            builder.uri(uri);
        }

        if (name != null) {
            builder.endpointName(name);
        }

        if (autoClose != CitrusSettings.isAutoCloseDynamicEndpoints()) {
            builder.autoClose(autoClose);
        }

        if (autoRemove != CitrusSettings.isAutoRemoveDynamicEndpoints()) {
            builder.autoRemove(autoRemove);
        }

        if (properties != null) {
            properties.getProperties().forEach(p -> builder.property(p.name, p.value));
        }

        if (anyEndpoint != null) {
            Object endpoint = anyEndpoint;

            if (anyEndpoint instanceof JAXBElement) {
                endpoint = ((JAXBElement<?>) anyEndpoint).getValue();
            }

            if (anyEndpoint instanceof Node node) {
                Optional<EndpointBuilder<?>> builder = EndpointBuilder.lookup(node.getLocalName());
                if (builder.isPresent()) {
                    try {
                        Unmarshaller unmarshaller = JAXBContext.newInstance(builder.get().getClass()).createUnmarshaller();
                        endpoint = unmarshaller.unmarshal(node, builder.get().getClass()).getValue();
                    } catch (JAXBException e) {
                        throw new CitrusRuntimeException("Failed to create XMLTestLoader instance", e);
                    }
                }
            }

            if (endpoint instanceof EndpointBuilder<?>) {
                builder.endpoint((EndpointBuilder<?>) endpoint);
            }
        }

        return builder.build();
    }

    @XmlElement
    public void setDescription(String value) {
        builder.description(value);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "properties"
    })
    public static class Properties {

        @XmlElement(name = "property")
        protected List<Property> properties = new ArrayList<>();

        public List<Property> getProperties() {
            if (properties == null) {
                properties = new ArrayList<>();
            }
            return properties;
        }

        public void setProperties(List<Property> properties) {
            this.properties = properties;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
        })
        public static class Property {

            @XmlAttribute
            protected String name;
            @XmlAttribute
            protected String value;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
