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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CitrusArchiveBuilderTest {

    @Test
    public void testResolveAll() throws Exception {
        File[] artifactResources = CitrusArchiveBuilder
                .latestVersion()
                .transitivity(false)
                .all()
                .build();

        Assert.assertNotNull(artifactResources);
        Assert.assertEquals(artifactResources.length, 10);

        List<String> artifactFileNames = new ArrayList<>();
        for (File artifactResource : artifactResources) {
            artifactFileNames.add(artifactResource.getName());
        }

        String version = "2.2";
        Assert.assertTrue(artifactFileNames.contains("citrus-core-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-jms-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-http-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-ws-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-ftp-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-camel-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-ssh-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-mail-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-vertx-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-java-dsl-" + version + ".jar"));
    }

    @Test
    public void testResolveAllWithVersion() throws Exception {
        String version = "2.1";
        File[] artifactResources = CitrusArchiveBuilder
                .version(version)
                .transitivity(false)
                .all()
                .build();

        Assert.assertNotNull(artifactResources);
        Assert.assertEquals(artifactResources.length, 10);

        List<String> artifactFileNames = new ArrayList<>();
        for (File artifactResource : artifactResources) {
            artifactFileNames.add(artifactResource.getName());
        }

        Assert.assertTrue(artifactFileNames.contains("citrus-core-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-jms-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-http-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-ws-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-ftp-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-camel-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-ssh-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-mail-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-vertx-" + version + ".jar"));
        Assert.assertTrue(artifactFileNames.contains("citrus-java-dsl-" + version + ".jar"));
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

        Assert.assertEquals(artifactResources[0].getName(), "citrus-jms-2.2.jar");
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

        Assert.assertEquals(artifactResources[0].getName(), "citrus-core-2.2.jar");
    }
}