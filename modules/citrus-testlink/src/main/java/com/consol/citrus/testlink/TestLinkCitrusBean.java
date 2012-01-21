/*
 * File: TestLinkCitrusBean.java
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
 * last modified: Saturday, January 21, 2012 (20:19) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean holding all values for TestLink to CITRUS handling.
 * 
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public final class TestLinkCitrusBean extends AbstractTestLinkBean {

    // ~ Instance fields -------------------------------------------------------------------------

    /** testProjectId. */
    private Integer testProjectId;

    /** testCaseVersion. */
    private Integer testCaseVersion;

    /** testProjectName. */
    private String testProjectName;

    /** testProjectPrefix. */
    private String testProjectPrefix;

    /** testPlanName. */
    private String testPlanName;

    /** testCaseName. */
    private String testCaseName;

    /** testCaseDesc. */
    private String testCaseDesc;

    /** platformList. */
    private final List<String> platformList;

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code TestLinkCitrusBean} class.
     */
    public TestLinkCitrusBean() {

        super();

        this.platformList = new ArrayList<String>();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Returns the value of the {@code test project id} field.
     * 
     * @return {@code test project id} field.
     */
    public Integer getTestProjectId() {

        return this.testProjectId;
    }

    /**
     * Sets the value of the {@code test project id} field.
     * 
     * @param testProjectIdIn
     *            field to set.
     */
    public void setTestProjectId(final Integer testProjectIdIn) {

        this.testProjectId = testProjectIdIn;
    }

    /**
     * Returns the value of the {@code test case version} field.
     * 
     * @return {@code test case version} field.
     */
    public Integer getTestCaseVersion() {

        return this.testCaseVersion;
    }

    /**
     * Sets the value of the {@code test case version} field.
     * 
     * @param testCaseVersionIn
     *            field to set.
     */
    public void setTestCaseVersion(final Integer testCaseVersionIn) {

        this.testCaseVersion = testCaseVersionIn;
    }

    /**
     * Returns the value of the {@code test project name} field.
     * 
     * @return {@code test project name} field.
     */
    public String getTestProjectName() {

        return this.testProjectName;
    }

    /**
     * Sets the value of the {@code test project name} field.
     * 
     * @param testProjectNameIn
     *            field to set.
     */
    public void setTestProjectName(final String testProjectNameIn) {

        this.testProjectName = testProjectNameIn;
    }

    /**
     * Returns the value of the {@code test plan name} field.
     * 
     * @return {@code test plan name} field.
     */
    public String getTestPlanName() {

        return this.testPlanName;
    }

    /**
     * Sets the value of the {@code test plan name} field.
     * 
     * @param testPlanNameIn
     *            field to set.
     */
    public void setTestPlanName(final String testPlanNameIn) {

        this.testPlanName = testPlanNameIn;
    }

    /**
     * Returns the value of the {@code test project prefix} field.
     * 
     * @return {@code test project prefix} field.
     */
    public String getTestProjectPrefix() {

        return this.testProjectPrefix;
    }

    /**
     * Sets the value of the {@code test project prefix} field.
     * 
     * @param testProjectPrefixIn
     *            field to set.
     */
    public void setTestProjectPrefix(final String testProjectPrefixIn) {

        this.testProjectPrefix = testProjectPrefixIn;
    }

    /**
     * Returns the value of the {@code test case name} field.
     * 
     * @return {@code test case name} field.
     */
    public String getTestCaseName() {

        return this.testCaseName;
    }

    /**
     * Sets the value of the {@code test case name} field.
     * 
     * @param testCaseNameIn
     *            field to set.
     */
    public void setTestCaseName(final String testCaseNameIn) {

        this.testCaseName = testCaseNameIn;
    }

    /**
     * Returns the value of the {@code platform list} field.
     * 
     * @return {@code platform list} field.
     */
    public List<String> getPlatformList() {

        return this.platformList;
    }

    /**
     * Add platform to list.
     * 
     * @param platformIn
     *            Platform to add to list.
     */
    public void addPlatform(final String platformIn) {

        if ((null != platformIn) && (!platformIn.isEmpty())) {

            this.platformList.add(platformIn);
        }
    }

    /**
     * Returns the value of the {@code test case desc} field.
     * 
     * @return {@code test case desc} field.
     */
    public String getTestCaseDesc() {

        return this.testCaseDesc;
    }

    /**
     * Sets the value of the {@code test case desc} field.
     * 
     * @param testCaseDescIn
     *            field to set.
     */
    public void setTestCaseDesc(final String testCaseDescIn) {

        this.testCaseDesc = testCaseDescIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + ((this.testCaseName == null) ? 0 : this.testCaseName.hashCode());
        result = (prime * result)
                + ((this.testCaseVersion == null) ? 0 : this.testCaseVersion.hashCode());

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

        if (!super.equals(obj)) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final TestLinkCitrusBean other = (TestLinkCitrusBean) obj;

        if (this.testCaseName == null) {

            if (other.testCaseName != null) {
                return false;
            }
        } else if (!this.testCaseName.equals(other.testCaseName)) {
            return false;
        }

        if (this.testCaseVersion == null) {

            if (other.testCaseVersion != null) {
                return false;
            }
        } else if (!this.testCaseVersion.equals(other.testCaseVersion)) {
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
        builder.append("TestLinkCitrusBean [testProjectId=");
        builder.append(this.testProjectId);
        builder.append(", testCaseVersion=");
        builder.append(this.testCaseVersion);
        builder.append(", testProjectName=");
        builder.append(this.testProjectName);
        builder.append(", testProjectPrefix=");
        builder.append(this.testProjectPrefix);
        builder.append(", testPlanName=");
        builder.append(this.testPlanName);
        builder.append(", testCaseName=");
        builder.append(this.testCaseName);
        builder.append(", testCaseDesc=");
        builder.append(this.testCaseDesc);
        builder.append(", platformList=");
        builder.append(this.platformList);
        builder.append(", getTestProjectId()=");
        builder.append(this.getTestProjectId());
        builder.append(", getTestCaseVersion()=");
        builder.append(this.getTestCaseVersion());
        builder.append(", getTestProjectName()=");
        builder.append(this.getTestProjectName());
        builder.append(", getTestPlanName()=");
        builder.append(this.getTestPlanName());
        builder.append(", getTestProjectPrefix()=");
        builder.append(this.getTestProjectPrefix());
        builder.append(", getTestCaseName()=");
        builder.append(this.getTestCaseName());
        builder.append(", getPlatformList()=");
        builder.append(this.getPlatformList());
        builder.append(", getTestCaseDesc()=");
        builder.append(this.getTestCaseDesc());
        builder.append("]");

        return builder.toString();
    }

}
