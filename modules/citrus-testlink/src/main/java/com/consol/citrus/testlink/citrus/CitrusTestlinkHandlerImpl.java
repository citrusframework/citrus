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
 * last modified: Saturday, January 14, 2012 (13:09) by: Matthias Beil
 */
package com.consol.citrus.testlink.citrus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestCase;
import com.consol.citrus.testlink.TestLinkBean;

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

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusHandlerImpl} class.
     */
    public CitrusTestlinkHandlerImpl() {

        super();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public TestLinkBean convert(final TestCase citrusCase) {

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param citrusCase
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private TestLinkBean createTestLinkBean(final TestCase citrusCase) {

        if (null == citrusCase) {

            LOGGER.error("CITRUS test case is null!");

            return null;
        }

        return new TestLinkBean();
    }

}
