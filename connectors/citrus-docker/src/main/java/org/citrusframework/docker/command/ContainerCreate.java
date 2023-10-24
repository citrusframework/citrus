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

import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.message.DockerMessageHeaders;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;

/**
 * @author Christoph Deppisch
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
    public void execute(DockerClient dockerClient, TestContext context) {
        CreateContainerCmd command = dockerClient.getEndpointConfiguration().getDockerClient().createContainerCmd(getImageId(context));

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
            if (getParameters().get("capability-add") instanceof Capability[]) {
                command.withCapAdd((Capability[]) getParameters().get("capability-add"));
            } else {
                command.withCapAdd(getCapabilities("capability-add", context));
            }
        }

        if (hasParameter("capability-drop")) {
            if (getParameters().get("capability-drop") instanceof Capability[]) {
                command.withCapAdd((Capability[]) getParameters().get("capability-drop"));
            } else {
                command.withCapDrop(getCapabilities("capability-drop", context));
            }
        }

        if (hasParameter("domain-name")) {
            command.withDomainName(getParameter("domain-name", context));
        }

        if (hasParameter("cmd")) {
            if (getParameters().get("cmd") instanceof String[]
            	|| getParameters().get("cmd") instanceof Capability[]) {
                command.withCmd((String[]) getParameters().get("cmd"));
            } else {
                command.withCmd(getParameter("cmd", context).split(DELIMITER));
            }
        }

        if (hasParameter("env")) {
            if (getParameters().get("env") instanceof String[]
            	|| getParameters().get("env") instanceof Capability[]) {
                command.withEnv((String[]) getParameters().get("env"));
            } else {
                command.withEnv(getParameter("env", context).split(DELIMITER));
            }
        }

        if (hasParameter("entrypoint")) {
            command.withEntrypoint(getParameter("entrypoint", context));
        }

        if (hasParameter("hostname")) {
            command.withHostName(getParameter("hostname", context));
        }

        if (hasParameter("port-specs")) {
            if (getParameters().get("port-specs") instanceof String[]
            	|| getParameters().get("port-specs") instanceof Capability[]) {
                command.withPortSpecs((String[]) getParameters().get("port-specs"));
            } else {
                command.withPortSpecs(getParameter("port-specs", context).split(DELIMITER));
            }
        }

        ExposedPort[] exposedPorts = {};
        if (hasParameter("exposed-ports")) {
            if (getParameters().get("exposed-ports") instanceof ExposedPort[]) {
                exposedPorts = (ExposedPort[]) getParameters().get("exposed-ports");
            } else {
                exposedPorts = getExposedPorts(getParameter("exposed-ports", context), context);
            }
            command.withExposedPorts(exposedPorts);
        }

        if (hasParameter("port-bindings")) {
            if (getParameters().get("port-bindings") instanceof Ports) {
                command.withPortBindings((Ports) getParameters().get("port-bindings"));
            } if (getParameters().get("port-bindings") instanceof PortBinding[]) {
                command.withPortBindings((PortBinding[]) getParameters().get("port-bindings"));
            } else {
                command.withPortBindings(getPortBindings(getParameter("port-bindings", context), exposedPorts, context));
            }
        } else if (exposedPorts.length > 0) {
            command.withPortBindings(getPortBindings("", exposedPorts, context));
        }

        if (hasParameter("volumes")) {
            if (getParameters().get("volumes") instanceof Volume[]
            	|| getParameters().get("volumes") instanceof ExposedPort[]) {
                command.withVolumes((Volume[]) getParameters().get("volumes"));
            } else {
                command.withVolumes(getVolumes(context));
            }
        }

        if (hasParameter("working-dir")) {
            command.withWorkingDir(getParameter("working-dir", context));
        }

        CreateContainerResponse response = command.exec();
        context.setVariable(DockerMessageHeaders.CONTAINER_ID, response.getId());
        setCommandResult(response);

        if (!hasParameter("name")) {
            InspectContainerCmd inspect = dockerClient.getEndpointConfiguration().getDockerClient().inspectContainerCmd(response.getId());
            InspectContainerResponse inspectResponse = inspect.exec();
            context.setVariable(DockerMessageHeaders.CONTAINER_NAME, inspectResponse.getName().substring(1));
        }
    }

    /**
     * Gets the volume specs from comma delimited string.
     * @return
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
     * @return
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
     * Construct set of exposed ports from comma delimited list of ports.
     * @param portSpecs
     * @param context
     * @return
     */
    private ExposedPort[] getExposedPorts(String portSpecs, TestContext context) {
        String[] ports = portSpecs.split(",");
        ExposedPort[] exposedPorts = new ExposedPort[ports.length];

        for (int i = 0; i < ports.length; i++) {
            String portSpec = context.replaceDynamicContentInString(ports[i]);

            if (portSpec.startsWith("udp:")) {
                exposedPorts[i] = ExposedPort.udp(Integer.valueOf(portSpec.substring("udp:".length())));
            } else if (portSpec.startsWith("tcp:")) {
                exposedPorts[i] = ExposedPort.tcp(Integer.valueOf(portSpec.substring("tcp:".length())));
            } else {
                exposedPorts[i] = ExposedPort.tcp(Integer.valueOf(portSpec));
            }
        }

        return exposedPorts;
    }

    /**
     * Construct set of port bindings from comma delimited list of ports.
     * @param portSpecs
     * @param exposedPorts
     * @param context
     * @return
     */
    private Ports getPortBindings(String portSpecs, ExposedPort[] exposedPorts, TestContext context) {
        String[] ports = portSpecs.split(",");
        Ports portsBindings = new Ports();

        for (String portSpec : ports) {
            String[] binding = context.replaceDynamicContentInString(portSpec).split(":");
            if (binding.length == 2) {
                Integer hostPort = Integer.valueOf(binding[0]);
                Integer port = Integer.valueOf(binding[1]);

                portsBindings.bind(Stream.of(exposedPorts).filter(exposed -> port.equals(exposed.getPort())).findAny().orElseGet(() -> ExposedPort.tcp(port)), Ports.Binding.bindPort(hostPort));
            }
        }

        Stream.of(exposedPorts).filter(exposed -> !portsBindings.getBindings().keySet().contains(exposed)).forEach(exposed -> portsBindings.bind(exposed, Ports.Binding.empty()));

        return portsBindings;
    }

    /**
     * Sets the image id parameter.
     * @param id
     * @return
     */
    public ContainerCreate image(String id) {
        getParameters().put(IMAGE_ID, id);
        return this;
    }

    /**
     * Sets the image name parameter.
     * @param name
     * @return
     */
    public ContainerCreate name(String name) {
        getParameters().put("name", name);
        return this;
    }

    /**
     * Sets the attach-stderr parameter.
     * @param attachStderr
     * @return
     */
    public ContainerCreate attachStdErr(Boolean attachStderr) {
        getParameters().put("attach-stderr", attachStderr);
        return this;
    }

    /**
     * Sets the attach-stdin parameter.
     * @param attachStdin
     * @return
     */
    public ContainerCreate attachStdIn(Boolean attachStdin) {
        getParameters().put("attach-stdin", attachStdin);
        return this;
    }

    /**
     * Sets the attach-stdout parameter.
     * @param attachStdout
     * @return
     */
    public ContainerCreate attachStdOut(Boolean attachStdout) {
        getParameters().put("attach-stdout", attachStdout);
        return this;
    }

    /**
     * Adds capabilities as command parameter.
     * @param capabilities
     * @return
     */
    public ContainerCreate addCapability(Capability ... capabilities) {
        getParameters().put("capability-add", capabilities);
        return this;
    }

    /**
     * Drops capabilities as command parameter.
     * @param capabilities
     * @return
     */
    public ContainerCreate dropCapability(Capability ... capabilities) {
        getParameters().put("capability-drop", capabilities);
        return this;
    }

    /**
     * Sets the domain-name parameter.
     * @param domainName
     * @return
     */
    public ContainerCreate domainName(String domainName) {
        getParameters().put("domain-name", domainName);
        return this;
    }

    /**
     * Adds commands as command parameter.
     * @param commands
     * @return
     */
    public ContainerCreate cmd(String ... commands) {
        getParameters().put("cmd", commands);
        return this;
    }

    /**
     * Adds environment variables as command parameter.
     * @param envVars
     * @return
     */
    public ContainerCreate env(String ... envVars) {
        getParameters().put("env", envVars);
        return this;
    }

    /**
     * Sets the entrypoint parameter.
     * @param entrypoint
     * @return
     */
    public ContainerCreate entryPoint(String entrypoint) {
        getParameters().put("entrypoint", entrypoint);
        return this;
    }

    /**
     * Sets the hostname parameter.
     * @param hostname
     * @return
     */
    public ContainerCreate hostName(String hostname) {
        getParameters().put("hostname", hostname);
        return this;
    }

    /**
     * Adds port-specs variables as command parameter.
     * @param portSpecs
     * @return
     */
    public ContainerCreate portSpecs(String ... portSpecs) {
        getParameters().put("port-specs", portSpecs);
        return this;
    }

    /**
     * Adds exposed-ports variables as command parameter.
     * @param exposedPorts
     * @return
     */
    public ContainerCreate exposedPorts(ExposedPort ... exposedPorts) {
        getParameters().put("exposed-ports", exposedPorts);
        return this;
    }

    /**
     * Adds explicit port bindings as command parameter.
     * @param portBindings
     * @return
     */
    public ContainerCreate portBindings(Ports ... portBindings) {
        getParameters().put("port-bindings", portBindings);
        return this;
    }

    /**
     * Adds explicit port bindings as command parameter.
     * @param portBindings
     * @return
     */
    public ContainerCreate portBindings(PortBinding ... portBindings) {
        getParameters().put("port-bindings", portBindings);
        return this;
    }


    /**
     * Adds volumes variables as command parameter.
     * @param volumes
     * @return
     */
    public ContainerCreate volumes(Volume ... volumes) {
        getParameters().put("volumes", volumes);
        return this;
    }

    /**
     * Sets the working-dir parameter.
     * @param workingDir
     * @return
     */
    public ContainerCreate workingDir(String workingDir) {
        getParameters().put("working-dir", workingDir);
        return this;
    }

    /**
     * Command builder.
     */
    public static final class Builder extends AbstractDockerCommandBuilder<CreateContainerResponse, ContainerCreate, Builder> {

        public Builder(DockerExecuteAction.Builder parent) {
            super(parent, new ContainerCreate());
        }

        /**
         * Sets the image id parameter.
         * @param id
         * @return
         */
        public Builder image(String id) {
            command.image(id);
            return this;
        }

        /**
         * Sets the image name parameter.
         * @param name
         * @return
         */
        public Builder name(String name) {
            command.name(name);
            return this;
        }

        /**
         * Sets the attach-stderr parameter.
         * @param attachStderr
         * @return
         */
        public Builder attachStdErr(Boolean attachStderr) {
            command.attachStdErr(attachStderr);
            return this;
        }

        /**
         * Sets the attach-stdin parameter.
         * @param attachStdin
         * @return
         */
        public Builder attachStdIn(Boolean attachStdin) {
            command.attachStdIn(attachStdin);
            return this;
        }

        /**
         * Sets the attach-stdout parameter.
         * @param attachStdout
         * @return
         */
        public Builder attachStdOut(Boolean attachStdout) {
            command.attachStdOut(attachStdout);
            return this;
        }

        /**
         * Adds capabilities as command parameter.
         * @param capabilities
         * @return
         */
        public Builder addCapability(Capability ... capabilities) {
            command.addCapability(capabilities);
            return this;
        }

        /**
         * Drops capabilities as command parameter.
         * @param capabilities
         * @return
         */
        public Builder dropCapability(Capability ... capabilities) {
            command.dropCapability(capabilities);
            return this;
        }

        /**
         * Sets the domain-name parameter.
         * @param domainName
         * @return
         */
        public Builder domainName(String domainName) {
            command.domainName(domainName);
            return this;
        }

        /**
         * Adds commands as command parameter.
         * @param commands
         * @return
         */
        public Builder cmd(String ... commands) {
            command.cmd(commands);
            return this;
        }

        /**
         * Adds environment variables as command parameter.
         * @param envVars
         * @return
         */
        public Builder env(String ... envVars) {
            command.env(envVars);
            return this;
        }

        /**
         * Sets the entrypoint parameter.
         * @param entrypoint
         * @return
         */
        public Builder entryPoint(String entrypoint) {
            command.entryPoint(entrypoint);
            return this;
        }

        /**
         * Sets the hostname parameter.
         * @param hostname
         * @return
         */
        public Builder hostName(String hostname) {
            command.hostName(hostname);
            return this;
        }

        /**
         * Adds port-specs variables as command parameter.
         * @param portSpecs
         * @return
         */
        public Builder portSpecs(String ... portSpecs) {
            command.portSpecs(portSpecs);
            return this;
        }

        /**
         * Adds exposed-ports variables as command parameter.
         * @param exposedPorts
         * @return
         */
        public Builder exposedPorts(ExposedPort ... exposedPorts) {
            command.exposedPorts(exposedPorts);
            return this;
        }

        /**
         * Adds explicit port bindings as command parameter.
         * @param portBindings
         * @return
         */
        public Builder portBindings(Ports ... portBindings) {
            command.portBindings(portBindings);
            return this;
        }

        /**
         * Adds explicit port bindings as command parameter.
         * @param portBindings
         * @return
         */
        public Builder portBindings(PortBinding ... portBindings) {
            command.portBindings(portBindings);
            return this;
        }


        /**
         * Adds volumes variables as command parameter.
         * @param volumes
         * @return
         */
        public Builder volumes(Volume ... volumes) {
            command.volumes(volumes);
            return this;
        }

        /**
         * Sets the working-dir parameter.
         * @param workingDir
         * @return
         */
        public Builder workingDir(String workingDir) {
            command.workingDir(workingDir);
            return this;
        }
    }
}
