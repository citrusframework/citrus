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

package com.consol.citrus.generate.xml;

import com.consol.citrus.Citrus;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.generate.UnitFramework;
import com.consol.citrus.util.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class XsdXmlTestGeneratorTest {

    @Test(dataProvider = "nameProvider")
    public void testCreateTest(String requestName, String responseName, String generatedResponseName) throws IOException {
        XsdXmlTestGenerator generator = new XsdXmlTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("com.consol.citrus")
                .withFramework(UnitFramework.TESTNG);

        generator.withXsd("com/consol/citrus/xsd/HelloService.xsd");
        generator.withRequestMessage(requestName);
        generator.withResponseMessage(responseName);

        generator.create();

        File javaFile = new File(Citrus.DEFAULT_TEST_SRC_DIRECTORY + "java/com/consol/citrus/HelloIT.java");
        Assert.assertTrue(javaFile.exists());

        File xmlFile = new File(Citrus.DEFAULT_TEST_SRC_DIRECTORY + "resources/com/consol/citrus/HelloIT.xml");
        Assert.assertTrue(xmlFile.exists());

        String javaContent = FileUtils.readToString(new FileSystemResource(javaFile));
        Assert.assertTrue(javaContent.contains("@author Christoph"));
        Assert.assertTrue(javaContent.contains("public class HelloIT"));
        Assert.assertTrue(javaContent.contains("* This is a sample test"));
        Assert.assertTrue(javaContent.contains("package com.consol.citrus;"));
        Assert.assertTrue(javaContent.contains("extends AbstractTestNGCitrusTest"));

        String xmlContent = FileUtils.readToString(new FileSystemResource(xmlFile));
        Assert.assertTrue(xmlContent.contains("<author>Christoph</author>"));
        Assert.assertTrue(xmlContent.contains("<description>This is a sample test</description>"));
        Assert.assertTrue(xmlContent.contains("<testcase name=\"HelloIT\">"));
        Assert.assertTrue(xmlContent.contains("<data>&lt;hel:" + requestName));
        Assert.assertTrue(xmlContent.contains("<data>&lt;hel:" + generatedResponseName));
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
        XsdXmlTestGenerator generator = new XsdXmlTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("com.consol.citrus")
                .withFramework(UnitFramework.TESTNG);

        generator.withXsd("com/consol/citrus/xsd/HelloService.xsd");
        generator.withRequestMessage("Hello");
        generator.withResponseMessage("");

        generator.create();

        File javaFile = new File(Citrus.DEFAULT_TEST_SRC_DIRECTORY + "java/com/consol/citrus/HelloIT.java");
        Assert.assertTrue(javaFile.exists());

        File xmlFile = new File(Citrus.DEFAULT_TEST_SRC_DIRECTORY + "resources/com/consol/citrus/HelloIT.xml");
        Assert.assertTrue(xmlFile.exists());

        String javaContent = FileUtils.readToString(new FileSystemResource(javaFile));
        Assert.assertTrue(javaContent.contains("@author Christoph"));
        Assert.assertTrue(javaContent.contains("public class HelloIT"));
        Assert.assertTrue(javaContent.contains("* This is a sample test"));
        Assert.assertTrue(javaContent.contains("package com.consol.citrus;"));
        Assert.assertTrue(javaContent.contains("extends AbstractTestNGCitrusTest"));

        String xmlContent = FileUtils.readToString(new FileSystemResource(xmlFile));
        Assert.assertTrue(xmlContent.contains("<author>Christoph</author>"));
        Assert.assertTrue(xmlContent.contains("<description>This is a sample test</description>"));
        Assert.assertTrue(xmlContent.contains("<testcase name=\"HelloIT\">"));
        Assert.assertTrue(xmlContent.contains("<data>&lt;hel:"));
        Assert.assertFalse(xmlContent.contains("<receive"));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Unable to find element with name 'HiRequest'.*")
    public void testUnknownRequest() throws IOException {
        XsdXmlTestGenerator generator = new XsdXmlTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("com.consol.citrus")
                .withFramework(UnitFramework.TESTNG);

        generator.withXsd("com/consol/citrus/xsd/HelloService.xsd");
        generator.withRequestMessage("HiRequest");
        generator.withResponseMessage("HiResponse");

        generator.create();
    }
}