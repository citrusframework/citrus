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
 * last modified: Sunday, April 29, 2012 (14:13) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.List;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import org.springframework.util.CollectionUtils;

import com.consol.citrus.testlink.CitrusTestLinkEnum;
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

        // ask the user for each test case
        this.askUser(beanList);

        // finally build the CITRUS test cases
        this.buildTestCases(beanList);

        final StringBuilder builder = new StringBuilder();

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

                this.handleFiles(bean);

                if (!bean.isCreate()) {

                    continue;
                }

                // prompt for changes and variables
                this.promptForChanges(bean);
                this.promptForVariables(bean);
            } catch (final Exception ex) {

                this.getLog().error(
                        "Exception caught for CITRUS test case [ " + bean.getName() + " ]", ex);
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

            // ask if test case should be overwritten
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
        }
    }

    /**
     * Prompt for changes.
     *
     * @param bean
     *            CITRUS test case bean.
     *
     * @throws PrompterException
     *             Thrown in case of an error.
     */
    private void promptForChanges(final CitrusBean bean) throws PrompterException {

        final StringBuilder promptFirst = new StringBuilder("\n\nEdit parameters for test: ");
        promptFirst.append(bean.getName());
        promptFirst.append("\nEnter test author:");

        final String tauthor = this.prompter.prompt(promptFirst.toString(), bean.getAuthor());
        final String tdescription = this.prompter.prompt("Enter test description:", bean
                .getTestLink().getTestCaseDesc());
        final String ttargetPackage = this.prompter.prompt("Enter test package:",
                bean.getTargetPackage());
        final String tframework = this.prompter.prompt(
                "Choose unit test framework [testng | junit3 | junit4]:", bean.getFramework());

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
        } else {

            // replace values with new values
            bean.setAuthor(tauthor);
            bean.getTestLink().setTestCaseDesc(tdescription);
            bean.setFramework(tframework);
            bean.setTargetPackage(ttargetPackage);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     *
     * @throws PrompterException
     *             DOCUMENT ME!
     */
    private void promptForVariables(final CitrusBean bean) throws PrompterException {

        final StringBuilder promptFirst = new StringBuilder("\n\nEdit variables for test: ");
        promptFirst.append(bean.getName());
        promptFirst.append("\nEnter success text:");

        final String notesSuccess = this.prompter.prompt(promptFirst.toString(), bean.getVariables()
                .get(CitrusTestLinkEnum.NotesSuccess.getKey()));
        final String notesFailure = this.prompter.prompt("Enter failure text:", bean.getVariables()
                .get(CitrusTestLinkEnum.NotesFailure.getKey()));
        final String platform = this.prompter.prompt("Choose platform", bean.getTestLink()
                .getPlatformList(),
                bean.getVariables().get(CitrusTestLinkEnum.TestCasePlatform.getKey()));

        // ask for confirmation
        StringBuilder builder = new StringBuilder("\nVariables:");
        builder.append("\nSuccess notes: ");
        builder.append(notesSuccess);
        builder.append("\nFailure notes: ");
        builder.append(notesFailure);
        builder.append("\nPlatform: ");
        builder.append(platform);
        builder.append("\n\nConfirm variables: ");

        final String confirm = this.prompter.prompt(builder.toString(),
                CollectionUtils.arrayToList(new String[] { "y", "n" }), "y");

        // check for confirmation
        if ("y".equalsIgnoreCase(confirm)) {

            // replace values with new values
            bean.getVariables().put(CitrusTestLinkEnum.NotesSuccess.getKey(), notesSuccess);
            bean.getVariables().put(CitrusTestLinkEnum.NotesFailure.getKey(), notesFailure);
            bean.getVariables().put(CitrusTestLinkEnum.TestCasePlatform.getKey(), platform);
        }
    }

    /**
     * Build for all CITRUS test case bean a CITRUS test case.
     *
     * @param beanList
     *            List of CITRUS test case bean(s).
     */
    private void buildTestCases(final List<CitrusBean> beanList) {

        // previously make sure that list is not null and not empty
        for (final CitrusBean bean : beanList) {

            // make sure that only test case(s) which were confirmed are generated
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

                    this.getLog().error("Exception caught for creating test case [ " + bean + " ]",
                            ex);
                }
            } else {

                // test case should not be overwritten
                this.getLog().info("NOT overwriting CITRUS test case [ " + bean.getName() + " ]");
            }
        }
    }

    /**
     * Add TestLink variables to the newly created CITRUS test case. Add variables after the
     * description tag, as this is required by the XSD definition of CITRUS. In case this changes,
     * make sure to adapt it here as well.
     *
     * @param bean
     *            CITRUS test case bean.
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

                    // write description line
                    builder.append(line);
                    builder.append("\n");

                    // start to write CITRUS TestLink variables
                    // the indention is some experimental value
                    builder.append(CitrusUtils.buildVariables(bean, "        "));
                } else if (line.contains("variable")) {

                    // ignore lines containing variable definitions
                    // removing them by this
                } else {

                    builder.append(line);
                    builder.append("\n");
                }
            }
        } catch (final Exception ex) {

            this.getLog().error("Error while working on file [ " + file.getAbsolutePath() + " ]", ex);
        } finally {

            CitrusFileUtils.close(reader);
        }

        BufferedWriter writer = null;

        try {

            writer = new BufferedWriter(new FileWriter(file));
            writer.write(builder.toString());
        } catch (final Exception ex) {

            this.getLog().error("Error trying to write to file [ " + file.getAbsolutePath() + " ]",
                    ex);
        } finally {

            CitrusFileUtils.close(writer);
        }
    }

}
