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
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
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
    public Info info() {
		Info command = new Info();
        action.setCommand(command);
        return command;
    }

    /**
     * Create new pod.
     */
    public CreatePod createPod(Pod pod) {
		CreatePod command = new CreatePod();
		command.setPod(pod);
        action.setCommand(command);
        return command;
    }

    /**
     * Create new pod from template.
     */
    public CreatePod createPod(Resource template) {
		CreatePod command = new CreatePod();
		command.setTemplateResource(template);
        action.setCommand(command);
        return command;
    }

    /**
     * Gets pod by name.
     */
    public GetPod getPod(String name) {
		GetPod command = new GetPod();
		command.name(name);
        action.setCommand(command);
        return command;
    }

    /**
     * Use a list pods command.
     */
    public ListPods listPods() {
        ListPods command = new ListPods();
        action.setCommand(command);
        return command;
    }

    /**
     * Use a watch pods command.
     */
    public WatchPods watchPods() {
		WatchPods command = new WatchPods();
        action.setCommand(command);
        return command;
    }

    /**
     * Create new service.
     */
    public CreateService createService(Service service) {
        CreateService command = new CreateService();
        command.setService(service);
        action.setCommand(command);
        return command;
    }

    /**
     * Create new service from template.
     */
    public CreateService createService(Resource template) {
        CreateService command = new CreateService();
        command.setTemplateResource(template);
        action.setCommand(command);
        return command;
    }

    /**
     * Gets service by name.
     */
    public GetService getService(String name) {
        GetService command = new GetService();
        command.name(name);
        action.setCommand(command);
        return command;
    }

    /**
     * Use a list services command.
     */
    public ListServices listServices() {
		ListServices command = new ListServices();
        action.setCommand(command);
        return command;
    }

    /**
     * Use a watch services command.
     */
    public WatchServices watchServices() {
		WatchServices command = new WatchServices();
        action.setCommand(command);
        return command;
    }

    /**
     * Use a list replication controllers command.
     */
    public ListReplicationControllers listReplicationControllers() {
		ListReplicationControllers command = new ListReplicationControllers();
        action.setCommand(command);
        return command;
    }

    /**
     * Use a watch replication controllers command.
     */
    public WatchReplicationControllers watchReplicationControllers() {
		WatchReplicationControllers command = new WatchReplicationControllers();
        action.setCommand(command);
        return command;
    }

    /**
     * Use a list endpoints command.
     */
    public ListEndpoints listEndpoints() {
		ListEndpoints command = new ListEndpoints();
        action.setCommand(command);
        return command;
    }

    /**
     * Use a list events command.
     */
    public ListEvents listEvents() {
		ListEvents command = new ListEvents();
        action.setCommand(command);
        return command;
    }

    /**
     * Use a list nodes command.
     */
    public ListNodes listNodes() {
		ListNodes command = new ListNodes();
        action.setCommand(command);
        return command;
    }

    /**
     * Use a watch nodes command.
     */
    public WatchNodes watchNodes() {
		WatchNodes command = new WatchNodes();
        action.setCommand(command);
        return command;
    }

    /**
     * Use a list namespaces command.
     */
    public ListNamespaces listNamespaces() {
		ListNamespaces command = new ListNamespaces();
        action.setCommand(command);
        return command;
    }

    /**
     * Use a watch namespaces command.
     */
    public WatchNamespaces watchNamespaces() {
		WatchNamespaces command = new WatchNamespaces();
        action.setCommand(command);
        return command;
    }

	/**
	 * Adds expected command result.
	 * @param result
	 * @return
	 */
	public KubernetesActionBuilder result(String result) {
		action.setCommandResult(result);
		return this;
	}
}
