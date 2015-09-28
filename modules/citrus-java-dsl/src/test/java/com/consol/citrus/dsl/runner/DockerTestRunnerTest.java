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
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.DockerActionBuilder;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Version;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.3.1
 */
public class DockerTestRunnerTest extends AbstractTestNGUnitTest {

    private DockerClient dockerClient = EasyMock.createMock(DockerClient.class);

    @Test
    public void testDockerBuilder() {
        InfoCmd infoCmd = EasyMock.createMock(InfoCmd.class);
        PingCmd pingCmd = EasyMock.createMock(PingCmd.class);
        VersionCmd versionCmd = EasyMock.createMock(VersionCmd.class);
        CreateContainerCmd createCmd = EasyMock.createMock(CreateContainerCmd.class);
        InspectContainerCmd inspectCmd = EasyMock.createMock(InspectContainerCmd.class);

        CreateContainerResponse response = new CreateContainerResponse();
        response.setId(UUID.randomUUID().toString());

        reset(dockerClient, infoCmd, pingCmd, versionCmd, createCmd, inspectCmd);

        expect(dockerClient.infoCmd()).andReturn(infoCmd).once();
        expect(infoCmd.exec()).andReturn(new Info()).once();

        expect(dockerClient.pingCmd()).andReturn(pingCmd).once();
        expect(pingCmd.exec()).andReturn(null).once();

        expect(dockerClient.versionCmd()).andReturn(versionCmd).once();
        expect(versionCmd.exec()).andReturn(new Version()).once();

        expect(dockerClient.createContainerCmd("new_image")).andReturn(createCmd).once();
        expect(createCmd.withName("my_container")).andReturn(createCmd).once();
        expect(createCmd.exec()).andReturn(response).once();

        expect(dockerClient.inspectContainerCmd("my_container")).andReturn(inspectCmd).once();
        expect(inspectCmd.exec()).andReturn(new InspectContainerResponse()).once();

        replay(dockerClient, infoCmd, pingCmd, versionCmd, createCmd, inspectCmd);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                docker(new BuilderSupport<DockerActionBuilder>() {
                    @Override
                    public void configure(DockerActionBuilder builder) {
                        builder.client(new com.consol.citrus.docker.client.DockerClient(dockerClient))
                            .info()
                            .ping()
                            .version()
                            .create("new_image")
                                .withParam("name", "my_container")
                            .inspectContainer("my_container");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DockerExecuteAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), DockerExecuteAction.class);

        DockerExecuteAction action = (DockerExecuteAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "docker-execute");
        Assert.assertEquals(action.getCommands().size(), 5);

        verify(dockerClient, infoCmd, pingCmd, versionCmd, createCmd, inspectCmd);
    }
}
