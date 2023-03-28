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

import java.io.Serializable;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class HtmlDocConfiguration implements Serializable {

    /**
     * The overview title displayed at the top of the test overview
     */
    @Parameter(property = "citrus.docs.html.heading", defaultValue = "Overview")
    private String heading;

    /**
     * Number of columns in test overview table
     */
    @Parameter(property = "citrus.docs.html.columns", defaultValue = "1")
    private String columns;

    /**
     * Name of output file (.html file extension is added automatically and can be left out). Defaults to "CitrusTests"
     */
    @Parameter(property = "citrus.docs.html.outputFile", defaultValue = "CitrusTests")
    private String outputFile;

    /**
     * Page title displayed at the top of the page
     */
    @Parameter(property = "citrus.docs.html.pageTitle", defaultValue = "Citrus Test Documentation")
    private String pageTitle;

    /**
     * Company or project logo displayed on top of page. Defaults to "logo.png"
     */
    @Parameter(property = "citrus.docs.html.logo", defaultValue = "logo.png")
    private String logo;

    /**
     * Gets the heading.
     *
     * @return
     */
    public String getHeading() {
        return heading;
    }

    /**
     * Sets the heading.
     *
     * @param heading
     */
    public void setHeading(String heading) {
        this.heading = heading;
    }

    /**
     * Gets the columns.
     *
     * @return
     */
    public String getColumns() {
        return columns;
    }

    /**
     * Sets the columns.
     *
     * @param columns
     */
    public void setColumns(String columns) {
        this.columns = columns;
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
     * Gets the logo.
     *
     * @return
     */
    public String getLogo() {
        return logo;
    }

    /**
     * Sets the logo.
     *
     * @param logo
     */
    public void setLogo(String logo) {
        this.logo = logo;
    }

}
