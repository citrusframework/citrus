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

package org.citrusframework.kubernetes.yaml;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.actions.KubernetesExecuteAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;

public class Execute implements TestActionBuilder<KubernetesExecuteAction>, ReferenceResolverAware {

    private final KubernetesExecuteAction.Builder delegate = new KubernetesExecuteAction.Builder();

    private AbstractCommand command;
    private String description;

    @SchemaProperty(description = "Test action description printed when the action is executed.")
    public void setDescription(String description) {
        this.description = description;
    }

    @SchemaProperty(description = "Reads the Kubernetes cluster info.")
    public void setInfo(Info info) {
        this.command = info;
    }

    @SchemaProperty(description = "Lists all pods in the namespace.")
    public void setListPods(ListPods listPods) {
        this.command = listPods;
    }

    @SchemaProperty(description = "Lists all services in the namespace.")
    public void setListServices(ListServices listServices) {
        this.command = listServices;
    }

    @SchemaProperty(description = "Lists all replication controllers in the namespace.")
    public void setListReplicationControllers(ListReplicationControllers listReplicationControllers) {
        this.command = listReplicationControllers;
    }

    @SchemaProperty(description = "Lists all endpoints in the namespace.")
    public void setListEndpoints(ListEndpoints listEndpoints) {
        this.command = listEndpoints;
    }

    @SchemaProperty(description = "Lists all cluster nodes.")
    public void setListNodes(ListNodes listNodes) {
        this.command = listNodes;
    }

    @SchemaProperty(description = "Lists all events in the namespace.")
    public void setListEvents(ListEvents listEvents) {
        this.command = listEvents;
    }

    @SchemaProperty(description = "Lists all namespaces.")
    public void setListNamespaces(ListNamespaces listNamespaces) {
        this.command = listNamespaces;
    }

    @SchemaProperty(description = "Reads a pod by name.")
    public void setGetPod(GetPod getPod) {
        this.command = getPod;
    }

    @SchemaProperty(description = "Reads a service by name.")
    public void setGetService(GetService getService) {
        this.command = getService;
    }

    void namespace(String namespace) {
        if (command != null) {
            command.namespace(namespace);
        }
    }

    void client(org.citrusframework.kubernetes.client.KubernetesClient client) {
        delegate.client(client);
    }

