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

package org.citrusframework.docker.actions;

import java.io.File;
import java.util.Collections;
import java.util.UUID;
import java.util.List;

import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.command.ContainerCreate;
import org.citrusframework.docker.command.ContainerInspect;
import org.citrusframework.docker.command.ContainerRemove;
import org.citrusframework.docker.command.ContainerStart;
import org.citrusframework.docker.command.ContainerStop;
import org.citrusframework.docker.command.ContainerWait;
import org.citrusframework.docker.command.ImageBuild;
import org.citrusframework.docker.command.ImageInspect;
import org.citrusframework.docker.command.ImagePull;
import org.citrusframework.docker.command.ImageRemove;
import org.citrusframework.docker.command.Info;
import org.citrusframework.docker.command.Ping;
import org.citrusframework.docker.command.Version;
import org.citrusframework.docker.message.DockerMessageHeaders;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InfoCmd;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectImageCmd;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.RemoveImageCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.command.VersionCmd;
import com.github.dockerjava.api.command.WaitContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.ContainerConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.ResponseItem;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.api.model.WaitResponse;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class DockerExecuteActionTest extends AbstractTestNGUnitTest {

    private final com.github.dockerjava.api.DockerClient dockerClient = Mockito.mock(com.github.dockerjava.api.DockerClient.class);

    private final DockerClient client = new DockerClient();

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

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new Info())
                .build();
        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), result);

    }

    @Test
    public void testPing() throws Exception {
        PingCmd command = Mockito.mock(PingCmd.class);

        reset(dockerClient, command);

        when(dockerClient.pingCmd()).thenReturn(command);

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new Ping())
                .build();
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

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new Version())
                .build();
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

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new ContainerCreate()
                        .image("image_create")
                        .name("myContainer"))
                .build();
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

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new ContainerCreate().image("image_create"))
                .build();
        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), response);
        Assert.assertEquals(context.getVariable(DockerMessageHeaders.CONTAINER_ID), response.getId());
        Assert.assertEquals(context.getVariable(DockerMessageHeaders.CONTAINER_NAME), "my_container");

    }
    
    @Test
    public void testCreateWithEnvArgs() throws Exception {
    	CreateContainerCmd command = Mockito.mock(CreateContainerCmd.class);
    	CreateContainerResponse response = Mockito.mock(CreateContainerResponse.class);
    	
    	reset(dockerClient, command);
    	
    	when(client.getEndpointConfiguration().getDockerClient().createContainerCmd(anyString())).thenReturn(command);
    	when(command.exec()).thenReturn(response);
    	when(response.getId()).thenReturn(UUID.randomUUID().toString());
    	when(dockerClient.createContainerCmd("image_create")).thenReturn(command);
    	
    	String[] containerEnvVars = {"VAR_1=value_1","VAR_2=value_2","VAR_3=value_3"};
    	
    	DockerExecuteAction containerCreateAction = new DockerExecuteAction.Builder()
    		.client(client)
    		.command(new ContainerCreate()
    			.image("image_create")
    			.name("myContainer")
    			.env("VAR_1=value_1","VAR_2=value_2","VAR_3=value_3"))
    		.build();
    	containerCreateAction.execute(context);
    	
    	ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
    	verify(command).withEnv(argumentCaptor.capture());
    	String[] capturedArguments = (String[]) argumentCaptor.getValue();
    	
    	Assert.assertEquals(containerEnvVars.length, capturedArguments.length);
    	for(int i=0; i<containerEnvVars.length; i++) {
    		Assert.assertEquals(containerEnvVars[i], capturedArguments[i]);
    	}
    }
    
    @Test
    public void testCreateWithVolumeArgs() throws Exception {
    	CreateContainerCmd command = Mockito.mock(CreateContainerCmd.class);
    	CreateContainerResponse response = Mockito.mock(CreateContainerResponse.class);
    	
    	reset(dockerClient, command);
    	
    	when(client.getEndpointConfiguration().getDockerClient().createContainerCmd(anyString())).thenReturn(command);
    	when(command.exec()).thenReturn(response);
    	when(response.getId()).thenReturn(UUID.randomUUID().toString());
    	when(dockerClient.createContainerCmd("image_create")).thenReturn(command);
    	
    	Volume[] containerVolumes = {
	    		new Volume("/source/dir/one:/destination/dir/one"),
	    		new Volume("/source/dir/two:/destination/dir/two"),
	    		new Volume("/source/dir/three:/destination/dir/three")
    		};
    	
    	DockerExecuteAction containerCreateAction = new DockerExecuteAction.Builder()
    		.client(client)
    		.command(new ContainerCreate()
    			.image("image_create")
    			.name("myContainer")
    			.volumes(
    				new Volume("/source/dir/one:/destination/dir/one"),
		    		new Volume("/source/dir/two:/destination/dir/two"),
		    		new Volume("/source/dir/three:/destination/dir/three")
		    		))
    		.build();
    	containerCreateAction.execute(context);
    	
    	ArgumentCaptor<Volume[]> argumentCaptor = ArgumentCaptor.forClass(Volume[].class);
    	verify(command).withVolumes(argumentCaptor.capture());
    	Volume[] capturedArguments = (Volume[]) argumentCaptor.getValue();
    	
    	Assert.assertEquals(containerVolumes.length, capturedArguments.length);
    	for(int i=0; i<containerVolumes.length; i++) {
    		Assert.assertEquals(containerVolumes[i], capturedArguments[i]);
    	}
    	
    }

    @Test
    public void testInspectContainer() throws Exception {
        InspectContainerCmd command = Mockito.mock(InspectContainerCmd.class);
        InspectContainerResponse response = new InspectContainerResponse();

        reset(dockerClient, command);

        when(dockerClient.inspectContainerCmd("container_inspect")).thenReturn(command);
        when(command.exec()).thenReturn(response);

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new ContainerInspect().container("container_inspect"))
                .build();
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

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new ImageInspect().image("image_inspect"))
                .build();
        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), response);

    }

    @Test
    public void testRemoveContainer() throws Exception {
        RemoveContainerCmd command = Mockito.mock(RemoveContainerCmd.class);

        reset(dockerClient, command);

        when(dockerClient.removeContainerCmd("container_inspect")).thenReturn(command);

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new ContainerRemove().container("container_inspect"))
                .build();
        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommand().getCommandResult()).getStatus(), "success");

    }

    @Test
    public void testRemoveImage() throws Exception {
        RemoveImageCmd command = Mockito.mock(RemoveImageCmd.class);

        reset(dockerClient, command);

        when(dockerClient.removeImageCmd("image_remove")).thenReturn(command);

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new ImageRemove().image("image_remove"))
                .build();
        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommand().getCommandResult()).getStatus(), "success");

    }

    @Test
    public void testStartContainer() throws Exception {
        StartContainerCmd command = Mockito.mock(StartContainerCmd.class);

        reset(dockerClient, command);

        when(dockerClient.startContainerCmd("container_start")).thenReturn(command);

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new ContainerStart().container("container_start"))
                .build();
        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommand().getCommandResult()).getStatus(), "success");

    }

    @Test
    public void testStopContainer() throws Exception {
        StopContainerCmd command = Mockito.mock(StopContainerCmd.class);

        reset(dockerClient, command);

        when(dockerClient.stopContainerCmd("container_stop")).thenReturn(command);

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new ContainerStop().container("container_stop"))
                .build();
        action.execute(context);

        Assert.assertEquals(((ResponseItem)action.getCommand().getCommandResult()).getStatus(), "success");

    }

    @Test
    public void testWaitContainer() throws Exception {
        WaitContainerCmd command = Mockito.mock(WaitContainerCmd.class);
        final WaitResponse responseItem = Mockito.mock(WaitResponse.class);

        reset(dockerClient, command);

        when(dockerClient.waitContainerCmd("container_wait")).thenReturn(command);
        doAnswer((Answer<WaitContainerResultCallback>) invocation -> {
            WaitContainerResultCallback resultCallback = (WaitContainerResultCallback) invocation.getArguments()[0];

            resultCallback.onNext(responseItem);
            resultCallback.onComplete();

            return resultCallback;
        }).when(command).exec(any(WaitContainerResultCallback.class));

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new ContainerWait().container("container_wait"))
                .build();
        action.execute(context);

        Assert.assertEquals(((WaitResponse)action.getCommand().getCommandResult()).getStatusCode(), new Integer(0));

    }

    @Test
    public void testPullImage() throws Exception {
        PullImageCmd command = Mockito.mock(PullImageCmd.class);
        final PullResponseItem responseItem = Mockito.mock(PullResponseItem.class);

        reset(dockerClient, command, responseItem);

        when(dockerClient.pullImageCmd("image_pull")).thenReturn(command);
        when(responseItem.getStatus()).thenReturn("Success");
        when(responseItem.isPullSuccessIndicated()).thenReturn(true);
        when(command.withTag("image_tag")).thenReturn(command);
        doAnswer((Answer<PullImageResultCallback>) invocation -> {
            PullImageResultCallback resultCallback = (PullImageResultCallback) invocation.getArguments()[0];

            resultCallback.onNext(responseItem);
            resultCallback.onComplete();

            return resultCallback;
        }).when(command).exec(any(PullImageResultCallback.class));

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new ImagePull().image("image_pull").tag("image_tag"))
                .build();
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
        when(command.withTags(Collections.singleton("latest"))).thenReturn(command);
        doAnswer((Answer<BuildImageResultCallback>) invocation -> {
            BuildImageResultCallback resultCallback = (BuildImageResultCallback) invocation.getArguments()[0];

            resultCallback.onNext(responseItem);
            resultCallback.onComplete();

            return resultCallback;
        }).when(command).exec(any(BuildImageResultCallback.class));

        DockerExecuteAction action = new DockerExecuteAction.Builder()
                .client(client)
                .command(new ImageBuild()
                        .dockerFile(Resources.fromClasspath("org/citrusframework/docker/Dockerfile"))
                        .tag("new_image:latest"))
                .build();
        action.execute(context);

        Assert.assertEquals(action.getCommand().getCommandResult(), responseItem);
        Assert.assertEquals(context.getVariable(DockerMessageHeaders.IMAGE_ID), "new_image");

    }
}
