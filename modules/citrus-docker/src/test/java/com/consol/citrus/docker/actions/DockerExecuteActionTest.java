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

import com.consol.citrus.docker.command.*;
import com.consol.citrus.docker.command.Info;
import com.consol.citrus.docker.command.Version;
import com.consol.citrus.docker.message.DockerMessageHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.easymock.EasyMock.*;

public class DockerExecuteActionTest extends AbstractTestNGUnitTest {

    private DockerClient dockerClient = EasyMock.createMock(DockerClient.class);

    @Test
    public void testInfo() throws Exception {
        InfoCmd command = EasyMock.createMock(InfoCmd.class);
        com.github.dockerjava.api.model.Info result = new com.github.dockerjava.api.model.Info();

        reset(dockerClient, command);

        expect(dockerClient.infoCmd()).andReturn(command).once();
        expect(command.exec()).andReturn(result).once();

        replay(dockerClient, command);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new Info());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.execute(context);

        Assert.assertEquals(action.getCommands().get(0).getCommandResult(), result);

        verify(dockerClient, command);
    }

    @Test
    public void testPing() throws Exception {
        PingCmd command = EasyMock.createMock(PingCmd.class);

        reset(dockerClient, command);

        expect(dockerClient.pingCmd()).andReturn(command).once();
        expect(command.exec()).andReturn(null).once();

        replay(dockerClient, command);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new Ping());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommands().get(0).getCommandResult()).getStatus(), "success");

        verify(dockerClient, command);
    }

    @Test
    public void testVersion() throws Exception {
        VersionCmd command = EasyMock.createMock(VersionCmd.class);
        com.github.dockerjava.api.model.Version result = new com.github.dockerjava.api.model.Version();

        reset(dockerClient, command);

        expect(dockerClient.versionCmd()).andReturn(command).once();
        expect(command.exec()).andReturn(result).once();

        replay(dockerClient, command);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new Version());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.execute(context);

        Assert.assertEquals(action.getCommands().get(0).getCommandResult(), result);

        verify(dockerClient, command);
    }

    @Test
    public void testCreate() throws Exception {
        CreateContainerCmd command = EasyMock.createMock(CreateContainerCmd.class);
        CreateContainerResponse response = new CreateContainerResponse();
        response.setId(UUID.randomUUID().toString());

        reset(dockerClient, command);

        expect(dockerClient.createContainerCmd("image_create")).andReturn(command).once();
        expect(command.withName("my_container")).andReturn(command).once();
        expect(command.exec()).andReturn(response).once();

        replay(dockerClient, command);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerCreate());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.getCommands().get(0).getParameters().put("image", "image_create");
        action.getCommands().get(0).getParameters().put("tag", "latest");
        action.getCommands().get(0).getParameters().put("name", "my_container");

        action.execute(context);

        Assert.assertEquals(action.getCommands().get(0).getCommandResult(), response);
        Assert.assertEquals(context.getVariable(DockerMessageHeaders.CONTAINER_ID), response.getId());

        verify(dockerClient, command);
    }

    @Test
    public void testCreateNoName() throws Exception {
        CreateContainerCmd command = EasyMock.createMock(CreateContainerCmd.class);
        InspectContainerCmd inspectCommand = EasyMock.createMock(InspectContainerCmd.class);

        InspectContainerResponse inspectResponse = EasyMock.createMock(InspectContainerResponse.class);
        CreateContainerResponse response = new CreateContainerResponse();
        response.setId(UUID.randomUUID().toString());

        reset(dockerClient, command, inspectCommand, inspectResponse);

        expect(dockerClient.createContainerCmd("image_create")).andReturn(command).once();
        expect(dockerClient.inspectContainerCmd(response.getId())).andReturn(inspectCommand).once();
        expect(command.exec()).andReturn(response).once();
        expect(inspectCommand.exec()).andReturn(inspectResponse).once();
        expect(inspectResponse.getName()).andReturn("/my_container").once();

        replay(dockerClient, command, inspectCommand, inspectResponse);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerCreate());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.getCommands().get(0).getParameters().put("image", "image_create");
        action.getCommands().get(0).getParameters().put("tag", "latest");

        action.execute(context);

        Assert.assertEquals(action.getCommands().get(0).getCommandResult(), response);
        Assert.assertEquals(context.getVariable(DockerMessageHeaders.CONTAINER_ID), response.getId());
        Assert.assertEquals(context.getVariable(DockerMessageHeaders.CONTAINER_NAME), "my_container");

        verify(dockerClient, command, inspectCommand, inspectResponse);
    }

    @Test
    public void testInspectContainer() throws Exception {
        InspectContainerCmd command = EasyMock.createMock(InspectContainerCmd.class);
        InspectContainerResponse response = new InspectContainerResponse();

        reset(dockerClient, command);

        expect(dockerClient.inspectContainerCmd("container_inspect")).andReturn(command).once();
        expect(command.exec()).andReturn(response).once();

        replay(dockerClient, command);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerInspect());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.getCommands().get(0).getParameters().put("container", "container_inspect");

        action.execute(context);

        Assert.assertEquals(action.getCommands().get(0).getCommandResult(), response);

        verify(dockerClient, command);
    }

    @Test
    public void testInspectImage() throws Exception {
        InspectImageCmd command = EasyMock.createMock(InspectImageCmd.class);
        InspectImageResponse response = new InspectImageResponse();

        reset(dockerClient, command);

        expect(dockerClient.inspectImageCmd("image_inspect")).andReturn(command).once();
        expect(command.exec()).andReturn(response).once();

        replay(dockerClient, command);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ImageInspect());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.getCommands().get(0).getParameters().put("image", "image_inspect");

        action.execute(context);

        Assert.assertEquals(action.getCommands().get(0).getCommandResult(), response);

        verify(dockerClient, command);
    }

    @Test
    public void testRemoveContainer() throws Exception {
        RemoveContainerCmd command = EasyMock.createMock(RemoveContainerCmd.class);

        reset(dockerClient, command);

        expect(dockerClient.removeContainerCmd("container_inspect")).andReturn(command).once();
        expect(command.exec()).andReturn(null).once();

        replay(dockerClient, command);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerRemove());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.getCommands().get(0).getParameters().put("container", "container_inspect");

        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommands().get(0).getCommandResult()).getStatus(), "success");

        verify(dockerClient, command);
    }

    @Test
    public void testRemoveImage() throws Exception {
        RemoveImageCmd command = EasyMock.createMock(RemoveImageCmd.class);

        reset(dockerClient, command);

        expect(dockerClient.removeImageCmd("image_remove")).andReturn(command).once();
        expect(command.exec()).andReturn(null).once();

        replay(dockerClient, command);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ImageRemove());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.getCommands().get(0).getParameters().put("image", "image_remove");

        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommands().get(0).getCommandResult()).getStatus(), "success");

        verify(dockerClient, command);
    }

    @Test
    public void testStartContainer() throws Exception {
        StartContainerCmd command = EasyMock.createMock(StartContainerCmd.class);

        reset(dockerClient, command);

        expect(dockerClient.startContainerCmd("container_start")).andReturn(command).once();
        expect(command.exec()).andReturn(null).once();

        replay(dockerClient, command);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerStart());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.getCommands().get(0).getParameters().put("container", "container_start");

        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommands().get(0).getCommandResult()).getStatus(), "success");

        verify(dockerClient, command);
    }

    @Test
    public void testStopContainer() throws Exception {
        StopContainerCmd command = EasyMock.createMock(StopContainerCmd.class);

        reset(dockerClient, command);

        expect(dockerClient.stopContainerCmd("container_stop")).andReturn(command).once();
        expect(command.exec()).andReturn(null).once();

        replay(dockerClient, command);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ContainerStop());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.getCommands().get(0).getParameters().put("container", "container_stop");

        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommands().get(0).getCommandResult()).getStatus(), "success");

        verify(dockerClient, command);
    }

    @Test
    public void testPullImage() throws Exception {
        PullImageCmd command = EasyMock.createMock(PullImageCmd.class);
        final PullResponseItem responseItem = EasyMock.createMock(PullResponseItem.class);

        reset(dockerClient, command, responseItem);

        expect(dockerClient.pullImageCmd("image_pull")).andReturn(command).once();
        expect(responseItem.isPullSuccessIndicated()).andReturn(true).once();
        expect(command.withTag("image_tag")).andReturn(command).once();
        expect(command.exec(anyObject(PullImageResultCallback.class))).andAnswer(new IAnswer<PullImageResultCallback>() {
            @Override
            public PullImageResultCallback answer() throws Throwable {
                PullImageResultCallback resultCallback = (PullImageResultCallback) getCurrentArguments()[0];

                resultCallback.onNext(responseItem);
                resultCallback.onComplete();

                return resultCallback;
            }
        }).once();

        replay(dockerClient, command, responseItem);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ImagePull());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.getCommands().get(0).getParameters().put("image", "image_pull");
        action.getCommands().get(0).getParameters().put("tag", "image_tag");

        action.execute(context);

        Assert.assertEquals(action.getCommands().get(0).getCommandResult(), responseItem);

        verify(dockerClient, command, responseItem);
    }

    @Test
    public void testBuildImage() throws Exception {
        BuildImageCmd command = EasyMock.createMock(BuildImageCmd.class);
        final BuildResponseItem responseItem = EasyMock.createMock(BuildResponseItem.class);

        reset(dockerClient, command, responseItem);

        expect(dockerClient.buildImageCmd()).andReturn(command).once();
        expect(responseItem.isBuildSuccessIndicated()).andReturn(true).once();
        expect(responseItem.getImageId()).andReturn("new_image").once();
        expect(command.withTag("latest")).andReturn(command).once();
        expect(command.exec(anyObject(BuildImageResultCallback.class))).andAnswer(new IAnswer<BuildImageResultCallback>() {
            @Override
            public BuildImageResultCallback answer() throws Throwable {
                BuildImageResultCallback resultCallback = (BuildImageResultCallback) getCurrentArguments()[0];

                resultCallback.onNext(responseItem);
                resultCallback.onComplete();

                return resultCallback;
            }
        }).once();

        replay(dockerClient, command, responseItem);

        DockerExecuteAction action = new DockerExecuteAction();
        action.setCommand(new ImageBuild());
        action.setDockerClient(new com.consol.citrus.docker.client.DockerClient(dockerClient));

        action.getCommands().get(0).getParameters().put("image", "image_pull");
        action.getCommands().get(0).getParameters().put("tag", "latest");

        action.execute(context);

        Assert.assertEquals(action.getCommands().get(0).getCommandResult(), responseItem);
        Assert.assertEquals(context.getVariable(DockerMessageHeaders.IMAGE_ID), "new_image");

        verify(dockerClient, command, responseItem);
    }
}