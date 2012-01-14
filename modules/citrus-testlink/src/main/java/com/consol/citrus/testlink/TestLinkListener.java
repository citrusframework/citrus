/*
 * File: TestLinkListener.java
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
 * last modified: Saturday, January 14, 2012 (19:25) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestCase;
import com.consol.citrus.report.TestListener;
import com.consol.citrus.testlink.citrus.CitrusTestLinkBean;
import com.consol.citrus.testlink.citrus.CitrusTestLinkFactory;
import com.consol.citrus.testlink.citrus.CitrusTestLinkHandler;
import com.consol.citrus.testlink.utils.CitrusTestLinkUtils;

/**
 * Implements the {@link TestListener} interface which will be called during the execution of a CITRUS test. The
 * behavior is of a normal reporting element, behalf of the finish method which tries to write the result to TestLink in
 * case it is a CITRUS / TestLink test case. This is verified by checking if all mandatory TestLink variables are set.
 *
 * <p>
 * Make sure to catch in each method all exceptions, to avoid to crash a test case during this reporting behavior.
 * </p>
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public class TestLinkListener implements TestListener {

    // ~ Static fields/initializers ------------------------------------------------------------------------------------

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestLinkListener.class);

    // ~ Instance fields -----------------------------------------------------------------------------------------------

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

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code TestLinkListener} class.
     */
    public TestLinkListener() {

        super();

        this.handler = CitrusTestLinkFactory.getHandler();
        this.citrusMap = new ConcurrentHashMap<String, CitrusTestLinkBean>();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>
     * As was seen during the development, there is no context element available at this point of reporting, so there is
     * no possibility to access TestLink at this point.
     * </p>
     */
    public void onTestStart(final TestCase citrusCase) {

        try {

            final CitrusTestLinkBean bean = CitrusTestLinkUtils.createCitrusBean(citrusCase, this.testLinkUrl,
                    this.testLinkKey, this.testLinkPlatform);

            if (null == bean) {

                LOGGER.error("Could not create a new citrus bean for test case [ {} ]", citrusCase);

                return;
            }

            if (this.citrusMap.containsKey(bean.getId())) {

                LOGGER.warn(
                        "Citrus bean for test case [ {} ] already exist, can not handle multiple citrus test cases",
                        citrusCase);

                return;
            }

            bean.setStartTime(System.currentTimeMillis());
            this.citrusMap.put(bean.getId(), bean);
        } catch (final Exception ex) {

            LOGGER.error("Exception caught while initialization of CITRUS / TestLink handling for test case [ {} ]",
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

            final String id = CitrusTestLinkUtils.buildId(citrusCase);

            if ((null == id) || (id.isEmpty())) {

                LOGGER.error("Could not create an identifier while finishing test case [ {} ]", citrusCase);

                return;
            }

            if (!this.citrusMap.containsKey(id)) {

                LOGGER.warn("Citrus bean for identifier [ {} ] while finishing for test case [ {} ] not found", id,
                        citrusCase);

                return;
            }

            bean = this.citrusMap.get(id);

            if (null != bean.getSuccess()) {

                // finally write to TestLink
                this.handler.writeToTestLink(bean);
            }
        } catch (final Exception ex) {

            LOGGER.error("Exception caught while finishing CITRUS / TestLink handling for test case [ {} ]",
                    citrusCase, ex);
        } finally {

            // make sure that in each case the CITRUS map is freed
            if (null != bean) {

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
     * DOCUMENT ME!
     *
     * @param citrusCase
     *            DOCUMENT ME!
     * @param state
     *            DOCUMENT ME!
     * @param cause
     *            DOCUMENT ME!
     * @param desc
     *            DOCUMENT ME!
     */
    private void handleState(final TestCase citrusCase, final Boolean state, final Throwable cause, final String desc) {

        try {

            final String id = CitrusTestLinkUtils.buildId(citrusCase);

            if ((null == id) || (id.isEmpty())) {

                LOGGER.error("Could not create an identifier for {} of test case [ {} ]", desc, citrusCase);

                return;
            }

            if (!this.citrusMap.containsKey(id)) {

                LOGGER.warn("Citrus bean for identifier [ {} ] for {} for test case [ {} ] not found", new Object[] {
                        id, desc, citrusCase });

                return;
            }

            final CitrusTestLinkBean bean = this.citrusMap.get(id);
            bean.setSuccess(state);
            bean.setEndTime(System.currentTimeMillis());

            CitrusTestLinkUtils.buildNotes(bean, cause);
        } catch (final Exception ex) {

            LOGGER.error("Exception caught while setting {} of CITRUS / TestLink handling for test case [ {} ]",
                    new Object[] { desc, citrusCase }, ex);
        }
    }

}
