/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.util;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;

/**
 * Tests the file helper default implementation.
 *
 * @author Martin.Maher@consol.de
 * @since 2013.04.22
 */
public class FileHelperImplTest {
    private FileHelperImpl testling = new FileHelperImpl();

    private File tmpDir = null;
    private File tmpFile = null;

    @BeforeTest
    public void setup() throws Exception {
        tmpDir = createRandomDirectory(getTmpDirectory());
        File tmpSubDir = createRandomDirectory(tmpDir);
        tmpFile = createTmpFile(tmpSubDir);
    }

    @AfterTest
    public void cleanup() throws Exception {
        FileUtils.deleteDirectory(tmpDir);
        tmpDir.delete();
    }

    @Test
    public void testFindFileInPath() throws Exception {
        File foundFile = testling.findFileInPath(tmpDir, tmpFile.getName(), true);
        Assert.assertNotNull(foundFile);
        Assert.assertEquals(foundFile.getAbsolutePath(), tmpFile.getAbsolutePath());

        foundFile = testling.findFileInPath(tmpDir, tmpFile.getName() + "_", true);
        Assert.assertNull(foundFile);
    }

    private File createTmpFile(File rootDirectory) throws IOException {
        return File.createTempFile("abc",".xml", rootDirectory);
    }

    private File createRandomDirectory(File rootDirectory) throws IOException {
        File tmpDir = new File(rootDirectory, RandomStringUtils.randomAlphanumeric(8));
        if(tmpDir.mkdir()) {
            return tmpDir;
        }
        throw new RuntimeException(String.format("Could not create directory '%s'", tmpDir.getAbsolutePath()));
    }

    private File getTmpDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }
}
