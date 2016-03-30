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

package com.consol.citrus.docker.command;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.docker.client.DockerClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dockerjava.api.command.WaitContainerCmd;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class ContainerWait extends AbstractDockerCommand<ContainerWait.ExitCode> {

    /**
     * Default constructor initializing the command name.
     */
    public ContainerWait() {
        super("docker:wait");
    }

    @Override
    public void execute(DockerClient dockerClient, TestContext context) {
        WaitContainerCmd command = dockerClient.getEndpointConfiguration().getDockerClient().waitContainerCmd(getContainerId(context));
        Integer exitCode = command.exec();

        setCommandResult(new ExitCode(getContainerId(context), exitCode));
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class ExitCode {
        @JsonProperty
        private String containerId;

        @JsonProperty
        private Integer exitCode;

        public ExitCode(String containerId, Integer exitCode) {
            this.containerId = containerId;
            this.exitCode = exitCode;
        }

        public void setContainerId(String containerId) {
            this.containerId = containerId;
        }

        public String getContainerId() {
            return containerId;
        }

        public void setExitCode(Integer exitCode) {
            this.exitCode = exitCode;
        }

        public Integer getExitCode() {
            return exitCode;
        }
    }

    /**
     * Sets the container id parameter.
     * @param id
     * @return
     */
    public ContainerWait container(String id) {
        getParameters().put(CONTAINER_ID, id);
        return this;
    }
}
