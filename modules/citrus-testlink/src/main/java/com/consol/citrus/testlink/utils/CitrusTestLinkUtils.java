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
 * last modified: Saturday, January 14, 2012 (19:23) by: Matthias Beil
 */
package com.consol.citrus.testlink.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestCase;
import com.consol.citrus.testlink.citrus.CitrusTestLinkBean;

/**
 * DOCUMENT ME!
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class CitrusTestLinkUtils {

    // ~ Static fields/initializers ------------------------------------------------------------------------------------

    /** log. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CitrusTestLinkUtils.class);

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusTestLinkUtils} class.
     */
    private CitrusTestLinkUtils() {

        super();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param citrusCase
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
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
     * DOCUMENT ME!
     *
     * @param citrusCase
     *            DOCUMENT ME!
     * @param url
     *            DOCUMENT ME!
     * @param key
     *            DOCUMENT ME!
     * @param platform
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static final CitrusTestLinkBean createCitrusBean(final TestCase citrusCase, final String url,
            final String key, final String platform) {

        final String id = buildId(citrusCase);

        if ((null == id) || (id.isEmpty())) {

            return null;
        }

        final CitrusTestLinkBean bean = new CitrusTestLinkBean();
        bean.setId(id);
        bean.setCitrusCase(citrusCase);

        bean.setUrl(url);
        bean.setKey(key);
        bean.setPlatform(platform);

        return bean;
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     * @param cause
     *            DOCUMENT ME!
     */
    public static final void buildNotes(final CitrusTestLinkBean bean, final Throwable cause) {

        if ((null == bean) || (null == bean.getSuccess())) {

            return;
        }

        final StringBuilder builder = new StringBuilder("Execution time took '");
        builder.append(bean.getEndTime() - bean.getStartTime());
        builder.append("' milliseconds");

        if (!bean.getSuccess().booleanValue() && (null != cause)) {

            builder.append("\nFailure due to [ \n");
            builder.append(throwableToString(cause));
            builder.append(" ]\n");
        }

        bean.setNotes(builder.toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @param cause
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static final String throwableToString(final Throwable cause) {

        final StringBuilder builder = new StringBuilder();

        if (null != cause) {

            PrintWriter writer = null;
            ByteArrayOutputStream baos = null;

            try {

                baos = new ByteArrayOutputStream();
                writer = new PrintWriter(baos);

                cause.printStackTrace(writer);
                builder.append(baos.toString());
            } catch (final Exception ex) {

                LOGGER.error("Exception caught while converting throwable [ {} ]", cause, ex);
            } finally {

                if (null != writer) {

                    try {

                        writer.close();
                    } catch (final Exception ex) {

                        LOGGER.error("Exception caught while closing print writer for throwable [ {} ]", cause);
                    }
                }
            }
        }

        return builder.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param obj
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static final Integer convertToInteger(final Object obj) {

        final String val = convertToString(obj);

        if ((null != val) && (!val.isEmpty())) {

            try {

                return Integer.decode(val);
            } catch (final Exception ex) {

                LOGGER.error("Could not convert [ {} ] to an integer!", obj, ex);
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param obj
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static final String convertToString(final Object obj) {

        if (null == obj) {

            return null;
        }

        if (obj instanceof String) {

            return (String) obj;
        }

        return obj.toString();
    }

}
