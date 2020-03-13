package com.consol.citrus.dsl.builder;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.docker.command.AbstractDockerCommand;
import com.consol.citrus.docker.command.AbstractDockerCommandBuilder;
import com.consol.citrus.docker.command.ContainerCreate;
import com.consol.citrus.docker.command.ContainerInspect;
import com.consol.citrus.docker.command.ContainerStart;
import com.consol.citrus.docker.command.ContainerStop;
import com.consol.citrus.docker.command.ContainerWait;
import com.consol.citrus.docker.command.DockerCommand;
import com.consol.citrus.docker.command.ImageBuild;
import com.consol.citrus.docker.command.ImageInspect;
import com.consol.citrus.docker.command.ImagePull;
import com.consol.citrus.docker.command.ImageRemove;
import com.consol.citrus.docker.command.Info;
import com.consol.citrus.docker.command.Ping;
import com.consol.citrus.docker.command.Version;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Christoph Deppisch
 */
public class DockerExecuteActionBuilder extends AbstractTestActionBuilder<DockerExecuteAction, DockerExecuteActionBuilder> {

    private final DockerExecuteAction.Builder delegate = new DockerExecuteAction.Builder();

    /**
     * Use a custom docker client.
     */
    public DockerExecuteActionBuilder client(DockerClient dockerClient) {
        delegate.client(dockerClient);
        return this;
    }

    public DockerExecuteActionBuilder mapper(ObjectMapper jsonMapper) {
        delegate.mapper(jsonMapper);
        return this;
    }

    public DockerExecuteActionBuilder validator(MessageValidator<? extends ValidationContext> validator) {
        delegate.validator(validator);
        return this;
    }

    public <R, S extends AbstractDockerCommandBuilder<R, AbstractDockerCommand<R>, S>> DockerExecuteActionBuilder command(final DockerCommand<R> dockerCommand) {
        delegate.command(dockerCommand);
        return this;
    }

    public Info.Builder info() {
        return delegate.info();
    }

    public Ping.Builder ping() {
        return delegate.ping();
    }

    public Version.Builder version() {
        return delegate.version();
    }

    public ContainerCreate.Builder create(String imageId) {
        return delegate.create(imageId);
    }

    public ContainerStart.Builder start(String containerId) {
        return delegate.start(containerId);
    }

    public ContainerStop.Builder stop(String containerId) {
        return delegate.stop(containerId);
    }

    public ContainerWait.Builder wait(String containerId) {
        return delegate.wait(containerId);
    }

    public ContainerInspect.Builder inspectContainer(String containerId) {
        return delegate.inspectContainer(containerId);
    }

    public ImageInspect.Builder inspectImage(String imageId) {
        return delegate.inspectImage(imageId);
    }

    public ImageBuild.Builder buildImage() {
        return delegate.buildImage();
    }

    public ImagePull.Builder pullImage(String imageId) {
        return delegate.pullImage(imageId);
    }

    public ImageRemove.Builder removeImage(String imageId) {
        return delegate.removeImage(imageId);
    }

    public DockerExecuteActionBuilder result(String result) {
        delegate.result(result);
        return this;
    }

    @Override
    public DockerExecuteAction build() {
        return delegate.build();
    }
}
