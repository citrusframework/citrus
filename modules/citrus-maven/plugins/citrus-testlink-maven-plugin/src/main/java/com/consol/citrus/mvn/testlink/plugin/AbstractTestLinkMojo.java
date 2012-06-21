/*
 * File: AbstractTestLinkMojo.java
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
 * last modified: Monday, February 20, 2012 (09:29) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.consol.citrus.testlink.TestLinkCitrusBean;
import com.consol.citrus.testlink.impl.TestLinkHandlerImpl;

/**
 * DRY: Do not repeat yourself. Regroup here all methods and variable needed for all Mojo classes.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class AbstractTestLinkMojo extends AbstractMojo {

    // ~ Instance fields -------------------------------------------------------------------------

    /**
     * URL pointing to the TestLink URL. The path to the XML-RPC call will be append.
     *
     * @parameter
     * @required
     */
    protected String url;

    /**
     * Development needed for authorization to TestLink. This must be generated within TestLink. For
     * this TestLink must be configured.
     *
     * @parameter
     * @required
     */
    protected String devKey;

    /**
     * The test author. Use as default {@code TestLink}. TestLink Java API seems not to return the
     * author login information.
     *
     * @parameter expression="${author}" default-value="TestLink"
     */
    protected String author;

    /**
     * Which package (folder structure) is assigned to this test. Defaults to "com.consol.citrus"
     *
     * @parameter expression="${targetPackage}"
     */
    protected String targetPackage;

    /**
     * Which unit test framework to use for test execution (default: testng; options: testng, junit3,
     * junit4)
     *
     * @parameter expression="${framework}" default-value="testng"
     */
    protected String framework;

    /**
     * Get the values of the actual project to extract the group ID value.
     *
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    /** handler. */
    private final TestLinkHandlerImpl handler;

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code AbstractTestLinkMojo} class.
     */
    public AbstractTestLinkMojo() {

        this(new TestLinkHandlerImpl());
    }

    /**
     * Constructor for {@code AbstractTestLinkMojo} class.
     *
     * @param handlerIn
     *            Handler implementing {@link TestLinkHandlerImpl} interface. May be used for test
     *            purposes.
     */
    public AbstractTestLinkMojo(final TestLinkHandlerImpl handlerIn) {

        super();

        this.handler = handlerIn;
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * The CITRUS test case list is finally created, now see what to do with those test cases.
     *
     * @param beanList
     *            CITRUS test case bean list which is neither {@code null}, nor empty.
     */
    public abstract void handleCitrusTestCases(final List<CitrusBean> beanList);

    /**
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        // make sure all mandatory fields are set, if not throws a MojoFailureException
        this.checkMandatoryFields();

        try {

            // get all available test cases
            final List<TestLinkCitrusBean> testCaseList = this.handler.readTestCases(this.url,
                    this.devKey);

            // make sure there are some test case(s)
            if ((null != testCaseList) && (!testCaseList.isEmpty())) {

                // build from the TestLink bean list the CITRUS bean list
                final List<CitrusBean> beanList = this.buildCitrusBeanList(testCaseList);

                // make sure there are still some CITRUS test cases
                if ((null != beanList) && (!beanList.isEmpty())) {

                    // handle newly created CITRUS test case bean list
                    this.handleCitrusTestCases(beanList);
                } else {

                    // no CITRUS test case(s) available
                    this.getLog().error("No CITRUS test case(s) creation possible!");
                }
            } else {

                // no valid TestLink test case(s) found
                this.getLog().info("No TestLink test case(s) found!");
            }
        } catch (final Exception ex) {

            throw new MojoExecutionException("Exception caught for creating test case(s)!", ex);
        }
    }

    /**
     * Check for mandatory fields. Each field which is missing throws an {@link MojoFailureException}.
     *
     * @throws MojoFailureException
     *             Thrown in case a mandatory field is missing.
     */
    private void checkMandatoryFields() throws MojoFailureException {

        // make sure mandatory fields are set
        if ((null == this.url) || (this.url.isEmpty())) {

            throw new MojoFailureException("Parameter <url> may not be null or empty!");
        }

        if ((null == this.devKey) || (this.devKey.isEmpty())) {

            throw new MojoFailureException("Parameter <devKey> may not be null or empty!");
        }
    }

    /**
     * From the TestLink bean list create the CITRUS bean list.
     *
     * @param testLinkList
     *            Incoming TestLink bean list.
     *
     * @return Newly created CITRUS bean list, will never be {@code null}.
     */
    private List<CitrusBean> buildCitrusBeanList(final List<TestLinkCitrusBean> testLinkList) {

        // create CITRUS bean list, which will be returned in each case
        final List<CitrusBean> citrusList = new ArrayList<CitrusBean>();

        // previously made sure that there are some elements
        for (final TestLinkCitrusBean tbean : testLinkList) {

            // create CITRUS test case bean
            final CitrusBean bean = CitrusUtils.createCitrusBean(tbean);

            // make sure it is not null
            if (null != bean) {

                // check here if the test case name is valid, as this can be logged here
                if (CitrusUtils.isValidTestCaseName(bean.getName())) {

                    // the test case name is valid,
                    // add the remaining variables to the CITRUS test case bean
                    if (null == bean.getAuthor()) {

                        bean.setAuthor(this.author);
                    }

                    if ((null != this.targetPackage) && (!this.targetPackage.isEmpty())) {

                        bean.setTargetPackage(this.targetPackage);
                    } else {

                        bean.setTargetPackage(this.mavenProject.getGroupId());
                    }

                    bean.setFramework(this.framework);

                    // add bean to CITRUS bean list
                    citrusList.add(bean);
                } else {

                    // make user aware that CITRUS test case name does not match the Java class naming
                    // convention
                    this.getLog().warn(
                            "Skipping CITRUS test case as the test case name [ " + bean.getName()
                                    + " ] does not conform to the java class name reg.exp. [ "
                                    + CitrusUtils.JAVA_CLASS_REXP + " ]");
                }
            } else {

                // there was some error with the test case, log this
                if (null != tbean) {

                    this.getLog().warn("Invalid TestLink test case [ " + tbean + " ]");
                } else {

                    // ??? no test case at all ???
                    this.getLog().error("TestLink test case is null!?");
                }
            }
        }

        // return CITRUS bean list, even if only empty
        return citrusList;
    }

}
