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
 * last modified: Monday, January 2, 2012 (19:39) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import java.util.List;

/**
 * Show all available info's from {@code TestLink}. For this the URL to TestLink and the generated development key must
 * be provided.
 *
 * @author Matthias Beil
 * @since Thursday, December 29, 2011
 * @goal show
 */
public class ShowTestlinkMojo extends AbstractTestLinkMojo {

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code ShowTestlinkMojo} class.
     */
    public ShowTestlinkMojo() {

        super();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCitrusTestCases(final List<CitrusBean> beanList) {

        // iterate over all CITRUS test case bean(s)
        for (final CitrusBean bean : beanList) {

            // log bean
            this.getLog().info(bean.toString());
        }
    }

}
