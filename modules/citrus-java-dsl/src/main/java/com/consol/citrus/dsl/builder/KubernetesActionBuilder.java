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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.kubernetes.actions.KubernetesExecuteAction;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import com.consol.citrus.kubernetes.command.*;
import io.fabric8.kubernetes.api.model.*;
import org.springframework.core.io.Resource;

/**
 * Action executes kubernetes commands.
 * 
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesActionBuilder extends AbstractTestActionBuilder<KubernetesExecuteAction> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public KubernetesActionBuilder(KubernetesExecuteAction action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public KubernetesActionBuilder() {
		super(new KubernetesExecuteAction());
	}

	/**
	 * Use a custom kubernetes client.
	 */
	public KubernetesActionBuilder client(KubernetesClient kubernetesClient) {
		action.setKubernetesClient(kubernetesClient);
		return this;
	}

	/**
	 * Use a kubernetes command.
	 */
	public <T extends KubernetesCommand> T command(T command) {
		action.setCommand(command);
		return command;
	}

	/**
     * Use a info command.
     */
    public BaseActionBuilder<BaseActionBuilder, InfoResult> info() {
        return new BaseActionBuilder<>(new Info());
    }

    /**
     * Pod action builder.
     */
    public PodActionBuilder pod() {
        return new PodActionBuilder();
    }

    /**
     * Pods action builder.
     */
    public PodsActionBuilder pods() {
        return new PodsActionBuilder();
    }

    /**
     * Service action builder.
     */
    public ServiceActionBuilder service() {
        return new ServiceActionBuilder();
    }

    /**
     * Services action builder.
     */
    public ServicesActionBuilder services() {
        return new ServicesActionBuilder();
    }

    /**
     * ReplicationControllers action builder.
     */
    public ReplicationControllersActionBuilder replicationControllers() {
        return new ReplicationControllersActionBuilder();
    }

    /**
     * Endpoints action builder.
     */
    public EndpointsActionBuilder endpoints() {
        return new EndpointsActionBuilder();
    }

    /**
     * Nodes action builder.
     */
    public NodesActionBuilder nodes() {
        return new NodesActionBuilder();
    }

    /**
     * Events action builder.
     */
    public EventsActionBuilder events() {
        return new EventsActionBuilder();
    }

    /**
     * Namespaces action builder.
     */
    public NamespacesActionBuilder namespaces() {
        return new NamespacesActionBuilder();
    }

    /**
     * Base kubernetes action builder.
     */
    public class BaseActionBuilder<T extends BaseActionBuilder, R extends KubernetesResource> implements TestActionBuilder {

        /** Kubernetes command */
        private KubernetesCommand<R> command;

        /** Self reference for fluent API */
        private T self;

        /**
         * Default constructor.
         */
        BaseActionBuilder() {
            super();
            self = (T) this;
        }

        /**
         * Constructor using command.
         * @param command
         */
        BaseActionBuilder(KubernetesCommand<R> command) {
            command(command);
        }

        /**
         * Adds expected command result.
         * @param result
         * @return
         */
        public T result(String result) {
            action.setCommandResult(result);
            return self;
        }

        /**
         * Adds command result callback.
         * @param callback
         * @return
         */
        public T validate(CommandResultCallback<R> callback) {
            command.validate(callback);
            return self;
        }

        /**
         * Sets the label parameter.
         * @param key
         * @param value
         * @return
         */
        public T label(String key, String value) {
            command.label(key, value);
            return self;
        }

        /**
         * Sets the label parameter.
         * @param key
         * @return
         */
        public T label(String key) {
            command.label(key);
            return self;
        }

        /**
         * Sets the namespace parameter.
         * @param key
         * @return
         */
        public T namespace(String key) {
            command.namespace(key);
            return self;
        }

        /**
         * Sets the name parameter.
         * @param key
         * @return
         */
        public T name(String key) {
            command.name(key);
            return self;
        }

        /**
         * Sets the without label parameter.
         * @param key
         * @param value
         * @return
         */
        public T withoutLabel(String key, String value) {
            command.withoutLabel(key, value);
            return self;
        }

        /**
         * Sets the without label parameter.
         * @param key
         * @return
         */
        public T withoutLabel(String key) {
            command.withoutLabel(key);
            return self;
        }

        /**
         * Sets command.
         * @param command
         * @return
         */
        protected T command(KubernetesCommand<R> command) {
            this.command = command;
            KubernetesActionBuilder.this.command(command);
            return self;
        }

        @Override
        public TestAction build() {
            return KubernetesActionBuilder.this.build();
        }
    }

    /**
     * Pod action builder.
     */
    public class PodActionBuilder extends BaseActionBuilder<PodActionBuilder, Pod> {

        /**
         * Creates new pod.
         * @param pod
         */
        public PodActionBuilder create(Pod pod) {
            CreatePod command = new CreatePod();
            command.setPod(pod);
            command(command);
            return this;
        }

        /**
         * Create new pod from template.
         * @param template
         */
        public PodActionBuilder create(Resource template) {
            CreatePod command = new CreatePod();
            command.setTemplateResource(template);
            command(command);
            return this;
        }

        /**
         * Create new pod from template path.
         * @param templatePath
         */
        public PodActionBuilder create(String templatePath) {
            CreatePod command = new CreatePod();
            command.setTemplate(templatePath);
            command(command);
            return this;
        }

        /**
         * Gets pod by name.
         * @param name
         */
        public PodActionBuilder get(String name) {
            GetPod command = new GetPod();
            command.name(name);
            command(command);
            return this;
        }
    }

    /**
     * Pods action builder.
     */
    public class PodsActionBuilder extends BaseActionBuilder<PodsActionBuilder, PodList> {

        /**
         * List pods.
         */
        public PodsActionBuilder list() {
            ListPods command = new ListPods();
            command(command);
            return this;
        }

        /**
         * Watch pods.
         */
        public WatchActionBuilder<Pod> watch() {
            return new WatchActionBuilder<>(new WatchPods());
        }

    }

    /**
     * Service action builder.
     */
    public class ServiceActionBuilder extends BaseActionBuilder<ServiceActionBuilder, Service> {

        /**
         * Creates new pod.
         * @param pod
         */
        public ServiceActionBuilder create(Service pod) {
            CreateService command = new CreateService();
            command.setService(pod);
            command(command);
            return this;
        }

        /**
         * Create new pod from template.
         * @param template
         */
        public ServiceActionBuilder create(Resource template) {
            CreateService command = new CreateService();
            command.setTemplateResource(template);
            command(command);
            return this;
        }

        /**
         * Create new pod from template path.
         * @param templatePath
         */
        public ServiceActionBuilder create(String templatePath) {
            CreateService command = new CreateService();
            command.setTemplate(templatePath);
            command(command);
            return this;
        }

        /**
         * Gets pod by name.
         * @param name
         */
        public ServiceActionBuilder get(String name) {
            GetService command = new GetService();
            command.name(name);
            command(command);
            return this;
        }

    }

    /**
     * Services action builder.
     */
    public class ServicesActionBuilder extends BaseActionBuilder<ServiceActionBuilder, ServiceList> {

        /**
         * List services.
         */
        public ServicesActionBuilder list() {
            ListServices command = new ListServices();
            command(command);
            return this;
        }

        /**
         * Watch services.
         */
        public WatchActionBuilder<Service> watch() {
            return new WatchActionBuilder<>(new WatchServices());
        }
    }

    /**
     * Endpoints action builder.
     */
    public class EndpointsActionBuilder extends BaseActionBuilder<EndpointsActionBuilder, EndpointsList> {

        /**
         * List endpoints.
         */
        public EndpointsActionBuilder list() {
            ListEndpoints command = new ListEndpoints();
            command(command);
            return this;
        }
    }

    /**
     * Nodes action builder.
     */
    public class NodesActionBuilder extends BaseActionBuilder<NodesActionBuilder, NodeList> {

        /**
         * List nodes.
         */
        public NodesActionBuilder list() {
            ListNodes command = new ListNodes();
            command(command);
            return this;
        }

        /**
         * Watch nodes.
         */
        public WatchActionBuilder<Node> watch() {
            return new WatchActionBuilder<>(new WatchNodes());
        }
    }

    /**
     * Namespaces action builder.
     */
    public class NamespacesActionBuilder extends BaseActionBuilder<NamespacesActionBuilder, NamespaceList> {

        /**
         * List namespaces.
         */
        public NamespacesActionBuilder list() {
            ListNamespaces command = new ListNamespaces();
            command(command);
            return this;
        }

        /**
         * Watch namespaces.
         */
        public WatchActionBuilder<Namespace> watch() {
            return new WatchActionBuilder<>(new WatchNamespaces());
        }
    }

    /**
     * Events action builder.
     */
    public class EventsActionBuilder extends BaseActionBuilder<EventsActionBuilder, EventList> {

        /**
         * List endpoints.
         */
        public EventsActionBuilder list() {
            ListEvents command = new ListEvents();
            command(command);
            return this;
        }

    }

    /**
     * ReplicationControllers action builder.
     */
    public class ReplicationControllersActionBuilder extends BaseActionBuilder<ReplicationControllersActionBuilder, ReplicationControllerList> {

        /**
         * List replication controllers.
         */
        public ReplicationControllersActionBuilder list() {
            ListReplicationControllers command = new ListReplicationControllers();
            command(command);
            return this;
        }

        /**
         * Watch pods.
         */
        public WatchActionBuilder<ReplicationController> watch() {
            return new WatchActionBuilder<>(new WatchReplicationControllers());
        }
    }

    /**
     * Watch action builder.
     */
    public class WatchActionBuilder<R extends KubernetesResource> extends BaseActionBuilder<WatchActionBuilder<R>, R> {

        /**
         * Constructor using command.
         * @param command
         */
        WatchActionBuilder(KubernetesCommand<R> command) {
            command(command);
        }
    }
}
