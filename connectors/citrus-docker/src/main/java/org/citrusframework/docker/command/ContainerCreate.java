/*
 * Copyright the original author or authors.
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

import java.util.stream.Stream;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import org.citrusframework.actions.docker.DockerContainerCreateActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.message.DockerMessageHeaders;

import static com.github.dockerjava.api.model.ExposedPort.tcp;
import static com.github.dockerjava.api.model.ExposedPort.udp;
import static java.lang.Integer.parseInt;

/**
 * @since 2.4
 */
public class ContainerCreate extends AbstractDockerCommand<CreateContainerResponse> {

    public static final String DELIMITER = ";";

    /**
     * Default constructor initializing the command name.
     */
    public ContainerCreate() {
        super("docker:container:create");
    }

    @Override
    public void execute(DockerClient dockerClient, TestContext testContext) {
        try (var command = dockerClient.getEndpointConfiguration().getDockerClient().createContainerCmd(getImageId(testContext))) {
            executeDockerCommand(dockerClient, command, testContext);
        }
    }

    private void executeDockerCommand(DockerClient dockerClient, CreateContainerCmd command, TestContext testContext) {
        if (hasParameter("name")) {
            command.withName(getParameter("name", testContext));
        }

        if (hasParameter("attach-stderr")) {
            command.withAttachStderr(Boolean.valueOf(getParameter("attach-stderr", testContext)));
        }

        if (hasParameter("attach-stdin")) {
            command.withAttachStdin(Boolean.valueOf(getParameter("attach-stdin", testContext)));
        }

        if (hasParameter("attach-stdout")) {
            command.withAttachStdout(Boolean.valueOf(getParameter("attach-stdout", testContext)));
        }

        if (hasParameter("capability-add")) {
            if (getParameters().get("capability-add") instanceof Capability[]) {
                command.withCapAdd((Capability[]) getParameters().get("capability-add"));
            } else {
                command.withCapAdd(getCapabilities("capability-add", testContext));
            }
        }

        if (hasParameter("capability-drop")) {
            if (getParameters().get("capability-drop") instanceof Capability[]) {
                command.withCapAdd((Capability[]) getParameters().get("capability-drop"));
            } else {
                command.withCapDrop(getCapabilities("capability-drop", testContext));
            }
        }

        if (hasParameter("domain-name")) {
            command.withDomainName(getParameter("domain-name", testContext));
        }

        if (hasParameter("cmd")) {
            if (getParameters().get("cmd") instanceof String[]
                    || getParameters().get("cmd") instanceof Capability[]) {
                command.withCmd((String[]) getParameters().get("cmd"));
            } else {
                command.withCmd(getParameter("cmd", testContext).split(DELIMITER));
            }
        }

        if (hasParameter("env")) {
            if (getParameters().get("env") instanceof String[]
                    || getParameters().get("env") instanceof Capability[]) {
                command.withEnv((String[]) getParameters().get("env"));
            } else {
                command.withEnv(getParameter("env", testContext).split(DELIMITER));
            }
        }

        if (hasParameter("entrypoint")) {
            command.withEntrypoint(getParameter("entrypoint", testContext));
        }

        if (hasParameter("hostname")) {
            command.withHostName(getParameter("hostname", testContext));
        }

        if (hasParameter("port-specs")) {
            if (getParameters().get("port-specs") instanceof String[]
                    || getParameters().get("port-specs") instanceof Capability[]) {
                command.withPortSpecs((String[]) getParameters().get("port-specs"));
            } else {
                command.withPortSpecs(getParameter("port-specs", testContext).split(DELIMITER));
            }
        }

        ExposedPort[] exposedPorts = {};
        if (hasParameter("exposed-ports")) {
            if (getParameters().get("exposed-ports") instanceof ExposedPort[]) {
                exposedPorts = (ExposedPort[]) getParameters().get("exposed-ports");
            } else {
                exposedPorts = getExposedPorts(getParameter("exposed-ports", testContext), testContext);
            }
            command.withExposedPorts(exposedPorts);
        }

        if (hasParameter("port-bindings")) {
            if (getParameters().get("port-bindings") instanceof Ports) {
                command.withPortBindings((Ports) getParameters().get("port-bindings"));
            } if (getParameters().get("port-bindings") instanceof PortBinding[]) {
                command.withPortBindings((PortBinding[]) getParameters().get("port-bindings"));
            } else {
                command.withPortBindings(getPortBindings(getParameter("port-bindings", testContext), exposedPorts, testContext));
            }
        } else if (exposedPorts.length > 0) {
            command.withPortBindings(getPortBindings("", exposedPorts, testContext));
        }

        if (hasParameter("volumes")) {
            if (getParameters().get("volumes") instanceof Volume[]
                    || getParameters().get("volumes") instanceof ExposedPort[]) {
                command.withVolumes((Volume[]) getParameters().get("volumes"));
            } else {
                command.withVolumes(getVolumes(testContext));
            }
        }

        if (hasParameter("working-dir")) {
            command.withWorkingDir(getParameter("working-dir", testContext));
        }

        CreateContainerResponse response = command.exec();
        testContext.setVariable(DockerMessageHeaders.CONTAINER_ID, response.getId());
        setCommandResult(response);

        if (!hasParameter("name")) {
            try (var inspect = dockerClient.getEndpointConfiguration().getDockerClient().inspectContainerCmd(response.getId())) {
                InspectContainerResponse inspectResponse = inspect.exec();
                testContext.setVariable(DockerMessageHeaders.CONTAINER_NAME, inspectResponse.getName().substring(1));
            }
        }
    }

