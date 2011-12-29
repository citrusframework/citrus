/*
 * Copyright 2006-2011 the original author or authors.
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
 * File: TestLinkContainerImpl.java
 * last modified: Thursday, December 29, 2011 (16:08) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;


/**
 * Hold all objects needed to interact with TestLink.
 *
 * @author  Matthias Beil
 * @since   CITRUS 1.2 M2
 */
public final class TestLinkContainerImpl implements TestLinkContainer {

    // ~ Static fields/initializers ------------------------------------------------------------------------------------

    /** XML_RPC. */
    private static final String XML_RPC = "/lib/api/xmlrpc.php";

    // ~ Instance fields -----------------------------------------------------------------------------------------------

    /** log. */
    private final Log log;

    /** api. */
    private TestLinkAPI api = null;

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code TestLinkContainerImpl} class.
     *
     * @param  logIn  Logger to allow for logging using the Maven plugin logger.
     */
    public TestLinkContainerImpl(final Log logIn) {

        super();

        this.log = logIn;
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void connect(final String url, final String devKey) throws MojoExecutionException {

        final String finalUrl = (url + TestLinkContainerImpl.XML_RPC);

        try {

            final URL testlinkUrl = new URL(finalUrl);

            this.api = new TestLinkAPI(testlinkUrl, devKey);

            this.log.info("TestLink reached at [ " + testlinkUrl + " ]");
        } catch (final Exception ex) {

            throw new MojoExecutionException("Could not connect to [ " + finalUrl + " ]", ex);
        }
    }

}
