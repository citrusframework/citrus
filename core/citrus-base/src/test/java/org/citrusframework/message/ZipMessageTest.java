/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.message;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class ZipMessageTest {

    @Test
    public void testAddSingleFile() throws Exception {
        ZipMessage message = new ZipMessage();
        message.addEntry(Resources.fromClasspath("org/citrusframework/archive/foo.txt"));
        File archive = new File (createTempDir().toFile(), "archive.zip");
        FileUtils.writeToFile(new ByteArrayInputStream(message.getPayload()), archive);

        Assert.assertTrue(archive.exists());

        ZipFile zipFile = new ZipFile(archive.getAbsolutePath());
        Assert.assertEquals(zipFile.size(), 1);

        Assert.assertNotNull(zipFile.getEntry("/foo.txt"));
        Assert.assertEquals(FileUtils.readToString(zipFile.getInputStream(new ZipEntry("/foo.txt"))), "Foo!");
    }

    @Test
    public void testAddDirectory() throws Exception {
        ZipMessage message = new ZipMessage();
        message.addEntry(Resources.fromClasspath("org/citrusframework/archive"));
        File archive = new File (createTempDir().toFile(), "archive.zip");
        FileUtils.writeToFile(new ByteArrayInputStream(message.getPayload()), archive);

        Assert.assertTrue(archive.exists());

        ZipFile zipFile = new ZipFile(archive.getAbsolutePath());
        Assert.assertEquals(zipFile.size(), 5);

        Assert.assertNotNull(zipFile.getEntry("/archive/"));
        Assert.assertTrue(zipFile.getEntry("/archive/").isDirectory());
        Assert.assertNotNull(zipFile.getEntry("archive/foo.txt"));
        Assert.assertEquals(FileUtils.readToString(zipFile.getInputStream(new ZipEntry("archive/foo.txt"))), "Foo!");
        Assert.assertNotNull(zipFile.getEntry("archive/bar.txt"));
        Assert.assertEquals(FileUtils.readToString(zipFile.getInputStream(new ZipEntry("archive/bar.txt"))), "Bar!");
        Assert.assertNotNull(zipFile.getEntry("archive/dir/"));
        Assert.assertTrue(zipFile.getEntry("archive/dir/").isDirectory());
        Assert.assertNotNull(zipFile.getEntry("archive/dir/sub_foo.txt"));
        Assert.assertEquals(FileUtils.readToString(zipFile.getInputStream(new ZipEntry("archive/dir/sub_foo.txt"))), "SubFoo!");
    }

    @Test
    public void testNewDirectoryStructure() throws Exception {
        ZipMessage message = new ZipMessage();
        message.addEntry(new ZipMessage.Entry("foos/")
                                        .addEntry(new ZipMessage.Entry("foo.txt",
                                                Resources.fromClasspath("org/citrusframework/archive/foo.txt").getFile())));

        File archive = new File (createTempDir().toFile(), "archive.zip");
        FileUtils.writeToFile(new ByteArrayInputStream(message.getPayload()), archive);

        Assert.assertTrue(archive.exists());

        ZipFile zipFile = new ZipFile(archive.getAbsolutePath());
        Assert.assertEquals(zipFile.size(), 2);

        Assert.assertNotNull(zipFile.getEntry("/foos/"));
        Assert.assertTrue(zipFile.getEntry("/foos/").isDirectory());
        Assert.assertNotNull(zipFile.getEntry("foos/foo.txt"));
        Assert.assertEquals(FileUtils.readToString(zipFile.getInputStream(new ZipEntry("foos/foo.txt"))), "Foo!");
    }

    @Test
    public void testEmptyDirectory() throws Exception {
        ZipMessage message = new ZipMessage();
        message.addEntry(new ZipMessage.Entry("foos/"));
        message.addEntry(new ZipMessage.Entry("bars/")
                                       .addEntry(new ZipMessage.Entry("bar.txt",
                                               Resources.fromClasspath("org/citrusframework/archive/bar.txt").getFile())));

        File archive = new File (createTempDir().toFile(), "archive.zip");
        FileUtils.writeToFile(new ByteArrayInputStream(message.getPayload()), archive);

        Assert.assertTrue(archive.exists());

        ZipFile zipFile = new ZipFile(archive.getAbsolutePath());
        Assert.assertEquals(zipFile.size(), 3);

        Assert.assertNotNull(zipFile.getEntry("/foos/"));
        Assert.assertTrue(zipFile.getEntry("/foos/").isDirectory());
        Assert.assertNotNull(zipFile.getEntry("/bars/"));
        Assert.assertTrue(zipFile.getEntry("/bars/").isDirectory());
        Assert.assertNotNull(zipFile.getEntry("bars/bar.txt"));
        Assert.assertEquals(FileUtils.readToString(zipFile.getInputStream(new ZipEntry("bars/bar.txt"))), "Bar!");
    }

    private Path createTempDir() throws IOException {
        Path tempDir = Files.createTempDirectory("citrus-core-");
        tempDir.toFile().deleteOnExit();

        System.out.println(tempDir.toAbsolutePath());
        return tempDir;
    }
}
