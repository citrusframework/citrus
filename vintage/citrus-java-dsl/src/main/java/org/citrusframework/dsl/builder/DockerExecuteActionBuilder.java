package org.citrusframework.dsl.builder;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.command.AbstractDockerCommand;
import org.citrusframework.docker.command.AbstractDockerCommandBuilder;
import org.citrusframework.docker.command.ContainerCreate;
import org.citrusframework.docker.command.ContainerInspect;
import org.citrusframework.docker.command.ContainerStart;
import org.citrusframework.docker.command.ContainerStop;
import org.citrusframework.docker.command.ContainerWait;
import org.citrusframework.docker.command.DockerCommand;
import org.citrusframework.docker.command.ImageBuild;
import org.citrusframework.docker.command.ImageInspect;
import org.citrusframework.docker.command.ImagePull;
import org.citrusframework.docker.command.ImageRemove;
import org.citrusframework.docker.command.Info;
import org.citrusframework.docker.command.Ping;
import org.citrusframework.docker.command.Version;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
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
