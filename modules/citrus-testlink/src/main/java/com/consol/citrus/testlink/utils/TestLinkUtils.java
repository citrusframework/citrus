/*
 * File: TestLinkUtils.java
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
 * last modified: Saturday, January 21, 2012 (21:28) by: Matthias Beil
 */
package com.consol.citrus.testlink.utils;

import com.consol.citrus.testlink.TestLinkCitrusBean;

/**
 * Utility class for TestLink static methods.
 * 
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class TestLinkUtils {

    // ~ Static fields/initializers --------------------------------------------------------------

    /** CITRUS_CUSTOM_FIELD. */
    public static final String CITRUS_CUSTOM_FIELD = "CITRUS";

    /** TEST_CASE_PREFIX. */
    public static final String TEST_CASE_PREFIX = "CitTlk";

    /** REPLACE_CHRS. */
    public static final String REPLACE_CHRS = "- ";

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code TestLinkUtils} class.
     */
    private TestLinkUtils() {

        super();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Verify if the given bean is not null and that the test case element is not null.
     * 
     * @param bean
     *            {@link TestLinkCitrusBean} object to test.
     * 
     * @return {@code True} if bean is not null and test case element is not null.
     */
    public static final boolean isValidTestLinkBean(final TestLinkCitrusBean bean) {

        return ((null != bean) && (null != bean.getTestCaseId()));
    }

    /**
     * Build from the CITRUS custom field value the name of the CITRUS test case. Use for this the
     * custom field value and append the version of the TestLink test case. This to make sure that the
     * TestLink immutable version behavior is reflected in the CITRUS test case.
     * 
     * @param bean
     *            {@link TestLinkCitrusBean} object holding the needed information.
     * 
     * @return Name of the CITRUS test case or in case of some error {@code null} is returned.
     */
    public static final String getCitrusTestCaseName(final TestLinkCitrusBean bean) {

        // get CITRUS custom field value
        String citrusName = buildTestCaseName(bean);

        // make sure it is a valid value string
        if ((null != citrusName) && (!citrusName.isEmpty())) {

            // try at least to solve the first character problem
            if (Character.isLowerCase(citrusName.charAt(0))) {

                // set first character from lower case to upper case
                final StringBuilder builder = new StringBuilder(Character.toTitleCase(citrusName
                        .substring(0, 1).charAt(0)));
                builder.append(citrusName.substring(1));

                citrusName = builder.toString();
            }

            // append test case version, this is to make sure the immutable version handling of
            // TestLink is preserved
            final StringBuilder builder = new StringBuilder(citrusName);

            // use a V to mark the used version
            builder.append("V");
            builder.append(bean.getTestCaseVersion());

            // return the final test case name for this given test case
            return builder.toString();
        }

        // there was some error, so return null
        return null;
    }

    /**
     * Build name of test case as used to create a CITRUS test case.
     * 
     * @param bean
     *            {@link TestLinkCitrusBean} object holding the needed information.
     * 
     * @return Name of test case which will be used to create a CITRUS test case.
     */
    public static final String buildTestCaseName(final TestLinkCitrusBean bean) {

        // make sure there is a bean
        if (null != bean) {

            final StringBuilder builder = new StringBuilder();

            // see if there is a project prefix
            if (null != bean.getTestProjectPrefix()) {

                final String prefix = bean.getTestProjectPrefix();

                if ((null != prefix) && (!prefix.isEmpty())) {

                    // there is a project prefix, make sure to remove not allowed characters
                    prefix.replace(REPLACE_CHRS, "");

                    if (!prefix.isEmpty()) {

                        // there is still some prefix, use it
                        builder.append(prefix);
                    } else {

                        // use default prefix
                        builder.append(TEST_CASE_PREFIX);
                    }
                } else {

                    // there was no project prefix, use default prefix
                    builder.append(TEST_CASE_PREFIX);
                }

                if (null != bean.getTestProjectId()) {

                    // add project id
                    builder.append("Prj");
                    builder.append(bean.getTestProjectId());
                }
            } else {

                // use default prefix
                builder.append(TEST_CASE_PREFIX);
            }

            if (null != bean.getTestPlanId()) {

                // add test plan id
                builder.append("Plan");
                builder.append(bean.getTestPlanId());
            }

            if (null != bean.getTestCaseId()) {

                // add test case id
                builder.append("Tc");
                builder.append(bean.getTestCaseId());
            }

            return builder.toString();
        }

        // there was some kind of error, return null
        return null;
    }

}
