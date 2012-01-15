/*
 * File: CitrusTestlinkHandlerImpl.java
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
 * last modified: Sunday, January 15, 2012 (10:10) by: Matthias Beil
 */
package com.consol.citrus.testlink.citrus;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;

import com.consol.citrus.testlink.TestLinkBean;
import com.consol.citrus.testlink.TestLinkHandler;
import com.consol.citrus.testlink.impl.TestLinkHandlerImpl;
import com.consol.citrus.testlink.utils.CitrusTestLinkUtils;

/**
 * DOCUMENT ME!
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public final class CitrusTestlinkHandlerImpl implements CitrusTestLinkHandler {

    // ~ Static fields/initializers ------------------------------------------------------------------------------------

    /** log. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CitrusTestlinkHandlerImpl.class);

    // ~ Instance fields -----------------------------------------------------------------------------------------------

    /** handler. */
    private final TestLinkHandler handler;

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusHandlerImpl} class.
     */
    public CitrusTestlinkHandlerImpl() {

        this(new TestLinkHandlerImpl());
    }

    /**
     * Constructor for {@code CitrusTestlinkHandlerImpl} class.
     *
     * @param handlerIn
     *            Allows to set the handler. May be used for testing purposes.
     */
    public CitrusTestlinkHandlerImpl(final TestLinkHandler handlerIn) {

        super();

        this.handler = handlerIn;
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void writeToTestLink(final CitrusTestLinkBean citrusBean) {

        if ((null == citrusBean) || (null == citrusBean.getCitrusCase())
                || (null == citrusBean.getCitrusCase().getTestContext())) {

            throw new IllegalStateException("CITRUS / TestLink bean or test case or test case context may not be null!");
        }

        this.createTestLinkBean(citrusBean);

        if (citrusBean.hasTestLinkVariables()) {

            this.validate(citrusBean);

            if (citrusBean.isValid()) {

                final TestLinkBean bean = citrusBean.getTlkBean();

                bean.setUrl(citrusBean.getUrl());
                bean.setKey(citrusBean.getKey());
                bean.setPlatform(citrusBean.getPlatform());
                bean.setNotes(citrusBean.getNotes());
                bean.setTestCaseName(citrusBean.getId());

                if (citrusBean.getSuccess().booleanValue()) {

                    citrusBean.getTlkBean().getTestCase().setExecutionStatus(ExecutionStatus.PASSED);
                } else {

                    citrusBean.getTlkBean().getTestCase().setExecutionStatus(ExecutionStatus.FAILED);
                }

                this.handler.writeToTestLink(bean);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param citrusBean
     *            ctxVariables DOCUMENT ME!
     */
    private void createTestLinkBean(final CitrusTestLinkBean citrusBean) {

        final Map<String, Object> ctxVariables = citrusBean.getCitrusCase().getTestContext().getVariables();

        if ((null == ctxVariables) || (ctxVariables.isEmpty())) {

            throw new IllegalStateException("There are no test case context variables defined!");
        }

        citrusBean.setTlkBean(new TestLinkBean());

        for (final Entry<String, Object> entry : ctxVariables.entrySet()) {

            if (CitrusTestLinkEnum.BuildId.getKey().equals(entry.getKey())) {

                this.buildId(citrusBean, entry);
                citrusBean.setTestLinkVariables(true);
            } else if (CitrusTestLinkEnum.BuildName.getKey().equals(entry.getKey())) {

                this.buildName(citrusBean, entry);
                citrusBean.setTestLinkVariables(true);
            } else if (CitrusTestLinkEnum.CaseId.getKey().equals(entry.getKey())) {

                this.caseId(citrusBean, entry);
                citrusBean.setTestLinkVariables(true);
            } else if (CitrusTestLinkEnum.CaseInternalId.getKey().equals(entry.getKey())) {

                this.caseInternalId(citrusBean, entry);
                citrusBean.setTestLinkVariables(true);
            } else if (CitrusTestLinkEnum.CasePlatform.getKey().equals(entry.getKey())) {

                this.platform(citrusBean, entry);
                citrusBean.setTestLinkVariables(true);
            } else if (CitrusTestLinkEnum.Key.getKey().equals(entry.getKey())) {

                this.key(citrusBean, entry);
                citrusBean.setTestLinkVariables(true);
            } else if (CitrusTestLinkEnum.PlanId.getKey().equals(entry.getKey())) {

                this.planId(citrusBean, entry);
                citrusBean.setTestLinkVariables(true);
            } else if (CitrusTestLinkEnum.Url.getKey().equals(entry.getKey())) {

                this.url(citrusBean, entry);
                citrusBean.setTestLinkVariables(true);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     */
    private void validate(final CitrusTestLinkBean bean) {

        this.validateString(bean, bean.getUrl(), CitrusTestLinkEnum.Url);
        this.validateString(bean, bean.getKey(), CitrusTestLinkEnum.Key);

        if (this.validateObject(bean, bean.getTlkBean().getBuild(), CitrusTestLinkEnum.BuildId)) {

            this.validateInteger(bean, bean.getTlkBean().getBuild().getId(), CitrusTestLinkEnum.BuildId);
        }

        if (this.validateObject(bean, bean.getTlkBean().getPlan(), CitrusTestLinkEnum.PlanId)) {

            this.validateInteger(bean, bean.getTlkBean().getPlan().getId(), CitrusTestLinkEnum.PlanId);
        }

        if (this.validateObject(bean, bean.getTlkBean().getTestCase(), CitrusTestLinkEnum.CaseId)) {

            this.validateInteger(bean, bean.getTlkBean().getTestCase().getId(), CitrusTestLinkEnum.CaseId);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param citrusBean
     *            DOCUMENT ME!
     * @param entry
     *            DOCUMENT ME!
     */
    private void buildId(final CitrusTestLinkBean citrusBean, final Entry<String, Object> entry) {

        if (null == citrusBean.getTlkBean().getBuild()) {

            citrusBean.getTlkBean().setBuild(new Build());
        }

        final Build build = citrusBean.getTlkBean().getBuild();
        build.setId(CitrusTestLinkUtils.convertToInteger(entry.getValue()));
    }

    /**
     * DOCUMENT ME!
     *
     * @param citrusBean
     *            DOCUMENT ME!
     * @param entry
     *            DOCUMENT ME!
     */
    private void buildName(final CitrusTestLinkBean citrusBean, final Entry<String, Object> entry) {

        if (null == citrusBean.getTlkBean().getBuild()) {

            citrusBean.getTlkBean().setBuild(new Build());
        }

        final Build build = citrusBean.getTlkBean().getBuild();
        build.setName(CitrusTestLinkUtils.convertToString(entry.getValue()));
    }

    /**
     * DOCUMENT ME!
     *
     * @param citrusBean
     *            DOCUMENT ME!
     * @param entry
     *            DOCUMENT ME!
     */
    private void caseId(final CitrusTestLinkBean citrusBean, final Entry<String, Object> entry) {

        if (null == citrusBean.getTlkBean().getTestCase()) {

            citrusBean.getTlkBean().setTestCase(new TestCase());
        }

        final TestCase testCase = citrusBean.getTlkBean().getTestCase();
        testCase.setId(CitrusTestLinkUtils.convertToInteger(entry.getValue()));
    }

    /**
     * DOCUMENT ME!
     *
     * @param citrusBean
     *            DOCUMENT ME!
     * @param entry
     *            DOCUMENT ME!
     */
    private void caseInternalId(final CitrusTestLinkBean citrusBean, final Entry<String, Object> entry) {

        if (null == citrusBean.getTlkBean().getTestCase()) {

            citrusBean.getTlkBean().setTestCase(new TestCase());
        }

        final TestCase testCase = citrusBean.getTlkBean().getTestCase();
        testCase.setInternalId(CitrusTestLinkUtils.convertToInteger(entry.getValue()));
    }

    /**
     * DOCUMENT ME!
     *
     * @param citrusBean
     *            DOCUMENT ME!
     * @param entry
     *            DOCUMENT ME!
     */
    private void planId(final CitrusTestLinkBean citrusBean, final Entry<String, Object> entry) {

        if (null == citrusBean.getTlkBean().getPlan()) {

            citrusBean.getTlkBean().setPlan(new TestPlan());
        }

        final TestPlan plan = citrusBean.getTlkBean().getPlan();
        plan.setId(CitrusTestLinkUtils.convertToInteger(entry.getValue()));
    }

    /**
     * DOCUMENT ME!
     *
     * @param citrusBean
     *            DOCUMENT ME!
     * @param entry
     *            DOCUMENT ME!
     */
    private void url(final CitrusTestLinkBean citrusBean, final Entry<String, Object> entry) {

        final String str = CitrusTestLinkUtils.convertToString(entry.getValue());

        if ((null != str) && (!str.isEmpty())) {

            citrusBean.setUrl(str);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param citrusBean
     *            DOCUMENT ME!
     * @param entry
     *            DOCUMENT ME!
     */
    private void key(final CitrusTestLinkBean citrusBean, final Entry<String, Object> entry) {

        final String str = CitrusTestLinkUtils.convertToString(entry.getValue());

        if ((null != str) && (!str.isEmpty())) {

            citrusBean.setKey(str);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param citrusBean
     *            DOCUMENT ME!
     * @param entry
     *            DOCUMENT ME!
     */
    private void platform(final CitrusTestLinkBean citrusBean, final Entry<String, Object> entry) {

        final String str = CitrusTestLinkUtils.convertToString(entry.getValue());

        if ((null != str) && (!str.isEmpty())) {

            citrusBean.setPlatform(str);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     * @param val
     *            DOCUMENT ME!
     * @param tenum
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private boolean validateObject(final CitrusTestLinkBean bean, final Object val, final CitrusTestLinkEnum tenum) {

        if (null == val) {

            bean.setValid(false);

            LOGGER.warn("CITRUS / TestLink variable [ {} ] is mandatory but is either null or empty!", tenum.getKey());
        }

        return bean.isValid();
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     * @param val
     *            DOCUMENT ME!
     * @param tenum
     *            DOCUMENT ME!
     */
    private void validateString(final CitrusTestLinkBean bean, final String val, final CitrusTestLinkEnum tenum) {

        if ((null == val) || (val.isEmpty())) {

            bean.setValid(false);
            LOGGER.warn("CITRUS / TestLink variable [ {} ] is mandatory but is either null or empty!", tenum.getKey());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     * @param val
     *            DOCUMENT ME!
     * @param tenum
     *            DOCUMENT ME!
     */
    private void validateInteger(final CitrusTestLinkBean bean, final Integer val, final CitrusTestLinkEnum tenum) {

        if (null == val) {

            bean.setValid(false);
            LOGGER.warn("CITRUS / TestLink variable [ {} ] is mandatory but is either null or empty!", tenum.getKey());
        }
    }

}
