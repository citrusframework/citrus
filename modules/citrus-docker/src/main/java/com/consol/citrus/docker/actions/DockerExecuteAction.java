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
import com.consol.citrus.docker.DockerCommand;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.jaxrs.DockerCmdExecFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.3.1
 */
public class DockerExecuteAction extends AbstractTestAction {

    /** Docker client instance  */
    private DockerClient dockerClient;

    /** Docker client configuration */
    private DockerClientConfig dockerClientConfig;

    /** Docker image id */
    private String imageId;

    /** Docker container id */
    private String containerId;

    /** Docker command to execute */
    private DockerCommand command;

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
        getDockerClient();

        try {
            switch (command) {
                case INFO:
                    info(context);
                    break;
                case PING:
                    ping(context);
                    break;
                case VERSION:
                    version(context);
                    break;
                case IMAGE_PULL:
                    pull(context);
                    break;
                case IMAGE_BUILD:
                    buildImage(context);
                    break;
                case IMAGE_REMOVE:
                    removeImage(context);
                    break;
                case IMAGE_INSPECT:
                    inspectImage(context);
                    break;
                case CONTAINER_CREATE:
                    createContainer(context);
                    break;
                case CONTAINER_REMOVE:
                    removeContainer(context);
                    break;
                case CONTAINER_START:
                    startContainer(context);
                    break;
                case CONTAINER_STOP:
                    stopContainer(context);
                    break;
                case CONTAINER_INSPECT:
                    inspectContainer(context);
                    break;
                default:
                    info(context);
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Unable to perform docker command", e);
        }
    }

    /**
     * Executes info command and prints result to console.
     */
    private void info(TestContext context) {
        InfoCmd command = dockerClient.infoCmd();
        log.info(command.exec().toString());
    }

    /**
     * Executes ping command and prints result to console.
     */
    private void ping(TestContext context) {
        PingCmd command = dockerClient.pingCmd();
        command.exec();
    }

    /**
     * Executes version command and prints result to console.
     */
    private void version(TestContext context) {
        VersionCmd command = dockerClient.versionCmd();
        log.info(command.exec().toString());
    }

    /**
     * Executes info command and prints result to console.
     */
    private void pull(TestContext context) {
        PullImageCmd command = dockerClient.pullImageCmd(context.replaceDynamicContentInString(imageId));
        PullImageResultCallback imageResult = new PullImageResultCallback();
        command.exec(imageResult);

        imageResult.awaitSuccess();
    }

    /**
     * Build image.
     */
    private void buildImage(TestContext context) {
        BuildImageCmd command = dockerClient.buildImageCmd();
        BuildImageResultCallback imageResult = new BuildImageResultCallback();
        command.exec(imageResult);
        String imageId = imageResult.awaitImageId();

        context.setVariable("DOCKER_IMAGE_ID", imageId);
    }

    /**
     * Build image.
     */
    private void removeImage(TestContext context) {
        RemoveImageCmd command = dockerClient.removeImageCmd(context.replaceDynamicContentInString(imageId));
        command.exec();
    }

    /**
     * Build image.
     */
    private void inspectImage(TestContext context) {
        InspectImageCmd command = dockerClient.inspectImageCmd(context.replaceDynamicContentInString(imageId));
        InspectImageResponse response = command.exec();
        log.info(response.toString());
    }

    /**
     * Create container from image.
     */
    private void createContainer(TestContext context) {
        CreateContainerCmd command = dockerClient.createContainerCmd(context.replaceDynamicContentInString(imageId));
        CreateContainerResponse response = command.exec();

        InspectContainerCmd inspect = dockerClient.inspectContainerCmd(response.getId());
        InspectContainerResponse inspectResponse = inspect.exec();

        context.setVariable("DOCKER_CONTAINER_ID", response.getId());
        context.setVariable("DOCKER_CONTAINER_NAME", inspectResponse.getName().substring(1));
    }

    /**
     * Create container from image.
     */
    private void removeContainer(TestContext context) {
        RemoveContainerCmd command = dockerClient.removeContainerCmd(context.replaceDynamicContentInString(containerId));
        command.exec();
    }

    /**
     * Starts container.
     */
    private void startContainer(TestContext context) {
        StartContainerCmd command = dockerClient.startContainerCmd(context.replaceDynamicContentInString(containerId));
        command.exec();
    }

    /**
     * Stops container.
     */
    private void stopContainer(TestContext context) {
        StopContainerCmd command = dockerClient.stopContainerCmd(context.replaceDynamicContentInString(containerId));
        command.exec();
    }

    /**
     * Executes inspect command and prints result to console.
     */
    private void inspectContainer(TestContext context) {
        InspectContainerCmd command = dockerClient.inspectContainerCmd(context.replaceDynamicContentInString(containerId));
        InspectContainerResponse response = command.exec();
        log.info(response.toString());
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
     * Gets the docker image id.
     * @return
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * Sets the docker image id.
     * @param imageId
     * @return
     */
    public DockerExecuteAction setImageId(String imageId) {
        this.imageId = imageId;
        return this;
    }

    /**
     * Gets the docker container id.
     * @return
     */
    public String getContainerId() {
        return containerId;
    }

    /**
     * Sets the docker container id.
     * @param containerId
     * @return
     */
    public DockerExecuteAction setContainerId(String containerId) {
        this.containerId = containerId;
        return this;
    }

    /**
     * Gets the docker command to execute.
     * @return
     */
    public DockerCommand getCommand() {
        return command;
    }

    /**
     * Sets the docker command to execute.
     * @param command
     * @return
     */
    public DockerExecuteAction setCommand(DockerCommand command) {
        this.command = command;
        return this;
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
