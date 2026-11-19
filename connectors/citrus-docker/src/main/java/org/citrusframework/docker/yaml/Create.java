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

package org.citrusframework.docker.yaml;

import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.command.ContainerCreate;
import org.citrusframework.docker.command.DockerCommand;
import org.citrusframework.docker.command.DockerCommandProvider;

public class Create implements DockerCommandProvider {

    private final ContainerCreate.Builder delegate = new ContainerCreate.Builder(new DockerExecuteAction.Builder());

    @SchemaProperty(description = "The id of the image to create the container from", required = true)
    public void setImage(String image) {
        delegate.image(image);
    }

    @SchemaProperty(description = "The name of the container to create")
    public void setName(String name) {
        delegate.name(name);
    }

    @SchemaProperty(description = "Attach standard error output to the container")
    public void setAttachStdErr(Boolean attachStdErr) {
        delegate.attachStdErr(attachStdErr);
    }

    @SchemaProperty(description = "Attach standard input to the container")
    public void setAttachStdIn(Boolean attachStdIn) {
        delegate.attachStdIn(attachStdIn);
    }

    @SchemaProperty(description = "Attach standard output to the container")
    public void setAttachStdOut(Boolean attachStdOut) {
        delegate.attachStdOut(attachStdOut);
    }

    @SchemaProperty(description = "Add capabilities to the container, separated by semicolon")
    public void setAddCapability(String capabilityAdd) {
        delegate.withParam("capability-add", capabilityAdd);
    }

    @SchemaProperty(description = "Drop capabilities from the container, separated by semicolon")
    public void setDropCapability(String capabilityDrop) {
        delegate.withParam("capability-drop", capabilityDrop);
    }

    @SchemaProperty(description = "The domain name of the container")
    public void setDomainName(String domainName) {
        delegate.domainName(domainName);
    }

    @SchemaProperty(description = "The command to run in the container, arguments separated by semicolon")
    public void setCmd(String cmd) {
        delegate.cmd(cmd);
    }

    @SchemaProperty(description = "The environment variables of the container, separated by semicolon")
    public void setEnv(String env) {
        delegate.env(env);
    }

    @SchemaProperty(description = "The entry point of the container")
    public void setEntryPoint(String entryPoint) {
        delegate.entryPoint(entryPoint);
    }

    @SchemaProperty(description = "The host name of the container")
    public void setHostName(String hostName) {
        delegate.hostName(hostName);
    }

    @SchemaProperty(description = "The exposed port specs of the container, separated by semicolon")
    public void setPortSpecs(String portSpecs) {
        delegate.portSpecs(portSpecs);
    }

    @SchemaProperty(description = "The exposed ports of the container, separated by comma")
    public void setExposedPorts(String exposedPorts) {
        delegate.exposedPorts(exposedPorts);
    }

    @SchemaProperty(description = "The port bindings of the container, separated by comma")
    public void setPortBindings(String portBindings) {
        delegate.portBindings(portBindings);
    }

    @SchemaProperty(description = "The volumes of the container, separated by comma")
    public void setVolumes(String volumes) {
        delegate.volumes(volumes);
    }

    @SchemaProperty(description = "The working directory of the container")
    public void setWorkingDir(String workingDir) {
        delegate.workingDir(workingDir);
    }

    @Override
    public DockerCommand<?> getCommand() {
        return delegate.command();
    }
}
