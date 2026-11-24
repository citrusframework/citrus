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

package org.citrusframework.docker;

import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.WaitContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.ResponseItem;
import com.github.dockerjava.api.model.WaitResponse;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.base.context.StaticTestContextFactory;
import org.citrusframework.context.TestContext;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.util.ReflectionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Shared setup for Docker declarative test action tests. Holds the mocked
 * docker-java client wiring so XML and YAML tests do not maintain copies.
 */
@ExtendWith(MockitoExtension.class)
public abstract class AbstractDockerActionTest extends AbstractUnitTest {

    protected Citrus citrus;

    @Mock
    protected CitrusContext citrusContext;

    @BeforeEach
    @Override
    public void prepareTest() {
        citrus = CitrusInstanceManager.newInstance(() -> citrusContext);

        super.prepareTest();
    }

    @Override
    protected TestContext createTestContext() {
        TestContext context = super.createTestContext();
        lenient().when(citrusContext.getReferenceResolver()).thenReturn(context.getReferenceResolver());
        lenient().when(citrusContext.getMessageValidatorRegistry()).thenReturn(context.getMessageValidatorRegistry());
        lenient().when(citrusContext.getTestContextFactory()).thenReturn(new StaticTestContextFactory(context));
        lenient().doAnswer(invocationOnMock -> {
            CitrusAnnotations.parseConfiguration(invocationOnMock.getArgument(0, Object.class), citrusContext);
            return null;
        }).when(citrusContext).parseConfiguration((Object) any());
        lenient().doAnswer(invocationOnMock -> {
            context.getReferenceResolver().bind(invocationOnMock.getArgument(0), invocationOnMock.getArgument(1));
            return null;
        }).when(citrusContext).addComponent(anyString(), any());
        CitrusAnnotations.injectAll(this, citrus, context);
        return context;
    }

    protected DockerClient bindDockerClient() {
        DockerClient dockerClient = mock(DockerClient.class, Mockito.RETURNS_DEEP_STUBS);
        var docker = dockerClient.getEndpointConfiguration().getDockerClient();

        try {
            WaitContainerCmd waitCmd = mock(WaitContainerCmd.class);
            lenient().doAnswer(invocation -> {
                WaitContainerResultCallback callback = invocation.getArgument(0);
                WaitResponse response = new WaitResponse();
                ReflectionHelper.setField(
                        ReflectionHelper.findField(WaitResponse.class, "statusCode"), response, 0);
                callback.onNext(response);
                callback.onComplete();
                return callback;
            }).when(waitCmd).exec(any(WaitContainerResultCallback.class));
            lenient().doReturn(waitCmd).when(docker).waitContainerCmd(anyString());

            PullImageCmd pullCmd = mock(PullImageCmd.class);
            lenient().doAnswer(invocation -> {
                PullImageResultCallback callback = invocation.getArgument(0);
                PullResponseItem item = new PullResponseItem();
                ReflectionHelper.setField(
                        ReflectionHelper.findField(ResponseItem.class, "status"), item, "Download complete");
                callback.onNext(item);
                callback.onComplete();
                return callback;
            }).when(pullCmd).exec(any(PullImageResultCallback.class));
            lenient().doReturn(pullCmd).when(docker).pullImageCmd(anyString());

            CreateContainerCmd createCmd = mock(CreateContainerCmd.class);
            CreateContainerResponse createResponse = new CreateContainerResponse();
            createResponse.setId("myContainerId");
            lenient().doAnswer(invocation -> createResponse).when(createCmd).exec();
            com.github.dockerjava.api.model.HostConfig hostConfig = mock(com.github.dockerjava.api.model.HostConfig.class);
            lenient().doReturn(hostConfig).when(createCmd).getHostConfig();
            lenient().doReturn(hostConfig).when(hostConfig).withCapAdd(any(com.github.dockerjava.api.model.Capability[].class));
            lenient().doReturn(hostConfig).when(hostConfig).withCapDrop(any(com.github.dockerjava.api.model.Capability[].class));
            lenient().doReturn(hostConfig).when(hostConfig).withPortBindings(any(com.github.dockerjava.api.model.Ports.class));
            lenient().doReturn(hostConfig).when(hostConfig).withPortBindings(any(com.github.dockerjava.api.model.PortBinding[].class));
            lenient().doReturn(createCmd).when(docker).createContainerCmd(anyString());

            BuildImageCmd buildCmd = mock(BuildImageCmd.class);
            lenient().doAnswer(invocation -> {
                BuildImageResultCallback callback = invocation.getArgument(0);
                BuildResponseItem item = new BuildResponseItem();
                ReflectionHelper.setField(
                        ReflectionHelper.findField(ResponseItem.class, "stream"), item,
                        "Successfully built sha256:1234567890");
                callback.onNext(item);
                callback.onComplete();
                return callback;
            }).when(buildCmd).exec(any(BuildImageResultCallback.class));
            lenient().doReturn(buildCmd).when(docker).buildImageCmd();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to prepare Docker client mock", e);
        }

        context.getReferenceResolver().bind("myDockerClient", dockerClient);
        return dockerClient;
    }
}
