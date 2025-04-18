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

import org.citrusframework.CitrusSettings;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.spi.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @since 2.7
 */
public class FileUtilsTest extends UnitTestSupport {
    @Test
    public void testGetFileResource() {
        Resource resource = FileUtils.getFileResource("classpath:citrus-context.xml", context);

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    public void testGetFileResourceExplicitCharset() {
        Resource resource = FileUtils.getFileResource("classpath:citrus-context.xml" + FileUtils.FILE_PATH_CHARSET_PARAMETER + "ISO-8859-1", context);

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    public void testGetCharset() {
        assertEquals(FileUtils.getCharset("/path/to/some/file.txt").displayName(), CitrusSettings.CITRUS_FILE_ENCODING);
        assertEquals(FileUtils.getCharset("/path/to/some/file.txt" + FileUtils.FILE_PATH_CHARSET_PARAMETER + "ISO-8859-1"), StandardCharsets.ISO_8859_1);
    }

    @Test
    public void testGetBaseName() {
        Assert.assertNull(FileUtils.getBaseName(null));
        assertEquals(FileUtils.getBaseName(""), "");
        assertEquals(FileUtils.getBaseName("foo"), "foo");
        assertEquals(FileUtils.getBaseName("foo.xml"), "foo");
        assertEquals(FileUtils.getBaseName("/path/to/some/foo.xml"), "/path/to/some/foo");
        assertEquals(FileUtils.getBaseName("foo.bar.java"), "foo.bar");
    }

    @Test
    public void testGetFileName() {
        assertEquals(FileUtils.getFileName(null), "");
        assertEquals(FileUtils.getFileName(""), "");
        assertEquals(FileUtils.getFileName("foo"), "foo");
        assertEquals(FileUtils.getFileName("foo.xml"), "foo.xml");
        assertEquals(FileUtils.getFileName("/path/to/some/foo.xml"), "foo.xml");
        assertEquals(FileUtils.getFileName("foo.bar.java"), "foo.bar.java");
        assertEquals(FileUtils.getFileName("/path/to/some/foo.bar.java"), "foo.bar.java");
    }
}
