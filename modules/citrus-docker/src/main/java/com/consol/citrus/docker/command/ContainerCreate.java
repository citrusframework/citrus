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
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.3.1
 */
public class ContainerCreate extends AbstractDockerCommand<Boolean> {

    public static final String DELIMITER = ";";

    /**
     * Default constructor initializing the command name.
     */
    public ContainerCreate() {
        super("docker:container:create");

        setCommandResult(false);
    }

    @Override
    public void execute(DockerClient dockerClient, TestContext context) {
        CreateContainerCmd command = dockerClient.getDockerClient().createContainerCmd(getImageId(context));

        if (hasParameter("name")) {
            command.withName(getParameter("name", context));
        }

        if (hasParameter("attach-stderr")) {
            command.withAttachStderr(Boolean.valueOf(getParameter("attach-stderr", context)));
        }

        if (hasParameter("attach-stdin")) {
            command.withAttachStdin(Boolean.valueOf(getParameter("attach-stdin", context)));
        }

        if (hasParameter("attach-stdout")) {
            command.withAttachStdout(Boolean.valueOf(getParameter("attach-stdout", context)));
        }

        if (hasParameter("capability-add")) {
            command.withCapAdd(getCapabilities("capability-add", context));
        }

        if (hasParameter("capability-drop")) {
            command.withCapDrop(getCapabilities("capability-drop", context));
        }

        if (hasParameter("domain-name")) {
            command.withDomainName(getParameter("domain-name", context));
        }

        if (hasParameter("cmd")) {
            command.withCmd(StringUtils.delimitedListToStringArray(getParameter("cmd", context), DELIMITER));
        }

        if (hasParameter("env")) {
            command.withEnv(StringUtils.delimitedListToStringArray(getParameter("env", context), DELIMITER));
        }

        if (hasParameter("entrypoint")) {
            command.withEntrypoint(getParameter("entrypoint", context));
        }

        if (hasParameter("hostname")) {
            command.withHostName(getParameter("hostname", context));
        }

        if (hasParameter("port-specs")) {
            command.withPortSpecs(StringUtils.delimitedListToStringArray(getParameter("port-specs", context), DELIMITER));
        }

        if (hasParameter("exposed-ports")) {
            command.withExposedPorts(getExposedPorts(context));
        }

        if (hasParameter("volumes")) {
            command.withVolumes(getVolumes(context));
        }

        if (hasParameter("working-dir")) {
            command.withWorkingDir(getParameter("working-dir", context));
        }

        CreateContainerResponse response = command.exec();
        context.setVariable(DockerMessageHeaders.CONTAINER_ID, response.getId());

        if (!hasParameter("name")) {
            InspectContainerCmd inspect = dockerClient.getDockerClient().inspectContainerCmd(response.getId());
            InspectContainerResponse inspectResponse = inspect.exec();
            context.setVariable(DockerMessageHeaders.CONTAINER_NAME, inspectResponse.getName().substring(1));
        }

        setCommandResult(true);
    }

    /**
     * Gets the volume specs from comma delimited string.
     * @return
     */
    private Volume[] getVolumes(TestContext context) {
        String[] volumes = StringUtils.commaDelimitedListToStringArray(getParameter("volumes", context));
        Volume[] volumeSpecs = new Volume[volumes.length];

        for (int i = 0; i < volumes.length; i++) {
            volumeSpecs[i] = new Volume(volumes[i]);
        }

        return volumeSpecs;
    }

    /**
     * Gets the capabilities added.
     * @return
     */
    private Capability[] getCapabilities(String addDrop, TestContext context) {
        String[] capabilities = StringUtils.commaDelimitedListToStringArray(getParameter(addDrop, context));
        Capability[] capAdd = new Capability[capabilities.length];

        for (int i = 0; i < capabilities.length; i++) {
            capAdd[i] = Capability.valueOf(capabilities[i]);
        }

        return capAdd;
    }

    /**
     * Construct set of exposed ports from comma delimited list of ports.
     * @return
     */
    private ExposedPort[] getExposedPorts(TestContext context) {
        String[] ports = StringUtils.commaDelimitedListToStringArray(getParameter("exposed-ports", context));
        ExposedPort[] exposedPorts = new ExposedPort[ports.length];

        for (int i = 0; i < ports.length; i++) {
            if (ports[i].startsWith("udp:")) {
                exposedPorts[i] = ExposedPort.udp(Integer.valueOf(ports[i].substring("udp:".length())));
            } else if (ports[i].startsWith("tcp:")) {
                exposedPorts[i] = ExposedPort.tcp(Integer.valueOf(ports[i].substring("tcp:".length())));
            } else {
                exposedPorts[i] = ExposedPort.tcp(Integer.valueOf(ports[i]));
            }
        }

        return exposedPorts;
    }
}
