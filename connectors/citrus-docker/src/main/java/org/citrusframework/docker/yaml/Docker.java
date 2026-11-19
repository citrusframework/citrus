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

import org.citrusframework.TestActionBuilder;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.command.DockerCommandProvider;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.util.StringUtils;

import static org.citrusframework.api.yaml.SchemaProperty.Kind.ACTION;

public class Docker implements TestActionBuilder<DockerExecuteAction>, ReferenceResolverAware {

    private final DockerExecuteAction.Builder delegate = new DockerExecuteAction.Builder();

    private DockerCommandProvider commandProvider;

    private String description;
    private String dockerClient;
    private String result;
    private ReferenceResolver referenceResolver;

    @SchemaProperty(description = "The test action description.")
    public void setDescription(String description) {
        this.description = description;
    }

    @SchemaProperty(advanced = true, description = "Reference to a Docker client bean. Defaults to a new Docker client.")
    public void setClient(String dockerClient) {
        this.dockerClient = dockerClient;
    }

    @SchemaProperty(advanced = true, description = "Expected command result, validated as JSON.")
    public void setResult(String result) {
        this.result = result;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Prints general Docker information.")
    public void setInfo(Info info) {
        this.commandProvider = info;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Pings the Docker daemon.")
    public void setPing(Ping ping) {
        this.commandProvider = ping;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Prints the Docker version information.")
    public void setVersion(Version version) {
        this.commandProvider = version;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Creates a new container from an image.")
    public void setCreate(Create create) {
        this.commandProvider = create;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Starts a stopped container.")
    public void setStart(Start start) {
        this.commandProvider = start;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Stops a running container.")
    public void setStop(Stop stop) {
        this.commandProvider = stop;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Removes a container.")
    public void setRemove(Remove remove) {
        this.commandProvider = remove;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Waits for a container to stop.")
    public void setWait(Wait wait) {
        this.commandProvider = wait;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Inspects a container and prints its details.")
    public void setInspect(Inspect inspect) {
        this.commandProvider = inspect;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Inspects an image and prints its details.")
    public void setInspectImage(InspectImage inspectImage) {
        this.commandProvider = inspectImage;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Builds an image from a Dockerfile.")
    public void setBuildImage(BuildImage buildImage) {
        this.commandProvider = buildImage;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Pulls an image from a registry.")
    public void setPullImage(PullImage pullImage) {
        this.commandProvider = pullImage;
    }

    @SchemaProperty(kind = ACTION, group = "docker", module = "citrus-docker", description = "Removes an image.")
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
