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

package org.citrusframework.docker.config.xml;

import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.command.*;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DockerExecuteActionParserTest extends AbstractActionParserTest<DockerExecuteAction> {

    @Test
    public void testDockerExecuteActionParser() {
        assertActionCount(14);
        assertActionClassAndName(DockerExecuteAction.class, "docker-execute");

        DockerExecuteAction action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(Info.class, action.getCommand().getClass());
        Assertions.assertEquals(DockerClient.class, action.getDockerClient().getClass());
        Assertions.assertEquals(0, action.getCommand().getParameters().size());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(Info.class, action.getCommand().getClass());
        Assertions.assertEquals(beanDefinitionContext.getBean("myDockerClient", DockerClient.class), action.getDockerClient());
        Assertions.assertEquals(0, action.getCommand().getParameters().size());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(Ping.class, action.getCommand().getClass());
        Assertions.assertEquals(0, action.getCommand().getParameters().size());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(Version.class, action.getCommand().getClass());
        Assertions.assertEquals(0, action.getCommand().getParameters().size());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ImagePull.class, action.getCommand().getClass());
        Assertions.assertEquals(4, action.getCommand().getParameters().size());
        Assertions.assertEquals("image_pull", action.getCommand().getParameters().get("image"));
        Assertions.assertEquals("image_tag", action.getCommand().getParameters().get("tag"));
        Assertions.assertEquals("docker_registry", action.getCommand().getParameters().get("registry"));
        Assertions.assertEquals("docker_repository", action.getCommand().getParameters().get("repository"));

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ImageBuild.class, action.getCommand().getClass());
        Assertions.assertEquals(6, action.getCommand().getParameters().size());
        Assertions.assertEquals("image_tag", action.getCommand().getParameters().get("tag"));
        Assertions.assertEquals("base_dir", action.getCommand().getParameters().get("basedir"));
        Assertions.assertEquals("path_to_dockerfile", action.getCommand().getParameters().get("dockerfile"));
        Assertions.assertEquals("true", action.getCommand().getParameters().get("no-cache"));
        Assertions.assertEquals("true", action.getCommand().getParameters().get("quiet"));
        Assertions.assertEquals("false", action.getCommand().getParameters().get("remove"));

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ImageRemove.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("image_remove", action.getCommand().getParameters().get("image"));

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ContainerRemove.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("container_remove", action.getCommand().getParameters().get("container"));

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ImageInspect.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("image_inspect", action.getCommand().getParameters().get("image"));

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ContainerInspect.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("container_inspect", action.getCommand().getParameters().get("container"));

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ContainerCreate.class, action.getCommand().getClass());
        Assertions.assertEquals(17, action.getCommand().getParameters().size());
        Assertions.assertEquals("image_create", action.getCommand().getParameters().get("image"));
        Assertions.assertEquals("echo 'Hello World'", action.getCommand().getParameters().get("cmd"));
        Assertions.assertEquals("CHOWN,KILL", action.getCommand().getParameters().get("capability-add"));
        Assertions.assertEquals("domain_name", action.getCommand().getParameters().get("domain-name"));
        Assertions.assertEquals("-Dsource.encoding=UTF-8", action.getCommand().getParameters().get("env"));
        Assertions.assertEquals("tcp:8080", action.getCommand().getParameters().get("exposed-ports"));
        Assertions.assertEquals("8088:8080", action.getCommand().getParameters().get("port-bindings"));
        Assertions.assertEquals("foo_host", action.getCommand().getParameters().get("hostname"));
        Assertions.assertEquals("foo_container", action.getCommand().getParameters().get("name"));
        Assertions.assertEquals(".", action.getCommand().getParameters().get("working-dir"));

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ContainerStart.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("container_start", action.getCommand().getParameters().get("container"));

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ContainerStop.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("container_stop", action.getCommand().getParameters().get("container"));

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ContainerWait.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("container_wait", action.getCommand().getParameters().get("container"));
    }

    @Test
    public void testDockerExecuteActionParserFailed() {
        BeanDefinitionStoreException e = assertThrows(BeanDefinitionStoreException.class, () -> createApplicationContext("failed"));
        Assertions.assertTrue(e.getCause().getMessage().startsWith("Both docker image and docker container are specified"));
    }

    @Test
    public void testDockerExecuteActionParserFailed2() {
        BeanDefinitionStoreException e = assertThrows(BeanDefinitionStoreException.class, () -> createApplicationContext("failed2"));
        Assertions.assertTrue(e.getCause().getMessage().startsWith("Missing docker image or docker container name attribute"));
    }
}
