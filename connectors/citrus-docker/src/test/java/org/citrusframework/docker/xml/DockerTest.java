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

package org.citrusframework.docker.xml;

import java.util.Map;

import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import org.citrusframework.TestCase;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.command.ContainerCreate;
import org.citrusframework.docker.command.ContainerRemove;
import org.citrusframework.docker.command.ImageBuild;
import org.citrusframework.docker.command.ImagePull;
import org.citrusframework.docker.command.ImageRemove;
import org.citrusframework.docker.command.Info;
import org.citrusframework.docker.command.Ping;
import org.citrusframework.docker.command.Version;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.XmlTestActionBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class DockerTest extends AbstractXmlActionTest {

    @Test
    public void shouldLookupDockerActionBuilder() {
        Assertions.assertTrue(XmlTestActionBuilder.lookup("docker").isPresent());
        Assertions.assertEquals(Docker.class, XmlTestActionBuilder.lookup("docker").get().getClass());
    }

    @Test
    public void shouldLoadInfoCommand() {
        DockerClient client = bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-info.citrus.it.xml");

        Assertions.assertEquals(Info.class, action.getCommand().getClass());
        Assertions.assertSame(client, action.getDockerClient());
    }

    @Test
    public void shouldLoadPingCommand() {
        DockerClient client = bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-ping.citrus.it.xml");

        Assertions.assertEquals(Ping.class, action.getCommand().getClass());
        Assertions.assertSame(client, action.getDockerClient());
        Assertions.assertEquals("""
                {"stream": null, "status": "success", "progressDetail": null, "progress": null, "id": null, "from": null, "time": null, "errorDetail": null, "error": null, "aux": null}""", action.getExpectedCommandResult());
    }

    @Test
    public void shouldLoadVersionCommand() {
        DockerClient client = bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-version.citrus.it.xml");

        Assertions.assertEquals(Version.class, action.getCommand().getClass());
        Assertions.assertSame(client, action.getDockerClient());
    }

    @Test
    public void shouldLoadCreateCommand() {
        DockerClient client = bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-create.citrus.it.xml");

        Assertions.assertEquals(ContainerCreate.class, action.getCommand().getClass());
        Map<String, Object> parameters = action.getCommand().getParameters();
        Assertions.assertEquals("citrus:latest", parameters.get("image"));
        Assertions.assertEquals("myContainer", parameters.get("name"));
        Assertions.assertEquals(true, parameters.get("attach-stdout"));
        Assertions.assertEquals(true, parameters.get("attach-stderr"));
        Assertions.assertEquals("NET_ADMIN,SYS_ADMIN", parameters.get("capability-add"));
        Assertions.assertEquals("MKNOD", parameters.get("capability-drop"));
        Assertions.assertEquals("sleep 5;echo done", parameters.get("cmd"));
        Assertions.assertEquals("JAVA_HOME=/opt/java;PATH=/usr/bin", parameters.get("env"));
        Assertions.assertEquals("citrusHost", parameters.get("hostname"));
        Assertions.assertEquals("citrusframework.org", parameters.get("domain-name"));
        Assertions.assertEquals("/tmp", parameters.get("working-dir"));
        Assertions.assertEquals("/bin/sh", parameters.get("entrypoint"));
        Assertions.assertEquals("8080;9090", parameters.get("port-specs"));
        Assertions.assertEquals("8080,9090", parameters.get("exposed-ports"));
        Assertions.assertEquals("80:8080,90:9090", parameters.get("port-bindings"));
        Assertions.assertEquals("/tmp:/tmp,/var:/var", parameters.get("volumes"));
        Assertions.assertEquals("Create a container from an image", action.getDescription());
        Assertions.assertSame(client, action.getDockerClient());
    }

    @Test
    public void shouldExecuteCreateCommand() {
        DockerClient client = bindDockerClient();
        DockerExecuteAction action = loadDockerAction("docker-create.citrus.it.xml");

        action.execute(context);

        var command = client.getEndpointConfiguration().getDockerClient().createContainerCmd("citrus:latest");
        verify(command, atLeastOnce()).withCmd("sleep 5", "echo done");
        verify(command, atLeastOnce()).withEnv("JAVA_HOME=/opt/java", "PATH=/usr/bin");
        verify(command, atLeastOnce()).withPortSpecs("8080", "9090");
        verify(command, atLeastOnce()).withExposedPorts(any(ExposedPort[].class));
        verify(command, atLeastOnce()).withPortBindings(any(Ports.class));
        verify(command, atLeastOnce()).withVolumes(any(Volume[].class));
        verify(command, atLeastOnce()).withCapAdd(Capability.NET_ADMIN, Capability.SYS_ADMIN);
        verify(command, atLeastOnce()).withCapDrop(Capability.MKNOD);
    }

    @Test
    public void shouldUseDefaultClientWhenNotConfigured() {
        DockerClient boundClient = bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-info-default-client.citrus.it.xml");

        Assertions.assertNotNull(action.getDockerClient());
        Assertions.assertNotSame(boundClient, action.getDockerClient());
    }

    @Test
    public void shouldLoadStartCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-start.citrus.it.xml");

        Assertions.assertEquals("myContainer", action.getCommand().getParameters().get("container"));
    }

    @Test
    public void shouldLoadStopCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-stop.citrus.it.xml");

        Assertions.assertEquals("myContainer", action.getCommand().getParameters().get("container"));
    }

    @Test
    public void shouldLoadRemoveCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-remove.citrus.it.xml");

        Assertions.assertEquals(ContainerRemove.class, action.getCommand().getClass());
        Assertions.assertEquals("myContainer", action.getCommand().getParameters().get("container"));
        Assertions.assertEquals(true, action.getCommand().getParameters().get("force"));
    }

    @Test
    public void shouldLoadWaitCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-wait.citrus.it.xml");

        Assertions.assertEquals("myContainer", action.getCommand().getParameters().get("container"));
    }

    @Test
    public void shouldLoadInspectCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-inspect.citrus.it.xml");

        Assertions.assertEquals("myContainer", action.getCommand().getParameters().get("container"));
    }

    @Test
    public void shouldLoadInspectImageCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-inspect-image.citrus.it.xml");

        Assertions.assertEquals("citrus:latest", action.getCommand().getParameters().get("image"));
    }

    @Test
    public void shouldLoadBuildImageCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-build-image.citrus.it.xml");

        Assertions.assertEquals(ImageBuild.class, action.getCommand().getClass());
        Map<String, Object> parameters = action.getCommand().getParameters();
        Assertions.assertEquals(true, parameters.get("no-cache"));
        Assertions.assertEquals("citrus:latest", parameters.get("tag"));
        Assertions.assertEquals(true, parameters.get("quiet"));
    }

    @Test
    public void shouldLoadPullImageCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-pull-image.citrus.it.xml");

        Assertions.assertEquals(ImagePull.class, action.getCommand().getClass());
        Map<String, Object> parameters = action.getCommand().getParameters();
        Assertions.assertEquals("citrus", parameters.get("image"));
        Assertions.assertEquals("latest", parameters.get("tag"));
        Assertions.assertEquals("docker.io", parameters.get("registry"));
    }

    @Test
    public void shouldLoadRemoveImageCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-remove-image.citrus.it.xml");

        Assertions.assertEquals(ImageRemove.class, action.getCommand().getClass());
        Assertions.assertEquals("citrus:latest", action.getCommand().getParameters().get("image"));
        Assertions.assertEquals(true, action.getCommand().getParameters().get("force"));
    }

    @Test
    public void shouldRejectUnknownCommand() {
        bindDockerClient();

        XmlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/docker/xml/docker-unknown.citrus.it.xml");

        Assertions.assertThrows(CitrusRuntimeException.class, testLoader::load);
    }

    private DockerExecuteAction loadDockerAction(String source) {
        XmlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/docker/xml/" + source);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        return (DockerExecuteAction) result.getTestAction(0);
    }
}
