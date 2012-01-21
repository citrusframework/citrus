/*
 * File: CitrusTestLinkListener.java
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
 * last modified: Saturday, January 21, 2012 (20:56) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestCase;
import com.consol.citrus.report.TestListener;
import com.consol.citrus.testlink.citrus.CitrusTestLinkHandlerImpl;
import com.consol.citrus.testlink.utils.CitrusTestLinkUtils;
import com.consol.citrus.testlink.utils.ConvertUtils;

/**
 * Implements the {@link TestListener} interface which will be called during the execution of a CITRUS
 * test. The behavior is of a normal reporting element, behalf of the finish method which tries to
 * write the result to TestLink in case it is a CITRUS / TestLink test case.
 *
 * <p>
 * Make sure to catch in each method all exceptions, to avoid to crash a CITRUS test case during this
 * reporting life cycle.
 * </p>
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public class CitrusTestLinkListener implements TestListener {

    // ~ Static fields/initializers --------------------------------------------------------------

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CitrusTestLinkListener.class);

    // ~ Instance fields -------------------------------------------------------------------------

    /** testLinkUrl. */
    private String testLinkUrl;

    /** testLinkKey. */
    private String testLinkKey;

    /** testLinkPlatform. */
    private String testLinkPlatform;

    /** handler. */
    private final CitrusTestLinkHandler handler;

    /** citrusMap. */
    private final ConcurrentMap<String, CitrusTestLinkBean> citrusMap;

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code TestLinkListener} class.
     */
    public CitrusTestLinkListener() {

        this(new CitrusTestLinkHandlerImpl());
    }

    /**
     * Constructor for {@code TestLinkListener} class.
     *
     * @param handlerIn
     *            Implementation of {@link CitrusTestLinkHandler} interface. May be used for testing
     *            purposes.
     */
    public CitrusTestLinkListener(final CitrusTestLinkHandler handlerIn) {

        super();

        this.handler = handlerIn;
        this.citrusMap = new ConcurrentHashMap<String, CitrusTestLinkBean>();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>
     * As was seen during the development, there is no CITRUS test case context element available at
     * this point of reporting, so there is no possibility to access any variables at this point of
     * reporting life cycle.
     * </p>
     */
    public void onTestStart(final TestCase citrusCase) {

        try {

            // as there is no context, no CITRUS variables are available,
            // so get in each case a CITRUS / TestLink bean
            final CitrusTestLinkBean bean = CitrusTestLinkUtils.createCitrusBean(citrusCase,
                    this.testLinkUrl, this.testLinkKey, this.testLinkPlatform);

            // make sure there is a CITRUS TestLink bean
            if (null == bean) {

                LOGGER.error("Could not create a new citrus bean for test case [ {} ]", citrusCase);

                return;
            }

            // make sure there is only one single bean for the given bean ID
            if (this.citrusMap.containsKey(bean.getId())) {

                LOGGER.warn(
                        "Citrus bean for test case [ {} ] already exist, can not handle multiple citrus test cases",
                        citrusCase);

                return;
            }

            // set start time
            bean.setStartTime(System.currentTimeMillis());

            // add bean to internal CITRUS TestLink beans
            this.citrusMap.put(bean.getId(), bean);
        } catch (final Exception ex) {

            LOGGER.error(
                    "Exception caught while initialization of CITRUS / TestLink handling for test case [ {} ]",
                    citrusCase, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onTestSuccess(final TestCase citrusCase) {

        this.handleState(citrusCase, Boolean.TRUE, null, "success");
    }

    /**
     * {@inheritDoc}
     */
    public void onTestFailure(final TestCase citrusCase, final Throwable cause) {

        this.handleState(citrusCase, Boolean.FALSE, cause, "failure");
    }

    /**
     * {@inheritDoc}
     */
    public void onTestSkipped(final TestCase citrusCase) {

        this.handleState(citrusCase, null, null, "skipping");
    }

    /**
     * {@inheritDoc}
     */
    public void onTestFinish(final TestCase citrusCase) {

        CitrusTestLinkBean bean = null;

        try {

            // build CITRUS ID to find bean in CITRUS map
            final String id = CitrusTestLinkUtils.buildId(citrusCase);

            if ((null == id) || (id.isEmpty())) {

                LOGGER.error("Could not create an identifier while finishing test case [ {} ]",
                        citrusCase);

                return;
            }

            if (!this.citrusMap.containsKey(id)) {

                LOGGER.warn(
                        "Citrus bean for identifier [ {} ] while finishing for test case [ {} ] not found",
                        id, citrusCase);

                return;
            }

            // CITRUS bean must be here
            bean = this.citrusMap.get(id);

            // check if this CITRUS test case must write to TestLink and
            // that either success or failure was called
            if (CitrusTestLinkUtils.writeToTestLink(citrusCase) && (null != bean.getSuccess())) {

                // add CITRUS test case with all variables to CITRUS TestLink bean
                bean.setCitrusTestCase(citrusCase);

                // finally write to TestLink
                this.handler.writeToTestLink(bean);

                // inform about the response
                this.handleResponse(bean);
            }
        } catch (final Exception ex) {

            LOGGER.error(
                    "Exception caught while finishing CITRUS / TestLink handling for test case [ {} ]",
                    citrusCase, ex);
        } finally {

            // make sure that in each case the CITRUS map is freed
            if (null != bean) {

                // make sure the removed bean is the one expected
                if (!bean.equals(this.citrusMap.remove(bean.getId()))) {

                    LOGGER.error("Could not remove CITRUS / TestLink bean [ {} ]", bean);
                }
            }
        }
    }

    /**
     * Sets the value of the {@code test link url} field.
     *
     * @param testLinkUrlIn
     *            field to set.
     */
    public void setTestLinkUrl(final String testLinkUrlIn) {

        this.testLinkUrl = testLinkUrlIn;
    }

    /**
     * Sets the value of the {@code test link key} field.
     *
     * @param testLinkKeyIn
     *            field to set.
     */
    public void setTestLinkKey(final String testLinkKeyIn) {

        this.testLinkKey = testLinkKeyIn;
    }

    /**
     * Sets the value of the {@code test link platform} field.
     *
     * @param testLinkPlatformIn
     *            field to set.
     */
    public void setTestLinkPlatform(final String testLinkPlatformIn) {

        this.testLinkPlatform = testLinkPlatformIn;
    }

    /**
     * Handle for success, skipping and failure the setting of the status.
     *
     * @param citrusCase
     *            CITRUS test case used to build the CITRUS ID.
     * @param state
     *            The state of the test case. In case of skipping the value is {@code null}.
     * @param cause
     *            In case of a failure the cause of this failure.
     * @param desc
     *            Description of this state for logging.
     */
    private void handleState(final TestCase citrusCase, final Boolean state, final Throwable cause,
            final String desc) {

        try {

            // build CITRUS ID
            final String id = CitrusTestLinkUtils.buildId(citrusCase);

            if ((null == id) || (id.isEmpty())) {

                LOGGER.error("Could not create an identifier for {} of test case [ {} ]", desc,
                        citrusCase);

                return;
            }

            if (!this.citrusMap.containsKey(id)) {

                LOGGER.warn(
                        "Citrus bean for identifier [ {} ] for {} for test case [ {} ] not found",
                        new Object[] { id, desc, citrusCase });

                return;
            }

            // get bean from CITRUS map
            final CitrusTestLinkBean bean = this.citrusMap.get(id);

            // set new state
            bean.setSuccess(state);

            // the CITRUS test case is terminated, set end time
            bean.setEndTime(System.currentTimeMillis());

            // add also available cause
            bean.setCause(cause);
        } catch (final Exception ex) {

            LOGGER.error(
                    "Exception caught while setting {} of CITRUS / TestLink handling for test case [ {} ]",
                    new Object[] { desc, citrusCase }, ex);
        }
    }

    /**
     * Depending on the response values build response and log the result.
     *
     * @param bean
     *            CITRUS TestLink bean holding response.
     */
    private void handleResponse(final CitrusTestLinkBean bean) {

        // check if there was some writing to TestLink
        if (null != bean.getResponseState()) {

            // check if writing to TestLink was successful
            if (bean.getResponseState().booleanValue()) {

                // YEAH it was
                LOGGER.info("Writing to TestLink was successful for [ {} ]", bean.getId());
            } else {

                // there was some error writing to TestLink, log it
                final StringBuilder builder = new StringBuilder();

                builder.append("Failure writing to TestLink");

                if (!bean.getResponseList().isEmpty()) {

                    builder.append(" due to \n");

                    for (final String response : bean.getResponseList()) {

                        builder.append(response);
                        builder.append("\n");
                    }
                } else {

                    builder.append("!\n");
                }

                if (null != bean.getResponseCause()) {

                    builder.append("Exception caught:\n");
                    builder.append(ConvertUtils.throwableToString(bean.getResponseCause()));
                    builder.append("\n");
                }

                LOGGER.error(builder.toString());
            }
        } else {

            // check if there is some information during handling to write to TestLink
            if (!bean.getResponseList().isEmpty()) {

                final StringBuilder builder = new StringBuilder();

                builder.append("Error trying to write to TestLink due to \n");

                for (final String response : bean.getResponseList()) {

                    builder.append(response);
                    builder.append("\n");
                }

                if (null != bean.getResponseCause()) {

                    builder.append("Exception caught:\n");
                    builder.append(ConvertUtils.throwableToString(bean.getResponseCause()));
                    builder.append("\n");
                }

                LOGGER.error(builder.toString());
            }
        }
    }

}
