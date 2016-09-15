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

package com.consol.citrus.arquillian.shrinkwrap;

import com.consol.citrus.Citrus;
import org.springframework.util.ReflectionUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class CitrusArchiveBuilderTest {

    @BeforeClass
    public void setCitrusVersion() {
        Field version = ReflectionUtils.findField(Citrus.class, "version");
        ReflectionUtils.makeAccessible(version);
        ReflectionUtils.setField(version, Citrus.class, "2.7-SNAPSHOT");
    }

    @Test
    public void testResolveAll() throws Exception {
        File[] artifactResources = CitrusArchiveBuilder
                .latestVersion()
                .transitivity(false)
                .all()
                .build();

        Assert.assertNotNull(artifactResources);
        Assert.assertEquals(artifactResources.length, 17);

        verifyArtifact(artifactResources, "citrus-core-.*jar");
        verifyArtifact(artifactResources, "citrus-jms-.*jar");
        verifyArtifact(artifactResources, "citrus-http-.*jar");
        verifyArtifact(artifactResources, "citrus-websocket-.*jar");
        verifyArtifact(artifactResources, "citrus-ws-.*jar");
        verifyArtifact(artifactResources, "citrus-ftp-.*jar");
        verifyArtifact(artifactResources, "citrus-camel-.*jar");
        verifyArtifact(artifactResources, "citrus-ssh-.*jar");
        verifyArtifact(artifactResources, "citrus-mail-.*jar");
        verifyArtifact(artifactResources, "citrus-vertx-.*jar");
        verifyArtifact(artifactResources, "citrus-docker-.*jar");
        verifyArtifact(artifactResources, "citrus-cucumber-.*jar");
        verifyArtifact(artifactResources, "citrus-zookeeper-.*jar");
        verifyArtifact(artifactResources, "citrus-rmi-.*jar");
        verifyArtifact(artifactResources, "citrus-jmx-.*jar");
        verifyArtifact(artifactResources, "citrus-restdocs-.*jar");
        verifyArtifact(artifactResources, "citrus-java-dsl-.*jar");
    }

    @Test
    public void testResolveAllWithVersion() throws Exception {
        File[] artifactResources = CitrusArchiveBuilder
                .version(Citrus.getVersion())
                .transitivity(false)
                .all()
                .build();

        Assert.assertNotNull(artifactResources);
        Assert.assertEquals(artifactResources.length, 17);

        List<String> artifactFileNames = new ArrayList<>();
        for (File artifactResource : artifactResources) {
            artifactFileNames.add(artifactResource.getName());
        }

        verifyArtifact(artifactResources, "citrus-core-.*jar");
        verifyArtifact(artifactResources, "citrus-jms-.*jar");
        verifyArtifact(artifactResources, "citrus-http-.*jar");
        verifyArtifact(artifactResources, "citrus-websocket-.*jar");
        verifyArtifact(artifactResources, "citrus-ws-.*jar");
        verifyArtifact(artifactResources, "citrus-ftp-.*jar");
        verifyArtifact(artifactResources, "citrus-camel-.*jar");
        verifyArtifact(artifactResources, "citrus-ssh-.*jar");
        verifyArtifact(artifactResources, "citrus-mail-.*jar");
        verifyArtifact(artifactResources, "citrus-vertx-.*jar");
        verifyArtifact(artifactResources, "citrus-docker-.*jar");
        verifyArtifact(artifactResources, "citrus-cucumber-.*jar");
        verifyArtifact(artifactResources, "citrus-zookeeper-.*jar");
        verifyArtifact(artifactResources, "citrus-rmi-.*jar");
        verifyArtifact(artifactResources, "citrus-jmx-.*jar");
        verifyArtifact(artifactResources, "citrus-restdocs-.*jar");
        verifyArtifact(artifactResources, "citrus-java-dsl-.*jar");
    }

    @Test
    public void testResolveSelective() throws Exception {
        File[] artifactResources = CitrusArchiveBuilder
                .latestVersion()
                .transitivity(false)
                .jms()
                .build();

        Assert.assertNotNull(artifactResources);
        Assert.assertEquals(artifactResources.length, 1);

        verifyArtifact(artifactResources, "citrus-jms-.*jar");
    }

    @Test
    public void testResolveOnline() throws Exception {
        File[] artifactResources = CitrusArchiveBuilder
                .latestVersion()
                .transitivity(false)
                .workOffline(false)
                .core()
                .build();

        Assert.assertNotNull(artifactResources);
        Assert.assertEquals(artifactResources.length, 1);

        verifyArtifact(artifactResources, "citrus-core-.*jar");
    }

    private void verifyArtifact(File[] artifactResources, String expectedFileNamePattern) {
        for (File artifactResource : artifactResources) {
            if (artifactResource.getName().matches(expectedFileNamePattern)) {
                return;
            }
        }

        Assert.fail("Missing artifact resource for file name pattern: " + expectedFileNamePattern);
    }
}