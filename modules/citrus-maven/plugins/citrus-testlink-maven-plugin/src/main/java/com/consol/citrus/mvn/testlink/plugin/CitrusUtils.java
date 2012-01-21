/*
 * File: CitrusUtils.java
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
 * last modified: Saturday, January 21, 2012 (19:05) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import com.consol.citrus.testlink.CitrusTestLinkEnum;
import com.consol.citrus.testlink.TestLinkCitrusBean;
import com.consol.citrus.testlink.utils.ConvertUtils;
import com.consol.citrus.testlink.utils.TestLinkUtils;

/**
 * Utility class for CITRUS static methods.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class CitrusUtils {

    // ~ Static fields/initializers --------------------------------------------------------------

    /** Defines the reg. exp. a Java class name must match. */
    public static final String JAVA_CLASS_REXP = "^[A-Z][a-zA-Z_$0-9]*$";

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusUtils} class.
     */
    private CitrusUtils() {

        super();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Create a new {@link CitrusBean} and copying values from the provided {@link TestLinkCitrusBean}
     * bean, but only if a test case name is given.
     *
     * @param bean
     *            Bean holding some values for the new CITRUS test bean.
     * @param interActive
     *            Allows to set if the CITRUS test case should be set automatically. If the
     *            interactive mode is chosen, the user is in charge of defining this value, otherwise
     *            it is set to true.
     *
     * @return Newly created {@link CitrusBean} if the test case name is available otherwise
     *         {@code null} is returned in case of some error.
     */
    public static final CitrusBean createCitrusBean(final TestLinkCitrusBean bean,
            final boolean interActive) {

        // get test case name
        final String testCaseName = TestLinkUtils.getCitrusTestCaseName(bean);

        // make sure there is a test case name
        if ((null != testCaseName) && (!testCaseName.isEmpty())) {

            // create new CITRUS bean
            final CitrusBean cbean = new CitrusBean();

            // if interactive is false, then the test case will be created automatically
            cbean.setCreate(!interActive);
            cbean.setName(testCaseName);
            cbean.setTestLink(bean);

            updateVariables(cbean);

            return cbean;
        }

        // there is no test case name so return null
        return null;
    }

    /**
     * Checks if the provided test case name is valid. As this name is used to generate the Java test
     * class, the provided name must match the regular expression for a Java class name.
     *
     * @param name
     *            Name of provided test case name.
     *
     * @return {@code valid test case name} field.
     */
    public static final boolean isValidTestCaseName(final String name) {

        // name must not be null or empty
        if ((null != name) && (!name.isEmpty())) {

            // make sure name matches Java class name regular expression
            return name.matches(CitrusUtils.JAVA_CLASS_REXP);
        }

        // test case name is not valid, return false
        return false;
    }

    /**
     * Set file(s) for this CITRUS test case. Try to set the Java and Test file(s).
     *
     * @param bean
     *            CITRUS test case bean.
     */
    public static final void setFiles(final CitrusBean bean) {

        // make sure that there is a CITRUS test case bean
        if (null != bean) {

            // start with JAVA test case file
            String fileName = CitrusFileUtils.buildFileName(CitrusFileEnum.JAVA, bean);

            // check if this file is valid
            bean.setJavaFileValid(CitrusFileUtils.isValidFile(fileName));

            // the JAVA file is valid
            if (bean.isJavaFileValid()) {

                // set the absolute file path of the JAVA file
                bean.setJavaFileName(CitrusFileUtils.getAbsolutePath(fileName));
            }

            // see for the TEST file
            fileName = CitrusFileUtils.buildFileName(CitrusFileEnum.TEST, bean);

            // check if this file is valid
            bean.setTestFileValid(CitrusFileUtils.isValidFile(fileName));

            // the TEST file is valid
            if (bean.isTestFileValid()) {

                // set the absolute file path of the TEST file
                bean.setTestFileName(CitrusFileUtils.getAbsolutePath(fileName));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     */
    private static final void updateVariables(final CitrusBean bean) {

        if (null == bean) {

            // quit joking!
            return;
        }

        bean.addVariable(CitrusTestLinkEnum.BuildId.getKey(),
                ConvertUtils.convertToString(bean.getTestLink().getBuildId()));

        bean.addVariable(CitrusTestLinkEnum.BuildName.getKey(),
                ConvertUtils.convertToString(bean.getTestLink().getBuildName()));

        bean.addVariable(CitrusTestLinkEnum.Key.getKey(),
                ConvertUtils.convertToString(bean.getTestLink().getKey()));

        bean.addVariable(CitrusTestLinkEnum.NotesFailure.getKey(),
                ConvertUtils.convertToString(bean.getTestLink().getNotesFailure()));

        bean.addVariable(CitrusTestLinkEnum.NotesSuccess.getKey(),
                ConvertUtils.convertToString(bean.getTestLink().getNotesSuccess()));

        bean.addVariable(CitrusTestLinkEnum.TestCaseId.getKey(),
                ConvertUtils.convertToString(bean.getTestLink().getTestCaseId()));

        bean.addVariable(CitrusTestLinkEnum.TestCaseInternalId.getKey(),
                ConvertUtils.convertToString(bean.getTestLink().getTestCaseInternalId()));

        addPlatformVariable(bean, null);

        bean.addVariable(CitrusTestLinkEnum.TestPlanId.getKey(),
                ConvertUtils.convertToString(bean.getTestLink().getTestPlanId()));

        bean.addVariable(CitrusTestLinkEnum.Url.getKey(),
                ConvertUtils.convertToString(bean.getTestLink().getUrl()));

        bean.addVariable(CitrusTestLinkEnum.WriteToTestLink.getKey(), Boolean.TRUE.toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     * @param defValue
     *            DOCUMENT ME!
     */
    private static final void addPlatformVariable(final CitrusBean bean, final String defValue) {

        final String platform;

        if (!bean.getTestLink().getPlatformList().isEmpty()) {

            platform = bean.getTestLink().getPlatformList().get(0);
        } else {

            platform = defValue;
        }

        bean.addVariable(CitrusTestLinkEnum.TestCasePlatform.getKey(), platform);
    }

}
