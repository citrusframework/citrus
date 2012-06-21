/*
 * File: CitrusTestLinkHandlerImpl.java
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
 * last modified: Friday, May 18, 2012 (10:42) by: Matthias Beil
 */
package com.consol.citrus.testlink.citrus;

import java.util.Map;
import java.util.Map.Entry;

import com.consol.citrus.testlink.CitrusTestLinkBean;
import com.consol.citrus.testlink.CitrusTestLinkEnum;
import com.consol.citrus.testlink.CitrusTestLinkHandler;
import com.consol.citrus.testlink.TestLinkHandler;
import com.consol.citrus.testlink.impl.TestLinkHandlerImpl;
import com.consol.citrus.testlink.utils.CitrusTestLinkUtils;
import com.consol.citrus.testlink.utils.ConvertUtils;

/**
 * Implement handling of CITRUS to TestLink functionality.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public final class CitrusTestLinkHandlerImpl implements CitrusTestLinkHandler {

    // ~ Instance fields -------------------------------------------------------------------------

    /** handler. */
    private final TestLinkHandler handler;

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusHandlerImpl} class.
     */
    public CitrusTestLinkHandlerImpl() {

        // set the CitrusTestLinkHandler
        this(new TestLinkHandlerImpl());
    }

    /**
     * Constructor for {@code CitrusTestlinkHandlerImpl} class.
     *
     * @param handlerIn
     *            Allows to set the handler. May be used for testing purposes.
     */
    public CitrusTestLinkHandlerImpl(final TestLinkHandler handlerIn) {

        super();

        this.handler = handlerIn;
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void prepareWriteToTestLink(final CitrusTestLinkBean bean) {

        // fill bean with all available values
        this.handleCitrusTestLinkBean(bean);

        // check if all mandatory variables are set
        if (bean.isValid()) {

            // after all variables are set build notes
            CitrusTestLinkUtils.buildNotes(bean);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeToTestLink(final CitrusTestLinkBean bean) {

        try {

            // finally try to write to TestLink
            this.handler.writeToTestLink(bean);
        } catch (final Exception ex) {

            // this allows to also catch NPE, so do not check against null
            bean.addResponse("Exception caught while trying to write to TestLink!");
            bean.setResponseCause(ex);
        }
    }

    /**
     * Handle all missing variables.
     *
     * @param bean
     *            Bean holding all needed variables.
     */
    private void handleCitrusTestLinkBean(final CitrusTestLinkBean bean) {

        // get CITRUS test case variables
        final Map<String, Object> ctxVariables = bean.getCitrusTestCase().getTestContext()
                .getVariables();

        // there must be some variables, at least the write to TestLink flag must be set
        // iterate over all variable entries
        for (final Entry<String, Object> entry : ctxVariables.entrySet()) {

            if (CitrusTestLinkEnum.BuildId.getKey().equals(entry.getKey())) {

                bean.setBuildId(this.handleInteger(bean, entry, CitrusTestLinkEnum.BuildId));
            } else if (CitrusTestLinkEnum.TestCaseId.getKey().equals(entry.getKey())) {

                bean.setTestCaseId(this.handleInteger(bean, entry, CitrusTestLinkEnum.TestCaseId));
            } else if (CitrusTestLinkEnum.TestCaseInternalId.getKey().equals(entry.getKey())) {

                bean.setTestCaseInternalId(this.handleInteger(bean, entry,
                        CitrusTestLinkEnum.TestCaseInternalId));
            } else if (CitrusTestLinkEnum.TestCasePlatform.getKey().equals(entry.getKey())) {

                bean.setPlatform(this.handleString(bean, entry, CitrusTestLinkEnum.TestCasePlatform));
            } else if (CitrusTestLinkEnum.TestPlanId.getKey().equals(entry.getKey())) {

                bean.setTestPlanId(this.handleInteger(bean, entry, CitrusTestLinkEnum.TestPlanId));
            } else if (CitrusTestLinkEnum.NotesSuccess.getKey().equals(entry.getKey())) {

                bean.setNotesSuccess(this.handleString(bean, entry, CitrusTestLinkEnum.NotesSuccess));
            } else if (CitrusTestLinkEnum.NotesFailure.getKey().equals(entry.getKey())) {

                bean.setNotesFailure(this.handleString(bean, entry, CitrusTestLinkEnum.NotesFailure));
            }
        }
    }

    /**
     * Handle conversion of string. Additionally check if string is mandatory.
     *
     * @param bean
     *            CITRUS TestLink bean for setting value.
     * @param entry
     *            Entry element holding the CITRUS test case value.
     * @param tenum
     *            Enumeration holding key and mandatory flag.
     *
     * @return Converted object as string, which might be {@code null}.
     */
    private String handleString(final CitrusTestLinkBean bean, final Entry<String, Object> entry,
            final CitrusTestLinkEnum tenum) {

        final String val = ConvertUtils.convertToString(entry.getValue());

        if (tenum.isMandatory()) {

            // check if string is valid
            if ((null == val) || (val.isEmpty())) {

                // element is mandatory, so bean is invalid
                bean.setValid(false);
                bean.addResponse("CITRUS / TestLink variable [ " + tenum.getKey()
                        + " ] is mandatory but is either null or empty!");
            }
        }

        return val;
    }

    /**
     * Handle conversion of integer. Additionally check if integer is mandatory.
     *
     * @param bean
     *            CITRUS TestLink bean for setting value.
     * @param entry
     *            Entry element holding the CITRUS test case value.
     * @param tenum
     *            Enumeration holding key and mandatory flag.
     *
     * @return Converted object as integer, which might be {@code null}.
     */
    private Integer handleInteger(final CitrusTestLinkBean bean, final Entry<String, Object> entry,
            final CitrusTestLinkEnum tenum) {

        final Integer val = ConvertUtils.convertToInteger(entry.getValue());

        if (tenum.isMandatory()) {

            // check if integer is valid
            if (null == val) {

                // element is mandatory, so bean is invalid
                bean.setValid(false);
                bean.addResponse("CITRUS / TestLink variable [ " + tenum.getKey()
                        + " ] is mandatory but is either null or empty!");
            }
        }

        return val;
    }

}
