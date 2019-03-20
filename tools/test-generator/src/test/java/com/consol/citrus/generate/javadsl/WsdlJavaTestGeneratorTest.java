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

package com.consol.citrus.generate.javadsl;

import com.consol.citrus.Citrus;
import com.consol.citrus.generate.UnitFramework;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.utils.CleanupUtils;
import org.springframework.core.io.FileSystemResource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class WsdlJavaTestGeneratorTest {

    private String testDir = Citrus.DEFAULT_TEST_SRC_DIRECTORY + "java/com/consol/citrus/";

    private final CleanupUtils cleanupUtils = new CleanupUtils();

    @AfterMethod
    public void cleanUp(){
        cleanupUtils.deleteFiles(testDir, Collections.singleton("BookStore*"));
    }

    @Test
    public void testCreateTest() throws IOException {
        WsdlJavaTestGenerator generator = new WsdlJavaTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("com.consol.citrus")
                .withFramework(UnitFramework.TESTNG);

        generator.withWsdl("com/consol/citrus/wsdl/BookStore.wsdl");

        generator.create();

        verifyTest("BookStore_addBook_IT", "book:addBook", "book:addBookResponse");
        verifyTest("BookStore_addBookAudio_IT", "aud:addBookAudio", "aud:addBookAudioResponse");
        verifyTest("BookStore_deleteBook_IT", "book:deleteBook", "book:deleteBookResponse");
    }

    private void verifyTest(String name, String requestName, String responseName) throws IOException {
        File javaFile = new File(testDir + name + ".java");
        Assert.assertTrue(javaFile.exists());

        String javaContent = FileUtils.readToString(new FileSystemResource(javaFile));
        Assert.assertTrue(javaContent.contains("@author Christoph"));
        Assert.assertTrue(javaContent.contains("public class " + name));
        Assert.assertTrue(javaContent.contains("* This is a sample test"));
        Assert.assertTrue(javaContent.contains("package com.consol.citrus;"));
        Assert.assertTrue(javaContent.contains("extends TestNGCitrusTestRunner"));
        Assert.assertTrue(javaContent.contains(requestName));
        Assert.assertTrue(javaContent.contains(responseName));
    }

}