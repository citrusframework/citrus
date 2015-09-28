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

package com.consol.citrus.docker.config.xml;

import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.docker.command.*;
import com.consol.citrus.testng.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DockerExecuteActionParserTest extends AbstractActionParserTest<DockerExecuteAction> {

    @Test
    public void testDockerExecuteActionParser() {
        assertActionCount(13);
        assertActionClassAndName(DockerExecuteAction.class, "docker-execute");

        DockerExecuteAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), Info.class);
        Assert.assertEquals(action.getDockerClient().getClass(), DockerClient.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), Info.class);
        Assert.assertEquals(action.getDockerClient(), beanDefinitionContext.getBean("myDockerClient", DockerClient.class));
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), Ping.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), Version.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), ImagePull.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 4);
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("image"), "image_pull");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("tag"), "image_tag");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("registry"), "docker_registry");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("repository"), "docker_repository");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), ImageBuild.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 6);
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("tag"), "image_tag");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("basedir"), "base_dir");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("dockerfile"), "path_to_dockerfile");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("no-cache"), "true");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("quiet"), "true");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("remove"), "false");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), ImageRemove.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 1);
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("image"), "image_remove");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), ContainerRemove.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 1);
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("container"), "container_remove");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), ImageInspect.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 1);
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("image"), "image_inspect");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), ContainerInspect.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 1);
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("container"), "container_inspect");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), ContainerCreate.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 16);
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("image"), "image_create");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("cmd"), "echo 'Hello World'");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("capability-add"), "CHOWN,KILL");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("domain-name"), "domain_name");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("env"), "-Dsource.encoding=UTF-8");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("exposed-ports"), "tcp:8080");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("hostname"), "foo_host");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("name"), "foo_container");
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("working-dir"), ".");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), ContainerStart.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 1);
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("container"), "container_start");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommands().get(0));
        Assert.assertEquals(action.getCommands().get(0).getClass(), ContainerStop.class);
        Assert.assertEquals(action.getCommands().get(0).getParameters().size(), 1);
        Assert.assertEquals(action.getCommands().get(0).getParameters().get("container"), "container_stop");
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