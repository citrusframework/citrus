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

package org.citrusframework.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @since 2.7
 */
public class FileUtilsTest {
    @Test
    public void testGetFileResource() {
        Resource resource = FileUtils.getFileResource("classpath:citrus-context.xml", new TestContext());

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void readFileToString_shouldNotLockFile() throws Exception {
        Path tempFile = Files.createTempFile("citrus-test", ".txt");
        Files.writeString(tempFile, "hello citrus");

        String content = FileUtils.readToString(tempFile.toFile());

        assertEquals("hello citrus", content);
        assertTrue(Files.deleteIfExists(tempFile), "File should be deletable after readToString — indicates InputStream was closed");
    }

    @Test
    void readResourceToString_shouldNotLockFile() throws Exception {
        Path tempFile = Files.createTempFile("citrus-test", ".txt");
        Files.writeString(tempFile, "hello citrus");

        Resource resource = Resources.create(tempFile.toFile());
        String content = FileUtils.readToString(resource);

        assertEquals("hello citrus", content);
        assertTrue(Files.deleteIfExists(tempFile), "File should be deletable after readToString — indicates InputStream was closed");
    }

    @Test
    public void testGetFileResourceExplicitCharset() {
        Resource resource = FileUtils.getFileResource("classpath:citrus-context.xml" + FileUtils.FILE_PATH_CHARSET_PARAMETER + "ISO-8859-1", new TestContext());

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    public void testGetCharset() {
        assertEquals(CitrusSettings.getFileEncoding(), FileUtils.getCharset("/path/to/some/file.txt").displayName());
        assertEquals(StandardCharsets.ISO_8859_1, FileUtils.getCharset("/path/to/some/file.txt" + FileUtils.FILE_PATH_CHARSET_PARAMETER + "ISO-8859-1"));
    }

    @Test
    public void testGetBaseName() {
        Assertions.assertNull(FileUtils.getBaseName(null));
        assertEquals("", FileUtils.getBaseName(""));
        assertEquals("foo", FileUtils.getBaseName("foo"));
        assertEquals("foo", FileUtils.getBaseName("foo.xml"));
        assertEquals("/path/to/some/foo", FileUtils.getBaseName("/path/to/some/foo.xml"));
        assertEquals("foo.bar", FileUtils.getBaseName("foo.bar.java"));
    }

    @Test
    public void testGetFileName() {
        assertEquals("", FileUtils.getFileName(null));
        assertEquals("", FileUtils.getFileName(""));
        assertEquals("foo", FileUtils.getFileName("foo"));
        assertEquals("foo.xml", FileUtils.getFileName("foo.xml"));
        assertEquals("foo.xml", FileUtils.getFileName("/path/to/some/foo.xml"));
        assertEquals("foo.bar.java", FileUtils.getFileName("foo.bar.java"));
        assertEquals("foo.bar.java", FileUtils.getFileName("/path/to/some/foo.bar.java"));
    }
}
