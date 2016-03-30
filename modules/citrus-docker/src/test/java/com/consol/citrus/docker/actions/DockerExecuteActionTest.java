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

package com.consol.citrus.docker.actions;

import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.docker.command.*;
import com.consol.citrus.docker.command.Info;
import com.consol.citrus.docker.command.Version;
import com.consol.citrus.docker.message.DockerMessageHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class DockerExecuteActionTest extends AbstractTestNGUnitTest {

    private com.github.dockerjava.api.DockerClient dockerClient = Mockito.mock(com.github.dockerjava.api.DockerClient.class);

    private DockerClient client = new DockerClient();

    @BeforeClass
    public void setup() {
        client.getEndpointConfiguration().setDockerClient(dockerClient);
    }

    @Test
    public void testInfo() throws Exception {
        InfoCmd command = Mockito.mock(InfoCmd.class);
        com.github.dockerjava.api.model.Info result = new com.github.dockerjava.api.model.Info();

        reset(dockerClient, command);

        when(dockerClient.infoCmd()).thenReturn(command);
        when(command.exec()).thenReturn(result);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new Info());
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), result);

    }

    @Test
    public void testPing() throws Exception {
        PingCmd command = Mockito.mock(PingCmd.class);

        reset(dockerClient, command);

        when(dockerClient.pingCmd()).thenReturn(command);
        when(command.exec()).thenReturn(null);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new Ping());
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommand().getCommandResult()).getStatus(), "success");

    }

    @Test
    public void testVersion() throws Exception {
        VersionCmd command = Mockito.mock(VersionCmd.class);
        com.github.dockerjava.api.model.Version result = new com.github.dockerjava.api.model.Version();

        reset(dockerClient, command);

        when(dockerClient.versionCmd()).thenReturn(command);
        when(command.exec()).thenReturn(result);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new Version());
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), result);

    }

    @Test
    public void testCreate() throws Exception {
        CreateContainerCmd command = Mockito.mock(CreateContainerCmd.class);
        CreateContainerResponse response = new CreateContainerResponse();
        response.setId(UUID.randomUUID().toString());

        reset(dockerClient, command);

        when(dockerClient.createContainerCmd("image_create")).thenReturn(command);
        when(command.withName("my_container")).thenReturn(command);
        when(command.exec()).thenReturn(response);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerCreate()
            .image("image_create")
            .name("my_container"));
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), response);
        Assert.assertEquals(context.getVariable(DockerMessageHeaders.CONTAINER_ID), response.getId());

    }

    @Test
    public void testCreateNoName() throws Exception {
        CreateContainerCmd command = Mockito.mock(CreateContainerCmd.class);
        InspectContainerCmd inspectCommand = Mockito.mock(InspectContainerCmd.class);

        InspectContainerResponse inspectResponse = Mockito.mock(InspectContainerResponse.class);
        CreateContainerResponse response = new CreateContainerResponse();
        response.setId(UUID.randomUUID().toString());

        reset(dockerClient, command, inspectCommand, inspectResponse);

        when(dockerClient.createContainerCmd("image_create")).thenReturn(command);
        when(dockerClient.inspectContainerCmd(response.getId())).thenReturn(inspectCommand);
        when(command.exec()).thenReturn(response);
        when(inspectCommand.exec()).thenReturn(inspectResponse);
        when(inspectResponse.getName()).thenReturn("/my_container");

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerCreate()
            .image("image_create"));
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), response);
        Assert.assertEquals(context.getVariable(DockerMessageHeaders.CONTAINER_ID), response.getId());
        Assert.assertEquals(context.getVariable(DockerMessageHeaders.CONTAINER_NAME), "my_container");

    }

    @Test
    public void testInspectContainer() throws Exception {
        InspectContainerCmd command = Mockito.mock(InspectContainerCmd.class);
        InspectContainerResponse response = new InspectContainerResponse();

        reset(dockerClient, command);

        when(dockerClient.inspectContainerCmd("container_inspect")).thenReturn(command);
        when(command.exec()).thenReturn(response);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerInspect()
            .container("container_inspect"));
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), response);

    }

    @Test
    public void testInspectImage() throws Exception {
        InspectImageCmd command = Mockito.mock(InspectImageCmd.class);
        InspectImageResponse response = new InspectImageResponse();

        reset(dockerClient, command);

        when(dockerClient.inspectImageCmd("image_inspect")).thenReturn(command);
        when(command.exec()).thenReturn(response);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ImageInspect()
            .image("image_inspect"));
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), response);

    }

    @Test
    public void testRemoveContainer() throws Exception {
        RemoveContainerCmd command = Mockito.mock(RemoveContainerCmd.class);

        reset(dockerClient, command);

        when(dockerClient.removeContainerCmd("container_inspect")).thenReturn(command);
        when(command.exec()).thenReturn(null);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerRemove()
            .container("container_inspect"));
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommand().getCommandResult()).getStatus(), "success");

    }

    @Test
    public void testRemoveImage() throws Exception {
        RemoveImageCmd command = Mockito.mock(RemoveImageCmd.class);

        reset(dockerClient, command);

        when(dockerClient.removeImageCmd("image_remove")).thenReturn(command);
        when(command.exec()).thenReturn(null);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ImageRemove()
            .image("image_remove"));
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommand().getCommandResult()).getStatus(), "success");

    }

    @Test
    public void testStartContainer() throws Exception {
        StartContainerCmd command = Mockito.mock(StartContainerCmd.class);

        reset(dockerClient, command);

        when(dockerClient.startContainerCmd("container_start")).thenReturn(command);
        when(command.exec()).thenReturn(null);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerStart()
            .container("container_start"));
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommand().getCommandResult()).getStatus(), "success");

    }

    @Test
    public void testStopContainer() throws Exception {
        StopContainerCmd command = Mockito.mock(StopContainerCmd.class);

        reset(dockerClient, command);

        when(dockerClient.stopContainerCmd("container_stop")).thenReturn(command);
        when(command.exec()).thenReturn(null);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerStop()
            .container("container_stop"));
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommand().getCommandResult()).getStatus(), "success");

    }

    @Test
    public void testWaitContainer() throws Exception {
        WaitContainerCmd command = Mockito.mock(WaitContainerCmd.class);

        reset(dockerClient, command);

        when(dockerClient.waitContainerCmd("container_wait")).thenReturn(command);
        when(command.exec()).thenReturn(0);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerWait()
                .container("container_wait"));
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(((ContainerWait.ExitCode)action.getCommand().getCommandResult()).getExitCode(), new Integer(0));

    }

    @Test
    public void testPullImage() throws Exception {
        PullImageCmd command = Mockito.mock(PullImageCmd.class);
        final PullResponseItem responseItem = Mockito.mock(PullResponseItem.class);

        reset(dockerClient, command, responseItem);

        when(dockerClient.pullImageCmd("image_pull")).thenReturn(command);
        when(responseItem.isPullSuccessIndicated()).thenReturn(true);
        when(command.withTag("image_tag")).thenReturn(command);
        doAnswer(new Answer<PullImageResultCallback>() {
            @Override
            public PullImageResultCallback answer(InvocationOnMock invocation) throws Throwable {
                PullImageResultCallback resultCallback = (PullImageResultCallback) invocation.getArguments()[0];

                resultCallback.onNext(responseItem);
                resultCallback.onComplete();

                return resultCallback;
            }
        }).when(command).exec(any(PullImageResultCallback.class));

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ImagePull()
            .image("image_pull")
            .tag("image_tag"));
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), responseItem);

    }

    @Test
    public void testBuildImage() throws Exception {
        BuildImageCmd command = Mockito.mock(BuildImageCmd.class);
        final BuildResponseItem responseItem = Mockito.mock(BuildResponseItem.class);

        reset(dockerClient, command, responseItem);

        when(dockerClient.buildImageCmd()).thenReturn(command);
        when(responseItem.isBuildSuccessIndicated()).thenReturn(true);
        when(responseItem.getImageId()).thenReturn("new_image");
        when(command.withDockerfile(any(File.class))).thenReturn(command);
        when(command.withTag("latest")).thenReturn(command);
        doAnswer(new Answer<BuildImageResultCallback>() {
            @Override
            public BuildImageResultCallback answer(InvocationOnMock invocation) throws Throwable {
                BuildImageResultCallback resultCallback = (BuildImageResultCallback) invocation.getArguments()[0];

                resultCallback.onNext(responseItem);
                resultCallback.onComplete();

                return resultCallback;
            }
        }).when(command).exec(any(BuildImageResultCallback.class));

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ImageBuild()
                .dockerFile(new ClassPathResource("com/consol/citrus/docker/Dockerfile"))
                .tag("latest"));
        action.setDockerClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), responseItem);
        Assert.assertEquals(context.getVariable(DockerMessageHeaders.IMAGE_ID), "new_image");

    }
}