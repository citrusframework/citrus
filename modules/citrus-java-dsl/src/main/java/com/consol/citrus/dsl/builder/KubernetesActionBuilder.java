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
     * Pods action builder.
     */
    public PodsActionBuilder pods() {
        return new PodsActionBuilder();
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
     * Base kubernetes action builder with namespace.
     */
    public class NamespacedActionBuilder<R extends KubernetesResource> extends BaseActionBuilder<NamespacedActionBuilder<R>, R> {
        /**
         * Constructor using command.
         * @param command
         */
        NamespacedActionBuilder(KubernetesCommand<R> command) {
            super(command);
        }

        /**
         * Sets the namespace parameter.
         * @param key
         * @return
         */
        public NamespacedActionBuilder<R> namespace(String key) {
            command.namespace(key);
            return this;
        }
    }

    /**
     * Base kubernetes action builder with name option.
     */
    public class NamedActionBuilder<R extends KubernetesResource> extends BaseActionBuilder<NamedActionBuilder<R>, R> {
        /**
         * Constructor using command.
         * @param command
         */
        NamedActionBuilder(KubernetesCommand<R> command) {
            super(command);
        }

        /**
         * Sets the name parameter.
         * @param key
         * @return
         */
        public NamedActionBuilder<R> name(String key) {
            command.name(key);
            return this;
        }

        /**
         * Sets the namespace parameter.
         * @param key
         * @return
         */
        public NamedActionBuilder<R> namespace(String key) {
            command.namespace(key);
            return this;
        }
    }

    /**
     * Base kubernetes action builder.
     */
    public class BaseActionBuilder<T extends BaseActionBuilder, R extends KubernetesResource> extends AbstractTestActionBuilder<KubernetesExecuteAction> {

        /** Kubernetes command */
        protected final KubernetesCommand<R> command;

        /** Self reference for fluent API */
        protected T self;

        /**
         * Constructor using command.
         * @param command
         */
        BaseActionBuilder(KubernetesCommand<R> command) {
            super(KubernetesActionBuilder.this.action);
            self = (T) this;
            this.command = command;
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
         * Adds JsonPath command result validation.
         * @param path
         * @param value
         * @return
         */
        public T validate(String path, Object value) {
            action.getCommandResultExpressions().put(path, value);
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
            KubernetesActionBuilder.this.command(command);
            return self;
        }
    }

    /**
     * Pods action builder.
     */
    public class PodsActionBuilder {
        /**
         * List pods.
         */
        public NamespacedActionBuilder<PodList> list() {
            ListPods command = new ListPods();
            return new NamespacedActionBuilder<>(command);
        }

        /**
         * Creates new pod.
         * @param pod
         */
        public NamedActionBuilder<Pod> create(Pod pod) {
            CreatePod command = new CreatePod();
            command.setPod(pod);
            return new NamedActionBuilder<>(command);
        }

        /**
         * Create new pod from template.
         * @param template
         */
        public NamedActionBuilder<Pod> create(Resource template) {
            CreatePod command = new CreatePod();
            command.setTemplateResource(template);
            return new NamedActionBuilder<>(command);
        }

        /**
         * Create new pod from template path.
         * @param templatePath
         */
        public NamedActionBuilder<Pod> create(String templatePath) {
            CreatePod command = new CreatePod();
            command.setTemplate(templatePath);
            return new NamedActionBuilder<>(command);
        }

        /**
         * Gets pod by name.
         * @param name
         */
        public NamedActionBuilder<Pod> get(String name) {
            GetPod command = new GetPod();
            command.name(name);
            return new NamedActionBuilder<>(command);
        }

        /**
         * Deletes pod by name.
         * @param name
         */
        public NamedActionBuilder<DeleteResult> delete(String name) {
            DeletePod command = new DeletePod();
            command.name(name);
            return new NamedActionBuilder<>(command);
        }

        /**
         * Watch pods.
         */
        public NamedActionBuilder<Pod> watch() {
            return new NamedActionBuilder<>(new WatchPods());
        }
    }

    /**
     * Services action builder.
     */
    public class ServicesActionBuilder {
        /**
         * List services.
         */
        public NamespacedActionBuilder<ServiceList> list() {
            return new NamespacedActionBuilder<>(new ListServices());
        }

        /**
         * Creates new service.
         * @param pod
         */
        public NamedActionBuilder<Service> create(Service pod) {
            CreateService command = new CreateService();
            command.setService(pod);
            return new NamedActionBuilder<>(command);
        }

        /**
         * Create new service from template.
         * @param template
         */
        public NamedActionBuilder<Service> create(Resource template) {
            CreateService command = new CreateService();
            command.setTemplateResource(template);
            return new NamedActionBuilder<>(command);
        }

        /**
         * Create new service from template path.
         * @param templatePath
         */
        public NamedActionBuilder<Service> create(String templatePath) {
            CreateService command = new CreateService();
            command.setTemplate(templatePath);
            return new NamedActionBuilder<>(command);
        }

        /**
         * Gets service by name.
         * @param name
         */
        public NamedActionBuilder<Service> get(String name) {
            GetService command = new GetService();
            command.name(name);
            return new NamedActionBuilder<>(command);
        }

        /**
         * Deletes service by name.
         * @param name
         */
        public NamedActionBuilder<DeleteResult> delete(String name) {
            DeleteService command = new DeleteService();
            command.name(name);
            return new NamedActionBuilder<>(command);
        }

        /**
         * Watch services.
         */
        public NamedActionBuilder<Service> watch() {
            return new NamedActionBuilder<>(new WatchServices());
        }
    }

    /**
     * Endpoints action builder.
     */
    public class EndpointsActionBuilder {
        /**
         * List endpoints.
         */
        public NamespacedActionBuilder<EndpointsList> list() {
            return new NamespacedActionBuilder<>(new ListEndpoints());
        }
    }

    /**
     * Nodes action builder.
     */
    public class NodesActionBuilder {
        /**
         * List nodes.
         */
        public BaseActionBuilder<BaseActionBuilder, NodeList> list() {
            return new BaseActionBuilder<>(new ListNodes());
        }

        /**
         * Watch nodes.
         */
        public BaseActionBuilder<BaseActionBuilder, Node> watch() {
            return new BaseActionBuilder<>(new WatchNodes());
        }
    }

    /**
     * Namespaces action builder.
     */
    public class NamespacesActionBuilder {
        /**
         * List namespaces.
         */
        public BaseActionBuilder<BaseActionBuilder, NamespaceList> list() {
            return new BaseActionBuilder<>(new ListNamespaces());
        }

        /**
         * Watch namespaces.
         */
        public BaseActionBuilder<BaseActionBuilder, Namespace> watch() {
            return new BaseActionBuilder<>(new WatchNamespaces());
        }
    }

    /**
     * Events action builder.
     */
    public class EventsActionBuilder {
        /**
         * List endpoints.
         */
        public NamespacedActionBuilder<EventList> list() {
            return new NamespacedActionBuilder<>(new ListEvents());
        }
    }

    /**
     * ReplicationControllers action builder.
     */
    public class ReplicationControllersActionBuilder {
        /**
         * List replication controllers.
         */
        public NamespacedActionBuilder<ReplicationControllerList> list() {
            return new NamespacedActionBuilder<>(new ListReplicationControllers());
        }

        /**
         * Watch pods.
         */
        public NamespacedActionBuilder<ReplicationController> watch() {
            return new NamespacedActionBuilder<>(new WatchReplicationControllers());
        }
    }
}
