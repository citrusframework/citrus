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
import com.consol.citrus.docker.message.DockerMessageHeaders;
import com.github.dockerjava.api.command.WaitContainerCmd;
import com.github.dockerjava.api.model.WaitResponse;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class ContainerWait extends AbstractDockerCommand<WaitResponse> {

    /**
     * Default constructor initializing the command name.
     */
    public ContainerWait() {
        super("docker:wait");
    }

    @Override
    public void execute(DockerClient dockerClient, TestContext context) {
        WaitContainerCmd command = dockerClient.getEndpointConfiguration().getDockerClient().waitContainerCmd(getContainerId(context));

        WaitContainerResultCallback waitResult = new WaitContainerResultCallback() {
            @Override
            public void onNext(WaitResponse waitResponse) {
                super.onNext(waitResponse);
                setCommandResult(waitResponse);
            }
        };

        command.exec(waitResult);

        Integer statusCode = waitResult.awaitStatusCode();
        context.setVariable(DockerMessageHeaders.DOCKER_PREFIX + "statusCode", statusCode);
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
