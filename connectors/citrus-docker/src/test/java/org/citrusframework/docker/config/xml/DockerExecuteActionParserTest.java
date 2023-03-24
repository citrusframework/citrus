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

package org.citrusframework.docker.config.xml;

import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.command.*;
import org.citrusframework.testng.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DockerExecuteActionParserTest extends AbstractActionParserTest<DockerExecuteAction> {

    @Test
    public void testDockerExecuteActionParser() {
        assertActionCount(14);
        assertActionClassAndName(DockerExecuteAction.class, "docker-execute");

        DockerExecuteAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), Info.class);
        Assert.assertEquals(action.getDockerClient().getClass(), DockerClient.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), Info.class);
        Assert.assertEquals(action.getDockerClient(), beanDefinitionContext.getBean("myDockerClient", DockerClient.class));
        Assert.assertEquals(action.getCommand().getParameters().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), Ping.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), Version.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ImagePull.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 4);
        Assert.assertEquals(action.getCommand().getParameters().get("image"), "image_pull");
        Assert.assertEquals(action.getCommand().getParameters().get("tag"), "image_tag");
        Assert.assertEquals(action.getCommand().getParameters().get("registry"), "docker_registry");
        Assert.assertEquals(action.getCommand().getParameters().get("repository"), "docker_repository");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ImageBuild.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 6);
        Assert.assertEquals(action.getCommand().getParameters().get("tag"), "image_tag");
        Assert.assertEquals(action.getCommand().getParameters().get("basedir"), "base_dir");
        Assert.assertEquals(action.getCommand().getParameters().get("dockerfile"), "path_to_dockerfile");
        Assert.assertEquals(action.getCommand().getParameters().get("no-cache"), "true");
        Assert.assertEquals(action.getCommand().getParameters().get("quiet"), "true");
        Assert.assertEquals(action.getCommand().getParameters().get("remove"), "false");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ImageRemove.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get("image"), "image_remove");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ContainerRemove.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get("container"), "container_remove");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ImageInspect.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get("image"), "image_inspect");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ContainerInspect.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get("container"), "container_inspect");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ContainerCreate.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 17);
        Assert.assertEquals(action.getCommand().getParameters().get("image"), "image_create");
        Assert.assertEquals(action.getCommand().getParameters().get("cmd"), "echo 'Hello World'");
        Assert.assertEquals(action.getCommand().getParameters().get("capability-add"), "CHOWN,KILL");
        Assert.assertEquals(action.getCommand().getParameters().get("domain-name"), "domain_name");
        Assert.assertEquals(action.getCommand().getParameters().get("env"), "-Dsource.encoding=UTF-8");
        Assert.assertEquals(action.getCommand().getParameters().get("exposed-ports"), "tcp:8080");
        Assert.assertEquals(action.getCommand().getParameters().get("port-bindings"), "8088:8080");
        Assert.assertEquals(action.getCommand().getParameters().get("hostname"), "foo_host");
        Assert.assertEquals(action.getCommand().getParameters().get("name"), "foo_container");
        Assert.assertEquals(action.getCommand().getParameters().get("working-dir"), ".");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ContainerStart.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get("container"), "container_start");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ContainerStop.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get("container"), "container_stop");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ContainerWait.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get("container"), "container_wait");
    }

    @Test
    public void testDockerExecuteActionParserFailed() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to invalid attributes");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertTrue(e.getCause().getMessage().startsWith("Both docker image and docker container are specified"));
        }
    }

    @Test
    public void testDockerExecuteActionParserFailed2() {
        try {
            createApplicationContext("failed2");
            Assert.fail("Missing bean creation exception due to missing attributes");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertTrue(e.getCause().getMessage().startsWith("Missing docker image or docker container name attribute"));
        }
    }
}
