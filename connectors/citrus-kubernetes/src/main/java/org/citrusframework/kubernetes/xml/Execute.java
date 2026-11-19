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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.api.actions.kubernetes.command.KubernetesCommand;
import org.citrusframework.kubernetes.actions.KubernetesExecuteAction;
import org.citrusframework.kubernetes.command.GetPod;
import org.citrusframework.kubernetes.command.GetService;
import org.citrusframework.kubernetes.command.Info;
import org.citrusframework.kubernetes.command.ListEndpoints;
import org.citrusframework.kubernetes.command.ListEvents;
import org.citrusframework.kubernetes.command.ListNamespaces;
import org.citrusframework.kubernetes.command.ListNodes;
import org.citrusframework.kubernetes.command.ListPods;
import org.citrusframework.kubernetes.command.ListReplicationControllers;
import org.citrusframework.kubernetes.command.ListServices;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;

@XmlRootElement(name = "execute")
public class Execute implements TestActionBuilder<KubernetesExecuteAction>, ReferenceResolverAware {

    private final KubernetesExecuteAction.Builder delegate = new KubernetesExecuteAction.Builder();

    private AbstractCommand command;

    private String description;

    @XmlElement
    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElements({
            @XmlElement(name = "info", type = InfoCommand.class),
            @XmlElement(name = "list-pods", type = ListPodsCommand.class),
            @XmlElement(name = "list-services", type = ListServicesCommand.class),
            @XmlElement(name = "list-replication-controllers", type = ListReplicationControllersCommand.class),
            @XmlElement(name = "list-endpoints", type = ListEndpointsCommand.class),
            @XmlElement(name = "list-nodes", type = ListNodesCommand.class),
            @XmlElement(name = "list-events", type = ListEventsCommand.class),
            @XmlElement(name = "list-namespaces", type = ListNamespacesCommand.class),
            @XmlElement(name = "get-pod", type = GetPodCommand.class),
            @XmlElement(name = "get-service", type = GetServiceCommand.class)
    })
    public void setCommand(AbstractCommand command) {
        this.command = command;
    }

    void namespace(String namespace) {
        if (command != null) {
            command.getCommand().namespace(namespace);
        }
    }

    void client(KubernetesClient client) {
        delegate.client(client);
    }

    void actor(TestActor actor) {
        delegate.actor(actor);
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public KubernetesExecuteAction build() {
        if (command == null) {
            throw new org.citrusframework.exceptions.CitrusRuntimeException(
                    "Missing Kubernetes command - please provide proper command details");
        }

        if (StringUtils.hasText(description)) {
            delegate.description(description);
        }

        delegate.command(command.getCommand());

        if (StringUtils.hasText(command.getResult())) {
            delegate.result(command.getResult());
        }

        command.getValidations().forEach(validation -> delegate.validate(validation.getPath(), validation.getValue()));

        return delegate.build();
    }

    public abstract static class AbstractCommand {

        protected final KubernetesCommand<?, ?, ?> command;

        private String result;

        private List<Validation> validations = new ArrayList<>();

        protected AbstractCommand(KubernetesCommand<?, ?, ?> command) {
            this.command = command;
        }

        @XmlAttribute
        public void setNamespace(String namespace) {
            command.namespace(namespace);
        }

        @XmlAttribute
        public void setName(String name) {
            command.name(name);
        }

        @XmlAttribute
        public void setLabel(String labelExpression) {
            String[] tokens = labelExpression.split("=", 2);
            command.label(tokens[0].trim(), tokens[1].trim());
        }

        @XmlElement
        public void setLabels(Labels labels) {
            labels.getLabels().forEach(label -> command.label(label.getName(), label.getValue()));
        }

        @XmlElement(name = "without-labels")
        public void setWithoutLabels(WithoutLabels withoutLabels) {
            withoutLabels.getWithoutLabels().forEach(label -> {
                if (label.getValue() == null) {
                    command.withoutLabel(label.getName());
                } else {
                    command.withoutLabel(label.getName(), label.getValue());
                }
            });
        }

        @XmlAttribute
        public void setResult(String result) {
            this.result = result;
        }

        @XmlElement(name = "validate")
        public void setValidations(List<Validation> validations) {
            this.validations = validations;
        }

        KubernetesCommand<?, ?, ?> getCommand() {
            return command;
        }

        String getResult() {
            return result;
        }

        List<Validation> getValidations() {
            return validations;
        }
    }

    @XmlRootElement(name = "info")
    public static class InfoCommand extends AbstractCommand {

        public InfoCommand() {
            super(new Info());
        }
    }

    @XmlRootElement(name = "list-pods")
    public static class ListPodsCommand extends AbstractCommand {

        public ListPodsCommand() {
            super(new ListPods());
        }
    }

    @XmlRootElement(name = "list-services")
    public static class ListServicesCommand extends AbstractCommand {

        public ListServicesCommand() {
            super(new ListServices());
        }
    }

    @XmlRootElement(name = "list-replication-controllers")
    public static class ListReplicationControllersCommand extends AbstractCommand {

        public ListReplicationControllersCommand() {
            super(new ListReplicationControllers());
        }
    }

    @XmlRootElement(name = "list-endpoints")
    public static class ListEndpointsCommand extends AbstractCommand {

        public ListEndpointsCommand() {
            super(new ListEndpoints());
        }
    }

    @XmlRootElement(name = "list-nodes")
    public static class ListNodesCommand extends AbstractCommand {

        public ListNodesCommand() {
            super(new ListNodes());
        }
    }

    @XmlRootElement(name = "list-events")
    public static class ListEventsCommand extends AbstractCommand {

        public ListEventsCommand() {
            super(new ListEvents());
        }
    }

    @XmlRootElement(name = "list-namespaces")
    public static class ListNamespacesCommand extends AbstractCommand {

        public ListNamespacesCommand() {
            super(new ListNamespaces());
        }
    }

    @XmlRootElement(name = "get-pod")
    public static class GetPodCommand extends AbstractCommand {

        public GetPodCommand() {
            super(new GetPod());
        }
    }

    @XmlRootElement(name = "get-service")
    public static class GetServiceCommand extends AbstractCommand {

        public GetServiceCommand() {
            super(new GetService());
        }
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "withoutLabels"
    })
    public static class WithoutLabels {

        @XmlElement(name = "without-label", required = true)
        protected List<WithoutLabel> withoutLabels;

        public List<WithoutLabel> getWithoutLabels() {
            if (withoutLabels == null) {
                withoutLabels = new ArrayList<>();
            }
            return this.withoutLabels;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class WithoutLabel {

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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Validation {

        @XmlAttribute(required = true)
        protected String path;
        @XmlAttribute
        protected String value;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
