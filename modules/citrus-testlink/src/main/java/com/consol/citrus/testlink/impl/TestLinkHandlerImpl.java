/*
 * File: TestLinkHandlerImpl.java
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
 * last modified: Saturday, January 21, 2012 (22:31) by: Matthias Beil
 */
package com.consol.citrus.testlink.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.TestLinkAPIException;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.Platform;
import br.eti.kinoshita.testlinkjavaapi.model.ReportTCResultResponse;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;

import com.consol.citrus.testlink.CitrusTestLinkBean;
import com.consol.citrus.testlink.CitrusTestLinkHandler;
import com.consol.citrus.testlink.TestLinkCitrusBean;
import com.consol.citrus.testlink.TestLinkCitrusHandler;

/**
 * Implementation of interaction with TestLink.
 * 
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public final class TestLinkHandlerImpl implements TestLinkCitrusHandler, CitrusTestLinkHandler {

    // ~ Static fields/initializers --------------------------------------------------------------

    /** XML_RPC. */
    private static final String XML_RPC = "/lib/api/xmlrpc.php";

    /** log. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestLinkHandlerImpl.class);

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code TestLinkHandlerImpl} class.
     */
    public TestLinkHandlerImpl() {

        super();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void writeToTestLink(final CitrusTestLinkBean bean) {

        try {

            final String notes;
            final ExecutionStatus status;

            // depending on the success of the CITRUS test case set status and notes
            if (bean.getSuccess().booleanValue()) {

                status = ExecutionStatus.PASSED;
                notes = bean.getNotesSuccess();
            } else {

                status = ExecutionStatus.FAILED;
                notes = bean.getNotesFailure();
            }

            // establish connection to TestLink
            final TestLinkAPI api = this.connect(bean.getUrl(), bean.getKey());

            // set result and read response
            final ReportTCResultResponse response = api.reportTCResult(bean.getTestCaseId(),
                    bean.getTestCaseInternalId(), bean.getTestPlanId(), status, bean.getBuildId(),
                    bean.getBuildName(), notes, null, // guess
                    null, // bug id
                    null, // platform id
                    bean.getPlatform(), // platform name
                    null, // custom fields
                    null);

            // set response
            bean.setResponseState(Boolean.TRUE);
            bean.addResponse(response.getMessage());
        } catch (final Exception ex) {

            // set response to failure
            bean.setResponseState(Boolean.FALSE);
            bean.addResponse("Exception caught while writing to TestLink");
            bean.setResponseCause(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<TestLinkCitrusBean> readTestCases(final String url, final String key) {

        // make sure the returned list is never null
        final List<TestLinkCitrusBean> beanList = new ArrayList<TestLinkCitrusBean>();

        try {

            // open connection to TestLink, there seems to be no close method
            final TestLinkAPI api = this.connect(url, key);

            // get all available projects
            final TestProject[] projects = this.readTestProjects(api);

            // make sure there are some project(s)
            if (null != projects) {

                // iterate over all projects
                for (final TestProject project : projects) {

                    // make sure that project is active,
                    // but ignore isPublic flag as this is for testing
                    if (project.isActive().booleanValue()) {

                        // get build for project and get test case(s)
                        this.handleBuild(beanList, project, api, url);
                    } else {

                        // inform user that this is not an active project
                        LOGGER.info("Skipping project [ {} ] as it is not active!", project.getName());
                    }
                }
            }
        } catch (final Exception ex) {

            LOGGER.error("Error while reading test case(s) from TestLink!", ex);
        }

        return beanList;
    }

    /**
     * Get for each test plan the latest build. For this build get all test suite(s) and for each test
     * suite get all test case(s).
     * 
     * @param testCaseList
     *            List to add new test case(s).
     * @param project
     *            Project for getting test plans for.
     * @param api
     *            Connection object for TestLink.
     * @param url
     *            TestLink URL without the XML-RPC part, in the TestLinkAPI the full URL is saved.
     */
    private void handleBuild(final List<TestLinkCitrusBean> testCaseList, final TestProject project,
            final TestLinkAPI api, final String url) {

        // get test plan(s) for this project
        final TestPlan[] plans = this.readTestPlans(api, project.getId());

        // make sure there are some test plan(s)
        if (null != plans) {

            // iterate over all test plan(s)
            for (final TestPlan plan : plans) {

                // make sure the test plan is active
                if (plan.isActive().booleanValue()) {

                    // get the latest build for this test plan,
                    // as only one single build should be assign for a test plan
                    final Build build = api.getLatestBuildForTestPlan(plan.getId());

                    // make sure there is a build and a corresponding test plan ID
                    if ((null != build) && (null != build.getTestPlanId())) {

                        if (!plan.getId().equals(build.getTestPlanId())) {

                            // do not know what to do when this happens log it to see if it happens
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(
                                        "Test plan ID is [ {} ] but build test plan ID is [ {} ]?",
                                        plan.getId(), build.getTestPlanId());
                            }
                        }

                        // handle all test case(s)
                        this.handleTestCases(testCaseList, project, plan, build, api, url);
                    }
                } else {

                    // inform user that this is not an active test plan
                    LOGGER.info("Skipping test plan [ {} ] as it is not active!", plan.getName());
                }
            }
        }
    }

    /**
     * Handle all test case(s) for the latest build for the given test plan of the given project.
     * 
     * @param beanList
     *            List to add new test case(s).
     * @param project
     *            TestProject for this test case(s).
     * @param plan
     *            TestPlan for this test case(s)
     * @param build
     *            Build for this test case(s).
     * @param api
     *            Connection object for TestLink.
     * @param url
     *            TestLink URL without the XML-RPC part, in the TestLinkAPI the full URL is saved.
     */
    private void handleTestCases(final List<TestLinkCitrusBean> beanList, final TestProject project,
            final TestPlan plan, final Build build, final TestLinkAPI api, final String url) {

        // get all test case(s) for each test suite
        final TestCase[] cases = this.readTestCases(api, build.getTestPlanId());

        // make sure there are some test case(s)
        if (null != cases) {

            // iterate over all test case(s), regardless if marked to be manually or automatically
            for (final TestCase testCase : cases) {

                // create new TestLink CITRUS bean
                final TestLinkCitrusBean bean = new TestLinkCitrusBean();

                // fill fields with values
                bean.setBuildId(build.getId());
                bean.setBuildName(build.getName());

                bean.setKey(api.getDevKey());

                // testLink URL without XML-RPC part, as this will be stored in the CITRUS variables
                bean.setUrl(url);

                // preset with some value
                bean.setNotesFailure("failure-to-be-defined");
                bean.setNotesSuccess("success-to-be-defined");

                bean.setTestCaseId(testCase.getId());
                bean.setTestCaseInternalId(testCase.getInternalId());

                final String summary = testCase.getSummary();

                if ((null != summary) && (!summary.isEmpty())) {

                    // description will be used as a XML string, so escape this string
                    bean.setTestCaseDesc(StringEscapeUtils.escapeXml(summary));
                }

                bean.setTestCaseName(testCase.getName());
                bean.setTestCaseVersion(testCase.getVersion());

                bean.setTestPlanId(plan.getId());
                bean.setTestPlanName(plan.getName());

                bean.setTestProjectId(project.getId());
                bean.setTestProjectName(project.getName());
                bean.setTestProjectPrefix(project.getPrefix());

                // try to get all platform(s)
                this.readPlatforms(bean, api, build.getTestPlanId());

                // add this test case to the returning result list
                beanList.add(bean);
            }
        }
    }

    /**
     * Establish connection to TestLink. A new connection object is created and returned.
     * 
     * @param url
     *            URL pointing to TestLink XML-RPC.
     * @param key
     *            Development key as generated in TestLink.
     * 
     * @return Newly created {@link TestLinkAPI} object.
     * 
     * @throws TestLinkAPIException
     *             MojoExecutionException Thrown in case the connection could not be established.
     */
    private TestLinkAPI connect(final String url, final String key) throws TestLinkAPIException {

        final String rpcUrl = (url + TestLinkHandlerImpl.XML_RPC);

        try {

            // build final URL
            final URL testlinkUrl = new URL(rpcUrl);

            // get new connection using URL and development key
            return new TestLinkAPI(testlinkUrl, key);
        } catch (final TestLinkAPIException tlex) {

            throw tlex;
        } catch (final Exception ex) {

            throw new TestLinkAPIException("Could not connect to TestLink with URL [ " + rpcUrl
                    + " ]", ex);
        }
    }

    /**
     * Read all test project(s) catching any exceptions.
     * 
     * @param api
     *            Connection object for TestLink.
     * 
     * @return Array of {@link TestProject} in case there was no error and there are some element(s)
     *         otherwise {@code null} is returned.
     */
    private TestProject[] readTestProjects(final TestLinkAPI api) {

        try {

            // read test project(s)
            final TestProject[] projects = api.getProjects();

            // make sure there are some project(s)
            if ((null != projects) && (projects.length > 0)) {

                return projects;
            }
        } catch (final Exception ex) {

            LOGGER.error("Exception caught while reading project(s)!", ex);
        }

        // no project(s) found
        return null;
    }

    /**
     * Read all test plan(s) catching any exceptions and allowing to continue for next project in case
     * this one has a problem.
     * 
     * @param api
     *            Connection object for TestLink.
     * @param projectId
     *            Test project ID.
     * 
     * @return Array of {@link TestPlan} in case there was no error and there are some element(s)
     *         otherwise {@code null} is returned.
     */
    private TestPlan[] readTestPlans(final TestLinkAPI api, final Integer projectId) {

        try {

            // get all TestLink test plan(s) for this test project ID
            final TestPlan[] plans = api.getProjectTestPlans(projectId);

            // check if there are some test plan(s)
            if ((null != plans) && (plans.length > 0)) {

                // there are some test plan(s), return them
                return plans;
            }
        } catch (final Exception ex) {

            LOGGER.error("Exception caught while reading test plan(s) for test project ID [ {} ]!",
                    projectId, ex);
        }

        // there was some error, return null
        return null;
    }

    /**
     * Read all test case(s) catching any exceptions and allowing to continue in case this one has a
     * problem.
     * 
     * @param api
     *            Connection object for TestLink.
     * @param planId
     *            Test plan ID. This test plan ID is from the latest build for the given test plan.
     * 
     * @return Array of {@link TestCase} in case there was no error and there are some element(s)
     *         otherwise {@code null} is returned. Return also all manually test case, so the user is
     *         informed about skipping those.
     */
    private TestCase[] readTestCases(final TestLinkAPI api, final Integer planId) {

        try {

            // use this API call as there is only need for a test plan ID and it is possible to expand
            final TestCase[] cases = api.getTestCasesForTestPlan(planId, null, null, null, null,
                    null, null, null, null, Boolean.TRUE);

            // make sure there are some TestLink test case(s)
            if ((null != cases) && (cases.length > 0)) {

                // return TestLink test case(s)
                return cases;
            }
        } catch (final Exception ex) {

            LOGGER.error("Exception caught while reading test case(s) for test plan ID [ {} ]!",
                    planId, ex);
        }

        // there was some error, return null
        return null;
    }

    /**
     * Read platforms for test plan. The platform is needed when writing to TestLink.
     * 
     * @param bean
     *            TestLink CITRUS bean for adding platform name(s).
     * @param api
     *            Connection object for TestLink.
     * @param planId
     *            Test plan ID. This test plan ID is from the latest build for the given test plan.
     */
    private void readPlatforms(final TestLinkCitrusBean bean, final TestLinkAPI api,
            final Integer planId) {

        try {

            // read platform(s)
            final Platform[] platforms = api.getTestPlanPlatforms(planId);

            // check if there are some platform(s) defined for this test plan
            if ((null != platforms) && (platforms.length > 0)) {

                // add all platform(s) to the result list
                for (final Platform platform : platforms) {

                    // add name of platform
                    bean.addPlatform(platform.getName());
                }
            }
        } catch (final Exception ex) {

            LOGGER.error("Exception caught while reading platform(s) for test plan ID [ {} ]!",
                    planId, ex);
        }
    }

}
