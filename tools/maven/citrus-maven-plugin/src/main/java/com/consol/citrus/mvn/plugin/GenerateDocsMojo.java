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

package com.consol.citrus.mvn.plugin;

import com.consol.citrus.docs.ExcelTestDocGenerator;
import com.consol.citrus.docs.HtmlTestDocGenerator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;

import java.util.Optional;

/**
 * Generates test overview documentation based on plugin configuration. Html documentation creates a web page that
 * contains a list of all available tests with meta information. Excel documentation creates a table of all available tests with
 * meta information such as name, author, status and so on.
 *
 * @author Christoph Deppisch
 * @since 2.7.4
 */
@Mojo(name = "generate-docs", defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
public class GenerateDocsMojo extends AbstractCitrusMojo {

    @Parameter(property = "citrus.skip.generate.docs", defaultValue = "false")
    protected boolean skipGenerateDocs;

    private final ExcelTestDocGenerator excelTestDocGenerator;
    private final HtmlTestDocGenerator htmlTestDocGenerator;

    /**
     * Default constructor.
     */
    public GenerateDocsMojo() {
        this(new ExcelTestDocGenerator(), new HtmlTestDocGenerator());
    }

    /**
     * Constructor using final fields.
     * @param excelTestDocGenerator
     * @param htmlTestDocGenerator
     */
    public GenerateDocsMojo(ExcelTestDocGenerator excelTestDocGenerator, HtmlTestDocGenerator htmlTestDocGenerator) {
        this.excelTestDocGenerator = excelTestDocGenerator;
        this.htmlTestDocGenerator = htmlTestDocGenerator;
    }

    @Override
    public void doExecute() throws MojoExecutionException {
        if (skipGenerateDocs) {
            return;
        }

        if (getDocs().getExcel() != null) {
            ExcelTestDocGenerator creator = getExcelTestDocGenerator();

            creator.withOutputFile(getDocs().getExcel().getOutputFile() + (getDocs().getExcel().getOutputFile().endsWith(".xls") ? "" : ".xls"))
                    .withPageTitle(getDocs().getExcel().getPageTitle())
                    .withAuthor(getDocs().getExcel().getAuthor())
                    .withCompany(getDocs().getExcel().getCompany())
                    .useSrcDirectory(getTestSrcDirectory())
                    .withCustomHeaders(getDocs().getExcel().getHeaders());

            creator.generateDoc();

            getLog().info("Successfully created Excel documentation: outputFile='target/" + getDocs().getExcel().getOutputFile() + (getDocs().getExcel().getOutputFile().endsWith(".xls") ? "" : ".xls") + "'");
        }

        if (getDocs().getHtml() != null) {
            HtmlTestDocGenerator creator = getHtmlTestDocGenerator();

            creator.withOutputFile(getDocs().getHtml().getOutputFile() + (getDocs().getHtml().getOutputFile().endsWith(".html") ? "" : ".html"))
                    .withPageTitle(getDocs().getHtml().getPageTitle())
                    .withOverviewTitle(getDocs().getHtml().getHeading())
                    .withColumns(getDocs().getHtml().getColumns())
                    .useSrcDirectory(getTestSrcDirectory())
                    .withLogo(getDocs().getHtml().getLogo());

            creator.generateDoc();

            getLog().info("Successfully created HTML documentation: outputFile='target/" + getDocs().getHtml().getOutputFile() + (getDocs().getHtml().getOutputFile().endsWith(".html") ? "" : ".html") + "'");
        }
    }

    /**
     * Gets the htmlTestDocGenerator.
     *
     * @return
     */
    public HtmlTestDocGenerator getHtmlTestDocGenerator() {
        return Optional.ofNullable(htmlTestDocGenerator).orElse(HtmlTestDocGenerator.build());
    }

    /**
     * Gets the excelTestDocGenerator.
     *
     * @return
     */
    public ExcelTestDocGenerator getExcelTestDocGenerator() {
        return Optional.ofNullable(excelTestDocGenerator).orElse(ExcelTestDocGenerator.build());
    }
}
