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

package org.citrusframework.mvn.plugin.config.tests;

import org.citrusframework.mvn.plugin.config.dictionary.DataDictionaryConfiguration;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.Serializable;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class TestConfiguration implements Serializable {

    @Parameter(property = "citrus.test.name", required = true)
    private String name;

    /**
     * Should generate disabled tests.
     */
    @Parameter(property = "citrus.test.disabled", defaultValue = "false")
    private boolean disabled;

    /**
     * The name suffix of all test cases.
     */
    @Parameter(property = "citrus.test.suffix", defaultValue = "_IT")
    private String suffix = "_IT";

    /**
     * The test author
     */
    @Parameter(property = "citrus.test.author", defaultValue = "Unknown")
    private String author = "Unknown";

    /**
     * Describes the test case and its actions
     */
    @Parameter(property = "citrus.test.description", defaultValue = "TODO: Description")
    private String description = "TODO: Description";

    /**
     * Which package (folder structure) is assigned to this test. Defaults to "org.citrusframework"
     */
    @Parameter(property = "citrus.test.packageName", defaultValue = "org.citrusframework")
    private String packageName = "org.citrusframework";

    /**
     * Endpoint used to send and receive messages in generated test.
     */
    @Parameter(property = "citrus.test.endpoint", defaultValue = "default")
    private String endpoint = "default";

    @Parameter
    private SwaggerConfiguration swagger;

    @Parameter
    private WsdlConfiguration wsdl;

    @Parameter
    private XsdConfiguration xsd;

    @Parameter
    private DataDictionaryConfiguration dictionary;

    /**
     * Gets the name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the disabled.
     *
     * @return
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Sets the disabled.
     *
     * @param disabled
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Gets the suffix.
     *
     * @return
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the suffix.
     *
     * @param suffix
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
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
     * Gets the description.
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the packageName.
     *
     * @return
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the packageName.
     *
     * @param packageName
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Gets the swagger.
     *
     * @return
     */
    public SwaggerConfiguration getSwagger() {
        return swagger;
    }

    /**
     * Sets the swagger.
     *
     * @param swagger
     */
    public void setSwagger(SwaggerConfiguration swagger) {
        this.swagger = swagger;
    }

    /**
     * Gets the wsdl.
     *
     * @return
     */
    public WsdlConfiguration getWsdl() {
        return wsdl;
    }

    /**
     * Sets the wsdl.
     *
     * @param wsdl
     */
    public void setWsdl(WsdlConfiguration wsdl) {
        this.wsdl = wsdl;
    }

    /**
     * Gets the xsd.
     *
     * @return
     */
    public XsdConfiguration getXsd() {
        return xsd;
    }

    /**
     * Sets the xsd.
     *
     * @param xsd
     */
    public void setXsd(XsdConfiguration xsd) {
        this.xsd = xsd;
    }

    /**
     * Gets the endpoint.
     *
     * @return
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the endpoint.
     *
     * @param endpoint
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the dictionary.
     *
     * @return
     */
    public DataDictionaryConfiguration getDictionary() {
        return dictionary;
    }

    /**
     * Sets the dictionary.
     *
     * @param dictionary
     */
    public void setDictionary(DataDictionaryConfiguration dictionary) {
        this.dictionary = dictionary;
    }
}
