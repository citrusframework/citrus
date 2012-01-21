/*
 * File: CitrusTestLinkUtils.java
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
 * last modified: Saturday, January 21, 2012 (21:19) by: Matthias Beil
 */
package com.consol.citrus.testlink.utils;

import com.consol.citrus.TestCase;
import com.consol.citrus.testlink.CitrusTestLinkBean;
import com.consol.citrus.testlink.CitrusTestLinkEnum;

/**
 * Utility class for handling CITRUS to TestLink functionality.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class CitrusTestLinkUtils {

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusTestLinkUtils} class.
     */
    private CitrusTestLinkUtils() {

        super();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Check if this CITRUS test case should write his result to TestLink. Use a boolean for this so
     * in CITRUS this can be set by some means of global variable.
     *
     * @param citrusCase
     *            CITRUS test case holding the test case variables.
     *
     * @return {@code True} in case the {@link CitrusTestLinkEnum#WriteToTestLink} value is defined
     *         and is set to {@code true}. In all other case {@code false} is returned.
     */
    public static final boolean writeToTestLink(final TestCase citrusCase) {

        // make sure there are some test case variables
        if ((null != citrusCase) && (null != citrusCase.getTestContext())
                && (null != citrusCase.getTestContext().getVariables())) {

            // check if write to TestLink variable is defined
            if (citrusCase.getTestContext().getVariables()
                    .containsKey(CitrusTestLinkEnum.WriteToTestLink.getKey())) {

                // get value and convert it to a Boolean
                final Object obj = citrusCase.getTestContext().getVariables()
                        .get(CitrusTestLinkEnum.WriteToTestLink.getKey());

                final Boolean write = ConvertUtils.convertToBoolean(obj);

                if (null != write) {

                    return write.booleanValue();
                }
            }
        }

        return false;
    }

    /**
     * Build the ID of the CITRUS test case. This ID is made up of the package and test case name.
     *
     * @param citrusCase
     *            CITRUS test case.
     *
     * @return The CITRUS ID allowing to identify this test case uniquely.
     */
    public static final String buildId(final TestCase citrusCase) {

        if (null == citrusCase) {

            return null;
        }

        final StringBuilder builder = new StringBuilder(citrusCase.getPackageName());
        builder.append(".");
        builder.append(citrusCase.getName());

        return builder.toString();
    }

    /**
     * Create a new CITRUS to TestLink bean, which must hold all data needed to write the result to
     * TestLink.
     *
     * @param citrusCase
     *            CITRUS test case.
     * @param url
     *            TestLink URL coming from the properties of the TestLink listener, if provided.
     * @param key
     *            TestLink development key from the properties of the TestLink listener, if provided.
     * @param platform
     *            The TestLink platform to be used from the properties of the TestLink listener, if
     *            provided.
     *
     * @return Newly create CITRUS to TestLink bean holding all predefined values. In case of an error
     *         {@code null} is returned.
     */
    public static final CitrusTestLinkBean createCitrusBean(final TestCase citrusCase,
            final String url, final String key, final String platform) {

        // get ID to allow to identify this bean
        final String id = buildId(citrusCase);

        if ((null == id) || (id.isEmpty())) {

            return null;
        }

        final CitrusTestLinkBean bean = new CitrusTestLinkBean();

        // preset with values from test listener, if they are defined
        bean.setId(id);
        bean.setUrl(url);
        bean.setKey(key);
        bean.setPlatform(platform);

        return bean;
    }

    /**
     * Build note and assign it depending on the success information. If there is no success
     * information available, no notes will be set.
     *
     * @param bean
     *            CITRUS TestLink bean.
     */
    public static final void buildNotes(final CitrusTestLinkBean bean) {

        // make sure there is some success / failure information
        if ((null == bean) || (null == bean.getSuccess())) {

            return;
        }

        final StringBuilder builder = new StringBuilder();

        // always add the execution duration
        builder.append("Execution duration for CITRUS test case [ ");
        builder.append(bean.getId());
        builder.append(" ] was [ ");
        builder.append(bean.getEndTime() - bean.getStartTime());
        builder.append(" ] milliseconds.");

        if (bean.getSuccess().booleanValue()) {

            // handle success note
            if ((null != bean.getNotesSuccess()) && (!bean.getNotesSuccess().isEmpty())) {

                builder.append("\n");
                builder.append(bean.getNotesSuccess());
            }

            bean.setNotesSuccess(builder.toString());
        } else {

            // handle failure note
            if ((null != bean.getNotesFailure()) && (!bean.getNotesFailure().isEmpty())) {

                builder.append("\n");
                builder.append(bean.getNotesFailure());
            }

            if (null != bean.getCause()) {

                builder.append("\nFailure due to [ \n");
                builder.append(ConvertUtils.throwableToString(bean.getCause()));
                builder.append("\n ]");
            }

            bean.setNotesFailure(builder.toString());
        }
    }

}
