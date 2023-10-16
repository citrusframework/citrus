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

package org.citrusframework.generate.xml;

import java.io.File;
import java.io.IOException;

import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.generate.TestGenerator;
import org.citrusframework.generate.TestGeneratorMain;
import org.citrusframework.generate.UnitFramework;
import org.citrusframework.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class XmlTestGeneratorTest {

    @Test
    public void testCreateTestNGTest() throws IOException {
        TestGenerator<?> generator = new XmlTestGenerator<>()
                                         .withAuthor("Christoph")
                                         .withDescription("This is a sample test")
                                         .withName("SampleIT")
                                         .usePackage("org.citrusframework")
                                         .withFramework(UnitFramework.TESTNG);

        generator.create();

        File javaFile = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "java/org/citrusframework/SampleIT.java");
        Assert.assertTrue(javaFile.exists());

        File xmlFile = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "resources/org/citrusframework/SampleIT.xml");
        Assert.assertTrue(xmlFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assert.assertTrue(javaContent.contains("@author Christoph"));
        Assert.assertTrue(javaContent.contains("public class SampleIT"));
        Assert.assertTrue(javaContent.contains("* This is a sample test"));
        Assert.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assert.assertTrue(javaContent.contains("extends TestNGCitrusSupport"));

        String xmlContent = FileUtils.readToString(xmlFile);
        Assert.assertTrue(xmlContent.contains("<author>Christoph</author>"));
        Assert.assertTrue(xmlContent.contains("<description>This is a sample test</description>"));
        Assert.assertTrue(xmlContent.contains("<testcase name=\"SampleIT\">"));
    }

    @Test
    public void testCreateJUnitTest() throws IOException {
        TestGenerator<?> generator = new XmlTestGenerator<>()
                                         .withAuthor("Christoph")
                                         .withDescription("This is a sample test")
                                         .withName("SampleIT")
                                         .usePackage("org.citrusframework")
                                         .withFramework(UnitFramework.JUNIT4);

        generator.create();

        File javaFile = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "java/org/citrusframework/SampleIT.java");
        Assert.assertTrue(javaFile.exists());

        File xmlFile = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "resources/org/citrusframework/SampleIT.xml");
        Assert.assertTrue(xmlFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assert.assertTrue(javaContent.contains("@author Christoph"));
        Assert.assertTrue(javaContent.contains("public class SampleIT"));
        Assert.assertTrue(javaContent.contains("* This is a sample test"));
        Assert.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assert.assertTrue(javaContent.contains("extends JUnit4CitrusSupport"));

        String xmlContent = FileUtils.readToString(xmlFile);
        Assert.assertTrue(xmlContent.contains("<author>Christoph</author>"));
        Assert.assertTrue(xmlContent.contains("<description>This is a sample test</description>"));
        Assert.assertTrue(xmlContent.contains("<testcase name=\"SampleIT\">"));
    }

    @Test
    public void testInvalidName() throws IOException {
        TestGenerator<?> generator = new XmlTestGenerator<>()
                                         .withAuthor("Christoph")
                                         .withDescription("This is a sample test")
                                         .withName("sampletest")
                                         .usePackage("org.citrusframework")
                                         .withFramework(UnitFramework.JUNIT4);

        try {
            generator.create();
            Assert.fail("Missing exception due to invalid test name");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("name must start with an uppercase letter"));
        }
    }

    @Test
    public void testDefaultValues() throws IOException {
        TestGeneratorMain.main(new String[] {"-name", "SampleIT"});

        File javaFile = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "java/org/citrusframework/SampleIT.java");
        Assert.assertTrue(javaFile.exists());

        File xmlFile = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "resources/org/citrusframework/SampleIT.xml");
        Assert.assertTrue(xmlFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assert.assertTrue(javaContent.contains("@author Unknown"));
        Assert.assertTrue(javaContent.contains("public class SampleIT"));
        Assert.assertTrue(javaContent.contains("* TODO: Description"));
        Assert.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assert.assertTrue(javaContent.contains("extends TestNGCitrusSupport"));

        String xmlContent = FileUtils.readToString(xmlFile);
        Assert.assertTrue(xmlContent.contains("<author>Unknown</author>"));
        Assert.assertTrue(xmlContent.contains("<description>TODO: Description</description>"));
        Assert.assertTrue(xmlContent.contains("<testcase name=\"SampleIT\">"));
    }

    @Test
    public void testHelp() {
        TestGeneratorMain.main(new String[] {"-help"});
    }

    @Test
    public void testInvalidArgument() {
        TestGeneratorMain.main(new String[] {"-invalid"});
    }
}
