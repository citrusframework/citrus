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

package org.citrusframework.mvn.plugin;

import org.citrusframework.docs.ExcelTestDocsGenerator;
import org.citrusframework.docs.HtmlTestDocsGenerator;
import org.citrusframework.mvn.plugin.config.docs.DocsConfiguration;
import org.citrusframework.mvn.plugin.config.docs.ExcelDocConfiguration;
import org.citrusframework.mvn.plugin.config.docs.HtmlDocConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class GenerateDocsMojoTest {

    private ExcelTestDocsGenerator excelTestDocGenerator = Mockito.mock(ExcelTestDocsGenerator.class);
    private HtmlTestDocsGenerator htmlTestDocGenerator = Mockito.mock(HtmlTestDocsGenerator.class);

    private GenerateDocsMojo mojo;
    
    @BeforeMethod
    public void setup() {
        mojo = new GenerateDocsMojo(excelTestDocGenerator, htmlTestDocGenerator);
    }
    
    @Test
    public void testCreateXls() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(excelTestDocGenerator);

        DocsConfiguration docs = new DocsConfiguration();
        ExcelDocConfiguration configuration = new ExcelDocConfiguration();
        configuration.setCompany("citrusframework.org");
        configuration.setAuthor("Citrus");
        configuration.setPageTitle("SampleTests");
        configuration.setOutputFile("SampleTests.xls");
        configuration.setHeaders("Id,Name,Description");
        docs.setExcel(configuration);

        when(excelTestDocGenerator.withCompany("citrusframework.org")).thenReturn(excelTestDocGenerator);
        when(excelTestDocGenerator.withAuthor("Citrus")).thenReturn(excelTestDocGenerator);
        when(excelTestDocGenerator.withPageTitle("SampleTests")).thenReturn(excelTestDocGenerator);
        when(excelTestDocGenerator.withOutputFile("SampleTests.xls")).thenReturn(excelTestDocGenerator);
        when(excelTestDocGenerator.useSrcDirectory("src/test/")).thenReturn(excelTestDocGenerator);
        when(excelTestDocGenerator.withCustomHeaders("Id,Name,Description")).thenReturn(excelTestDocGenerator);

        mojo.setDocs(docs);

        mojo.execute();

        verify(excelTestDocGenerator).generateDoc();
    }

    @Test
    public void testCreateHtml() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(htmlTestDocGenerator);

        DocsConfiguration docs = new DocsConfiguration();
        HtmlDocConfiguration configuration = new HtmlDocConfiguration();
        configuration.setColumns("2");
        configuration.setLogo("citrus-logo.png");
        configuration.setPageTitle("SampleTests");
        configuration.setOutputFile("SampleTests.html");
        configuration.setHeading("Tests");
        docs.setHtml(configuration);

        when(htmlTestDocGenerator.withColumns("2")).thenReturn(htmlTestDocGenerator);
        when(htmlTestDocGenerator.withLogo("citrus-logo.png")).thenReturn(htmlTestDocGenerator);
        when(htmlTestDocGenerator.withPageTitle("SampleTests")).thenReturn(htmlTestDocGenerator);
        when(htmlTestDocGenerator.withOutputFile("SampleTests.html")).thenReturn(htmlTestDocGenerator);
        when(htmlTestDocGenerator.useSrcDirectory("src/test/")).thenReturn(htmlTestDocGenerator);
        when(htmlTestDocGenerator.withOverviewTitle("Tests")).thenReturn(htmlTestDocGenerator);

        mojo.setDocs(docs);

        mojo.execute();

        verify(htmlTestDocGenerator).generateDoc();
    }
}
