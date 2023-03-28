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
import org.citrusframework.docker.message.DockerMessageHeaders;
import com.github.dockerjava.api.command.WaitContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.WaitResponse;

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

    /**
     * Command builder.
     */
    public static final class Builder extends AbstractDockerCommandBuilder<WaitResponse, ContainerWait, Builder> {

        public Builder(DockerExecuteAction.Builder parent) {
            super(parent, new ContainerWait());
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
