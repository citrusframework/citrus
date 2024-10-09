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
import java.util.List;

import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.VerifyPodAction;

@XmlRootElement(name = "verify-pod")
public class VerifyPod extends AbstractKubernetesAction.Builder<VerifyPodAction, VerifyPod> {

    private final VerifyPodAction.Builder delegate = new VerifyPodAction.Builder();

    @XmlAttribute
    public void setName(String name) {
        this.delegate.podName(name);
    }

    @XmlElement
    public void setLabels(Labels labels) {
        labels.getLabels().forEach(
                label -> this.delegate.label(label.getName(), label.getValue()));
    }

    @XmlAttribute
    public void setLabel(String labelExpression) {
        String[] tokens = labelExpression.split("=", 2);
        this.delegate.label(tokens[0].trim(), tokens[1].trim());
    }

    @XmlAttribute
    public void setPhase(String phase) {
        this.delegate.phase(phase);
    }

    @XmlAttribute(name = "log-message")
    public void setLogMessage(String message) {
        this.delegate.waitForLogMessage(message);
    }

    @XmlAttribute(name = "print-logs")
    public void setPrintLogs(boolean printLogs) {
        this.delegate.printLogs(printLogs);
    }

    @XmlAttribute(name = "max-attempts")
    public void setMaxAttempts(int maxAttempts) {
        this.delegate.maxAttempts(maxAttempts);
    }

    @XmlAttribute(name = "delay-between-attempts")
    public void setDelayBetweenAttempts(long delayBetweenAttempts) {
        this.delegate.delayBetweenAttempts(delayBetweenAttempts);
    }

    @Override
    public VerifyPod description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public VerifyPod actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public VerifyPod client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public VerifyPod inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public VerifyPod autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public VerifyPodAction doBuild() {
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
