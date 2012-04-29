/*
 * File: ConvertUtils.java
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
 * last modified: Sunday, April 29, 2012 (15:22) by: Matthias Beil
 */
package com.consol.citrus.testlink.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for converting functionality.
 * 
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class ConvertUtils {

    // ~ Static fields/initializers --------------------------------------------------------------

    /** LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertUtils.class);

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code ConvertUtils} class.
     */
    private ConvertUtils() {

        super();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Convert a {@link Throwable} to a string.
     * 
     * @param cause
     *            Throwable to convert, if {@code null} an empty string is returned.
     * 
     * @return Convert throwable or return an empty string in case there was some kind of error.
     */
    public static final String throwableToString(final Throwable cause) {

        final StringBuilder builder = new StringBuilder();

        // make sure the cause is available
        if (null != cause) {

            PrintWriter printWriter = null;
            Writer writer = null;

            try {

                writer = new StringWriter();
                printWriter = new PrintWriter(writer);

                cause.printStackTrace(printWriter);
                builder.append(writer.toString());
            } catch (final Exception ex) {

                LOGGER.error("Exception caught while converting throwable [ {} ]", cause, ex);
            } finally {

                if (null != printWriter) {

                    try {

                        printWriter.close();
                    } catch (final Exception ex) {

                        LOGGER.error(
                                "Exception caught while closing print writer for throwable [ {} ]",
                                cause);
                    }
                }
            }
        }

        // return at least an empty string
        return builder.toString();
    }

    /**
     * Convert value of object into a {@link String}.
     * 
     * @param obj
     *            Object for which the value should be converted.
     * 
     * @return Value of object as a string or {@code null} in case of an error.
     */
    public static final String convertToString(final Object obj) {

        // make sure that there is a value
        if (null == obj) {

            return null;
        }

        // see if object is already of instance string
        if (obj instanceof String) {

            // just cast to string
            return (String) obj;
        }

        // hope that the toString method returns some reasonable value
        return obj.toString();
    }

    /**
     * Convert object value to a {@link Boolean}.
     * 
     * @param obj
     *            Object for which the value should be converted.
     * 
     * @return Value of object as a boolean or {@code null} in case of an error.
     */
    public static final Boolean convertToBoolean(final Object obj) {

        // convert object to string
        final String val = convertToString(obj);

        // make sure there is some reasonable value
        if ((null != val) && (!val.isEmpty())) {

            // decode value
            return Boolean.valueOf(val);
        }

        // there was some kind of error, return null
        return null;
    }

    /**
     * Convert object value to an {@link Integer}.
     * 
     * @param obj
     *            Object for which the value should be converted.
     * 
     * @return Value of object as an integer or {@code null} in case of an error.
     */
    public static final Integer convertToInteger(final Object obj) {

        // convert object to string, in case of an integer the toString method does the job
        final String val = convertToString(obj);

        // make sure there is some reasonable value
        if ((null != val) && (!val.isEmpty())) {

            try {

                // decode value
                return Integer.decode(val);
            } catch (final Exception ex) {

                LOGGER.error("Could not convert [ {} ] to an integer!", obj, ex);
            }
        }

        // there was some kind of error, return null
        return null;
    }

}
