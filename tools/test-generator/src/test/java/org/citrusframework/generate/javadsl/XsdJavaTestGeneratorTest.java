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

package org.citrusframework.generate.javadsl;

import java.io.File;
import java.io.IOException;

import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.generate.UnitFramework;
import org.citrusframework.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class XsdJavaTestGeneratorTest {

    @Test(dataProvider = "nameProvider")
    public void testCreateTest(String requestName, String responseName, String generatedResponseName) throws IOException {
        XsdJavaTestGenerator generator = new XsdJavaTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("org.citrusframework")
                .withFramework(UnitFramework.TESTNG);

        generator.withXsd("org/citrusframework/xsd/HelloService.xsd");
        generator.withRequestMessage(requestName);
        generator.withResponseMessage(responseName);

        generator.create();

        File javaFile = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "java/org/citrusframework/HelloIT.java");
        Assert.assertTrue(javaFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assert.assertTrue(javaContent.contains("@author Christoph"));
        Assert.assertTrue(javaContent.contains("public class HelloIT"));
        Assert.assertTrue(javaContent.contains("* This is a sample test"));
        Assert.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assert.assertTrue(javaContent.contains("extends TestNGCitrusSupport"));
    }

    @DataProvider
    public Object[][] nameProvider() {
        return new Object[][] {
            new Object[] {"Hello", "HelloResponse", "HelloResponse"},
            new Object[] {"HelloRequest", "HelloResponse", "HelloResponse"},
            new Object[] {"HelloRequest", "", "HelloResponse"},
            new Object[] {"HelloRequestMessage", "HelloResponseMessage", "HelloResponseMessage"},
            new Object[] {"HelloRequestMessage", "", "HelloResponseMessage"},
            new Object[] {"HelloReq", "HelloRes", "HelloRes"},
            new Object[] {"HelloReq", "", "HelloRes"}
        };
    }

    @Test
    public void testCreateTestWithoutResponse() throws IOException {
        XsdJavaTestGenerator generator = new XsdJavaTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("org.citrusframework")
                .withFramework(UnitFramework.TESTNG);

        generator.withXsd("org/citrusframework/xsd/HelloService.xsd");
        generator.withRequestMessage("Hello");
        generator.withResponseMessage("");

        generator.create();

        File javaFile = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "java/org/citrusframework/HelloIT.java");
        Assert.assertTrue(javaFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assert.assertTrue(javaContent.contains("@author Christoph"));
        Assert.assertTrue(javaContent.contains("public class HelloIT"));
        Assert.assertTrue(javaContent.contains("* This is a sample test"));
        Assert.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assert.assertTrue(javaContent.contains("extends TestNGCitrusSupport"));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Unable to find element with name 'HiRequest'.*")
    public void testUnknownRequest() throws IOException {
        XsdJavaTestGenerator generator = new XsdJavaTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("org.citrusframework")
                .withFramework(UnitFramework.TESTNG);

        generator.withXsd("org/citrusframework/xsd/HelloService.xsd");
        generator.withRequestMessage("HiRequest");
        generator.withResponseMessage("HiResponse");

        generator.create();
    }
}
