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

package org.citrusframework.mvn.plugin;

import org.citrusframework.docs.ExcelTestDocsGenerator;
import org.citrusframework.docs.HtmlTestDocsGenerator;
import org.citrusframework.mvn.plugin.config.docs.ExcelDocConfiguration;
import org.citrusframework.mvn.plugin.config.docs.HtmlDocConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import java.util.Arrays;
import java.util.Optional;

/**
 * Creates test documentation in interactive mode. Either uses mode excel for MS Excel output or
 * html for HTML web page output.
 *
 * @author Christoph Deppisch
 * @since 2.7.4
 */
@Mojo(name = "create-docs")
public class CreateDocsMojo extends AbstractCitrusMojo {

    @Parameter(property = "citrus.skip.create.docs", defaultValue = "false")
    protected boolean skipCreateDocs;

    @Component
    private Prompter prompter;

    private final ExcelTestDocsGenerator excelTestDocsGenerator;
    private final HtmlTestDocsGenerator htmlTestDocsGenerator;

    /**
     * Default constructor.
     */
    public CreateDocsMojo() {
        this(new ExcelTestDocsGenerator(), new HtmlTestDocsGenerator());
    }

    /**
     * Constructor using final fields.
     * @param excelTestDocsGenerator
     * @param htmlTestDocsGenerator
     */
    public CreateDocsMojo(ExcelTestDocsGenerator excelTestDocsGenerator, HtmlTestDocsGenerator htmlTestDocsGenerator) {
        this.excelTestDocsGenerator = excelTestDocsGenerator;
        this.htmlTestDocsGenerator = htmlTestDocsGenerator;
    }

    @Override
    public void doExecute() throws MojoExecutionException {
        if (skipCreateDocs) {
            return;
        }

        try {
            String mode = prompter.prompt("Choose documentation mode:", Arrays.asList("excel", "html"), "html");

            if (mode.equals("excel")) {
                createExcelDoc();
            } else if (mode.equals("html")) {
                createHtmlDoc();
            }
        } catch (PrompterException e) {
            getLog().info(e);
        }
    }

    /**
     * Create HTML documentation in interactive mode.
     * @throws PrompterException
     */
    private void createHtmlDoc() throws PrompterException {
        HtmlDocConfiguration configuration = new HtmlDocConfiguration();

        String heading = prompter.prompt("Enter overview title:", configuration.getHeading());
        String columns = prompter.prompt("Enter number of columns in overview:", configuration.getColumns());
        String pageTitle = prompter.prompt("Enter page title:", configuration.getPageTitle());
        String outputFile = prompter.prompt("Enter output file name:", configuration.getOutputFile());
        String logo = prompter.prompt("Enter file path to logo:", configuration.getLogo());

        String confirm = prompter.prompt("Confirm HTML documentation: outputFile='target/" + outputFile + (outputFile.endsWith(".html") ? "" : ".html") + "'\n",
                Arrays.asList("y", "n"), "y");

        if (confirm.equalsIgnoreCase("n")) {
            return;
        }

        HtmlTestDocsGenerator generator = getHtmlTestDocsGenerator();

        generator.withOutputFile(outputFile + (outputFile.endsWith(".html") ? "" : ".html"))
                .withPageTitle(pageTitle)
                .withOverviewTitle(heading)
                .withColumns(columns)
                .useSrcDirectory(getTestSrcDirectory())
                .withLogo(logo);

        generator.generateDoc();

        getLog().info("Successfully created HTML documentation: outputFile='target/" + outputFile + (outputFile.endsWith(".html") ? "" : ".html") + "'");
    }

    /**
     * Create Excel documentation in interactive mode.
     * @throws PrompterException
     */
    private void createExcelDoc() throws PrompterException {
        ExcelDocConfiguration configuration = new ExcelDocConfiguration();
        String company = prompter.prompt("Enter company:", configuration.getCompany());
        String author = prompter.prompt("Enter author:", configuration.getAuthor());
        String pageTitle = prompter.prompt("Enter page title:", configuration.getPageTitle());
        String outputFile = prompter.prompt("Enter output file name:", configuration.getOutputFile());
        String headers = prompter.prompt("Enter custom headers:", configuration.getHeaders());

        String confirm = prompter.prompt("Confirm Excel documentation: outputFile='target/" + outputFile + (outputFile.endsWith(".xls") ? "" : ".xls") + "'\n",
                Arrays.asList("y", "n"), "y");

        if (confirm.equalsIgnoreCase("n")) {
            return;
        }

        ExcelTestDocsGenerator generator = getExcelTestDocsGenerator();

        generator.withOutputFile(outputFile + (outputFile.endsWith(".xls") ? "" : ".xls"))
                .withPageTitle(pageTitle)
                .withAuthor(author)
                .withCompany(company)
                .useSrcDirectory(getTestSrcDirectory())
                .withCustomHeaders(headers);

        generator.generateDoc();

        getLog().info("Successfully created Excel documentation: outputFile='target/" + outputFile + (outputFile.endsWith(".xls") ? "" : ".xls") + "'");
    }

    /**
     * Gets the htmlTestDocsGenerator.
     *
     * @return
     */
    public HtmlTestDocsGenerator getHtmlTestDocsGenerator() {
        return Optional.ofNullable(htmlTestDocsGenerator).orElseGet(HtmlTestDocsGenerator::build);
    }

    /**
     * Gets the excelTestDocsGenerator.
     *
     * @return
     */
    public ExcelTestDocsGenerator getExcelTestDocsGenerator() {
        return Optional.ofNullable(excelTestDocsGenerator).orElseGet(ExcelTestDocsGenerator::build);
    }

    /**
     * Sets the prompter.
     * @param prompter the prompter to set
     */
    public void setPrompter(Prompter prompter) {
        this.prompter = prompter;
    }
}
