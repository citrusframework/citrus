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

package org.citrusframework.kubernetes.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.CreateConfigMapAction;

@XmlRootElement(name = "create-config-map")
public class CreateConfigMap extends AbstractKubernetesAction.Builder<CreateConfigMapAction, CreateConfigMap> {

    private final CreateConfigMapAction.Builder delegate = new CreateConfigMapAction.Builder();

    @XmlAttribute(required = true)
    public void setName(String name) {
        this.delegate.configMap(name);
    }

    @XmlElement
    public void setProperties(Properties properties) {
        Map<String, String> props = new HashMap<>();
        properties.getProperties().forEach(property -> props.put(property.getName(), property.getValue()));
        this.delegate.properties(props);
    }

    @XmlAttribute
    public void setFile(String path) {
        delegate.fromFile(path);
    }

    @Override
    public CreateConfigMap description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public CreateConfigMap actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public CreateConfigMap client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public CreateConfigMap inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public CreateConfigMap autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public CreateConfigMapAction doBuild() {
        return delegate.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "properties"
    })
    public static class Properties {

        @XmlElement(name = "property", required = true)
        protected List<Property> properties;

        public List<Property> getProperties() {
            if (properties == null) {
                properties = new ArrayList<>();
            }
            return this.properties;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Property {

            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "value")
            protected String value;

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
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
