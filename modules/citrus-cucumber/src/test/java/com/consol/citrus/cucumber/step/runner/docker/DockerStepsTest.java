/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.cucumber.step.runner.docker;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.docker.client.DockerEndpointConfiguration;
import com.consol.citrus.docker.command.*;
import com.consol.citrus.docker.message.DockerMessageHeaders;
import com.consol.citrus.dsl.annotations.CitrusDslAnnotations;
import com.consol.citrus.dsl.runner.DefaultTestRunner;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import cucumber.api.Scenario;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class DockerStepsTest extends AbstractTestNGUnitTest {

    private Citrus citrus;
    private DockerSteps steps;

    private TestRunner runner;

    @Autowired
    private DockerClient dockerClient;

    @BeforeClass
    public void setup() {
        citrus = Citrus.newInstance(applicationContext);
    }

    @BeforeMethod
    public void injectResources() {
        steps = new DockerSteps();
        runner = new DefaultTestRunner(applicationContext, context);
        CitrusAnnotations.injectAll(steps, citrus, context);
        CitrusDslAnnotations.injectTestRunner(steps, runner);
    }

    @Test
    public void testCreateContainer() {
        com.github.dockerjava.api.DockerClient dockerJavaClient = Mockito.mock(com.github.dockerjava.api.DockerClient.class);
        CreateContainerCmd createCmd = Mockito.mock(CreateContainerCmd.class);

        DockerEndpointConfiguration endpointConfiguration = new DockerEndpointConfiguration();
        endpointConfiguration.setDockerClient(dockerJavaClient);

        when(dockerClient.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        CreateContainerResponse response = new CreateContainerResponse();
        response.setId(UUID.randomUUID().toString());

        when(dockerJavaClient.createContainerCmd("fooImage:latest")).thenReturn(createCmd);
        when(createCmd.withName("foo")).thenReturn(createCmd);
        when(createCmd.exec()).thenReturn(response);

        steps.setClient("dockerClient");
        steps.createContainer("foo", "fooImage:latest");

        Assert.assertEquals(runner.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) runner.getTestCase().getTestAction(0);

        Assert.assertEquals(action.getDockerClient(), dockerClient);
        Assert.assertTrue(action.getCommand() instanceof ContainerCreate);
        Assert.assertEquals(action.getCommand().getParameters().get("name"), "foo");
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractDockerCommand.IMAGE_ID), "fooImage:latest");

        Assert.assertEquals(context.getVariable(DockerMessageHeaders.CONTAINER_ID), response.getId());
    }

    @Test
    public void testBuildImage() {
        com.github.dockerjava.api.DockerClient dockerJavaClient = Mockito.mock(com.github.dockerjava.api.DockerClient.class);
        BuildImageCmd buildCmd = Mockito.mock(BuildImageCmd.class);
        BuildResponseItem response = Mockito.mock(BuildResponseItem.class);

        DockerEndpointConfiguration endpointConfiguration = new DockerEndpointConfiguration();
        endpointConfiguration.setDockerClient(dockerJavaClient);

        when(dockerClient.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(dockerJavaClient.buildImageCmd()).thenReturn(buildCmd);

        when(response.isBuildSuccessIndicated()).thenReturn(true);
        when(response.isErrorIndicated()).thenReturn(false);
        when(response.getImageId()).thenReturn(UUID.randomUUID().toString());

        when(buildCmd.withTag("fooImage:latest")).thenReturn(buildCmd);
        when(buildCmd.withDockerfile(any(File.class))).thenReturn(buildCmd);
        when(buildCmd.exec(any(BuildImageResultCallback.class))).thenAnswer(invocation -> {
            ((BuildImageResultCallback) invocation.getArguments()[0]).onNext(response);
            ((BuildImageResultCallback) invocation.getArguments()[0]).close();
            
            return invocation.getArguments()[0];
        });

        steps.setClient("dockerClient");
        steps.buildImage("fooImage:latest", "classpath:docker/Dockerfile");

        Assert.assertEquals(runner.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) runner.getTestCase().getTestAction(0);

        Assert.assertEquals(action.getDockerClient(), dockerClient);
        Assert.assertTrue(action.getCommand() instanceof ImageBuild);
        Assert.assertEquals(action.getCommand().getParameters().get("tag"), "fooImage:latest");
        Assert.assertEquals(action.getCommand().getParameters().get("dockerfile"), "classpath:docker/Dockerfile");

        Assert.assertEquals(context.getVariable(DockerMessageHeaders.IMAGE_ID), response.getImageId());
    }

    @Test
    public void testStartContainer() {
        com.github.dockerjava.api.DockerClient dockerJavaClient = Mockito.mock(com.github.dockerjava.api.DockerClient.class);
        StartContainerCmd startCmd = Mockito.mock(StartContainerCmd.class);

        DockerEndpointConfiguration endpointConfiguration = new DockerEndpointConfiguration();
        endpointConfiguration.setDockerClient(dockerJavaClient);

        when(dockerClient.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(dockerJavaClient.startContainerCmd("foo")).thenReturn(startCmd);

        when(startCmd.withContainerId("foo")).thenReturn(startCmd);

        steps.setClient("dockerClient");
        steps.startContainer("foo");

        Assert.assertEquals(runner.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) runner.getTestCase().getTestAction(0);

        Assert.assertEquals(action.getDockerClient(), dockerClient);
        Assert.assertTrue(action.getCommand() instanceof ContainerStart);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractDockerCommand.CONTAINER_ID), "foo");
    }

    @Test
    public void testStopContainer() {
        com.github.dockerjava.api.DockerClient dockerJavaClient = Mockito.mock(com.github.dockerjava.api.DockerClient.class);
        StopContainerCmd stopCmd = Mockito.mock(StopContainerCmd.class);

        DockerEndpointConfiguration endpointConfiguration = new DockerEndpointConfiguration();
        endpointConfiguration.setDockerClient(dockerJavaClient);

        when(dockerClient.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(dockerJavaClient.stopContainerCmd("foo")).thenReturn(stopCmd);

        when(stopCmd.withContainerId("foo")).thenReturn(stopCmd);

        steps.setClient("dockerClient");
        steps.stopContainer("foo");

        Assert.assertEquals(runner.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) runner.getTestCase().getTestAction(0);

        Assert.assertEquals(action.getDockerClient(), dockerClient);
        Assert.assertTrue(action.getCommand() instanceof ContainerStop);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractDockerCommand.CONTAINER_ID), "foo");
    }

    @Test
    public void testContainerRunning() {
        com.github.dockerjava.api.DockerClient dockerJavaClient = Mockito.mock(com.github.dockerjava.api.DockerClient.class);
        InspectContainerCmd inspectCmd = Mockito.mock(InspectContainerCmd.class);
        InspectContainerResponse response = Mockito.mock(InspectContainerResponse.class);
        InspectContainerResponse.ContainerState state = Mockito.mock(InspectContainerResponse.ContainerState.class);

        DockerEndpointConfiguration endpointConfiguration = new DockerEndpointConfiguration();
        endpointConfiguration.setDockerClient(dockerJavaClient);

        when(dockerClient.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(dockerJavaClient.inspectContainerCmd("foo")).thenReturn(inspectCmd);

        when(response.getId()).thenReturn(UUID.randomUUID().toString());
        when(response.getState()).thenReturn(state);
        when(state.getRunning()).thenReturn(true);

        when(inspectCmd.withContainerId("foo")).thenReturn(inspectCmd);
        when(inspectCmd.exec()).thenReturn(response);

        steps.setClient("dockerClient");
        steps.containerIsRunning("foo");

        Assert.assertEquals(runner.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) runner.getTestCase().getTestAction(0);

        Assert.assertEquals(action.getDockerClient(), dockerClient);
        Assert.assertTrue(action.getCommand() instanceof ContainerInspect);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractDockerCommand.CONTAINER_ID), "foo");
    }

    @Test
    public void testContainerStopped() {
        com.github.dockerjava.api.DockerClient dockerJavaClient = Mockito.mock(com.github.dockerjava.api.DockerClient.class);
        InspectContainerCmd inspectCmd = Mockito.mock(InspectContainerCmd.class);
        InspectContainerResponse response = Mockito.mock(InspectContainerResponse.class);
        InspectContainerResponse.ContainerState state = Mockito.mock(InspectContainerResponse.ContainerState.class);

        DockerEndpointConfiguration endpointConfiguration = new DockerEndpointConfiguration();
        endpointConfiguration.setDockerClient(dockerJavaClient);

        when(dockerClient.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(dockerJavaClient.inspectContainerCmd("foo")).thenReturn(inspectCmd);

        when(response.getId()).thenReturn(UUID.randomUUID().toString());
        when(response.getState()).thenReturn(state);
        when(state.getRunning()).thenReturn(false);

        when(inspectCmd.withContainerId("foo")).thenReturn(inspectCmd);
        when(inspectCmd.exec()).thenReturn(response);

        steps.setClient("dockerClient");
        steps.containerIsStopped("foo");

        Assert.assertEquals(runner.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) runner.getTestCase().getTestAction(0);

        Assert.assertEquals(action.getDockerClient(), dockerClient);
        Assert.assertTrue(action.getCommand() instanceof ContainerInspect);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractDockerCommand.CONTAINER_ID), "foo");
    }

    @Test
    public void testDefaultClientInitialization() {
        Assert.assertNull(steps.dockerClient);
        steps.before(Mockito.mock(Scenario.class));
        Assert.assertNotNull(steps.dockerClient);
    }

    @Test
    public void testClientInitialization() {
        Assert.assertNull(steps.dockerClient);
        steps.setClient("dockerClient");
        steps.before(Mockito.mock(Scenario.class));
        Assert.assertNotNull(steps.dockerClient);
    }

}