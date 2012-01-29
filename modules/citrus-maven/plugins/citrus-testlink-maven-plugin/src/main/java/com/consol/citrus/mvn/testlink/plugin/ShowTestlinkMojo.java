/*
 * File: ShowTestlinkMojo.java
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
 * last modified: Saturday, January 21, 2012 (19:59) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import java.util.List;

/**
 * Show all available info's from {@code TestLink}. For this the URL to TestLink and the generated
 * development key must be provided.
 *
 * @author Matthias Beil
 * @since Thursday, December 29, 2011
 * @goal show
 */
public class ShowTestlinkMojo extends AbstractTestLinkMojo {

    // ~ Static fields/initializers --------------------------------------------------------------

    /** LINE. */
    private static final String LINE = "===========================";

    /** START. */
    private static final String START = " Start of TestCase [ ";

    /** END. */
    private static final String END = "   End of TestCase [ ";

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code ShowTestlinkMojo} class.
     */
    public ShowTestlinkMojo() {

        super();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCitrusTestCases(final List<CitrusBean> beanList) {

        // iterate over all CITRUS test case bean(s)
        for (final CitrusBean bean : beanList) {

            // log bean
            this.getLog().info(this.buildBeanInfo(bean));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String buildBeanInfo(final CitrusBean bean) {

        final StringBuilder builder = new StringBuilder();

        builder.append("\n");
        builder.append(LINE);
        builder.append(START);
        builder.append(bean.getName());
        builder.append(" ] ");
        builder.append(LINE);
        builder.append("\n");

        builder.append(" TestLink Project [ ");
        builder.append(bean.getTestLink().getTestProjectName());
        builder.append(" ]\n");

        builder.append("TestLink TestPlan [ ");
        builder.append(bean.getTestLink().getTestPlanName());
        builder.append(" ]\n");

        builder.append("   TestLink Build [ ");
        builder.append(bean.getTestLink().getBuildName());
        builder.append(" ]\n");

        this.buildMetaData(bean, builder);
        this.buildVariables(bean, builder);

        builder.append(LINE);
        builder.append(END);
        builder.append(bean.getName());
        builder.append(" ] ");
        builder.append(LINE);
        builder.append("\n");

        return builder.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     * @param builder
     *            DOCUMENT ME!
     */
    private void buildMetaData(final CitrusBean bean, final StringBuilder builder) {

        builder.append("\n------------ CITRUS MetaData ------------\n");

        builder.append("Target package [ ");
        builder.append(bean.getTargetPackage());
        builder.append(" ]\n");

        builder.append("        Author [ ");
        builder.append(bean.getAuthor());
        builder.append(" ]\n");

        builder.append("Test framework [ ");
        builder.append(bean.getFramework());
        builder.append(" ]\n");

        builder.append("   Description [ ");
        builder.append(bean.getTestLink().getTestCaseDesc());
        builder.append(" ]\n");

        builder.append("------------ CITRUS MetaData ------------\n");
    }

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     * @param builder
     *            DOCUMENT ME!
     */
    private void buildVariables(final CitrusBean bean, final StringBuilder builder) {

        builder.append("\n------------ CITRUS Variables ------------\n");

        builder.append(CitrusUtils.buildVariables(bean, "  "));

        if (bean.getTestLink().getPlatformList().size() > 1) {

            builder.append("\n--- Possible platforms ---\n");

            for (final String platform : bean.getTestLink().getPlatformList()) {

                builder.append("  Platform [ ");
                builder.append(platform);
                builder.append(" ]\n");
            }

            builder.append("\n");
        }

        builder.append("------------ CITRUS Variables ------------\n");
    }

}
