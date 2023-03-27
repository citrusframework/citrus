/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.citrus.util;

import java.nio.charset.Charset;

import org.citrusframework.citrus.CitrusSettings;
import org.citrusframework.citrus.UnitTestSupport;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class FileUtilsTest extends UnitTestSupport {
    @Test
    public void testGetFileResource() throws Exception {
        Resource resource = FileUtils.getFileResource("classpath:citrus-context.xml", context);

        Assert.assertNotNull(resource);
        Assert.assertTrue(resource.exists());
    }

    @Test
    public void testGetFileResourceExplicitCharset() throws Exception {
        Resource resource = FileUtils.getFileResource("classpath:citrus-context.xml" + FileUtils.FILE_PATH_CHARSET_PARAMETER + "ISO-8859-1", context);

        Assert.assertNotNull(resource);
        Assert.assertTrue(resource.exists());
    }

    @Test
    public void testGetCharset() throws Exception {
        Assert.assertEquals(FileUtils.getCharset("/path/to/some/file.txt").displayName(), CitrusSettings.CITRUS_FILE_ENCODING);
        Assert.assertEquals(FileUtils.getCharset("/path/to/some/file.txt" + FileUtils.FILE_PATH_CHARSET_PARAMETER + "ISO-8859-1"), Charset.forName("ISO-8859-1"));
    }

    @Test
    public void testGetBaseName() throws Exception {
        Assert.assertNull(FileUtils.getBaseName(null));
        Assert.assertEquals(FileUtils.getBaseName(""), "");
        Assert.assertEquals(FileUtils.getBaseName("foo"), "foo");
        Assert.assertEquals(FileUtils.getBaseName("foo.xml"), "foo");
        Assert.assertEquals(FileUtils.getBaseName("foo.bar.java"), "foo.bar");
    }

}
