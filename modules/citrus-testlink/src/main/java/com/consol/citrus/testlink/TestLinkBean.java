/*
 * File: TestLinkBean.java
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
 * last modified: Sunday, January 15, 2012 (10:05) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.util.List;

import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.Platform;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;

/**
 * TestLink bean holding all relevant data for a given test case. Member variables are for reading and writing to
 * TestLink.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public final class TestLinkBean {

    // ~ Instance fields -----------------------------------------------------------------------------------------------

    /** url. */
    private String url;

    /** key. */
    private String key;

    /** Single platform needed for writing to TestLink. */
    private String platform;

    /** notes. */
    private String notes;

    /** testCaseName. */
    private String testCaseName;

    /** project. */
    private TestProject project;

    /** plan. */
    private TestPlan plan;

    /** build. */
    private Build build;

    /** suite. */
    private TestSuite suite;

    /** testCase. */
    private TestCase testCase;

    /** Platform list used for creating a test case. */
    private List<Platform> platformList;

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code TestLinkBean} class.
     */
    public TestLinkBean() {

        super();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * Returns the value of the {@code url} field.
     *
     * @return {@code url} field.
     */
    public String getUrl() {

        return this.url;
    }

    /**
     * Sets the value of the {@code url} field.
     *
     * @param urlIn
     *            field to set.
     */
    public void setUrl(final String urlIn) {

        this.url = urlIn;
    }

    /**
     * Returns the value of the {@code key} field.
     *
     * @return {@code key} field.
     */
    public String getKey() {

        return this.key;
    }

    /**
     * Sets the value of the {@code key} field.
     *
     * @param keyIn
     *            field to set.
     */
    public void setKey(final String keyIn) {

        this.key = keyIn;
    }

    /**
     * Returns the value of the {@code platform} field.
     *
     * @return {@code platform} field.
     */
    public String getPlatform() {

        return this.platform;
    }

    /**
     * Sets the value of the {@code platform} field.
     *
     * @param platformIn
     *            field to set.
     */
    public void setPlatform(final String platformIn) {

        this.platform = platformIn;
    }

    /**
     * Returns the value of the {@code notes} field.
     *
     * @return {@code notes} field.
     */
    public String getNotes() {

        return this.notes;
    }

    /**
     * Sets the value of the {@code notes} field.
     *
     * @param notesIn
     *            field to set.
     */
    public void setNotes(final String notesIn) {

        this.notes = notesIn;
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
     * Returns the value of the {@code project} field.
     *
     * @return {@code project} field.
     */
    public TestProject getProject() {

        return this.project;
    }

    /**
     * Sets the value of the {@code project} field.
     *
     * @param projectIn
     *            field to set.
     */
    public void setProject(final TestProject projectIn) {

        this.project = projectIn;
    }

    /**
     * Returns the value of the {@code plan} field.
     *
     * @return {@code plan} field.
     */
    public TestPlan getPlan() {

        return this.plan;
    }

    /**
     * Sets the value of the {@code plan} field.
     *
     * @param planIn
     *            field to set.
     */
    public void setPlan(final TestPlan planIn) {

        this.plan = planIn;
    }

    /**
     * Returns the value of the {@code build} field.
     *
     * @return {@code build} field.
     */
    public Build getBuild() {

        return this.build;
    }

    /**
     * Sets the value of the {@code build} field.
     *
     * @param buildIn
     *            field to set.
     */
    public void setBuild(final Build buildIn) {

        this.build = buildIn;
    }

    /**
     * Returns the value of the {@code suite} field.
     *
     * @return {@code suite} field.
     */
    public TestSuite getSuite() {

        return this.suite;
    }

    /**
     * Sets the value of the {@code suite} field.
     *
     * @param suiteIn
     *            field to set.
     */
    public void setSuite(final TestSuite suiteIn) {

        this.suite = suiteIn;
    }

    /**
     * Returns the value of the {@code test case} field.
     *
     * @return {@code test case} field.
     */
    public TestCase getTestCase() {

        return this.testCase;
    }

    /**
     * Sets the value of the {@code test case} field.
     *
     * @param testCaseIn
     *            field to set.
     */
    public void setTestCase(final TestCase testCaseIn) {

        this.testCase = testCaseIn;
    }

    /**
     * Returns the value of the {@code platform list} field.
     *
     * @return {@code platform list} field.
     */
    public List<Platform> getPlatformList() {

        return this.platformList;
    }

    /**
     * Sets the value of the {@code platform list} field.
     *
     * @param platformListIn
     *            field to set.
     */
    public void setPlatformList(final List<Platform> platformListIn) {

        this.platformList = platformListIn;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + (((null != this.build) && (null != this.build.getId())) ? this.build.getId().hashCode() : 0);
        result = (prime * result)
                + (((null != this.testCase) && (null != this.testCase.getId())) ? this.testCase.getId().hashCode() : 0);
        result = (prime * result)
                + (((null != this.plan) && (null != this.plan.getId())) ? this.plan.getId().hashCode() : 0);

        return result;
    }

    /**
     * @see Object#equals(Object)
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

        final TestLinkBean other = (TestLinkBean) obj;

        if (null == this.build) {

            if (null != other.getBuild()) {

                return false;
            }
        } else {

            if (null == other.getBuild()) {

                return false;
            }

            if (null == this.build.getId()) {

                if (null != other.getBuild().getId()) {

                    return false;
                }
            } else {

                if (!this.build.getId().equals(other.getBuild().getId())) {

                    return false;
                }
            }
        }

        if (null == this.testCase) {

            if (null != other.getTestCase()) {

                return false;
            }
        } else {

            if (null == other.getTestCase()) {

                return false;
            }

            if (null == this.testCase.getId()) {

                if (null != other.getTestCase().getId()) {

                    return false;
                }
            } else {

                if (!this.testCase.getId().equals(other.getTestCase().getId())) {

                    return false;
                }
            }
        }

        if (null == this.plan) {

            if (null != other.getPlan()) {

                return false;
            }
        } else {

            if (null == other.getPlan()) {

                return false;
            }

            if (null == this.plan.getId()) {

                if (null != other.getPlan().getId()) {

                    return false;
                }
            } else {

                if (!this.plan.getId().equals(other.getPlan().getId())) {

                    return false;
                }
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("TestLinkBean [url=");
        builder.append(this.url);
        builder.append(", key=");
        builder.append(this.key);
        builder.append(", platform=");
        builder.append(this.platform);
        builder.append(", notes=");
        builder.append(this.notes);
        builder.append(", testCaseName=");
        builder.append(this.testCaseName);
        builder.append(", project=");
        builder.append(this.project);
        builder.append(", plan=");
        builder.append(this.plan);
        builder.append(", build=");
        builder.append(this.build);
        builder.append(", suite=");
        builder.append(this.suite);
        builder.append(", testCase=");
        builder.append(this.testCase);
        builder.append(", platformList=");
        builder.append(this.platformList);
        builder.append("]");

        return builder.toString();
    }

}
