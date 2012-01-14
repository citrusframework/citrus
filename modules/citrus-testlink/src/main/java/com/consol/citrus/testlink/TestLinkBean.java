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
 * last modified: Friday, January 13, 2012 (18:58) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;


/**
 * TestLink bean holding all relevant data for a given test case. It was found that actual a TestSuite is not needed.
 * Keep it anyway in case the TestSuite corresponding to the TestCase is needed.
 *
 * @author  Matthias Beil
 * @since   CITRUS 1.2 M2
 */
public final class TestLinkBean {

    // ~ Instance fields -----------------------------------------------------------------------------------------------

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

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code TestLinkBean} class.
     */
    public TestLinkBean() {

        super();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * Returns the value of the {@code project} field.
     *
     * @return  {@code project} field.
     */
    public TestProject getProject() {

        return this.project;
    }

    /**
     * Sets the value of the {@code project} field.
     *
     * @param  projectIn  field to set.
     */
    public void setProject(final TestProject projectIn) {

        this.project = projectIn;
    }

    /**
     * Returns the value of the {@code plan} field.
     *
     * @return  {@code plan} field.
     */
    public TestPlan getPlan() {

        return this.plan;
    }

    /**
     * Sets the value of the {@code plan} field.
     *
     * @param  planIn  field to set.
     */
    public void setPlan(final TestPlan planIn) {

        this.plan = planIn;
    }

    /**
     * Returns the value of the {@code build} field.
     *
     * @return  {@code build} field.
     */
    public Build getBuild() {

        return this.build;
    }

    /**
     * Sets the value of the {@code build} field.
     *
     * @param  buildIn  field to set.
     */
    public void setBuild(final Build buildIn) {

        this.build = buildIn;
    }

    /**
     * Returns the value of the {@code suite} field.
     *
     * @return  {@code suite} field.
     */
    public TestSuite getSuite() {

        return this.suite;
    }

    /**
     * Sets the value of the {@code suite} field.
     *
     * @param  suiteIn  field to set.
     */
    public void setSuite(final TestSuite suiteIn) {

        this.suite = suiteIn;
    }

    /**
     * Returns the value of the {@code test case} field.
     *
     * @return  {@code test case} field.
     */
    public TestCase getTestCase() {

        return this.testCase;
    }

    /**
     * Sets the value of the {@code test case} field.
     *
     * @param  testCaseIn  field to set.
     */
    public void setTestCase(final TestCase testCaseIn) {

        this.testCase = testCaseIn;
    }

    /**
     * @see  Object#hashCode()
     */
    @Override public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result) +
            (((null != this.build) && (null != this.build.getId())) ? this.build.getId().hashCode() : 0);
        result = (prime * result) +
            (((null != this.testCase) && (null != this.testCase.getId())) ? this.testCase.getId().hashCode() : 0);
        result = (prime * result) +
            (((null != this.plan) && (null != this.plan.getId())) ? this.plan.getId().hashCode() : 0);
        result = (prime * result) +
            (((null != this.project) && (null != this.project.getId())) ? this.project.getId().hashCode() : 0);
        result = (prime * result) +
            (((null != this.suite) && (null != this.suite.getId())) ? this.suite.getId().hashCode() : 0);

        return result;
    }

    /**
     * @see  Object#equals(Object)
     */
    @Override public boolean equals(final Object obj) {

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

        if (null == this.project) {

            if (null != other.getProject()) {

                return false;
            }
        } else {

            if (null == other.getProject()) {

                return false;
            }

            if (null == this.project.getId()) {

                if (null != other.getProject().getId()) {

                    return false;
                }
            } else {

                if (!this.project.getId().equals(other.getProject().getId())) {

                    return false;
                }
            }
        }

        if (null == this.suite) {

            if (null != other.getSuite()) {

                return false;
            }
        } else {

            if (null == other.getSuite()) {

                return false;
            }

            if (null == this.suite.getId()) {

                if (null != other.getSuite().getId()) {

                    return false;
                }
            } else {

                if (!this.suite.getId().equals(other.getSuite().getId())) {

                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @see  Object#toString();
     */
    @Override public String toString() {

        return "TestLinkBean [\nproject=" + this.project + ", \nplan=" + this.plan + ", \nbuild=" + this.build +
            ", \nsuite=" + this.suite + ", \ntestCase=" + this.testCase + "\n]";
    }

}
