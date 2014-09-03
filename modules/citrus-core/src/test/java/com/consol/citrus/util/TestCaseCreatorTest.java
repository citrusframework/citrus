/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.util;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.util.TestCaseCreator.UnitFramework;
import org.springframework.core.io.FileSystemResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
public class TestCaseCreatorTest extends AbstractTestNGUnitTest {

    @Test
    public void testCreateTestNGTest() throws IOException {
        TestCaseCreator creator = TestCaseCreator.build()
                                         .withAuthor("Christoph")
                                         .withDescription("This is a sample test")
                                         .withName("SampleTest")
                                         .usePackage("com.consol.citrus")
                                         .withFramework(UnitFramework.TESTNG);

        creator.createTestCase();
        
        File javaFile = new File("src/citrus/java/com/consol/citrus/SampleTest.java");
        Assert.assertTrue(javaFile.exists());
        
        File xmlFile = new File("src/citrus/tests/com/consol/citrus/SampleTest.xml");
        Assert.assertTrue(xmlFile.exists());
        
        String javaContent = FileUtils.readToString(new FileSystemResource(javaFile));
        Assert.assertTrue(javaContent.contains("@author Christoph"));
        Assert.assertTrue(javaContent.contains("public class SampleTest"));
        Assert.assertTrue(javaContent.contains("* This is a sample test"));
        Assert.assertTrue(javaContent.contains("package com.consol.citrus;"));
        Assert.assertTrue(javaContent.contains("extends AbstractTestNGCitrusTest"));
        
        String xmlContent = FileUtils.readToString(new FileSystemResource(xmlFile));
        Assert.assertTrue(xmlContent.contains("<author>Christoph</author>"));
        Assert.assertTrue(xmlContent.contains("<description>This is a sample test</description>"));
        Assert.assertTrue(xmlContent.contains("<testcase name=\"SampleTest\">"));
    }
    
    @Test
    public void testCreateJUnitTest() throws IOException {
        TestCaseCreator creator = TestCaseCreator.build()
                                         .withAuthor("Christoph")
                                         .withDescription("This is a sample test")
                                         .withName("SampleTest")
                                         .usePackage("com.consol.citrus")
                                         .withFramework(UnitFramework.JUNIT);

        creator.createTestCase();
        
        File javaFile = new File("src/citrus/java/com/consol/citrus/SampleTest.java");
        Assert.assertTrue(javaFile.exists());
        
        File xmlFile = new File("src/citrus/tests/com/consol/citrus/SampleTest.xml");
        Assert.assertTrue(xmlFile.exists());
        
        String javaContent = FileUtils.readToString(new FileSystemResource(javaFile));
        Assert.assertTrue(javaContent.contains("@author Christoph"));
        Assert.assertTrue(javaContent.contains("public class SampleTest"));
        Assert.assertTrue(javaContent.contains("* This is a sample test"));
        Assert.assertTrue(javaContent.contains("package com.consol.citrus;"));
        Assert.assertTrue(javaContent.contains("extends AbstractJUnit4CitrusTest"));
        
        String xmlContent = FileUtils.readToString(new FileSystemResource(xmlFile));
        Assert.assertTrue(xmlContent.contains("<author>Christoph</author>"));
        Assert.assertTrue(xmlContent.contains("<description>This is a sample test</description>"));
        Assert.assertTrue(xmlContent.contains("<testcase name=\"SampleTest\">"));
    }
    
    @Test
    public void testInvalidName() throws IOException {
        TestCaseCreator creator = TestCaseCreator.build()
                                         .withAuthor("Christoph")
                                         .withDescription("This is a sample test")
                                         .withName("sampletest")
                                         .usePackage("com.consol.citrus")
                                         .withFramework(UnitFramework.JUNIT);

        try {
            creator.createTestCase();
            Assert.fail("Missing exception due to invalid test name");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("name must start with an uppercase letter"));
        }
    }
    
    @Test
    public void testDefaultValues() throws IOException {
        TestCaseCreator.main(new String[] {"-name", "SampleTest"});
        
        File javaFile = new File("src/citrus/java/com/consol/citrus/SampleTest.java");
        Assert.assertTrue(javaFile.exists());
        
        File xmlFile = new File("src/citrus/tests/com/consol/citrus/SampleTest.xml");
        Assert.assertTrue(xmlFile.exists());
        
        String javaContent = FileUtils.readToString(new FileSystemResource(javaFile));
        Assert.assertTrue(javaContent.contains("@author Unknown"));
        Assert.assertTrue(javaContent.contains("public class SampleTest"));
        Assert.assertTrue(javaContent.contains("* TODO: Description"));
        Assert.assertTrue(javaContent.contains("package com.consol.citrus;"));
        Assert.assertTrue(javaContent.contains("extends AbstractTestNGCitrusTest"));
        
        String xmlContent = FileUtils.readToString(new FileSystemResource(xmlFile));
        Assert.assertTrue(xmlContent.contains("<author>Unknown</author>"));
        Assert.assertTrue(xmlContent.contains("<description>TODO: Description</description>"));
        Assert.assertTrue(xmlContent.contains("<testcase name=\"SampleTest\">"));
    }
    
    @Test
    public void testHelp() {
        TestCaseCreator.main(new String[] {"-help"});
    }
    
    @Test
    public void testInvalidArgument() {
        TestCaseCreator.main(new String[] {"-invalid"});
    }
}