    /**
     * Gets the volume specs from comma-delimited string.
     */
    private Volume[] getVolumes(TestContext context) {
        String[] volumes = getParameter("volumes", context).split(",");
        Volume[] volumeSpecs = new Volume[volumes.length];

        for (int i = 0; i < volumes.length; i++) {
            volumeSpecs[i] = new Volume(volumes[i]);
        }

        return volumeSpecs;
    }

    /**
     * Gets the capabilities added.
     */
    private Capability[] getCapabilities(String addDrop, TestContext context) {
        String[] capabilities = getParameter(addDrop, context).split(",");
        Capability[] capAdd = new Capability[capabilities.length];

        for (int i = 0; i < capabilities.length; i++) {
            capAdd[i] = Capability.valueOf(capabilities[i]);
        }

        return capAdd;
    }

    /**
     * Construct set of exposed ports from comma-delimited list of ports.
     */
    private ExposedPort[] getExposedPorts(String portSpecs, TestContext context) {
        String[] ports = portSpecs.split(",");
        ExposedPort[] exposedPorts = new ExposedPort[ports.length];

        for (int i = 0; i < ports.length; i++) {
            String portSpec = context.replaceDynamicContentInString(ports[i]);

            if (portSpec.startsWith("udp:")) {
                exposedPorts[i] = udp(parseInt(portSpec.substring("udp:".length())));
            } else if (portSpec.startsWith("tcp:")) {
                exposedPorts[i] = tcp(parseInt(portSpec.substring("tcp:".length())));
            } else {
                exposedPorts[i] = tcp(parseInt(portSpec));
            }
        }

        return exposedPorts;
    }

    /**
     * Construct set of port bindings from comma-delimited list of ports.
     */
    private Ports getPortBindings(String portSpecs, ExposedPort[] exposedPorts, TestContext context) {
        String[] ports = portSpecs.split(",");
        Ports portsBindings = new Ports();

        for (String portSpec : ports) {
            String[] binding = context.replaceDynamicContentInString(portSpec).split(":");
            if (binding.length == 2) {
                Integer hostPort = Integer.valueOf(binding[0]);
                Integer port = Integer.valueOf(binding[1]);

                portsBindings.bind(Stream.of(exposedPorts).filter(exposed -> port.equals(exposed.getPort())).findAny().orElseGet(() -> tcp(port)), Ports.Binding.bindPort(hostPort));
            }
        }

        Stream.of(exposedPorts).filter(exposed -> !portsBindings.getBindings().keySet().contains(exposed)).forEach(exposed -> portsBindings.bind(exposed, Ports.Binding.empty()));

        return portsBindings;
    }

    /**
     * Sets the image id parameter.
     */
    public ContainerCreate image(String id) {
        getParameters().put(IMAGE_ID, id);
        return this;
    }

    /**
     * Sets the image name parameter.
     */
    public ContainerCreate name(String name) {
        getParameters().put("name", name);
        return this;
    }

    /**
     * Sets the attach-stderr parameter.
     */
    public ContainerCreate attachStdErr(Boolean attachStderr) {
        getParameters().put("attach-stderr", attachStderr);
        return this;
    }

    /**
     * Sets the attach-stdin parameter.
     */
    public ContainerCreate attachStdIn(Boolean attachStdin) {
        getParameters().put("attach-stdin", attachStdin);
        return this;
    }

    /**
     * Sets the attach-stdout parameter.
     */
    public ContainerCreate attachStdOut(Boolean attachStdout) {
        getParameters().put("attach-stdout", attachStdout);
        return this;
    }

    /**
     * Adds capabilities as command parameter.
     */
    public ContainerCreate addCapability(Capability ... capabilities) {
        getParameters().put("capability-add", capabilities);
        return this;
    }

    /**
     * Drops capabilities as command parameter.
     */
    public ContainerCreate dropCapability(Capability ... capabilities) {
        getParameters().put("capability-drop", capabilities);
        return this;
    }

    /**
     * Sets the domain-name parameter.
     */
    public ContainerCreate domainName(String domainName) {
        getParameters().put("domain-name", domainName);
        return this;
    }

    /**
     * Adds commands as command parameter.
     */
    public ContainerCreate cmd(String ... commands) {
        getParameters().put("cmd", commands);
        return this;
    }

    /**
     * Adds environment variables as command parameter.
     */
    public ContainerCreate env(String ... envVars) {
        getParameters().put("env", envVars);
        return this;
    }

    /**
     * Sets the entrypoint parameter.
     */
    public ContainerCreate entryPoint(String entrypoint) {
        getParameters().put("entrypoint", entrypoint);
        return this;
    }

