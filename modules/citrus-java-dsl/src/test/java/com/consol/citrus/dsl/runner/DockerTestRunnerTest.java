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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.docker.command.CommandResultCallback;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.DockerActionBuilder;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Version;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class DockerTestRunnerTest extends AbstractTestNGUnitTest {

    private com.github.dockerjava.api.DockerClient dockerClient = Mockito.mock(com.github.dockerjava.api.DockerClient.class);

    @Test
    public void testDockerBuilder() {
        InfoCmd infoCmd = Mockito.mock(InfoCmd.class);
        PingCmd pingCmd = Mockito.mock(PingCmd.class);
        VersionCmd versionCmd = Mockito.mock(VersionCmd.class);
        CreateContainerCmd createCmd = Mockito.mock(CreateContainerCmd.class);
        InspectContainerCmd inspectCmd = Mockito.mock(InspectContainerCmd.class);

        CreateContainerResponse response = new CreateContainerResponse();
        response.setId(UUID.randomUUID().toString());

        reset(dockerClient, infoCmd, pingCmd, versionCmd, createCmd, inspectCmd);

        when(dockerClient.infoCmd()).thenReturn(infoCmd);
        when(infoCmd.exec()).thenReturn(new Info());

        when(dockerClient.pingCmd()).thenReturn(pingCmd);
        when(pingCmd.exec()).thenReturn(null);

        when(dockerClient.versionCmd()).thenReturn(versionCmd);
        when(versionCmd.exec()).thenReturn(new Version());

        when(dockerClient.createContainerCmd("new_image")).thenReturn(createCmd);
        when(createCmd.withName("my_container")).thenReturn(createCmd);
        when(createCmd.exec()).thenReturn(response);

        when(dockerClient.inspectContainerCmd("my_container")).thenReturn(inspectCmd);
        when(inspectCmd.exec()).thenReturn(new InspectContainerResponse());

        final DockerClient client = new com.consol.citrus.docker.client.DockerClient();
        client.getEndpointConfiguration().setDockerClient(dockerClient);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                docker(new BuilderSupport<DockerActionBuilder>() {
                    @Override
                    public void configure(DockerActionBuilder builder) {
                        builder.client(client)
                            .info();
                    }
                });

                docker(new BuilderSupport<DockerActionBuilder>() {
                    @Override
                    public void configure(DockerActionBuilder builder) {
                        builder.client(client)
                            .ping();
                    }
                });

                docker(new BuilderSupport<DockerActionBuilder>() {
                    @Override
                    public void configure(DockerActionBuilder builder) {
                        builder.client(client)
                            .version()
                            .validateCommandResult(new CommandResultCallback<Version>() {
                                @Override
                                public void doWithCommandResult(Version result, TestContext context) {
                                    Assert.assertNotNull(result);
                                }
                            });
                    }
                });

                docker(new BuilderSupport<DockerActionBuilder>() {
                    @Override
                    public void configure(DockerActionBuilder builder) {
                        builder.client(client)
                            .create("new_image")
                                .name("my_container");
                    }
                });

                docker(new BuilderSupport<DockerActionBuilder>() {
                    @Override
                    public void configure(DockerActionBuilder builder) {
                        builder.client(client)
                            .inspectContainer("my_container");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 5);
        Assert.assertEquals(test.getActions().get(0).getClass(), DockerExecuteAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), DockerExecuteAction.class);

        DockerExecuteAction action = (DockerExecuteAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "docker-execute");
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.docker.command.Info.class);

        action = (DockerExecuteAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "docker-execute");
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.docker.command.Ping.class);

        action = (DockerExecuteAction)test.getActions().get(2);
        Assert.assertEquals(action.getName(), "docker-execute");
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.docker.command.Version.class);
        Assert.assertNotNull(action.getCommand().getResultCallback());

        action = (DockerExecuteAction)test.getActions().get(3);
        Assert.assertEquals(action.getName(), "docker-execute");
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.docker.command.ContainerCreate.class);

        action = (DockerExecuteAction)test.getActions().get(4);
        Assert.assertEquals(action.getName(), "docker-execute");
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.docker.command.ContainerInspect.class);

    }
}
