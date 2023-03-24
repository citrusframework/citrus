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
import org.citrusframework.docker.client.DockerClient;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.model.ResponseItem;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class ContainerRemove extends AbstractDockerCommand<ResponseItem> {

    /**
     * Default constructor initializing the command name.
     */
    public ContainerRemove() {
        super("docker:container:remove");
        setCommandResult(new ResponseItem());
    }

    @Override
    public void execute(DockerClient dockerClient, TestContext context) {
        RemoveContainerCmd command = dockerClient.getEndpointConfiguration().getDockerClient().removeContainerCmd(getContainerId(context));

        if (hasParameter("force")) {
            command.withForce(Boolean.valueOf(getParameter("force", context)));
        }

        command.exec();

        setCommandResult(success());
    }

    /**
     * Sets the container id parameter.
     * @param id
     * @return
     */
    public ContainerRemove container(String id) {
        getParameters().put(CONTAINER_ID, id);
        return this;
    }

    /**
     * Sets the force parameter.
     * @param force
     * @return
     */
    public ContainerRemove force(Boolean force) {
        getParameters().put("force", force);
        return this;
    }
}
