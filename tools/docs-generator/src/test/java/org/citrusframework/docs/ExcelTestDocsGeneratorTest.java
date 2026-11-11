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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
public class ExcelTestDocsGeneratorTest {

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
    public void testExcelDocGeneration() throws IOException {
        ExcelTestDocsGenerator generator = ExcelTestDocsGenerator.build();

        generator.generateDoc();

        String docContent = FileUtils.readToString(Resources.fromFileSystem(ExcelTestDocsGenerator.getOutputDirectory() + File.separator + generator.getOutputFile()));

        Assertions.assertTrue(docContent.contains("<Author>Citrus Testframework</Author>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Citrus Test Documentation</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Id</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Name</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Author</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Status</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Description</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Date</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">File</Data>"));

        Assertions.assertTrue(docContent.contains(">SampleIT<"));
        Assertions.assertTrue(docContent.contains(">Christoph<"));
        Assertions.assertTrue(docContent.contains(">DRAFT<"));
        Assertions.assertTrue(docContent.contains(">This is a sample test<"));
        Assertions.assertTrue(docContent.contains(">" + dateFormat.format(new Date()) + "<"));
        Assertions.assertTrue(docContent.contains(">SampleIT.xml<"));
    }

    @Test
    public void testCustomizedExcelDocGeneration() throws IOException {
        ExcelTestDocsGenerator generator = ExcelTestDocsGenerator.build()
                        .withAuthor("TestFactory")
                        .withCompany("TestCompany")
                        .withOutputFile("CustomCitrusTests.xls")
                        .withPageTitle("CustomPageTitle")
                        .withCustomHeaders("Id;Name;Autor;Status;Beschreibung;Datum;Dateiname")
                        .useSrcDirectory("src" + File.separator + "test" + File.separator);

        generator.generateDoc();

        String docContent = FileUtils.readToString(Resources.fromFileSystem(ExcelTestDocsGenerator.getOutputDirectory() + File.separator + generator.getOutputFile()));

        Assertions.assertTrue(docContent.contains("<Author>TestFactory</Author>"));
        Assertions.assertTrue(docContent.contains("<Company>TestCompany</Company>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">CustomPageTitle</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Id</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Name</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Autor</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Status</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Beschreibung</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Datum</Data>"));
        Assertions.assertTrue(docContent.contains("<Data ss:Type=\"String\">Dateiname</Data>"));

        Assertions.assertTrue(docContent.contains(">SampleIT<"));
        Assertions.assertTrue(docContent.contains(">Christoph<"));
        Assertions.assertTrue(docContent.contains(">DRAFT<"));
        Assertions.assertTrue(docContent.contains(">This is a sample test<"));
        Assertions.assertTrue(docContent.contains(">" + dateFormat.format(new Date()) + "<"));
        Assertions.assertTrue(docContent.contains(">SampleIT.xml<"));
    }
}
