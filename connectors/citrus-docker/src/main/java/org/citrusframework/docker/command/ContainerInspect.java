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

package org.citrusframework.docker.command;

import org.citrusframework.context.TestContext;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class ContainerInspect extends AbstractDockerCommand<InspectContainerResponse> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ContainerInspect.class);

    /**
     * Default constructor initializing the command name.
     */
    public ContainerInspect() {
        super("docker:container:inspect");
    }

    @Override
    public void execute(DockerClient dockerClient, TestContext context) {
        InspectContainerCmd command = dockerClient.getEndpointConfiguration().getDockerClient().inspectContainerCmd(getContainerId(context));
        InspectContainerResponse response = command.exec();

        setCommandResult(response);

        logger.debug(response.toString());
    }

    /**
     * Sets the container id parameter.
     * @param id
     * @return
     */
    public ContainerInspect container(String id) {
        getParameters().put(CONTAINER_ID, id);
        return this;
    }

    /**
     * Command builder.
     */
    public static final class Builder extends AbstractDockerCommandBuilder<InspectContainerResponse, ContainerInspect, Builder> {

        public Builder(DockerExecuteAction.Builder parent) {
            super(parent, new ContainerInspect());
        }

        /**
         * Sets the container id parameter.
         * @param id
         * @return
         */
        public Builder container(String id) {
            command.container(id);
            return this;
        }
    }
}
