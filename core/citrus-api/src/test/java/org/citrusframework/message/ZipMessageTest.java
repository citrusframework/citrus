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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.7.5
 */
public class ZipMessageTest {

    @Test
    public void testAddSingleFile() throws Exception {
        ZipMessage message = new ZipMessage();
        message.addEntry(Resources.fromClasspath("org/citrusframework/archive/foo.txt"));
        File archive = new File (createTempDir().toFile(), "archive.zip");
        FileUtils.writeToFile(new ByteArrayInputStream(message.getPayload()), archive);

        Assertions.assertTrue(archive.exists());

        try (var zipFile = new ZipFile(archive.getAbsolutePath())) {
            Assertions.assertEquals(1, zipFile.size());

            Assertions.assertNotNull(zipFile.getEntry("/foo.txt"));
            Assertions.assertEquals("Foo!", FileUtils.readToString(zipFile.getInputStream(new ZipEntry("/foo.txt"))));
        }
    }

    @Test
    public void testAddDirectory() throws Exception {
        ZipMessage message = new ZipMessage();
        message.addEntry(Resources.fromClasspath("org/citrusframework/archive"));
        File archive = new File (createTempDir().toFile(), "archive.zip");
        FileUtils.writeToFile(new ByteArrayInputStream(message.getPayload()), archive);

        Assertions.assertTrue(archive.exists());

        try (var zipFile = new ZipFile(archive.getAbsolutePath())) {
            Assertions.assertEquals(5, zipFile.size());

            Assertions.assertNotNull(zipFile.getEntry("/archive/"));
            Assertions.assertTrue(zipFile.getEntry("/archive/").isDirectory());
            Assertions.assertNotNull(zipFile.getEntry("archive/foo.txt"));
            Assertions.assertEquals("Foo!", FileUtils.readToString(zipFile.getInputStream(new ZipEntry("archive/foo.txt"))));
            Assertions.assertNotNull(zipFile.getEntry("archive/bar.txt"));
            Assertions.assertEquals("Bar!", FileUtils.readToString(zipFile.getInputStream(new ZipEntry("archive/bar.txt"))));
            Assertions.assertNotNull(zipFile.getEntry("archive/dir/"));
            Assertions.assertTrue(zipFile.getEntry("archive/dir/").isDirectory());
            Assertions.assertNotNull(zipFile.getEntry("archive/dir/sub_foo.txt"));
            Assertions.assertEquals("SubFoo!", FileUtils.readToString(zipFile.getInputStream(new ZipEntry("archive/dir/sub_foo.txt"))));
        }
    }

    @Test
    public void testNewDirectoryStructure() throws Exception {
        ZipMessage message = new ZipMessage();
        message.addEntry(new ZipMessage.Entry("foos/")
                                        .addEntry(new ZipMessage.Entry("foo.txt",
                                                Resources.fromClasspath("org/citrusframework/archive/foo.txt").file())));

        File archive = new File (createTempDir().toFile(), "archive.zip");
        FileUtils.writeToFile(new ByteArrayInputStream(message.getPayload()), archive);

        Assertions.assertTrue(archive.exists());

        try (var zipFile = new ZipFile(archive.getAbsolutePath())) {
            Assertions.assertEquals(2, zipFile.size());

            Assertions.assertNotNull(zipFile.getEntry("/foos/"));
            Assertions.assertTrue(zipFile.getEntry("/foos/").isDirectory());
            Assertions.assertNotNull(zipFile.getEntry("foos/foo.txt"));
            Assertions.assertEquals("Foo!", FileUtils.readToString(zipFile.getInputStream(new ZipEntry("foos/foo.txt"))));
        }
    }

    @Test
    public void testEmptyDirectory() throws Exception {
        ZipMessage message = new ZipMessage();
        message.addEntry(new ZipMessage.Entry("foos/"));
        message.addEntry(new ZipMessage.Entry("bars/")
                                       .addEntry(new ZipMessage.Entry("bar.txt",
                                               Resources.fromClasspath("org/citrusframework/archive/bar.txt").file())));

        File archive = new File (createTempDir().toFile(), "archive.zip");
        FileUtils.writeToFile(new ByteArrayInputStream(message.getPayload()), archive);

        Assertions.assertTrue(archive.exists());

        try (var zipFile = new ZipFile(archive.getAbsolutePath())) {
            Assertions.assertEquals(3, zipFile.size());

            Assertions.assertNotNull(zipFile.getEntry("/foos/"));
            Assertions.assertTrue(zipFile.getEntry("/foos/").isDirectory());
            Assertions.assertNotNull(zipFile.getEntry("/bars/"));
            Assertions.assertTrue(zipFile.getEntry("/bars/").isDirectory());
            Assertions.assertNotNull(zipFile.getEntry("bars/bar.txt"));
            Assertions.assertEquals("Bar!", FileUtils.readToString(zipFile.getInputStream(new ZipEntry("bars/bar.txt"))));
        }
    }

    private Path createTempDir() throws IOException {
        Path tempDir = Files.createTempDirectory("citrus-core-");
        tempDir.toFile().deleteOnExit();
        return tempDir;
    }
}
