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

package com.consol.citrus.mvn.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.Serializable;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class TestConfiguration implements Serializable {

    @Parameter(required = true)
    private String name;

    /**
     * The name suffix of all test cases.
     */
    @Parameter(defaultValue = "_IT")
    private String suffix = "_IT";

    /**
     * The test author
     */
    @Parameter(defaultValue = "Unknown")
    private String author = "Unknown";

    /**
     * Describes the test case and its actions
     */
    @Parameter(defaultValue = "TODO: Description")
    private String description = "TODO: Description";

    /**
     * Which package (folder structure) is assigned to this test. Defaults to "com.consol.citrus"
     */
    @Parameter(defaultValue = "com.consol.citrus")
    private String packageName = "com.consol.citrus";

    /**
     * Endpoint used to send and receive messages in generated test.
     */
    @Parameter(defaultValue = "default")
    private String endpoint = "default";

    @Parameter
    private WsdlConfiguration wsdl;

    @Parameter
    private XsdConfiguration xsd;

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
}
