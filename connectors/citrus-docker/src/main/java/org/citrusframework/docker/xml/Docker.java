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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.command.DockerCommandProvider;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;

@XmlRootElement(name = "docker")
public class Docker implements TestActionBuilder<DockerExecuteAction>, ReferenceResolverAware {

    private final DockerExecuteAction.Builder delegate = new DockerExecuteAction.Builder();

    private DockerCommandProvider commandProvider;

    private String description;
    private String dockerClient;
    private String result;
    private ReferenceResolver referenceResolver;

    @XmlElement
    public void setDescription(String description) {
        this.description = description;
    }

    @XmlAttribute
    public void setClient(String dockerClient) {
        this.dockerClient = dockerClient;
    }

    @XmlAttribute
    public void setResult(String result) {
        this.result = result;
    }

    @XmlElement
    public void setInfo(Info info) {
        this.commandProvider = info;
    }

    @XmlElement
    public void setPing(Ping ping) {
        this.commandProvider = ping;
    }

    @XmlElement
    public void setVersion(Version version) {
        this.commandProvider = version;
    }

    @XmlElement
    public void setCreate(Create create) {
        this.commandProvider = create;
    }

    @XmlElement
    public void setStart(Start start) {
        this.commandProvider = start;
    }

    @XmlElement
    public void setStop(Stop stop) {
        this.commandProvider = stop;
    }

    @XmlElement
    public void setRemove(Remove remove) {
        this.commandProvider = remove;
    }

    @XmlElement
    public void setWait(Wait wait) {
        this.commandProvider = wait;
    }

    @XmlElement
    public void setInspect(Inspect inspect) {
        this.commandProvider = inspect;
    }

    @XmlElement(name = "inspect-image")
    public void setInspectImage(InspectImage inspectImage) {
        this.commandProvider = inspectImage;
    }

    @XmlElement(name = "build-image")
    public void setBuildImage(BuildImage buildImage) {
        this.commandProvider = buildImage;
    }

    @XmlElement(name = "pull-image")
    public void setPullImage(PullImage pullImage) {
        this.commandProvider = pullImage;
    }

    @XmlElement(name = "remove-image")
    public void setRemoveImage(RemoveImage removeImage) {
        this.commandProvider = removeImage;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    @Override
    public DockerExecuteAction build() {
        if (commandProvider == null) {
            throw new CitrusRuntimeException("Missing Docker command - please provide proper command details");
        }

        delegate.command(commandProvider.getCommand());

        if (StringUtils.hasText(description)) {
            delegate.description(description);
        }

        if (StringUtils.hasText(result)) {
            delegate.result(result);
        }

        if (referenceResolver != null && StringUtils.hasText(dockerClient)) {
            delegate.client(referenceResolver.resolve(dockerClient, DockerClient.class));
        }

        return delegate.build();
    }
}
