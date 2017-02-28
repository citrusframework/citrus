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

package com.consol.citrus.cucumber.step.designer.docker;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.docker.command.*;
import com.consol.citrus.dsl.annotations.CitrusDslAnnotations;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import cucumber.api.Scenario;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class DockerStepsTest extends AbstractTestNGUnitTest {

    private Citrus citrus;
    private DockerSteps steps;

    private TestDesigner designer;

    @Autowired
    private DockerClient dockerClient;

    @BeforeClass
    public void setup() {
        citrus = Citrus.newInstance(applicationContext);
    }

    @BeforeMethod
    public void injectResources() {
        steps = new DockerSteps();
        designer = new DefaultTestDesigner(applicationContext, context);
        CitrusAnnotations.injectAll(steps, citrus, context);
        CitrusDslAnnotations.injectTestDesigner(steps, designer);
    }

    @Test
    public void testCreateContainer() {
        steps.setClient("dockerClient");
        steps.createContainer("foo", "fooImage:latest");

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(designer.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) designer.getTestCase().getTestAction(0);

        Assert.assertEquals(action.getDockerClient(), dockerClient);
        Assert.assertTrue(action.getCommand() instanceof ContainerCreate);
        Assert.assertEquals(action.getCommand().getParameters().get("name"), "foo");
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractDockerCommand.IMAGE_ID), "fooImage:latest");
    }

    @Test
    public void testBuildImage() {
        steps.setClient("dockerClient");
        steps.buildImage("fooImage:latest", "classpath:docker/Dockerfile");

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(designer.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) designer.getTestCase().getTestAction(0);

        Assert.assertEquals(action.getDockerClient(), dockerClient);
        Assert.assertTrue(action.getCommand() instanceof ImageBuild);
        Assert.assertEquals(action.getCommand().getParameters().get("tag"), "fooImage:latest");
        Assert.assertEquals(action.getCommand().getParameters().get("dockerfile"), "classpath:docker/Dockerfile");
    }

    @Test
    public void testStartContainer() {
        steps.setClient("dockerClient");
        steps.startContainer("foo");

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(designer.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) designer.getTestCase().getTestAction(0);

        Assert.assertEquals(action.getDockerClient(), dockerClient);
        Assert.assertTrue(action.getCommand() instanceof ContainerStart);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractDockerCommand.CONTAINER_ID), "foo");
    }

    @Test
    public void testStopContainer() {
        steps.setClient("dockerClient");
        steps.stopContainer("foo");

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(designer.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) designer.getTestCase().getTestAction(0);

        Assert.assertEquals(action.getDockerClient(), dockerClient);
        Assert.assertTrue(action.getCommand() instanceof ContainerStop);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractDockerCommand.CONTAINER_ID), "foo");
    }

    @Test
    public void testContainerRunning() {
        steps.setClient("dockerClient");
        steps.containerIsRunning("foo");

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(designer.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) designer.getTestCase().getTestAction(0);

        Assert.assertEquals(action.getDockerClient(), dockerClient);
        Assert.assertTrue(action.getCommand() instanceof ContainerInspect);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractDockerCommand.CONTAINER_ID), "foo");
    }

    @Test
    public void testContainerStopped() {
        steps.setClient("dockerClient");
        steps.containerIsStopped("foo");

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(designer.getTestCase().getTestAction(0) instanceof DockerExecuteAction);
        DockerExecuteAction action = (DockerExecuteAction) designer.getTestCase().getTestAction(0);

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