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
 * last modified: Saturday, January 14, 2012 (21:26) by: Matthias Beil
 */
package com.consol.citrus.testlink.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.TestLinkAPIException;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.CustomField;
import br.eti.kinoshita.testlinkjavaapi.model.ExecutionType;
import br.eti.kinoshita.testlinkjavaapi.model.ReportTCResultResponse;
import br.eti.kinoshita.testlinkjavaapi.model.ResponseDetails;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;

import com.consol.citrus.testlink.TestLinkBean;
import com.consol.citrus.testlink.TestLinkHandler;

/**
 * Implementation of interacting with TestLink.
 * 
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public final class TestLinkHandlerImpl implements TestLinkHandler {

    // ~ Static fields/initializers ------------------------------------------------------------------------------------

    /** XML_RPC. */
    private static final String XML_RPC = "/lib/api/xmlrpc.php";

    /** log. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestLinkHandlerImpl.class);

    // ~ Instance fields -----------------------------------------------------------------------------------------------

    /** tlinkUrl. */
    private final String rpcUrl;

    /** devKey. */
    private final String devKey;

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code TestLinkHandlerImpl} class.
     * 
     * @param urlIn
     *            URL to TestLink to which the path to the XML RPC will be added.
     * @param devKeyIn
     *            devKey Development key as generated in TestLink.
     */
    public TestLinkHandlerImpl(final String urlIn, final String devKeyIn) {

        super();

        this.rpcUrl = urlIn;
        this.devKey = devKeyIn;
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public List<TestLinkBean> readTestCases() throws TestLinkAPIException {

        try {

            // make sure the returned list is never null
            final List<TestLinkBean> testCaseList = new ArrayList<TestLinkBean>();

            // open connection to TestLink, there seems to be no close method
            final TestLinkAPI api = this.connect(this.rpcUrl, this.devKey);

            // get all available projects
            final TestProject[] projects = this.readTestProjects(api);

            // make sure there are some project(s)
            if (null != projects) {

                // iterate over all projects
                for (final TestProject project : projects) {

                    // make sure that project is active, but ignore isPublic flag as this is for testing
                    if (project.isActive().booleanValue()) {

                        // get build for project and get test case(s)
                        this.handleBuild(testCaseList, project, api);
                    } else {

                        // inform user that this is not an active project
                        TestLinkHandlerImpl.LOGGER.info("Skipping project [ " + project.getName()
                                + " ] as it is not active!");
                    }
                }
            }

            return testCaseList;
        } catch (final TestLinkAPIException tlex) {

            // just do nothing, as this exception was already handled
            throw tlex;
        } catch (final Exception ex) {

            throw new TestLinkAPIException("Error while reading test case(s) from TestLink!", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeToTestLink(final TestLinkBean bean) {

        try {

            final TestLinkAPI api = this.connect(bean.getUrl(), bean.getKey());

            final TestCase testCase = bean.getTestCase();
            final TestPlan testPlan = bean.getPlan();
            final Build build = bean.getBuild();

            final ReportTCResultResponse response = api.reportTCResult(testCase.getId(), testCase.getInternalId(),
                    testPlan.getId(), testCase.getExecutionStatus(), build.getId(), build.getName(), bean.getNotes(),
                    null, // guess
                    null, // bug id
                    null, // platform id
                    bean.getPlatform(), // platform name
                    null, // custom fields
                    null);

            LOGGER.info("CITRUS / TestLink response [ {} ] for test case [ {} ]", response.getMessage(),
                    bean.getTestCaseName());
        } catch (final Exception ex) {

            LOGGER.error("Exception while trying to write to TestLink with bean [ {} ]\n", bean, ex);
        }
    }

    /**
     * Get for each test plan the latest build. For this build get all test suite(s) and for each test suite get all
     * test case(s).
     * 
     * @param testCaseList
     *            List to add new test case(s).
     * @param project
     *            Project for getting test plans for.
     * @param api
     *            Connection object for TestLink.
     */
    private void handleBuild(final List<TestLinkBean> testCaseList, final TestProject project, final TestLinkAPI api) {

        // get test plan(s) for this project
        final TestPlan[] plans = this.readTestPlans(api, project.getId());

        // make sure there are some test plan(s)
        if (null != plans) {

            // iterate over all test plan(s)
            for (final TestPlan plan : plans) {

                // make sure the test plan is active
                if (plan.isActive().booleanValue()) {

                    // get the latest build for this test plan as only one single build should be assign for a test plan
                    final Build build = api.getLatestBuildForTestPlan(plan.getId());

                    // make sure there is a build and a corresponding test plan ID
                    if ((null != build) && (null != build.getTestPlanId())) {

                        // handle all test case(s)
                        this.handleTestCases(testCaseList, project, plan, build, api);
                    }
                } else {

                    // inform user that this is not an active test plan
                    TestLinkHandlerImpl.LOGGER.info("Skipping test plan [ " + plan.getName()
                            + " ] as it is not active!");
                }
            }
        }
    }

    /**
     * Handle all test case(s) for the latest build for the given test plan of the given project.
     * 
     * @param testCaseList
     *            List to add new test case(s).
     * @param project
     *            TestProject for this test case(s).
     * @param plan
     *            TestPlan for this test case(s)
     * @param build
     *            Build for this test case(s).
     * @param api
     *            Connection object for TestLink.
     */
    private void handleTestCases(final List<TestLinkBean> testCaseList, final TestProject project, final TestPlan plan,
            final Build build, final TestLinkAPI api) {

        // get all test case(s) for each test suite
        final TestCase[] cases = this.readTestCases(api, build.getTestPlanId());

        // make sure there are some test case(s)
        if (null != cases) {

            // iterate over all test case(s)
            for (final TestCase testCase : cases) {

                // only consider using test case(s) which are marked to be executed automatically
                if (testCase.getExecutionType() == ExecutionType.AUTOMATED) {

                    // set values which seems not to be set by the TestLinkAPI
                    testCase.setTestProjectId(project.getId());

                    // read CITRUS custom field, which requires the project id
                    final CustomField customField = this.readCitrusCustomField(api, testCase);

                    // only allow test case(s) where the CITRUS custom field was set
                    if (null != customField) {

                        // add custom field to test case
                        testCase.getCustomFields().add(customField);

                        // add this test case to the returning result list
                        final TestLinkBean bean = new TestLinkBean();

                        bean.setBuild(build);
                        bean.setPlan(plan);
                        bean.setProject(project);
                        bean.setSuite(null);
                        bean.setTestCase(testCase);

                        testCaseList.add(bean);
                    } else {

                        // inform user that custom field CITRUS is not set
                        TestLinkHandlerImpl.LOGGER.info("Skipping test case [ " + testCase.getName()
                                + " ] as it custom field CITRUS is not set!");
                    }
                } else {

                    // inform user that there are some manually executable test case(s)
                    TestLinkHandlerImpl.LOGGER.info("Skipping test case [ " + testCase.getName()
                            + " ] as it is a manually executable test case!");
                }
            }
        }
    }

    /**
     * Establish connection to TestLink. A new connection object is created and returned.
     * 
     * @param rpcUrlIn
     *            URL pointing to TestLink XML-RPC.
     * @param key
     *            Development key as generated in TestLink.
     * 
     * @return Newly created {@link TestLinkAPI} object.
     * 
     * @throws TestLinkAPIException
     *             MojoExecutionException Thrown in case the connection could not be established.
     */
    private TestLinkAPI connect(final String rpcUrlIn, final String key) throws TestLinkAPIException {

        try {

            // build final URL
            final URL testlinkUrl = new URL(rpcUrlIn + TestLinkHandlerImpl.XML_RPC);

            // get new connection using URL and development key
            return new TestLinkAPI(testlinkUrl, key);
        } catch (final TestLinkAPIException tlex) {

            throw tlex;
        } catch (final Exception ex) {

            throw new TestLinkAPIException("Could not connect to TestLink with URL [ " + rpcUrlIn + " ]", ex);
        }
    }

    /**
     * Read all test project(s) catching any exceptions.
     * 
     * @param api
     *            Connection object for TestLink.
     * 
     * @return Array of {@link TestProject} in case there was no error and there are some element(s) otherwise
     *         {@code null} is returned.
     */
    private TestProject[] readTestProjects(final TestLinkAPI api) {

        try {

            final TestProject[] projects = api.getProjects();

            if ((null != projects) && (projects.length > 0)) {

                return projects;
            }
        } catch (final Exception ex) {

            TestLinkHandlerImpl.LOGGER.error("Exception caught while reading project(s)!", ex);
        }

        return null;
    }

    /**
     * Read all test plan(s) catching any exceptions and allowing to continue for next project in case this one has a
     * problem.
     * 
     * @param api
     *            Connection object for TestLink.
     * @param projectID
     *            Test project ID.
     * 
     * @return Array of {@link TestPlan} in case there was no error and there are some element(s) otherwise {@code null}
     *         is returned.
     */
    private TestPlan[] readTestPlans(final TestLinkAPI api, final Integer projectID) {

        try {

            final TestPlan[] plans = api.getProjectTestPlans(projectID);

            if ((null != plans) && (plans.length > 0)) {

                return plans;
            }
        } catch (final Exception ex) {

            TestLinkHandlerImpl.LOGGER.error("Exception caught while reading test plan(s) for test project ID [ "
                    + projectID + " ]!", ex);
        }

        return null;
    }

    /**
     * Read all test case(s) catching any exceptions and allowing to continue in case this one has a problem.
     * 
     * @param api
     *            Connection object for TestLink.
     * @param planID
     *            Test plan ID. This test plan ID is from the latest build for the given test plan.
     * 
     * @return Array of {@link TestCase} in case there was no error and there are some element(s) otherwise {@code null}
     *         is returned. Return also all manually test case, so the user is informed about skipping those.
     */
    private TestCase[] readTestCases(final TestLinkAPI api, final Integer planID) {

        try {

            // use this API call as there is only need for a test plan ID and it is possible to expand
            final TestCase[] cases = api.getTestCasesForTestPlan(planID, null, null, null, null, null, null, null,
                    null, Boolean.TRUE);

            if ((null != cases) && (cases.length > 0)) {

                return cases;
            }
        } catch (final Exception ex) {

            TestLinkHandlerImpl.LOGGER.error("Exception caught while reading test case(s) for test plan ID [ " + planID
                    + " ]!", ex);
        }

        return null;
    }

    /**
     * Read CITRUS custom field for this test case.
     * 
     * @param api
     *            Connection object for TestLink.
     * @param testCase
     *            Test case for which the custom field is to be read.
     * 
     * @return {@link CustomField} if there was a not null custom field and the value was not null or empty. Otherwise
     *         {@code null} is returned.
     */
    private CustomField readCitrusCustomField(final TestLinkAPI api, final TestCase testCase) {

        try {

            // retrieve from TestLink the CITRUS custom field
            final CustomField customField = api.getTestCaseCustomFieldDesignValue(testCase.getId(), null,
                    testCase.getVersion(), testCase.getTestProjectId(), "CITRUS", ResponseDetails.FULL);

            // make sure it is a valid CITRUS custom field
            if ((null != customField) && ((null != customField.getValue()) && (!customField.getValue().isEmpty()))) {

                return customField;
            }
        } catch (final Exception ex) {

            TestLinkHandlerImpl.LOGGER.error("Exception caught while reading CITRUS custom field for test case [ "
                    + testCase.getName() + " ]!", ex);
        }

        return null;
    }

}
