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
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.docker.command.*;

/**
 * Action executes docker commands.
 * 
 * @author Christoph Deppisch
 * @since 2.4
 */
public class DockerActionBuilder extends AbstractTestActionBuilder<DockerExecuteAction> {

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
     * Use a info command.
     */
    public Info info() {
		Info command = new Info();
        action.setCommand(command);
        return command;
    }

	/**
	 * Adds a ping command.
	 */
	public Ping ping() {
		Ping command = new Ping();
		action.setCommand(command);
		return command;
	}

	/**
	 * Adds a version command.
	 */
	public Version version() {
		Version command = new Version();
		action.setCommand(command);
		return command;
	}

	/**
	 * Adds a create command.
	 */
	public ContainerCreate create(String imageId) {
		ContainerCreate command = new ContainerCreate();
		command.image(imageId);
		action.setCommand(command);
		return command;
	}

	/**
	 * Adds a start command.
	 */
	public ContainerStart start(String containerId) {
		ContainerStart command = new ContainerStart();
		command.container(containerId);
		action.setCommand(command);
		return command;
	}

	/**
	 * Adds a stop command.
	 */
	public ContainerStop stop(String containerId) {
		ContainerStop command = new ContainerStop();
		command.container(containerId);
		action.setCommand(command);
		return command;
	}

	/**
	 * Adds a wait command.
	 */
	public ContainerWait wait(String containerId) {
		ContainerWait command = new ContainerWait();
		command.container(containerId);
		action.setCommand(command);
		return command;
	}

	/**
	 * Adds a inspect container command.
	 */
	public ContainerInspect inspectContainer(String containerId) {
		ContainerInspect command = new ContainerInspect();
		command.container(containerId);
		action.setCommand(command);
		return command;
	}

	/**
	 * Adds a inspect container command.
	 */
	public ImageInspect inspectImage(String imageId) {
		ImageInspect command = new ImageInspect();
		command.image(imageId);
		action.setCommand(command);
		return command;
	}

	/**
	 * Adds a inspect container command.
	 */
	public ImageBuild buildImage() {
		ImageBuild command = new ImageBuild();
		action.setCommand(command);
		return command;
	}

	/**
	 * Adds expected command result.
	 * @param result
	 * @return
	 */
	public DockerActionBuilder result(String result) {
		action.setExpectedCommandResult(result);
		return this;
	}
}
