/*
 * File: CitrusBean.java
 *
 * Copyright (c) 2006-2012 the original author or authors.
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
 *
 * last modified: Saturday, January 28, 2012 (11:21) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.testlink.TestLinkCitrusBean;

/**
 * Bean holding all CITRUS related variables.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public final class CitrusBean {

    // ~ Instance fields -------------------------------------------------------------------------

    /** create. */
    private boolean create;

    /** javaFileValid. */
    private boolean javaFileValid;

    /** testFileValid. */
    private boolean testFileValid;

    /** name. */
    private String name;

    /** author. */
    private String author;

    /** targetPackage. */
    private String targetPackage;

    /** framework. */
    private String framework;

    /** javaFileName. */
    private String javaFileName;

    /** testFileName. */
    private String testFileName;

    /** testLink. */
    private TestLinkCitrusBean testLink;

    /** variables. */
    private final Map<String, String> variables;

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusBean} class.
     */
    public CitrusBean() {

        super();

        this.variables = new HashMap<String, String>();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Returns the value of the {@code java file valid} field.
     *
     * @return {@code java file valid} field.
     */
    public boolean isJavaFileValid() {

        return this.javaFileValid;
    }

    /**
     * Sets the value of the {@code java file valid} field.
     *
     * @param javaFileValidIn
     *            field to set.
     */
    public void setJavaFileValid(final boolean javaFileValidIn) {

        this.javaFileValid = javaFileValidIn;
    }

    /**
     * Returns the value of the {@code test file valid} field.
     *
     * @return {@code test file valid} field.
     */
    public boolean isTestFileValid() {

        return this.testFileValid;
    }

    /**
     * Sets the value of the {@code test file valid} field.
     *
     * @param testFileValidIn
     *            field to set.
     */
    public void setTestFileValid(final boolean testFileValidIn) {

        this.testFileValid = testFileValidIn;
    }

    /**
     * Returns the value of the {@code java file name} field.
     *
     * @return {@code java file name} field.
     */
    public String getJavaFileName() {

        return this.javaFileName;
    }

    /**
     * Sets the value of the {@code java file name} field.
     *
     * @param javaFileNameIn
     *            field to set.
     */
    public void setJavaFileName(final String javaFileNameIn) {

        this.javaFileName = javaFileNameIn;
    }

    /**
     * Returns the value of the {@code test file name} field.
     *
     * @return {@code test file name} field.
     */
    public String getTestFileName() {

        return this.testFileName;
    }

    /**
     * Sets the value of the {@code test file name} field.
     *
     * @param testFileNameIn
     *            field to set.
     */
    public void setTestFileName(final String testFileNameIn) {

        this.testFileName = testFileNameIn;
    }

    /**
     * Returns the value of the {@code create} field.
     *
     * @return {@code create} field.
     */
    public boolean isCreate() {

        return this.create;
    }

    /**
     * Sets the value of the {@code create} field.
     *
     * @param createIn
     *            field to set.
     */
    public void setCreate(final boolean createIn) {

        this.create = createIn;
    }

    /**
     * Returns the value of the {@code test link} field.
     *
     * @return {@code test link} field.
     */
    public TestLinkCitrusBean getTestLink() {

        return this.testLink;
    }

    /**
     * Sets the value of the {@code test link} field.
     *
     * @param testLinkIn
     *            field to set.
     */
    public void setTestLink(final TestLinkCitrusBean testLinkIn) {

        this.testLink = testLinkIn;
    }

    /**
     * Returns the value of the {@code name} field.
     *
     * @return {@code name} field.
     */
    public String getName() {

        return this.name;
    }

    /**
     * Sets the value of the {@code name} field.
     *
     * @param nameIn
     *            field to set.
     */
    public void setName(final String nameIn) {

        this.name = nameIn;
    }

    /**
     * Returns the value of the {@code author} field.
     *
     * @return {@code author} field.
     */
    public String getAuthor() {

        return this.author;
    }

    /**
     * Sets the value of the {@code author} field.
     *
     * @param authorIn
     *            field to set.
     */
    public void setAuthor(final String authorIn) {

        this.author = authorIn;
    }

    /**
     * Returns the value of the {@code target package} field.
     *
     * @return {@code target package} field.
     */
    public String getTargetPackage() {

        return this.targetPackage;
    }

    /**
     * Sets the value of the {@code target package} field.
     *
     * @param targetPackageIn
     *            field to set.
     */
    public void setTargetPackage(final String targetPackageIn) {

        this.targetPackage = targetPackageIn;
    }

    /**
     * Returns the value of the {@code framework} field.
     *
     * @return {@code framework} field.
     */
    public String getFramework() {

        return this.framework;
    }

    /**
     * Sets the value of the {@code framework} field.
     *
     * @param frameworkIn
     *            field to set.
     */
    public void setFramework(final String frameworkIn) {

        this.framework = frameworkIn;
    }

    /**
     * Returns the value of the {@code variables} field.
     *
     * @return {@code variables} field.
     */
    public Map<String, String> getVariables() {

        return this.variables;
    }

    /**
     * Add variable to internal variable map.
     *
     * @param key
     *            Key for identifying the value.
     * @param value
     *            Value to save.
     */
    public void addVariable(final String key, final String value) {

        if (((null != key) && (!key.isEmpty())) && ((null != value) && (!value.isEmpty()))) {

            // allow overwriting of key / value pairs
            this.variables.put(key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.author == null) ? 0 : this.author.hashCode());
        result = (prime * result) + (this.create ? 1231 : 1237);
        result = (prime * result) + ((this.framework == null) ? 0 : this.framework.hashCode());
        result = (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
        result = (prime * result)
                + ((this.targetPackage == null) ? 0 : this.targetPackage.hashCode());
        result = (prime * result) + ((this.testLink == null) ? 0 : this.testLink.hashCode());
        result = (prime * result) + ((this.variables == null) ? 0 : this.variables.hashCode());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final CitrusBean other = (CitrusBean) obj;

        if (this.author == null) {

            if (other.author != null) {
                return false;
            }
        } else if (!this.author.equals(other.author)) {
            return false;
        }

        if (this.create != other.create) {
            return false;
        }

        if (this.framework == null) {

            if (other.framework != null) {
                return false;
            }
        } else if (!this.framework.equals(other.framework)) {
            return false;
        }

        if (this.name == null) {

            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }

        if (this.targetPackage == null) {

            if (other.targetPackage != null) {
                return false;
            }
        } else if (!this.targetPackage.equals(other.targetPackage)) {
            return false;
        }

        if (this.testLink == null) {

            if (other.testLink != null) {
                return false;
            }
        } else if (!this.testLink.equals(other.testLink)) {
            return false;
        }

        if (this.variables == null) {

            if (other.variables != null) {
                return false;
            }
        } else if (!this.variables.equals(other.variables)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("CitrusBean [create=");
        builder.append(this.create);
        builder.append(", javaFileValid=");
        builder.append(this.javaFileValid);
        builder.append(", testFileValid=");
        builder.append(this.testFileValid);
        builder.append(", name=");
        builder.append(this.name);
        builder.append(", author=");
        builder.append(this.author);
        builder.append(", targetPackage=");
        builder.append(this.targetPackage);
        builder.append(", framework=");
        builder.append(this.framework);
        builder.append(", javaFileName=");
        builder.append(this.javaFileName);
        builder.append(", testFileName=");
        builder.append(this.testFileName);
        builder.append(", testLink=");
        builder.append(this.testLink);
        builder.append(", variables=");
        builder.append(this.variables);
        builder.append("]");

        return builder.toString();
    }

}
