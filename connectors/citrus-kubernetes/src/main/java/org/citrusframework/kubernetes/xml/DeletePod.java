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
import java.util.LinkedHashMap;
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
import org.citrusframework.kubernetes.actions.DeletePodAction;

@XmlRootElement(name = "delete-pod")
public class DeletePod extends AbstractKubernetesAction.Builder<DeletePodAction, DeletePod> {

    private final DeletePodAction.Builder delegate = new DeletePodAction.Builder();

    @XmlAttribute
    public void setName(String name) {
        this.delegate.podName(name);
    }

    @XmlElement
    public void setLabels(Labels labels) {
        Map<String, String> labelMap = new LinkedHashMap<>();
        labels.getLabels().forEach(label -> labelMap.put(label.getName(), label.getValue()));
        this.delegate.labels(labelMap);
    }

    @XmlAttribute
    public void setLabel(String labelExpression) {
        this.delegate.labelExpression(labelExpression);
    }

    @Override
    public DeletePod description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public DeletePod actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public DeletePod client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public DeletePod inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public DeletePod autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public DeletePodAction doBuild() {
        return delegate.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "labels"
    })
    public static class Labels {

        @XmlElement(name = "label", required = true)
        protected List<Label> labels;

        public List<Label> getLabels() {
            if (labels == null) {
                labels = new ArrayList<>();
            }
            return this.labels;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Label {

            @XmlAttribute(required = true)
            protected String name;
            @XmlAttribute
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
