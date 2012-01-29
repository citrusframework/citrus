/*
 * File: CreateTestCasesFromTestLink.java
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
 * last modified: Sunday, January 29, 2012 (13:33) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.List;

import org.codehaus.plexus.components.interactivity.Prompter;

import org.springframework.util.CollectionUtils;

import com.consol.citrus.util.TestCaseCreator;
import com.consol.citrus.util.TestCaseCreator.UnitFramework;

/**
 * Get all test cases from TestLink and create for each test case a CITRUS test case.
 *
 * <p>
 * Mojo offers an interactive mode, where the plugin prompts for parameters during execution. In
 * non-interactive mode the parameters are given as command line arguments.
 * </p>
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 * @goal create
 */
public class CreateTestCasesFromTestLink extends AbstractTestLinkMojo {

    // ~ Instance fields -------------------------------------------------------------------------

    /**
     * Prompter utility class injected by the Plexus IoC implementation.
     *
     * @component
     * @required
     */
    private Prompter prompter;

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code CreateTestCasesFromTestLink} class.
     */
    public CreateTestCasesFromTestLink() {

        super();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCitrusTestCases(final List<CitrusBean> beanList) {

        // check if interactive mode is active
        if (this.interactiveMode) {

            // ask the user for each test case
            this.askUser(beanList);
        }

        // finally build the CITRUS test cases
        this.buildTestCases(beanList);

        StringBuilder builder = new StringBuilder();

        builder.append("\nDo not forget to add the following to the CITRUS context:\n\n");
        builder.append(CitrusUtils.buildTestListener(beanList.get(0), "    "));
        builder.append("\n");

        this.getLog().info(builder.toString());
    }

    /**
     * Ask user for each CITRUS test case if there are modifications required.
     *
     * @param beanList
     *            List of CITRUS test case bean(s).
     */
    private void askUser(final List<CitrusBean> beanList) {

        // made sure previously that list is not null and not empty
        for (final CitrusBean bean : beanList) {

            // make sure each CITRUS test case is handled and an exception handled for each test case
            try {

                // prompt for changes
                final StringBuilder promptFirst = new StringBuilder("\n\nEdit parameters for test: ");
                promptFirst.append(bean.getName());
                promptFirst.append("\nEnter test author:");

                final String tauthor = this.prompter.prompt(promptFirst.toString(), bean.getAuthor());
                final String tdescription = this.prompter.prompt("Enter test description:", bean
                        .getTestLink().getTestCaseDesc());
                final String ttargetPackage = this.prompter.prompt("Enter test package:",
                        bean.getTargetPackage());
                final String tframework = this.prompter
                        .prompt("Choose unit test framework [testng | junit3 | junit4]:",
                                bean.getFramework());

                // ask for confirmation
                final StringBuilder builder = new StringBuilder("\nTest creation for:");
                builder.append("\nframework: ");
                builder.append(tframework);
                builder.append("\nname: ");
                builder.append(bean.getName());
                builder.append("\nauthor: ");
                builder.append(tauthor);
                builder.append("\ndescription: ");
                builder.append(tdescription);
                builder.append("\npackage: ");
                builder.append(ttargetPackage);
                builder.append("\n\nCreate test case [ ");
                builder.append(bean.getName());
                builder.append(" ]");

                final String confirm = this.prompter.prompt(builder.toString(),
                        CollectionUtils.arrayToList(new String[] { "y", "n" }), "y");

                // check if confirmation failed
                if ("n".equalsIgnoreCase(confirm)) {

                    // this test case should not be created
                    bean.setCreate(false);

                    // continue with next CITRUS test case
                    continue;
                }

                // replace values with new values
                bean.setAuthor(tauthor);
                bean.setCreate(true);
                bean.getTestLink().setTestCaseDesc(tdescription);
                bean.setFramework(tframework);
                bean.setTargetPackage(ttargetPackage);
            } catch (final Exception ex) {

                this.getLog().error(
                        "Exception caught for CITRUS test case [ " + bean.getName() + " ]", ex);
            }
        }
    }

    /**
     * Build for all CITRUS test case bean a CITRUS test case.
     *
     * @param beanList
     *            List of CITRUS test case bean(s).
     */
    private void buildTestCases(final List<CitrusBean> beanList) {

        // made sure previously that list is not null and not empty
        for (final CitrusBean bean : beanList) {

            // make sure that only test case(s) which were confirmed are generated
            if (bean.isCreate()) {

                // all parameters are set,
                // so set JAVA and TEST file and check for overwriting the test case
                this.handleFiles(bean);

                // make sure that even now the CITRUS test case should be created
                if (bean.isCreate()) {

                    // handle each test case by it own, so all test case(s) are handled
                    try {

                        // build CITRUS test case creator using CITRUS core functionality
                        final TestCaseCreator creator = TestCaseCreator.build()
                                .withFramework(UnitFramework.fromString(bean.getFramework()))
                                .withName(bean.getName()).withAuthor(bean.getAuthor())
                                .withDescription(bean.getTestLink().getTestCaseDesc())
                                .usePackage(bean.getTargetPackage());

                        // create CITRUS test case, overwrites available test case
                        creator.createTestCase();

                        // set JAVA and TEST file
                        CitrusUtils.setFiles(bean);
                        this.addVariables(bean);

                        // there was no exception, so the generation was successful
                        final StringBuilder builder = new StringBuilder(
                                "\n\nSuccessfully created new test case");
                        builder.append("\nframework: ");
                        builder.append(bean.getFramework());
                        builder.append("\nname: ");
                        builder.append(bean.getName());
                        builder.append("\nauthor: ");
                        builder.append(bean.getAuthor());
                        builder.append("\ndescription: ");
                        builder.append(bean.getTestLink().getTestCaseDesc());
                        builder.append("\npackage: ");
                        builder.append(bean.getTargetPackage());
                        builder.append("\n\n");

                        this.getLog().info(builder.toString());
                    } catch (final Exception ex) {

                        this.getLog().error(
                                "Exception caught for creating test case [ " + bean + " ]", ex);
                    }
                } else {

                    // test case should not be overwritten
                    this.getLog().info("NOT overwriting CITRUS test case [ " + bean.getName() + " ]");
                }
            } else {

                // test case was not confirmed
                this.getLog()
                        .info("Skipping creation of CITRUS test case [ " + bean.getName() + " ]");
            }
        }
    }

    /**
     * Handle the JAVA and TEST files. Make sure an available test case is not overwritten and the
     * whole test case is lost.
     *
     * @param bean
     *            CITRUS test case bean.
     */
    private void handleFiles(final CitrusBean bean) {

        // set JAVA and TEST file
        CitrusUtils.setFiles(bean);

        // make sure that at least one file is available
        if (bean.isJavaFileValid() || bean.isTestFileValid()) {

            // see if interactive mode is active
            if (this.interactiveMode) {

                // check if test case should be overwritten
                try {

                    final StringBuilder builder = new StringBuilder("\n\nTest case JAVA file [ ");
                    builder.append(bean.getJavaFileName());
                    builder.append(" ] exists [ ");
                    builder.append(bean.isJavaFileValid());
                    builder.append(" ]\n");
                    builder.append("Test case TEST file [ ");
                    builder.append(bean.getTestFileName());
                    builder.append(" ] exists [ ");
                    builder.append(bean.isTestFileValid());
                    builder.append(" ]\n\n");
                    builder.append("Do you want to skip this test case [ ");
                    builder.append(bean.getName());
                    builder.append(" ] and NOT overwrite it");

                    final String confirm = this.prompter.prompt(builder.toString(),
                            CollectionUtils.arrayToList(new String[] { "y", "n" }), "y");

                    // check if confirmation is true
                    if ("y".equalsIgnoreCase(confirm)) {

                        // this test case should not be created
                        bean.setCreate(false);
                    }
                } catch (final Exception ex) {

                    this.getLog().error(
                            "Exception caught while asking for overwritting, skipping test case [ "
                                    + bean.getName() + " ]", ex);

                    // this test case should not be created
                    bean.setCreate(false);
                }
            } else {

                // in automatic mode do not overwrite the available test case, inform user
                final StringBuilder builder = new StringBuilder("\n\nTest case JAVA file [ ");
                builder.append(bean.getJavaFileName());
                builder.append(" ] exists [ ");
                builder.append(bean.isJavaFileValid());
                builder.append(" ]\n");
                builder.append("Test case TEST file [ ");
                builder.append(bean.getTestFileName());
                builder.append(" ] exists [ ");
                builder.append(bean.isTestFileValid());
                builder.append(" ]\n");
                builder.append("As the interactive mode is deactivated, skip test case [ ");
                builder.append(bean.getName());
                builder.append(" ] as otherwise this test case will be overwritten!");

                this.getLog().info(builder.toString());

                // avoid creation of test case
                bean.setCreate(false);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     */
    private void addVariables(final CitrusBean bean) {

        final File file = new File(bean.getTestFileName());

        if (!file.exists() || !file.canWrite() || (file.length() <= 0)) {

            // there is no file, return
            return;
        }

        BufferedReader reader = null;
        final StringBuilder builder = new StringBuilder();

        try {

            reader = new BufferedReader(new FileReader(file));

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.contains("</description>")) {

                    builder.append(line);
                    builder.append("\n");

                    builder.append("\n");
                    builder.append(CitrusUtils.buildVariables(bean, "        "));
                } else if (line.contains("variable")) {

                    // ignore lines containing variable definitions
                } else {

                    builder.append(line);
                    builder.append("\n");
                }
            }
        } catch (final Exception ex) {

            this.getLog().error("Error trying to read from file [ " + file.getAbsolutePath() + " ]",
                    ex);
        } finally {

            if (reader != null) {

                try {

                    reader.close();
                } catch (final Exception ex) {

                    this.getLog().error(
                            "Error while closing file [ " + file.getAbsolutePath() + " ]", ex);
                }
            }
        }

        BufferedWriter writer = null;

        try {

            writer = new BufferedWriter(new FileWriter(file));
            writer.write(builder.toString());
        } catch (final Exception ex) {

            this.getLog().error("Error trying to write to file [ " + file.getAbsolutePath() + " ]",
                    ex);
        } finally {

            if (writer != null) {

                try {

                    writer.close();
                } catch (final Exception ex) {

                    this.getLog().error(
                            "Error while closing file [ " + file.getAbsolutePath() + " ]", ex);
                }
            }
        }
    }

}
