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

package com.consol.citrus.docker.actions;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.docker.command.DockerCommand;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.jaxrs.DockerCmdExecFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Executes docker command with given docker client implementation. Possible command result is stored within command object.
 *
 * @author Christoph Deppisch
 * @since 2.3.1
 */
public class DockerExecuteAction extends AbstractTestAction {

    /** Docker client instance  */
    private DockerClient dockerClient;

    /** Docker client configuration */
    private DockerClientConfig dockerClientConfig;

    /** Docker command to execute */
    private List<DockerCommand> commands = new ArrayList<>();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DockerExecuteAction.class);

    /**
     * Default constructor.
     */
    public DockerExecuteAction() {
        setName("docker-execute");
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            for (DockerCommand command : commands) {
                log.info(String.format("Executing Docker command '%s", command.getName()));
                command.execute(getDockerClient(), context);
                log.info(String.format("Successfully executed Docker command '%s", command.getName()));
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Unable to perform docker command", e);
        }
    }

    /**
     * Creates new Docker client instance with configuration.
     * @return
     */
    private DockerClient createDockerClient() {
        return DockerClientImpl.getInstance(getDockerClientConfig())
                .withDockerCmdExecFactory(new DockerCmdExecFactoryImpl());
    }

    /**
     * Gets the docker commands to execute.
     * @return
     */
    public List<DockerCommand> getCommands() {
        return commands;
    }

    /**
     * Sets the docker commands to execute.
     * @param commands
     * @return
     */
    public DockerExecuteAction setCommands(List<DockerCommand> commands) {
        this.commands = commands;
        return this;
    }

    /**
     * Adds docker command to execute.
     * @param command
     * @return
     */
    public DockerExecuteAction addCommand(DockerCommand command) {
        this.commands.add(command);
        return this;
    }

    /**
     * Sets single docker command to execute.
     * @param command
     * @return
     */
    public DockerExecuteAction setCommand(DockerCommand command) {
        return addCommand(command);
    }

    /**
     * Gets the docker client.
     * @return
     */
    public DockerClient getDockerClient() {
        if (dockerClient == null) {
            dockerClient = createDockerClient();
        }

        return dockerClient;
    }

    /**
     * Sets the docker client.
     * @param dockerClient
     */
    public DockerExecuteAction setDockerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
        return this;
    }

    /**
     * Gets the docker client configuration.
     * @return
     */
    public DockerClientConfig getDockerClientConfig() {
        if (dockerClientConfig == null) {
            dockerClientConfig = DockerClientConfig.createDefaultConfigBuilder().build();
        }

        return dockerClientConfig;
    }

    public DockerExecuteAction setDockerClientConfig(DockerClientConfig dockerClientConfig) {
        this.dockerClientConfig = dockerClientConfig;
        return this;
    }
}
