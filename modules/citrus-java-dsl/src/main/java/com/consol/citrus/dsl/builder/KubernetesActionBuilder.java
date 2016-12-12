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
     * Use a list pods command.
     */
    public ListPods listPods() {
		ListPods command = new ListPods();
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
     * Use a list namespaces command.
     */
    public ListNamespaces listNamespaces() {
		ListNamespaces command = new ListNamespaces();
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