    @Override
    public KubernetesExecuteAction build() {
        if (command == null) {
            throw new CitrusRuntimeException("Missing Kubernetes command - please provide proper command details");
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

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.setReferenceResolver(referenceResolver);
    }

    public abstract static class AbstractCommand {

        private final org.citrusframework.api.actions.kubernetes.command.KubernetesCommand<?, ?, ?> command;
        private String result;
        private final List<Validation> validations = new ArrayList<>();

        protected AbstractCommand(org.citrusframework.api.actions.kubernetes.command.KubernetesCommand<?, ?, ?> command) {
            this.command = command;
        }

        @SchemaProperty(description = "Expected command result as JSON.")
        public void setResult(String result) {
            this.result = result;
        }

        @SchemaProperty(description = "Path expressions to validate on the command result.")
        public void setValidate(List<Validation> validations) {
            this.validations.addAll(validations);
        }

        org.citrusframework.api.actions.kubernetes.command.KubernetesCommand<?, ?, ?> getCommand() {
            return command;
        }

        String getResult() {
            return result;
        }

        List<Validation> getValidations() {
            return validations;
        }

        void namespace(String namespace) {
            command.namespace(namespace);
        }

        void name(String name) {
            command.name(name);
        }

        void labelExpression(String labelExpression) {
            String[] tokens = labelExpression.split("=", 2);
            command.label(tokens[0].trim(), tokens[1].trim());
        }

        void labels(List<Label> labels) {
            labels.forEach(label -> command.label(label.getName(), label.getValue()));
        }

        void withoutLabels(List<WithoutLabel> withoutLabels) {
            withoutLabels.forEach(label -> {
                if (label.getValue() == null) {
                    command.withoutLabel(label.getName());
                } else {
                    command.withoutLabel(label.getName(), label.getValue());
                }
            });
        }
    }

    public static class Info extends AbstractCommand {

        public Info() {
            super(new org.citrusframework.kubernetes.command.Info());
        }
    }

    public static class ListPods extends AbstractCommand {

        public ListPods() {
            super(new org.citrusframework.kubernetes.command.ListPods());
        }

        @SchemaProperty(description = "Kubernetes namespace to list the pods in.")
        public void setNamespace(String namespace) {
            namespace(namespace);
        }

        @SchemaProperty(description = "Label expression in name=value form used to select the pods.")
        public void setLabel(String labelExpression) {
            labelExpression(labelExpression);
        }

        @SchemaProperty(description = "Labels used to select the pods.")
        public void setLabels(List<Label> labels) {
            labels(labels);
        }

        @SchemaProperty(description = "Labels that must not be present on the selected pods.")
        public void setWithoutLabels(List<WithoutLabel> withoutLabels) {
            withoutLabels(withoutLabels);
        }
    }

    public static class ListServices extends AbstractCommand {

        public ListServices() {
            super(new org.citrusframework.kubernetes.command.ListServices());
        }

        @SchemaProperty(description = "Kubernetes namespace to list the services in.")
        public void setNamespace(String namespace) {
            namespace(namespace);
        }

        @SchemaProperty(description = "Label expression in name=value form used to select the services.")
        public void setLabel(String labelExpression) {
            labelExpression(labelExpression);
        }

        @SchemaProperty(description = "Labels used to select the services.")
        public void setLabels(List<Label> labels) {
            labels(labels);
        }

        @SchemaProperty(description = "Labels that must not be present on the selected services.")
        public void setWithoutLabels(List<WithoutLabel> withoutLabels) {
            withoutLabels(withoutLabels);
        }
    }

    public static class ListReplicationControllers extends AbstractCommand {

        public ListReplicationControllers() {
            super(new org.citrusframework.kubernetes.command.ListReplicationControllers());
        }

        @SchemaProperty(description = "Kubernetes namespace to list the replication controllers in.")
        public void setNamespace(String namespace) {
            namespace(namespace);
        }

        @SchemaProperty(description = "Label expression in name=value form used to select the replication controllers.")
        public void setLabel(String labelExpression) {
            labelExpression(labelExpression);
        }

        @SchemaProperty(description = "Labels used to select the replication controllers.")
        public void setLabels(List<Label> labels) {
            labels(labels);
        }

        @SchemaProperty(description = "Labels that must not be present on the selected replication controllers.")
        public void setWithoutLabels(List<WithoutLabel> withoutLabels) {
            withoutLabels(withoutLabels);
        }
    }

    public static class ListEndpoints extends AbstractCommand {

        public ListEndpoints() {
            super(new org.citrusframework.kubernetes.command.ListEndpoints());
        }

        @SchemaProperty(description = "Kubernetes namespace to list the endpoints in.")
        public void setNamespace(String namespace) {
            namespace(namespace);
        }

        @SchemaProperty(description = "Label expression in name=value form used to select the endpoints.")
        public void setLabel(String labelExpression) {
            labelExpression(labelExpression);
        }

        @SchemaProperty(description = "Labels used to select the endpoints.")
        public void setLabels(List<Label> labels) {
            labels(labels);
        }

        @SchemaProperty(description = "Labels that must not be present on the selected endpoints.")
        public void setWithoutLabels(List<WithoutLabel> withoutLabels) {
            withoutLabels(withoutLabels);
        }
    }

    public static class ListNodes extends AbstractCommand {

        public ListNodes() {
            super(new org.citrusframework.kubernetes.command.ListNodes());
        }

        @SchemaProperty(description = "Kubernetes namespace to list the nodes in.")
        public void setNamespace(String namespace) {
            namespace(namespace);
        }

        @SchemaProperty(description = "Label expression in name=value form used to select the nodes.")
        public void setLabel(String labelExpression) {
            labelExpression(labelExpression);
        }

        @SchemaProperty(description = "Labels used to select the nodes.")
        public void setLabels(List<Label> labels) {
            labels(labels);
        }

        @SchemaProperty(description = "Labels that must not be present on the selected nodes.")
        public void setWithoutLabels(List<WithoutLabel> withoutLabels) {
            withoutLabels(withoutLabels);
        }
    }

    public static class ListEvents extends AbstractCommand {

        public ListEvents() {
            super(new org.citrusframework.kubernetes.command.ListEvents());
        }

        @SchemaProperty(description = "Kubernetes namespace to list the events in.")
        public void setNamespace(String namespace) {
            namespace(namespace);
        }

        @SchemaProperty(description = "Label expression in name=value form used to select the events.")
        public void setLabel(String labelExpression) {
            labelExpression(labelExpression);
        }

        @SchemaProperty(description = "Labels used to select the events.")
        public void setLabels(List<Label> labels) {
            labels(labels);
        }

        @SchemaProperty(description = "Labels that must not be present on the selected events.")
        public void setWithoutLabels(List<WithoutLabel> withoutLabels) {
            withoutLabels(withoutLabels);
        }
    }

    public static class ListNamespaces extends AbstractCommand {

        public ListNamespaces() {
            super(new org.citrusframework.kubernetes.command.ListNamespaces());
        }

        @SchemaProperty(description = "Label expression in name=value form used to select the namespaces.")
        public void setLabel(String labelExpression) {
            labelExpression(labelExpression);
        }

        @SchemaProperty(description = "Labels used to select the namespaces.")
        public void setLabels(List<Label> labels) {
            labels(labels);
        }

        @SchemaProperty(description = "Labels that must not be present on the selected namespaces.")
        public void setWithoutLabels(List<WithoutLabel> withoutLabels) {
            withoutLabels(withoutLabels);
        }
    }

    public static class GetPod extends AbstractCommand {

        public GetPod() {
            super(new org.citrusframework.kubernetes.command.GetPod());
        }

        @SchemaProperty(description = "Name of the pod to read.")
        public void setName(String name) {
            name(name);
        }

        @SchemaProperty(description = "Kubernetes namespace to read the pod from.")
        public void setNamespace(String namespace) {
            namespace(namespace);
        }

        @SchemaProperty(description = "Label expression in name=value form used to select the pod.")
        public void setLabel(String labelExpression) {
            labelExpression(labelExpression);
        }

        @SchemaProperty(description = "Labels used to select the pod.")
        public void setLabels(List<Label> labels) {
            labels(labels);
        }
    }

    public static class GetService extends AbstractCommand {

        public GetService() {
            super(new org.citrusframework.kubernetes.command.GetService());
        }

        @SchemaProperty(description = "Name of the service to read.")
        public void setName(String name) {
            name(name);
        }

        @SchemaProperty(description = "Kubernetes namespace to read the service from.")
        public void setNamespace(String namespace) {
            namespace(namespace);
        }

        @SchemaProperty(description = "Label expression in name=value form used to select the service.")
        public void setLabel(String labelExpression) {
            labelExpression(labelExpression);
        }

        @SchemaProperty(description = "Labels used to select the service.")
        public void setLabels(List<Label> labels) {
            labels(labels);
        }
    }

    public static class Label {

        protected String name;
        protected String value;

        public String getName() {
            return name;
        }

        @SchemaProperty
        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty
        public void setValue(String value) {
            this.value = value;
        }

    }

    public static class WithoutLabel {

        protected String name;
        protected String value;

        public String getName() {
            return name;
        }

        @SchemaProperty
        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = false)
        public void setValue(String value) {
            this.value = value;
        }

    }

    public static class Validation {

        protected String path;
        protected Object value;

        public String getPath() {
            return path;
        }

        @SchemaProperty
        public void setPath(String path) {
            this.path = path;
        }

        public Object getValue() {
            return value;
        }

        @SchemaProperty
        public void setValue(Object value) {
            this.value = value;
        }

    }
}
