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

import java.io.File;
import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
public class HtmlTestDocsGeneratorTest {

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
    public void testHtmlDocGeneration() throws IOException {
        HtmlTestDocsGenerator generator = HtmlTestDocsGenerator.build();

        generator.generateDoc();
        
        String docContent = FileUtils.readToString(new FileSystemResource(HtmlTestDocsGenerator.getOutputDirectory() + File.separator + generator.getOutputFile()));
        
        Assert.assertTrue(docContent.contains("<title>Citrus Test Documentation</title>"));
        Assert.assertTrue(docContent.contains("<img src=\"logo.png\" lowsrc=\"logo.png\" alt=\"Logo\"/>"));
        Assert.assertTrue(docContent.contains("<h1>Citrus Test Documentation</h1>"));
        Assert.assertTrue(docContent.contains(">Overview</th>"));
        Assert.assertTrue(docContent.contains("SampleIT.xml</a>"));
        Assert.assertTrue(docContent.contains(">Nr.</th>"));
        Assert.assertTrue(docContent.contains(">Test</th>"));
        Assert.assertTrue(docContent.contains("This is a sample test"));
        Assert.assertTrue(docContent.contains("src" + File.separator + "test" +
                                                        File.separator + "resources" +
                                                        File.separator + "com" + 
                                                        File.separator + "consol" + 
                                                        File.separator + "citrus" + 
                                                        File.separator + "sample" +
                                                        File.separator + "SampleIT.xml\">SampleIT.xml</a>"));
    }
    
    @Test
    public void testCustomizedHtmlDocGeneration() throws IOException {
        HtmlTestDocsGenerator generator = HtmlTestDocsGenerator.build()
                        .withLogo("test-logo.png")
                        .withOverviewTitle("CustomOverview")
                        .withPageTitle("CustomPageTitle")
                        .useSrcDirectory("src" + File.separator + "test" + File.separator);

        generator.generateDoc();
        
        String docContent = FileUtils.readToString(new FileSystemResource(HtmlTestDocsGenerator.getOutputDirectory() + File.separator + generator.getOutputFile()));
        
        Assert.assertTrue(docContent.contains("<title>CustomPageTitle</title>"));
        Assert.assertTrue(docContent.contains("<img src=\"test-logo.png\" lowsrc=\"test-logo.png\" alt=\"Logo\"/>"));
        Assert.assertTrue(docContent.contains("<h1>CustomPageTitle</h1>"));
        Assert.assertTrue(docContent.contains(">CustomOverview</th>"));
        Assert.assertTrue(docContent.contains("SampleIT.xml</a>"));
        Assert.assertTrue(docContent.contains(">Nr.</th>"));
        Assert.assertTrue(docContent.contains(">Test</th>"));
        Assert.assertTrue(docContent.contains("This is a sample test"));
        Assert.assertTrue(docContent.contains("src" + File.separator + "test" +
                                                        File.separator + "resources" +
                                                        File.separator + "com" + 
                                                        File.separator + "consol" + 
                                                        File.separator + "citrus" + 
                                                        File.separator + "sample" +
                                                        File.separator + "SampleIT.xml\">SampleIT.xml</a>"));
        
    }
}
