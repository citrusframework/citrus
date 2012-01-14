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
 * last modified: Friday, January 13, 2012 (18:59) by: Matthias Beil
 */
package com.consol.citrus.testlink.utils;

import java.util.List;

import com.consol.citrus.testlink.TestLinkBean;

import br.eti.kinoshita.testlinkjavaapi.model.CustomField;

/**
 * Utility class for TestLink static methods.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class TestLinkUtils {

    // ~ Static fields/initializers ------------------------------------------------------------------------------------

    /** CITRUS_CUSTOM_FIELD. */
    public static final String CITRUS_CUSTOM_FIELD = "CITRUS";

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code TestLinkUtils} class.
     */
    private TestLinkUtils() {

        super();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * Verify if the given bean is not null and that the test case element is not null.
     *
     * @param bean
     *            {@link TestLinkBean} object to test.
     *
     * @return {@code True} if bean is not null and test case element is not null.
     */
    public static final boolean isValidTestLinkBean(final TestLinkBean bean) {

        return ((null != bean) && (null != bean.getTestCase()));
    }

    /**
     * Build from the CITRUS custom field value the name of the CITRUS test case. Use for this the custom field value
     * and append the version of the TestLink test case. This to make sure that the TestLink immutable version behavior
     * is reflected in the CITRUS test case.
     *
     * @param bean
     *            {@link TestLinkBean} object holding the needed information.
     *
     * @return Name of the CITRUS test case or in case of some error {@code null} is returned.
     */
    public static final String getCitrusTestCaseName(final TestLinkBean bean) {

        // get CITRUS custom field value
        String citrusName = TestLinkUtils.getCustomFieldValue(bean, TestLinkUtils.CITRUS_CUSTOM_FIELD);

        // make sure it is a valid value string
        if ((null != citrusName) && (!citrusName.isEmpty())) {

            // try at least to solve the first character problem
            if (Character.isLowerCase(citrusName.charAt(0))) {

                // set first character from lower case to upper case
                final StringBuilder builder = new StringBuilder(Character.toTitleCase(citrusName.substring(0, 1)
                        .charAt(0)));
                builder.append(citrusName.substring(1));

                citrusName = builder.toString();
            }

            // append test case version, this is to make sure the immutable version handling of TestLink is preserved
            final StringBuilder builder = new StringBuilder(citrusName);

            // use a V to mark the used version
            builder.append("V");
            builder.append(bean.getTestCase().getVersion());

            // return the final test case name for this given test case
            return builder.toString();
        }

        // there was some error, so return null
        return null;
    }

    /**
     * Iterates over all custom field(s) and returns the value of the wanted custom field.
     *
     * @param bean
     *            {@link TestLinkBean} object holding the needed information.
     * @param customFieldKey
     *            Name of the custom field to search for.
     *
     * @return {@code custom field value} if the custom field could be found otherwise {@code null}.
     */
    public static final String getCustomFieldValue(final TestLinkBean bean, final String customFieldKey) {

        // make sure the incoming bean is valid
        if (TestLinkUtils.isValidTestLinkBean(bean)) {

            // get list of custom fields
            final List<CustomField> customList = bean.getTestCase().getCustomFields();

            // iterate over all custom fields
            for (final CustomField field : customList) {

                // try to find the field which matches the custom field key
                if (field.getName().equalsIgnoreCase(customFieldKey)) {

                    // return the value regardless if it is null and / or empty
                    return field.getValue();
                }
            }
        }

        // nothing can be done, so return null
        return null;
    }

}
