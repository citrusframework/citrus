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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ExcelTestDocsGeneratorTest {

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeClass
    public void createSampleIT() {
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

        Assert.assertTrue(docContent.contains("<Author>Citrus Testframework</Author>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Citrus Test Documentation</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Id</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Name</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Author</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Status</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Description</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Date</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">File</Data>"));

        Assert.assertTrue(docContent.contains(">SampleIT<"));
        Assert.assertTrue(docContent.contains(">Christoph<"));
        Assert.assertTrue(docContent.contains(">DRAFT<"));
        Assert.assertTrue(docContent.contains(">This is a sample test<"));
        Assert.assertTrue(docContent.contains(">" + dateFormat.format(new Date()) + "<"));
        Assert.assertTrue(docContent.contains(">SampleIT.xml<"));
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

        Assert.assertTrue(docContent.contains("<Author>TestFactory</Author>"));
        Assert.assertTrue(docContent.contains("<Company>TestCompany</Company>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">CustomPageTitle</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Id</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Name</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Autor</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Status</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Beschreibung</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Datum</Data>"));
        Assert.assertTrue(docContent.contains("<Data ss:Type=\"String\">Dateiname</Data>"));

        Assert.assertTrue(docContent.contains(">SampleIT<"));
        Assert.assertTrue(docContent.contains(">Christoph<"));
        Assert.assertTrue(docContent.contains(">DRAFT<"));
        Assert.assertTrue(docContent.contains(">This is a sample test<"));
        Assert.assertTrue(docContent.contains(">" + dateFormat.format(new Date()) + "<"));
        Assert.assertTrue(docContent.contains(">SampleIT.xml<"));
    }
}
