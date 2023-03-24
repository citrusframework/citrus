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

package org.citrusframework.mvn.plugin.config.docs;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class ExcelDocConfiguration {

    /**
     * Name of company that goes into Excel meta information.
     */
    @Parameter(property = "citrus.docs.excel.company", defaultValue = "Unknown")
    private String company;

    /**
     * Author name that goes into Excel meta information.
     */
    @Parameter(property = "citrus.docs.excel.author", defaultValue = "Citrus Testframework")
    private String author;

    /**
     * Name of output file (.xsl file extension is added automatically and can be left out). Defaults to "CitrusTests".
     */
    @Parameter(property = "citrus.docs.excel.outputFile", defaultValue = "CitrusTests")
    private String outputFile;

    /**
     * Page title displayed on top of the sheet.
     */
    @Parameter(property = "citrus.docs.excel.pageTitle", defaultValue = "Citrus Test Documentation")
    private String pageTitle;

    /**
     * Customized column headers as comma separated value string (e.g. "Nr;Name;Author;Status;TestCase;Date").
     */
    @Parameter(property = "citrus.docs.excel.headers")
    private String headers;

    /**
     * Gets the company.
     *
     * @return
     */
    public String getCompany() {
        return company;
    }

    /**
     * Sets the company.
     *
     * @param company
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * Gets the author.
     *
     * @return
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author.
     *
     * @param author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the outputFile.
     *
     * @return
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * Sets the outputFile.
     *
     * @param outputFile
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * Gets the pageTitle.
     *
     * @return
     */
    public String getPageTitle() {
        return pageTitle;
    }

    /**
     * Sets the pageTitle.
     *
     * @param pageTitle
     */
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    /**
     * Gets the headers.
     *
     * @return
     */
    public String getHeaders() {
        return headers;
    }

    /**
     * Sets the headers.
     *
     * @param headers
     */
    public void setHeaders(String headers) {
        this.headers = headers;
    }

}
