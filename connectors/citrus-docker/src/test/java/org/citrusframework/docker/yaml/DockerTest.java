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

package org.citrusframework.docker.yaml;

import java.util.Map;

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
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DockerTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadInfoCommand() {
        DockerClient client = bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-info.citrus.it.yaml");

        Assertions.assertEquals(Info.class, action.getCommand().getClass());
        Assertions.assertSame(client, action.getDockerClient());
    }

    @Test
    public void shouldLoadPingCommand() {
        DockerClient client = bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-ping.citrus.it.yaml");

        Assertions.assertEquals(Ping.class, action.getCommand().getClass());
        Assertions.assertSame(client, action.getDockerClient());
        Assertions.assertEquals("""
                {"stream": null, "status": "success", "progressDetail": null, "progress": null, "id": null, "from": null, "time": null, "errorDetail": null, "error": null, "aux": null}""", action.getExpectedCommandResult());
    }

    @Test
    public void shouldLoadVersionCommand() {
        DockerClient client = bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-version.citrus.it.yaml");

        Assertions.assertEquals(Version.class, action.getCommand().getClass());
        Assertions.assertSame(client, action.getDockerClient());
    }

    @Test
    public void shouldLoadCreateCommand() {
        DockerClient client = bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-create.citrus.it.yaml");

        Assertions.assertEquals(ContainerCreate.class, action.getCommand().getClass());
        Map<String, Object> parameters = action.getCommand().getParameters();
        Assertions.assertEquals("citrus:latest", parameters.get("image"));
        Assertions.assertEquals("myContainer", parameters.get("name"));
        Assertions.assertEquals(true, parameters.get("attach-stdout"));
        Assertions.assertEquals(true, parameters.get("attach-stderr"));
        Assertions.assertArrayEquals(new String[]{ "sleep 5" }, (String[]) parameters.get("cmd"));
        Assertions.assertArrayEquals(new String[]{ "JAVA_HOME=/opt/java" }, (String[]) parameters.get("env"));
        Assertions.assertEquals("citrusHost", parameters.get("hostname"));
        Assertions.assertEquals("citrusframework.org", parameters.get("domain-name"));
        Assertions.assertEquals("/tmp", parameters.get("working-dir"));
        Assertions.assertEquals("/bin/sh", parameters.get("entrypoint"));
        Assertions.assertSame(client, action.getDockerClient());
    }

    @Test
    public void shouldLoadStartCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-start.citrus.it.yaml");

        Assertions.assertEquals("myContainer", action.getCommand().getParameters().get("container"));
    }

    @Test
    public void shouldLoadStopCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-stop.citrus.it.yaml");

        Assertions.assertEquals("myContainer", action.getCommand().getParameters().get("container"));
    }

    @Test
    public void shouldLoadRemoveCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-remove.citrus.it.yaml");

        Assertions.assertEquals(ContainerRemove.class, action.getCommand().getClass());
        Assertions.assertEquals("myContainer", action.getCommand().getParameters().get("container"));
        Assertions.assertEquals(true, action.getCommand().getParameters().get("force"));
    }

    @Test
    public void shouldLoadWaitCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-wait.citrus.it.yaml");

        Assertions.assertEquals("myContainer", action.getCommand().getParameters().get("container"));
    }

    @Test
    public void shouldLoadInspectCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-inspect.citrus.it.yaml");

        Assertions.assertEquals("myContainer", action.getCommand().getParameters().get("container"));
    }

    @Test
    public void shouldLoadInspectImageCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-inspect-image.citrus.it.yaml");

        Assertions.assertEquals("citrus:latest", action.getCommand().getParameters().get("image"));
    }

    @Test
    public void shouldLoadBuildImageCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-build-image.citrus.it.yaml");

        Assertions.assertEquals(ImageBuild.class, action.getCommand().getClass());
        Map<String, Object> parameters = action.getCommand().getParameters();
        Assertions.assertEquals(true, parameters.get("no-cache"));
        Assertions.assertEquals("citrus:latest", parameters.get("tag"));
        Assertions.assertEquals(true, parameters.get("quiet"));
    }

    @Test
    public void shouldLoadPullImageCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-pull-image.citrus.it.yaml");

        Assertions.assertEquals(ImagePull.class, action.getCommand().getClass());
        Map<String, Object> parameters = action.getCommand().getParameters();
        Assertions.assertEquals("citrus", parameters.get("image"));
        Assertions.assertEquals("latest", parameters.get("tag"));
        Assertions.assertEquals("docker.io", parameters.get("registry"));
    }

    @Test
    public void shouldLoadRemoveImageCommand() {
        bindDockerClient();

        DockerExecuteAction action = loadDockerAction("docker-remove-image.citrus.it.yaml");

        Assertions.assertEquals(ImageRemove.class, action.getCommand().getClass());
        Assertions.assertEquals("citrus:latest", action.getCommand().getParameters().get("image"));
        Assertions.assertEquals(true, action.getCommand().getParameters().get("force"));
    }

    @Test
    public void shouldRejectUnknownCommand() {
        bindDockerClient();

        YamlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/docker/yaml/docker-unknown.citrus.it.yaml");

        Assertions.assertThrows(CitrusRuntimeException.class, testLoader::load);
    }

    private DockerExecuteAction loadDockerAction(String source) {
        YamlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/docker/yaml/" + source);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        return (DockerExecuteAction) result.getTestAction(0);
    }
}