    /**
     * Sets the hostname parameter.
     */
    public ContainerCreate hostName(String hostname) {
        getParameters().put("hostname", hostname);
        return this;
    }

    /**
     * Adds port-specs variables as command parameter.
     */
    public ContainerCreate portSpecs(String ... portSpecs) {
        getParameters().put("port-specs", portSpecs);
        return this;
    }

    /**
     * Adds exposed-ports variables as command parameter.
     */
    public ContainerCreate exposedPorts(ExposedPort ... exposedPorts) {
        getParameters().put("exposed-ports", exposedPorts);
        return this;
    }

    /**
     * Adds explicit port bindings as command parameter.
     */
    public ContainerCreate portBindings(Ports ... portBindings) {
        getParameters().put("port-bindings", portBindings);
        return this;
    }

    /**
     * Adds explicit port bindings as command parameter.
     */
    public ContainerCreate portBindings(PortBinding ... portBindings) {
        getParameters().put("port-bindings", portBindings);
        return this;
    }

    /**
     * Adds volumes variables as command parameter.
     */
    public ContainerCreate volumes(Volume ... volumes) {
        getParameters().put("volumes", volumes);
        return this;
    }

    /**
     * Sets the working-dir parameter.
     */
    public ContainerCreate workingDir(String workingDir) {
        getParameters().put("working-dir", workingDir);
        return this;
    }

    /**
     * Command builder.
     */
    public static final class Builder extends AbstractDockerCommandBuilder<CreateContainerResponse, ContainerCreate, Builder>
            implements DockerContainerCreateActionBuilder<CreateContainerResponse, DockerExecuteAction, Builder> {

        public Builder(DockerExecuteAction.Builder parent) {
            super(parent, new ContainerCreate());
        }

        @Override
        public Builder image(String id) {
            command.image(id);
            return this;
        }

        @Override
        public Builder name(String name) {
            command.name(name);
            return this;
        }

        @Override
        public Builder attachStdErr(Boolean attachStderr) {
            command.attachStdErr(attachStderr);
            return this;
        }

        @Override
        public Builder attachStdIn(Boolean attachStdin) {
            command.attachStdIn(attachStdin);
            return this;
        }

        @Override
        public Builder attachStdOut(Boolean attachStdout) {
            command.attachStdOut(attachStdout);
            return this;
        }

        @Override
        public Builder addCapability(Object... capabilities) {
            for (Object item : capabilities) {
                if (item instanceof Capability capability) {
                    addCapability(capability);
                }
            }

            return this;
        }

        public Builder addCapability(Capability... capabilities) {
            command.addCapability(capabilities);
            return this;
        }

        @Override
        public Builder dropCapability(Object... capabilities) {
            for (Object item : capabilities) {
                if (item instanceof Capability capability) {
                    dropCapability(capability);
                }
            }

            return this;
        }

        public Builder dropCapability(Capability... capabilities) {
            command.dropCapability(capabilities);
            return this;
        }

        @Override
        public Builder domainName(String domainName) {
            command.domainName(domainName);
            return this;
        }

        @Override
        public Builder cmd(String... commands) {
            command.cmd(commands);
            return this;
        }

        @Override
        public Builder env(String... envVars) {
            command.env(envVars);
            return this;
        }

        @Override
        public Builder entryPoint(String entrypoint) {
            command.entryPoint(entrypoint);
            return this;
        }

        @Override
        public Builder hostName(String hostname) {
            command.hostName(hostname);
            return this;
        }

        @Override
        public Builder portSpecs(String... portSpecs) {
            command.portSpecs(portSpecs);
            return this;
        }

        @Override
        public Builder exposedPorts(Object... exposedPorts) {
            for (Object item : exposedPorts) {
                if (item instanceof ExposedPort exposedPort) {
                    exposedPorts(exposedPort);
                }
            }

            return this;
        }

        public Builder exposedPorts(ExposedPort ... exposedPorts) {
            command.exposedPorts(exposedPorts);
            return this;
        }

        @Override
        public Builder portBindings(Object... portBindings) {
            for (Object item : portBindings) {
                if (item instanceof Ports ports) {
                    portBindings(ports);
                } else if (item instanceof PortBinding portBinding) {
                    portBindings(portBinding);
                }
            }

            return this;
        }

        /**
         * Adds explicit ports as command parameter.
         */
        public Builder portBindings(Ports ... portBindings) {
            command.portBindings(portBindings);
            return this;
        }

        /**
         * Adds explicit port bindings as command parameter.
         */
        public Builder portBindings(PortBinding ... portBindings) {
            command.portBindings(portBindings);
            return this;
        }

        @Override
        public Builder volumes(Object... volumes) {
            for (Object item : volumes) {
                if (item instanceof Volume volume) {
                    volumes(volume);
                }
            }

            return this;
        }

        public Builder volumes(Volume... volumes) {
            command.volumes(volumes);
            return this;
        }

        @Override
        public Builder workingDir(String workingDir) {
            command.workingDir(workingDir);
            return this;
        }
    }
}
