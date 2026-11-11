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

package org.citrusframework.docs;

import java.io.File;
import java.io.IOException;

import org.citrusframework.generate.TestGenerator;
import org.citrusframework.generate.UnitFramework;
import org.citrusframework.generate.xml.XmlTestGenerator;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated("Generates and reads shared SampleIT.xml plus the shared target/docs output directory")
public class HtmlTestDocsGeneratorTest {

    @BeforeAll
    public static void createSampleIT() {
        TestGenerator<?> generator = new XmlTestGenerator<>()
                .withAuthor("Christoph")
                .withDescription("This is a sample test")
                .withName("SampleIT")
                .usePackage("org.citrusframework.sample")
                .withFramework(UnitFramework.TESTNG);

        generator.create();
    }

    @Test
    public void testHtmlDocGeneration() throws IOException {
        HtmlTestDocsGenerator generator = HtmlTestDocsGenerator.build();

        generator.generateDoc();

        String docContent = FileUtils.readToString(Resources.fromFileSystem(HtmlTestDocsGenerator.getOutputDirectory() + File.separator + generator.getOutputFile()));

        Assertions.assertTrue(docContent.contains("<title>Citrus Test Documentation</title>"));
        Assertions.assertTrue(docContent.contains("<img src=\"logo.png\" lowsrc=\"logo.png\" alt=\"Logo\"/>"));
        Assertions.assertTrue(docContent.contains("<h1>Citrus Test Documentation</h1>"));
        Assertions.assertTrue(docContent.contains(">Overview</th>"));
        Assertions.assertTrue(docContent.contains("SampleIT.xml</a>"));
        Assertions.assertTrue(docContent.contains(">Nr.</th>"));
        Assertions.assertTrue(docContent.contains(">Test</th>"));
        Assertions.assertTrue(docContent.contains("This is a sample test"));
        Assertions.assertTrue(docContent.contains("src" + File.separator + "test" +
                                                        File.separator + "resources" +
                                                        File.separator + "org" +
                                                        File.separator + "citrusframework" +
                                                        File.separator + "sample" +
                                                        File.separator + "SampleIT.xml\">SampleIT.xml</a>"));
    }

    @Test
    public void testCustomizedHtmlDocGeneration() throws IOException {
        HtmlTestDocsGenerator generator = HtmlTestDocsGenerator.build()
                        .withLogo("test-logo.png")
                        .withOutputFile("CustomCitrusTests.html")
                        .withOverviewTitle("CustomOverview")
                        .withPageTitle("CustomPageTitle")
                        .useSrcDirectory("src" + File.separator + "test" + File.separator);

        generator.generateDoc();

        String docContent = FileUtils.readToString(Resources.fromFileSystem(HtmlTestDocsGenerator.getOutputDirectory() + File.separator + generator.getOutputFile()));

        Assertions.assertTrue(docContent.contains("<title>CustomPageTitle</title>"));
        Assertions.assertTrue(docContent.contains("<img src=\"test-logo.png\" lowsrc=\"test-logo.png\" alt=\"Logo\"/>"));
        Assertions.assertTrue(docContent.contains("<h1>CustomPageTitle</h1>"));
        Assertions.assertTrue(docContent.contains(">CustomOverview</th>"));
        Assertions.assertTrue(docContent.contains("SampleIT.xml</a>"));
        Assertions.assertTrue(docContent.contains(">Nr.</th>"));
        Assertions.assertTrue(docContent.contains(">Test</th>"));
        Assertions.assertTrue(docContent.contains("This is a sample test"));
        Assertions.assertTrue(docContent.contains("src" + File.separator + "test" +
                                                        File.separator + "resources" +
                                                        File.separator + "org" +
                                                        File.separator + "citrusframework" +
                                                        File.separator + "sample" +
                                                        File.separator + "SampleIT.xml\">SampleIT.xml</a>"));

    }
}
