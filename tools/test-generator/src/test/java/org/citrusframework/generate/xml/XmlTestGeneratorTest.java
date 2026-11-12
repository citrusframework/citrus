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

package org.citrusframework.generate.xml;

import java.io.File;
import java.io.IOException;

import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.generate.TestGenerator;
import org.citrusframework.generate.TestGeneratorMain;
import org.citrusframework.generate.UnitFramework;
import org.citrusframework.util.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        File javaFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "java/org/citrusframework/SampleIT.java");
        Assertions.assertTrue(javaFile.exists());

        File xmlFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "resources/org/citrusframework/SampleIT.xml");
        Assertions.assertTrue(xmlFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assertions.assertTrue(javaContent.contains("public class SampleIT"));
        Assertions.assertTrue(javaContent.contains("* This is a sample test"));
        Assertions.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assertions.assertTrue(javaContent.contains("extends TestNGCitrusSupport"));

        String xmlContent = FileUtils.readToString(xmlFile);
        Assertions.assertTrue(xmlContent.contains("<author>Christoph</author>"));
        Assertions.assertTrue(xmlContent.contains("<description>This is a sample test</description>"));
        Assertions.assertTrue(xmlContent.contains("<testcase name=\"SampleIT\">"));
    }

    @Test
    public void testCreateJUnitTest() throws IOException {
        TestGenerator<?> generator = new XmlTestGenerator<>()
                                         .withAuthor("Christoph")
                                         .withDescription("This is a sample test")
                                         .withName("SampleIT")
                                         .usePackage("org.citrusframework")
                                         .withFramework(UnitFramework.JUNIT);

        generator.create();

        File javaFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "java/org/citrusframework/SampleIT.java");
        Assertions.assertTrue(javaFile.exists());

        File xmlFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "resources/org/citrusframework/SampleIT.xml");
        Assertions.assertTrue(xmlFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assertions.assertTrue(javaContent.contains("public class SampleIT"));
        Assertions.assertTrue(javaContent.contains("* This is a sample test"));
        Assertions.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assertions.assertTrue(javaContent.contains("@ExtendWith(CitrusExtension.class)"));

        String xmlContent = FileUtils.readToString(xmlFile);
        Assertions.assertTrue(xmlContent.contains("<author>Christoph</author>"));
        Assertions.assertTrue(xmlContent.contains("<description>This is a sample test</description>"));
        Assertions.assertTrue(xmlContent.contains("<testcase name=\"SampleIT\">"));
    }

    @Test
    public void testCreateJUnit4Test() throws IOException {
        TestGenerator<?> generator = new XmlTestGenerator<>()
                                         .withAuthor("Christoph")
                                         .withDescription("This is a sample test")
                                         .withName("SampleIT")
                                         .usePackage("org.citrusframework")
                                         .withFramework(UnitFramework.JUNIT4);

        generator.create();

        File javaFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "java/org/citrusframework/SampleIT.java");
        Assertions.assertTrue(javaFile.exists());

        File xmlFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "resources/org/citrusframework/SampleIT.xml");
        Assertions.assertTrue(xmlFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assertions.assertTrue(javaContent.contains("public class SampleIT"));
        Assertions.assertTrue(javaContent.contains("* This is a sample test"));
        Assertions.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assertions.assertTrue(javaContent.contains("extends JUnit4CitrusSupport"));

        String xmlContent = FileUtils.readToString(xmlFile);
        Assertions.assertTrue(xmlContent.contains("<author>Christoph</author>"));
        Assertions.assertTrue(xmlContent.contains("<description>This is a sample test</description>"));
        Assertions.assertTrue(xmlContent.contains("<testcase name=\"SampleIT\">"));
    }

    @Test
    public void testInvalidName() {
        TestGenerator<?> generator = new XmlTestGenerator<>()
                                         .withAuthor("Christoph")
                                         .withDescription("This is a sample test")
                                         .withName("sampletest")
                                         .usePackage("org.citrusframework")
                                         .withFramework(UnitFramework.JUNIT);

        try {
            generator.create();
            Assertions.fail("Missing exception due to invalid test name");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().contains("name must start with an uppercase letter"));
        }
    }

    @Test
    public void testDefaultValues() throws IOException {
        TestGeneratorMain.main(new String[] {"-name", "SampleIT"});

        File javaFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "java/org/citrusframework/SampleIT.java");
        Assertions.assertTrue(javaFile.exists());

        File xmlFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "resources/org/citrusframework/SampleIT.xml");
        Assertions.assertTrue(xmlFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assertions.assertTrue(javaContent.contains("public class SampleIT"));
        Assertions.assertTrue(javaContent.contains("* TODO: Description"));
        Assertions.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assertions.assertTrue(javaContent.contains("@ExtendWith(CitrusExtension.class)"));

        String xmlContent = FileUtils.readToString(xmlFile);
        Assertions.assertTrue(xmlContent.contains("<author>Unknown</author>"));
        Assertions.assertTrue(xmlContent.contains("<description>TODO: Description</description>"));
        Assertions.assertTrue(xmlContent.contains("<testcase name=\"SampleIT\">"));
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
