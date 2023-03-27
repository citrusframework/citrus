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

package org.citrusframework.citrus.docs;

import org.citrusframework.citrus.generate.UnitFramework;
import org.citrusframework.citrus.generate.xml.XmlTestGenerator;
import org.citrusframework.citrus.util.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
public class SvgTestDocsGeneratorTest {

    @BeforeClass
    public void createSampleIT() {
        XmlTestGenerator generator = (XmlTestGenerator) new XmlTestGenerator()
                .withAuthor("Christoph")
                .withDescription("This is a sample test")
                .withName("SampleIT")
                .usePackage("org.citrusframework.citrus.sample")
                .withFramework(UnitFramework.TESTNG);

        generator.create();
    }
    
    @Test
    public void testSvgDocGeneration() throws IOException {
        SvgTestDocsGenerator generator = SvgTestDocsGenerator.build();
        
        generator.generateDoc();
        
        String docContent = FileUtils.readToString(new FileSystemResource(HtmlTestDocsGenerator.getOutputDirectory() + "/SampleIT.svg"));
        
        Assert.assertTrue(docContent.contains("<title>SampleIT</title>"));
        Assert.assertTrue(docContent.contains("<desc>This is a sample test"));
        Assert.assertTrue(docContent.contains("TestCase: SampleIT"));
    }
}
