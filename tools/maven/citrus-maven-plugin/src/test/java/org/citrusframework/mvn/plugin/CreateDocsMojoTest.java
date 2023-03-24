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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class CreateDocsMojoTest {

    private Prompter prompter = Mockito.mock(Prompter.class);
    
    private ExcelTestDocsGenerator excelTestDocGenerator = Mockito.mock(ExcelTestDocsGenerator.class);
    private HtmlTestDocsGenerator htmlTestDocGenerator = Mockito.mock(HtmlTestDocsGenerator.class);

    private CreateDocsMojo mojo;
    
    @BeforeMethod
    public void setup() {
        mojo = new CreateDocsMojo(excelTestDocGenerator, htmlTestDocGenerator);
        mojo.setPrompter(prompter);
    }
    
    @Test
    public void testCreateXls() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(prompter, excelTestDocGenerator);

        when(prompter.prompt(contains("mode"), any(List.class), eq("html"))).thenReturn("excel");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("Citrus");
        when(prompter.prompt(contains("company"), nullable(String.class))).thenReturn("citrusframework.org");
        when(prompter.prompt(contains("output file"), nullable(String.class))).thenReturn("SampleTests.xls");
        when(prompter.prompt(contains("headers"), nullable(String.class))).thenReturn("Id,Name,Description");
        when(prompter.prompt(contains("page title"), nullable(String.class))).thenReturn("SampleTests");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("y");

        when(excelTestDocGenerator.withCompany("citrusframework.org")).thenReturn(excelTestDocGenerator);
        when(excelTestDocGenerator.withAuthor("Citrus")).thenReturn(excelTestDocGenerator);
        when(excelTestDocGenerator.withPageTitle("SampleTests")).thenReturn(excelTestDocGenerator);
        when(excelTestDocGenerator.withOutputFile("SampleTests.xls")).thenReturn(excelTestDocGenerator);
        when(excelTestDocGenerator.useSrcDirectory("src/test/")).thenReturn(excelTestDocGenerator);
        when(excelTestDocGenerator.withCustomHeaders("Id,Name,Description")).thenReturn(excelTestDocGenerator);
        
        mojo.execute();

        verify(excelTestDocGenerator).generateDoc();
    }

    @Test
    public void testAbortXls() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(prompter, excelTestDocGenerator);

        when(prompter.prompt(contains("mode"), any(List.class), eq("html"))).thenReturn("excel");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("Citrus");
        when(prompter.prompt(contains("company"), nullable(String.class))).thenReturn("citrusframework.org");
        when(prompter.prompt(contains("output file"), nullable(String.class))).thenReturn("SampleTests.xls");
        when(prompter.prompt(contains("headers"), nullable(String.class))).thenReturn("Id,Name,Description");
        when(prompter.prompt(contains("page title"), nullable(String.class))).thenReturn("SampleTests");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("n");

        mojo.execute();

        verify(excelTestDocGenerator, times(0)).generateDoc();
    }

    @Test
    public void testCreateHtml() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(prompter, htmlTestDocGenerator);

        when(prompter.prompt(contains("mode"), any(List.class), eq("html"))).thenReturn("html");
        when(prompter.prompt(contains("overview"), nullable(String.class))).thenReturn("Tests");
        when(prompter.prompt(contains("columns"), nullable(String.class))).thenReturn("2");
        when(prompter.prompt(contains("page title"), nullable(String.class))).thenReturn("SampleTests");
        when(prompter.prompt(contains("output file"), nullable(String.class))).thenReturn("SampleTests.html");
        when(prompter.prompt(contains("logo"), nullable(String.class))).thenReturn("citrus-logo.png");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("y");

        when(htmlTestDocGenerator.withColumns("2")).thenReturn(htmlTestDocGenerator);
        when(htmlTestDocGenerator.withLogo("citrus-logo.png")).thenReturn(htmlTestDocGenerator);
        when(htmlTestDocGenerator.withPageTitle("SampleTests")).thenReturn(htmlTestDocGenerator);
        when(htmlTestDocGenerator.withOutputFile("SampleTests.html")).thenReturn(htmlTestDocGenerator);
        when(htmlTestDocGenerator.useSrcDirectory("src/test/")).thenReturn(htmlTestDocGenerator);
        when(htmlTestDocGenerator.withOverviewTitle("Tests")).thenReturn(htmlTestDocGenerator);

        mojo.execute();

        verify(htmlTestDocGenerator).generateDoc();
    }

    @Test
    public void testAbortHtml() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(prompter, htmlTestDocGenerator);

        when(prompter.prompt(contains("mode"), any(List.class), eq("html"))).thenReturn("html");
        when(prompter.prompt(contains("overview"), nullable(String.class))).thenReturn("Tests");
        when(prompter.prompt(contains("columns"), nullable(String.class))).thenReturn("2");
        when(prompter.prompt(contains("page title"), nullable(String.class))).thenReturn("SampleTests");
        when(prompter.prompt(contains("output file"), nullable(String.class))).thenReturn("SampleTests.html");
        when(prompter.prompt(contains("logo"), nullable(String.class))).thenReturn("citrus-logo.png");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("n");

        mojo.execute();

        verify(htmlTestDocGenerator, times(0)).generateDoc();
    }
}
