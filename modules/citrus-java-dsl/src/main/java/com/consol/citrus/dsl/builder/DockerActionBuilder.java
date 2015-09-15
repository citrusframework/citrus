/*
 * Copyright 2006-2015 the original author or authors.
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

import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.docker.command.*;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Action executes docker commands.
 * 
 * @author Christoph Deppisch
 * @since 2.3.1
 */
public class DockerActionBuilder extends AbstractTestActionBuilder<DockerExecuteAction> {

	/** Current command parameters */
	private Map<String, String> commandParameter = new HashMap<>();

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public DockerActionBuilder(DockerExecuteAction action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public DockerActionBuilder() {
		super(new DockerExecuteAction());
	}

	/**
	 * Use a custom docker client.
	 */
	public DockerActionBuilder client(DockerClient dockerClient) {
		action.setDockerClient(dockerClient);
		return this;
	}

	/**
	 * Use a custom docker client.
	 */
	public DockerActionBuilder configuration(DockerClientConfig dockerClientConfig) {
		action.setDockerClientConfig(dockerClientConfig);
		return this;
	}

	/**
     * Use a info command.
     */
    public DockerActionBuilder info() {
        action.addCommand(new Info());
        return this;
    }

	/**
	 * Adds a ping command.
	 */
	public DockerActionBuilder ping() {
		action.addCommand(new Ping());
		return this;
	}

	/**
	 * Adds a version command.
	 */
	public DockerActionBuilder version() {
		action.addCommand(new Version());
		return this;
	}

	/**
	 * Adds a create command.
	 */
	public DockerActionBuilder create(String imageId) {
		ContainerCreate command = new ContainerCreate();
		command.getParameters().put("image", imageId);

		this.commandParameter = command.getParameters();
		action.addCommand(command);
		return this;
	}

	/**
	 * Adds a start command.
	 */
	public DockerActionBuilder start(String containerId) {
		ContainerStart command = new ContainerStart();
		command.getParameters().put("container", containerId);

		this.commandParameter = command.getParameters();
		action.addCommand(command);
		return this;
	}

	/**
	 * Adds a stop command.
	 */
	public DockerActionBuilder stop(String containerId) {
		ContainerStop command = new ContainerStop();
		command.getParameters().put("container", containerId);

		this.commandParameter = command.getParameters();
		action.addCommand(command);
		return this;
	}

	/**
	 * Adds a inspect container command.
	 */
	public DockerActionBuilder inspectContainer(String containerId) {
		ContainerInspect command = new ContainerInspect();
		command.getParameters().put("container", containerId);

		this.commandParameter = command.getParameters();
		action.addCommand(command);
		return this;
	}

	/**
	 * Adds a inspect container command.
	 */
	public DockerActionBuilder inspectImage(String imageId) {
		ImageInspect command = new ImageInspect();
		command.getParameters().put("image", imageId);

		this.commandParameter = command.getParameters();
		action.addCommand(command);
		return this;
	}

	/**
	 * Adds command parameter to current command.
	 * @param name
	 * @param value
	 * @return
	 */
	public DockerActionBuilder withParam(String name, String value) {
		commandParameter.put(name, value);
		return this;
	}
}
