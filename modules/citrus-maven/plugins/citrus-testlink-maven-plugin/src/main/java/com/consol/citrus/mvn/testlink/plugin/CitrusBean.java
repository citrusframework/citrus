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
 * last modified: Monday, January 2, 2012 (19:43) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import com.consol.citrus.testlink.TestLinkBean;

/**
 * Bean holding all CITRUS related variables.
 * 
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public final class CitrusBean {

    // ~ Instance fields -----------------------------------------------------------------------------------------------

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

    /** description. */
    private String description;

    /** targetPackage. */
    private String targetPackage;

    /** framework. */
    private String framework;

    /** javaFileName. */
    private String javaFileName;

    /** testFileName. */
    private String testFileName;

    /** testLink. */
    private TestLinkBean testLink;

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusBean} class.
     */
    public CitrusBean() {

        super();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

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
    public TestLinkBean getTestLink() {

        return this.testLink;
    }

    /**
     * Sets the value of the {@code test link} field.
     * 
     * @param testLinkIn
     *            field to set.
     */
    public void setTestLink(final TestLinkBean testLinkIn) {

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
     * Returns the value of the {@code description} field.
     * 
     * @return {@code description} field.
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * Sets the value of the {@code description} field.
     * 
     * @param descriptionIn
     *            field to set.
     */
    public void setDescription(final String descriptionIn) {

        this.description = descriptionIn;
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.author == null) ? 0 : this.author.hashCode());
        result = (prime * result) + (this.create ? 1231 : 1237);
        result = (prime * result) + ((this.description == null) ? 0 : this.description.hashCode());
        result = (prime * result) + ((this.framework == null) ? 0 : this.framework.hashCode());
        result = (prime * result) + ((this.javaFileName == null) ? 0 : this.javaFileName.hashCode());
        result = (prime * result) + (this.javaFileValid ? 1231 : 1237);
        result = (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
        result = (prime * result) + ((this.targetPackage == null) ? 0 : this.targetPackage.hashCode());
        result = (prime * result) + ((this.testFileName == null) ? 0 : this.testFileName.hashCode());
        result = (prime * result) + (this.testFileValid ? 1231 : 1237);
        result = (prime * result) + ((this.testLink == null) ? 0 : this.testLink.hashCode());

        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
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

        if (this.description == null) {

            if (other.description != null) {
                return false;
            }
        } else if (!this.description.equals(other.description)) {
            return false;
        }

        if (this.framework == null) {

            if (other.framework != null) {
                return false;
            }
        } else if (!this.framework.equals(other.framework)) {
            return false;
        }

        if (this.javaFileName == null) {

            if (other.javaFileName != null) {
                return false;
            }
        } else if (!this.javaFileName.equals(other.javaFileName)) {
            return false;
        }

        if (this.javaFileValid != other.javaFileValid) {
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

        if (this.testFileName == null) {

            if (other.testFileName != null) {
                return false;
            }
        } else if (!this.testFileName.equals(other.testFileName)) {
            return false;
        }

        if (this.testFileValid != other.testFileValid) {
            return false;
        }

        if (this.testLink == null) {

            if (other.testLink != null) {
                return false;
            }
        } else if (!this.testLink.equals(other.testLink)) {
            return false;
        }

        return true;
    }

    /**
     * @see java.lang.Object#toString()
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
        builder.append(", description=");
        builder.append(this.description);
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
        builder.append("]");

        return builder.toString();
    }

}
