/*
 * Copyright 2006-2011 the original author or authors.
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
 * File: TestLinkHandlerImpl.java
 * last modified: Friday, December 30, 2011 (13:03) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.ExecutionType;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;

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

    // ~ Instance fields -----------------------------------------------------------------------------------------------

    /** log. */
    private final Log log;

    /** tlinkUrl. */
    private final String rpcUrl;

    /** devKey. */
    private final String devKey;

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code TestLinkContainerImpl} class.
     * 
     * @param urlIn
     *            URL to TestLink to which the path to the XML RPC will be added.
     * @param devKeyIn
     *            devKey Development key as generated in TestLink.
     * @param logIn
     *            Logger to allow for logging using the Maven plugin logger.
     */
    public TestLinkHandlerImpl(final String urlIn, final String devKeyIn, final Log logIn) {

        super();

        this.rpcUrl = (urlIn + TestLinkHandlerImpl.XML_RPC);
        this.devKey = devKeyIn;
        this.log = logIn;
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public List<TestCaseBean> readTestCases() throws MojoExecutionException {

        try {

            // make sure the returned list is never null
            final List<TestCaseBean> testCaseList = new ArrayList<TestCaseBean>();

            // open connection to TestLink, there seems to be no close method
            final TestLinkAPI api = this.connect(this.rpcUrl, this.devKey);

            // get all available projects
            final TestProject[] projects = api.getProjects();

            // make sure there are some projects
            if ((null != projects) && (projects.length > 0)) {

                // iterate over all projects
                for (final TestProject project : projects) {

                    // make sure that project is active, but ignore isPublic flag as this is for testing
                    if (project.isActive().booleanValue()) {

                        // get build for project and get test case(s)
                        this.handleBuild(testCaseList, project, api);
                    } else {

                        // inform user that this is a not active project
                        this.log.info("Skipping project [ " + project.getName() + " ] as it is not active!");
                    }
                }
            }

            return testCaseList;
        } catch (final MojoExecutionException moex) {

            throw moex;
        } catch (final Exception ex) {

            throw new MojoExecutionException("Error while reading test case(s) from TestLink!", ex);
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
    private void handleBuild(final List<TestCaseBean> testCaseList, final TestProject project, final TestLinkAPI api) {

        // get test plan(s) for this project
        final TestPlan[] plans = api.getProjectTestPlans(project.getId());

        // make sure there are some test plan(s)
        if ((null != plans) && (plans.length > 0)) {

            // iterate over all test plan(s)
            for (final TestPlan plan : plans) {

                // get the latest build for this test plan as only one single build should be assign for a test plan
                final Build build = api.getLatestBuildForTestPlan(plan.getId());

                // make sure there is a build and a corresponding test plan ID
                if ((null != build) && (null != build.getTestPlanId())) {

                    // get all test suite(s) for the test plan of the latest build
                    final TestSuite[] suites = api.getTestSuitesForTestPlan(build.getTestPlanId());

                    // make sure there are some test suite(s)
                    if ((null != suites) && (suites.length > 0)) {

                        // iterate over all test suite(s)
                        for (final TestSuite suite : suites) {

                            // get all test case(s) for each test suite
                            final TestCase[] cases = api.getTestCasesForTestSuite(suite.getId(), Boolean.TRUE, "full");

                            // handle all test case(s)
                            this.handleTestCases(testCaseList, project, plan, build, suite, cases);
                        }
                    }
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
     * @param suite
     *            TestSuite for this test case(s).
     * @param cases
     *            Test case(s) found for this test suite.
     */
    private void handleTestCases(final List<TestCaseBean> testCaseList, final TestProject project, final TestPlan plan,
            final Build build, final TestSuite suite, final TestCase[] cases) {

        // make sure there are some test case(s)
        if ((null != cases) && (cases.length > 0)) {

            // iterate over all test case(s)
            for (final TestCase testCase : cases) {

                // only consider using test case(s) which are marked to be executed automatically
                if (testCase.getExecutionType() == ExecutionType.AUTOMATED) {

                    // add this test case to the returning result list
                    final TestCaseBean bean = new TestCaseBean();

                    bean.setBuild(build);
                    bean.setPlan(plan);
                    bean.setProject(project);
                    bean.setSuite(suite);
                    bean.setTestCase(testCase);

                    testCaseList.add(bean);
                } else {

                    // inform user that there are some manually executable test case(s)
                    this.log.info("Skipping test case [ " + testCase.getName()
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
     * @throws MojoExecutionException
     *             Thrown in case the connection could not be established.
     */
    private TestLinkAPI connect(final String rpcUrlIn, final String key) throws MojoExecutionException {

        try {

            // build final URL
            final URL testlinkUrl = new URL(rpcUrlIn);

            // get new connection using URL and development key
            return new TestLinkAPI(testlinkUrl, key);
        } catch (final Exception ex) {

            throw new MojoExecutionException("Could not connect to TestLink with URL [ " + rpcUrlIn + " ]", ex);
        }
    }

}
