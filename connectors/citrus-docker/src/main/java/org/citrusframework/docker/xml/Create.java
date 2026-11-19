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
package org.citrusframework.docker.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.command.ContainerCreate;
import org.citrusframework.docker.command.DockerCommand;
import org.citrusframework.docker.command.DockerCommandProvider;

@XmlRootElement(name = "create")
public class Create implements DockerCommandProvider {

    private final ContainerCreate.Builder delegate = new ContainerCreate.Builder(new DockerExecuteAction.Builder());

    @XmlAttribute(required = true)
    public void setImage(String image) {
        delegate.image(image);
    }

    @XmlAttribute
    public void setName(String name) {
        delegate.name(name);
    }

    @XmlAttribute(name = "attach-stderr")
    public void setAttachStdErr(Boolean attachStdErr) {
        delegate.attachStdErr(attachStdErr);
    }

    @XmlAttribute(name = "attach-stdin")
    public void setAttachStdIn(Boolean attachStdIn) {
        delegate.attachStdIn(attachStdIn);
    }

    @XmlAttribute(name = "attach-stdout")
    public void setAttachStdOut(Boolean attachStdOut) {
        delegate.attachStdOut(attachStdOut);
    }

    @XmlAttribute(name = "capability-add")
    public void setAddCapability(String capabilityAdd) {
        delegate.withParam("capability-add", capabilityAdd);
    }

    @XmlAttribute(name = "capability-drop")
    public void setDropCapability(String capabilityDrop) {
        delegate.withParam("capability-drop", capabilityDrop);
    }

    @XmlAttribute(name = "domain-name")
    public void setDomainName(String domainName) {
        delegate.domainName(domainName);
    }

    @XmlAttribute
    public void setCmd(String cmd) {
        delegate.withParam("cmd", cmd);
    }

    @XmlAttribute
    public void setEnv(String env) {
        delegate.withParam("env", env);
    }

    @XmlAttribute(name = "entrypoint")
    public void setEntryPoint(String entryPoint) {
        delegate.entryPoint(entryPoint);
    }

    @XmlAttribute(name = "hostname")
    public void setHostName(String hostName) {
        delegate.hostName(hostName);
    }

    @XmlAttribute(name = "port-specs")
    public void setPortSpecs(String portSpecs) {
        delegate.withParam("port-specs", portSpecs);
    }

    @XmlAttribute(name = "exposed-ports")
    public void setExposedPorts(String exposedPorts) {
        delegate.withParam("exposed-ports", exposedPorts);
    }

    @XmlAttribute(name = "port-bindings")
    public void setPortBindings(String portBindings) {
        delegate.withParam("port-bindings", portBindings);
    }

    @XmlAttribute
    public void setVolumes(String volumes) {
        delegate.withParam("volumes", volumes);
    }

    @XmlAttribute(name = "working-dir")
    public void setWorkingDir(String workingDir) {
        delegate.workingDir(workingDir);
    }

    @Override
    public DockerCommand<?> getCommand() {
        return delegate.command();
    }
}
